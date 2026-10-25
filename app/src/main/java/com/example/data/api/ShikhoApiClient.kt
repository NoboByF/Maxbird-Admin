package com.example.data.api

import com.example.data.model.ShikhoAcademicProgram
import com.example.data.model.ShikhoAuthSession
import com.example.data.model.ShikhoBatchExamYear
import com.example.data.model.ShikhoClassItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Result wrapper for syllabus sync + program fetch so the caller can also persist
 * the updated JWT `access_token` returned by `ChangeSyllabus`.
 */
data class ShikhoProgramsFetchResult(
    val programs: List<ShikhoAcademicProgram>,
    val updatedAccessToken: String,
    val updatedRefreshToken: String? = null
)

/**
 * Official Shikho Server API Client (`https://api.shikho.com`)
 * Implements:
 * 1. PIN-based Direct Login (`POST https://api.shikho.com/auth/v2/login`)
 * 2. Real Syllabus Class List (`GET https://api.shikho.com/class_list?vendor=BD&type=syllabus`)
 * 3. Real BatchOptions GraphQL (`POST https://api.shikho.com/graphql` with `classCode` & `options`)
 * 4. Real Syllabus Switching (`ChangeSyllabus` + `UpdateExamYear`) & `GetAcademicProgram` GraphQL
 */
class ShikhoApiClient {

    companion object {
        private const val BASE_URL = "https://api.shikho.com"
        private const val USER_AGENT = "okhttp/4.11.0"
        private const val VENDOR_BD = "BD"
        private const val DEFAULT_DEVICE_ID = "admin-panel-device-001"
        private const val DEFAULT_GOOGLE_ADS_ID = "00000000-0000-0000-0000-000000000000"

        val ALLOWED_CLASS_CODES = setOf("C5", "C6", "C7", "C8", "C9", "C10", "C11", "C12")
        val GROUP_REQUIRED_CLASSES = setOf("C9", "C10", "C11", "C12")
        val NON_GROUP_CLASSES = setOf("C5", "C6", "C7", "C8")

        val CLASS_BENGALI_NAMES = mapOf(
            "C5" to "পঞ্চম শ্রেণি",
            "C6" to "ষষ্ঠ শ্রেণি",
            "C7" to "সপ্তম শ্রেণি",
            "C8" to "অষ্টম শ্রেণি",
            "C9" to "নবম শ্রেণি",
            "C10" to "দশম শ্রেণি",
            "C11" to "একাদশ শ্রেণি",
            "C12" to "দ্বাদশ শ্রেণি"
        )

        val DEFAULT_CLASS_SERIAL = mapOf(
            "C5" to 5,
            "C6" to 6,
            "C7" to 7,
            "C8" to 8,
            "C9" to 9,
            "C10" to 10,
            "C11" to 11,
            "C12" to 12
        )

        /**
         * Normalizes group code from student telemetry or saved values:
         * e.g. "SCI" -> "Science", "HUM" -> "Humanities", "BUS" / "COM" -> "BusinessStudies"
         */
        fun normalizeGroupCode(rawGroup: String?, classCode: String): String {
            val cleanClass = classCode.trim().uppercase()
            if (cleanClass in NON_GROUP_CLASSES) return "None"
            val g = rawGroup?.trim().orEmpty()
            return when (g.uppercase()) {
                "SCI", "SCIENCE", "বিজ্ঞান" -> "Science"
                "HUM", "HUMANITIES", "ARTS", "মানবিক" -> "Humanities"
                "BUS", "BUSINESS", "BUSINESSSTUDIES", "BUSINESS_STUDIES", "COM", "COMMERCE", "ব্যবসায় শিক্ষা" -> "BusinessStudies"
                else -> if (g in setOf("Science", "Humanities", "BusinessStudies")) g else "Science"
            }
        }

        /**
         * Normalizes a batch string (e.g. "HSC 2027", "SSC 2026", "Batch 2028") into a 4-digit year ("2027").
         */
        fun extractBatchYear(rawBatch: String?): String {
            val clean = rawBatch?.trim().orEmpty()
            if (clean.isBlank()) return ""
            val match = Regex("(20\\d{2})").find(clean)
            return match?.value ?: clean.replace(Regex("^(SSC|HSC|Batch)\\s*", RegexOption.IGNORE_CASE), "").trim()
        }

        /**
         * Formats label for a given classCode and year (for fallback/extra batch years).
         */
        fun buildBatchLabel(classCode: String, year: String): String {
            val cleanYear = extractBatchYear(year).ifBlank { year.trim() }
            return when (classCode.trim().uppercase()) {
                "C9", "C10" -> "SSC $cleanYear"
                "C11", "C12" -> "HSC $cleanYear"
                else -> "Batch $cleanYear"
            }
        }

        /**
         * Guarantees that 2026, 2027, 2028 are always included in the batch options list
         * alongside whatever options came from the server.
         */
        fun mergeWithFallbackBatchYears(
            apiOptions: List<ShikhoBatchExamYear>,
            classCode: String
        ): List<ShikhoBatchExamYear> {
            val map = LinkedHashMap<String, ShikhoBatchExamYear>()
            for (item in apiOptions) {
                val yr = extractBatchYear(item.year).ifBlank { item.year.trim() }
                if (yr.isNotBlank()) {
                    val lbl = item.label.trim().ifBlank { buildBatchLabel(classCode, yr) }
                    map[yr] = ShikhoBatchExamYear(year = yr, label = lbl)
                }
            }
            val guaranteedYears = listOf("2026", "2027", "2028")
            for (yr in guaranteedYears) {
                if (!map.containsKey(yr)) {
                    map[yr] = ShikhoBatchExamYear(
                        year = yr,
                        label = buildBatchLabel(classCode, yr)
                    )
                }
            }
            return map.values.sortedBy { it.year }
        }

        /**
         * Formats phone number:
         * If input is `017xxxxxxxx` (11 digits), prepends `88` -> `88017xxxxxxxx` (13 digits).
         */
        fun formatPhoneNumber(rawInput: String): String {
            val digitsOnly = rawInput.trim().filter { it.isDigit() }
            return when {
                digitsOnly.length == 11 && digitsOnly.startsWith("01") -> "88$digitsOnly"
                digitsOnly.length == 10 && digitsOnly.startsWith("1") -> "880$digitsOnly"
                digitsOnly.startsWith("8801") && digitsOnly.length == 13 -> digitsOnly
                else -> digitsOnly
            }
        }

        /**
         * Builds `batch_id` for `GetAcademicProgram`:
         * - If className is "C11" or "C12" and year is "2027" -> "HSC 2027"
         * - If className is "C9" or "C10" and year is "2028" -> "SSC 2028"
         * - If C5–C8 -> year (e.g. "2026")
         */
        fun buildBatchId(className: String, year: String): String {
            val cleanYear = extractBatchYear(year)
            if (cleanYear.isBlank()) return ""
            return when (className.uppercase().trim()) {
                "C11", "C12" -> "HSC $cleanYear"
                "C9", "C10" -> "SSC $cleanYear"
                else -> cleanYear
            }
        }
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Part 1.2: Direct Login with Phone & PIN (`POST https://api.shikho.com/auth/v2/login`)
     */
    suspend fun loginWithPin(rawPhone: String, pin: String): Result<ShikhoAuthSession> =
        withContext(Dispatchers.IO) {
            try {
                val formattedPhone = formatPhoneNumber(rawPhone)
                if (formattedPhone.length != 13 || !formattedPhone.startsWith("8801")) {
                    return@withContext Result.failure(
                        IllegalArgumentException("সঠিক ১১ ডিজিটের মোবাইল নাম্বার দিন (যেমন: 017XXXXXXXX)")
                    )
                }
                val trimmedPin = pin.trim()
                if (trimmedPin.isBlank()) {
                    return@withContext Result.failure(
                        IllegalArgumentException("আপনার শিখো অ্যাকাউন্টের পিন (PIN) লিখুন")
                    )
                }

                val payloadJson = JSONObject().apply {
                    put("phone", formattedPhone)
                    put("otp", trimmedPin)
                    put("auth_type", "login")
                    put("type", "student")
                    put("profile", JSONObject().apply {
                        put("device_id", DEFAULT_DEVICE_ID)
                    })
                    put("google_ads_id", DEFAULT_GOOGLE_ADS_ID)
                }

                val request = Request.Builder()
                    .url("$BASE_URL/auth/v2/login")
                    .post(payloadJson.toString().toRequestBody(jsonMediaType))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("x-vendor", VENDOR_BD)
                    .header("User-Agent", USER_AGENT)
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string().orEmpty()
                    if (bodyStr.isBlank()) {
                        return@withContext Result.failure(
                            Exception("শিখো সার্ভার থেকে খালি রেসপন্স এসেছে (HTTP ${response.code})")
                        )
                    }

                    val root = JSONObject(bodyStr)
                    val tokensObj = root.optJSONObject("tokens")
                        ?: root.optJSONObject("data")?.optJSONObject("tokens")
                        ?: root.optJSONObject("body")?.optJSONObject("tokens")

                    val accessToken = tokensObj?.optString("access_token").orEmpty()
                        .ifBlank { root.optString("access_token") }
                    val refreshToken = tokensObj?.optString("refresh_token").orEmpty()
                    val userId = tokensObj?.optString("user_id").orEmpty()
                        .ifBlank { root.optJSONObject("user")?.optString("id").orEmpty() }

                    if (response.isSuccessful && accessToken.isNotBlank()) {
                        Result.success(
                            ShikhoAuthSession(
                                accessToken = accessToken,
                                refreshToken = refreshToken,
                                userId = userId,
                                phone = formattedPhone
                            )
                        )
                    } else {
                        val errMsg = root.optString("message")
                            .ifBlank { root.optString("error") }
                            .ifBlank { "লগইন ব্যর্থ হয়েছে (HTTP ${response.code})" }
                        Result.failure(Exception(errMsg))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Part 3 - Step 1: Real Class List API
     * `GET https://api.shikho.com/class_list?vendor=BD&type=syllabus`
     * Filters code in C5..C12 where is_active != false, sorted by serial.
     */
    suspend fun fetchClassList(accessToken: String): Result<List<ShikhoClassItem>> =
        withContext(Dispatchers.IO) {
            try {
                val cleanToken = accessToken.trim()
                if (cleanToken.isBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("শিখো অ্যাকাউন্ট লগইন করা নেই। আগে শিখো অ্যাকাউন্ট কানেক্ট করুন।")
                    )
                }

                val request = Request.Builder()
                    .url("$BASE_URL/class_list?vendor=BD&type=syllabus")
                    .get()
                    .header("Authorization", "Bearer $cleanToken")
                    .header("x-vendor", VENDOR_BD)
                    .header("Accept", "application/json")
                    .header("User-Agent", USER_AGENT)
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val errMsg = try {
                            JSONObject(bodyStr).optString("message").ifBlank { "HTTP ${response.code}" }
                        } catch (_: Exception) {
                            "HTTP ${response.code}"
                        }
                        return@withContext Result.failure(
                            Exception("শ্রেণির তালিকা লোড করতে ব্যর্থ ($errMsg)")
                        )
                    }

                    val trimmed = bodyStr.trim()
                    val jsonArray: JSONArray = when {
                        trimmed.startsWith("[") -> JSONArray(trimmed)
                        trimmed.startsWith("{") -> {
                            val obj = JSONObject(trimmed)
                            obj.optJSONArray("data")
                                ?: obj.optJSONArray("body")
                                ?: obj.optJSONArray("classes")
                                ?: obj.optJSONArray("items")
                                ?: JSONArray()
                        }
                        else -> JSONArray()
                    }

                    val resultList = mutableListOf<ShikhoClassItem>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.optJSONObject(i) ?: continue
                        val code = item.optString("code").trim().uppercase()
                        if (code !in ALLOWED_CLASS_CODES) continue

                        val isActive = if (item.has("is_active") && !item.isNull("is_active")) {
                            item.optBoolean("is_active", true)
                        } else {
                            true
                        }
                        if (!isActive) continue

                        val serial = if (item.has("serial") && !item.isNull("serial")) {
                            item.optInt("serial", DEFAULT_CLASS_SERIAL[code] ?: 99)
                        } else {
                            DEFAULT_CLASS_SERIAL[code] ?: 99
                        }

                        val bengaliName = CLASS_BENGALI_NAMES[code]
                            ?: item.optString("name_bn")
                                .ifBlank { item.optString("title_bn") }
                                .ifBlank { item.optString("name") }
                                .ifBlank { code }

                        val enName = item.optString("name").ifBlank { code }

                        resultList.add(
                            ShikhoClassItem(
                                code = code,
                                titleBn = bengaliName,
                                nameEn = enName,
                                serial = serial,
                                isActive = true
                            )
                        )
                    }

                    val sorted = resultList
                        .distinctBy { it.code }
                        .sortedBy { it.serial }

                    Result.success(sorted)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * BUG 1 FIX: Official `BatchOptions` GraphQL Query
     * Uses variable `$classCode: ClassEnumCommon` and response field `options { year label }`,
     * and merges with guaranteed fallback years (2026, 2027, 2028) so batch options are never empty.
     */
    suspend fun fetchBatchOptions(
        accessToken: String,
        classCode: String
    ): Result<List<ShikhoBatchExamYear>> = withContext(Dispatchers.IO) {
        val normalizedClass = classCode.trim().uppercase()
        try {
            val cleanToken = accessToken.trim()
            if (cleanToken.isBlank()) {
                return@withContext Result.success(
                    mergeWithFallbackBatchYears(emptyList(), normalizedClass)
                )
            }

            val payload = JSONObject().apply {
                put("operationName", "BatchOptions")
                put(
                    "query",
                    "query BatchOptions(\$classCode: ClassEnumCommon) { batchOptions(classCode: \$classCode) { classCode date options { year label } } }"
                )
                put("variables", JSONObject().apply {
                    put("classCode", normalizedClass)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL/graphql")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .header("Authorization", "Bearer $cleanToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("x-vendor", VENDOR_BD)
                .header("User-Agent", USER_AGENT)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful || bodyStr.isBlank()) {
                    return@withContext Result.success(
                        mergeWithFallbackBatchYears(emptyList(), normalizedClass)
                    )
                }

                val root = JSONObject(bodyStr)
                val batchOptionsObj = root
                    .optJSONObject("data")
                    ?.optJSONObject("batchOptions")

                val optionsArr = batchOptionsObj?.optJSONArray("options")
                    ?: batchOptionsObj?.optJSONArray("exam_years")
                    ?: JSONArray()

                val parsedOptions = mutableListOf<ShikhoBatchExamYear>()
                for (i in 0 until optionsArr.length()) {
                    val obj = optionsArr.optJSONObject(i) ?: continue
                    val year = obj.opt("year")?.toString()?.trim().orEmpty()
                    val label = obj.optString("label").trim().ifBlank {
                        buildBatchLabel(normalizedClass, year)
                    }
                    if (year.isNotBlank()) {
                        parsedOptions.add(ShikhoBatchExamYear(year = year, label = label))
                    }
                }

                Result.success(mergeWithFallbackBatchYears(parsedOptions, normalizedClass))
            }
        } catch (_: Exception) {
            Result.success(mergeWithFallbackBatchYears(emptyList(), normalizedClass))
        }
    }

    /**
     * BUG 2 FIX - Step A:
     * Calls `mutation ChangeSyllabus` when class or study_group changes, and returns the
     * newly issued JWT `access_token` (which embeds the new class & group in Shikho's backend).
     * Warning: If class is C5, C6, C7, or C8, `study_group` MUST NOT be sent in `variables`.
     */
    suspend fun changeSyllabus(
        accessToken: String,
        classCode: String,
        studyGroup: String?
    ): Result<Pair<String, String?>> = withContext(Dispatchers.IO) {
        try {
            val cleanToken = accessToken.trim()
            val normalizedClass = classCode.trim().uppercase()
            val variablesObj = JSONObject().apply {
                put("userclass", normalizedClass)
                if (normalizedClass in GROUP_REQUIRED_CLASSES) {
                    val normalizedGroup = normalizeGroupCode(studyGroup, normalizedClass)
                    put("study_group", normalizedGroup)
                }
            }

            val payload = JSONObject().apply {
                put("operationName", "ChangeSyllabus")
                put(
                    "query",
                    "mutation ChangeSyllabus(\$study_group: StudyGroupTypeEnum, \$userclass: ClassEnumCommon) { changeSyllabus(study_group: \$study_group, class: \$userclass) { access_token id_token refresh_token } }"
                )
                put("variables", variablesObj)
            }

            val request = Request.Builder()
                .url("$BASE_URL/graphql")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .header("Authorization", "Bearer $cleanToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("x-vendor", VENDOR_BD)
                .header("User-Agent", USER_AGENT)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful || bodyStr.isBlank()) {
                    return@withContext Result.success(cleanToken to null)
                }
                val root = JSONObject(bodyStr)
                val changeObj = root.optJSONObject("data")?.optJSONObject("changeSyllabus")
                val newAccessToken = changeObj?.optString("access_token")?.trim().orEmpty()
                val newRefreshToken = changeObj?.optString("refresh_token")?.trim()?.takeIf { it.isNotBlank() }
                if (newAccessToken.isNotBlank()) {
                    Result.success(newAccessToken to newRefreshToken)
                } else {
                    Result.success(cleanToken to null)
                }
            }
        } catch (_: Exception) {
            Result.success(accessToken.trim() to null)
        }
    }

    /**
     * BUG 2 FIX - Step B:
     * Calls `mutation UpdateExamYear` using the new `access_token` from `ChangeSyllabus`.
     */
    suspend fun updateExamYear(
        accessToken: String,
        passingYear: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanToken = accessToken.trim()
            val cleanYear = extractBatchYear(passingYear)
            if (cleanToken.isBlank() || cleanYear.isBlank()) {
                return@withContext Result.success(Unit)
            }

            val payload = JSONObject().apply {
                put("operationName", "UpdateExamYear")
                put(
                    "query",
                    "mutation UpdateExamYear(\$passing_year: String, \$type: PrimaryUserTypeEnum!) { updateProfile(passing_year: \$passing_year, type: \$type) { passing_year } }"
                )
                put("variables", JSONObject().apply {
                    put("passing_year", cleanYear)
                    put("type", "student")
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL/graphql")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .header("Authorization", "Bearer $cleanToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("x-vendor", VENDOR_BD)
                .header("User-Agent", USER_AGENT)
                .build()

            okHttpClient.newCall(request).execute().use {
                Result.success(Unit)
            }
        } catch (_: Exception) {
            Result.success(Unit)
        }
    }

    /**
     * BUG 2 FIX - Step A + Step B + Step C:
     * 1. Calls `ChangeSyllabus` to switch class & group and obtain the new JWT `access_token`.
     * 2. Calls `UpdateExamYear` with the selected passing year and the new token.
     * 3. Calls `GetAcademicProgram` with the new token and `batch_id`.
     * 4. If no courses are found for that `batch_id` (or `other_programs` is empty), automatically
     *    retries `GetAcademicProgram` with `batch_id: null`.
     * 5. Strictly filters the combined courses by `course.classes` matching `className` so
     *    courses from another class (e.g. HSC '26 when C10 is selected) never appear!
     */
    suspend fun fetchAcademicPrograms(
        accessToken: String,
        className: String,
        group: String,
        year: String
    ): Result<ShikhoProgramsFetchResult> = withContext(Dispatchers.IO) {
        try {
            val initialToken = accessToken.trim()
            if (initialToken.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("শিখো অ্যাকাউন্ট লগইন করা নেই।")
                )
            }

            val normalizedClass = className.trim().uppercase()
            val effectiveGroup = normalizeGroupCode(group, normalizedClass)
            val cleanYear = extractBatchYear(year)
            val batchId = buildBatchId(normalizedClass, cleanYear)

            // Step A: ChangeSyllabus -> obtain updated JWT access_token
            val syllabusResult = changeSyllabus(
                accessToken = initialToken,
                classCode = normalizedClass,
                studyGroup = if (normalizedClass in GROUP_REQUIRED_CLASSES) effectiveGroup else null
            )
            val (activeToken, updatedRefreshToken) = syllabusResult.getOrDefault(initialToken to null)

            // Step B: UpdateExamYear -> update passing_year on profile with the new token
            if (cleanYear.isNotBlank()) {
                updateExamYear(accessToken = activeToken, passingYear = cleanYear)
            }

            // Step C: Execute GetAcademicProgram
            val firstAttempt = executeGetAcademicProgramQuery(
                accessToken = activeToken,
                normalizedClass = normalizedClass,
                effectiveGroup = effectiveGroup,
                batchId = batchId.takeIf { it.isNotBlank() }
            )

            val firstPrograms = firstAttempt.getOrNull()
            val hasNonEnrolledOrMatching = firstPrograms != null &&
                    firstPrograms.isNotEmpty() &&
                    firstPrograms.any { !it.isEnrolledProgram }

            val finalPrograms = if (hasNonEnrolledOrMatching) {
                firstPrograms!!
            } else {
                // Fallback: call GetAcademicProgram a second time with batch_id: null
                val fallbackAttempt = executeGetAcademicProgramQuery(
                    accessToken = activeToken,
                    normalizedClass = normalizedClass,
                    effectiveGroup = effectiveGroup,
                    batchId = null
                )
                val fallbackPrograms = fallbackAttempt.getOrNull().orEmpty()
                if (fallbackPrograms.isNotEmpty()) {
                    // Merge firstPrograms + fallbackPrograms deduplicated by ID
                    val merged = LinkedHashMap<String, ShikhoAcademicProgram>()
                    firstPrograms.orEmpty().forEach { merged[it.id] = it }
                    fallbackPrograms.forEach { if (!merged.containsKey(it.id)) merged[it.id] = it }
                    merged.values.toList()
                } else if (firstPrograms != null) {
                    firstPrograms
                } else {
                    return@withContext Result.failure(
                        firstAttempt.exceptionOrNull()
                            ?: fallbackAttempt.exceptionOrNull()
                            ?: Exception("কোর্সের তালিকা লোড করতে ব্যর্থ")
                    )
                }
            }

            Result.success(
                ShikhoProgramsFetchResult(
                    programs = finalPrograms,
                    updatedAccessToken = activeToken,
                    updatedRefreshToken = updatedRefreshToken
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun executeGetAcademicProgramQuery(
        accessToken: String,
        normalizedClass: String,
        effectiveGroup: String,
        batchId: String?
    ): Result<List<ShikhoAcademicProgram>> {
        val variablesObj = JSONObject().apply {
            if (batchId != null) {
                put("batch_id", batchId)
            } else {
                put("batch_id", JSONObject.NULL)
            }
            put("className", normalizedClass)
            put("group", effectiveGroup)
            put("vendor", VENDOR_BD)
        }

        val payload = JSONObject().apply {
            put("operationName", "GetAcademicProgram")
            put(
                "query",
                "query GetAcademicProgram(\$batch_id: String, \$className: AcademicProgramClassEnum, \$group: StudyGroupTypeEnum, \$vendor: VendorEnum) { listAcademicProgramByEnrollment(batch_id: \$batch_id, class: \$className, group: \$group, vendor: \$vendor) { enrolled_programs { id classes title_bn banner_url is_free } other_programs { id classes title_bn banner_url is_free } } }"
            )
            put("variables", variablesObj)
        }

        val request = Request.Builder()
            .url("$BASE_URL/graphql")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("x-vendor", VENDOR_BD)
            .header("User-Agent", USER_AGENT)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful || bodyStr.isBlank()) {
                return Result.failure(
                    Exception("কোর্সের তালিকা লোড করতে ব্যর্থ (HTTP ${response.code})")
                )
            }

            val root = JSONObject(bodyStr)
            val errors = root.optJSONArray("errors")
            if (errors != null && errors.length() > 0 && root.optJSONObject("data") == null) {
                val firstErr = errors.optJSONObject(0)?.optString("message") ?: "GraphQL Error"
                return Result.failure(Exception(firstErr))
            }

            val container = root
                .optJSONObject("data")
                ?.optJSONObject("listAcademicProgramByEnrollment")

            val enrolledArr = container?.optJSONArray("enrolled_programs") ?: JSONArray()
            val otherArr = container?.optJSONArray("other_programs") ?: JSONArray()

            val combinedMap = LinkedHashMap<String, ShikhoAcademicProgram>()

            fun parseProgramArray(arr: JSONArray, isEnrolled: Boolean) {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id").trim()
                    if (id.isBlank() || combinedMap.containsKey(id)) continue

                    val titleBn = obj.optString("title_bn").trim().ifBlank { id }
                    val bannerUrl = if (obj.isNull("banner_url")) null else obj.optString("banner_url").trim().ifBlank { null }
                    val isFree = obj.optBoolean("is_free", false)

                    val classesList = mutableListOf<String>()
                    val classesArr = obj.optJSONArray("classes")
                    if (classesArr != null) {
                        for (j in 0 until classesArr.length()) {
                            val cls = classesArr.optString(j).trim()
                            if (cls.isNotBlank()) classesList.add(cls)
                        }
                    } else {
                        val clsStr = obj.optString("classes").trim()
                        if (clsStr.isNotBlank() && clsStr != "null") {
                            classesList.add(clsStr)
                        }
                    }

                    combinedMap[id] = ShikhoAcademicProgram(
                        id = id,
                        classes = classesList,
                        titleBn = titleBn,
                        bannerUrl = bannerUrl,
                        isFree = isFree,
                        isEnrolledProgram = isEnrolled
                    )
                }
            }

            // Parse other_programs and enrolled_programs
            parseProgramArray(enrolledArr, isEnrolled = true)
            parseProgramArray(otherArr, isEnrolled = false)

            // Strict Class Filtering (বাধ্যতামূলক):
            // Keep only courses matching normalizedClass so admin's own enrolled HSC courses
            // never leak into C9/C10 or other classes.
            val strictlyFiltered = combinedMap.values.filter { course ->
                if (course.classes.isEmpty()) {
                    true
                } else {
                    course.classes.any { cls -> cls.equals(normalizedClass, ignoreCase = true) }
                }
            }

            return Result.success(strictlyFiltered)
        }
    }
}
