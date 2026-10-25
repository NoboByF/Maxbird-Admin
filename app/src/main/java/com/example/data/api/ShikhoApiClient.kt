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
 * Official Shikho Server API Client (`https://api.shikho.com`)
 * Implements:
 * 1. PIN-based Direct Login (`POST https://api.shikho.com/auth/v2/login`)
 * 2. Real Syllabus Class List (`GET https://api.shikho.com/class_list?vendor=BD&type=syllabus`)
 * 3. Real BatchOptions GraphQL (`POST https://api.shikho.com/graphql`)
 * 4. Real GetAcademicProgram GraphQL (`POST https://api.shikho.com/graphql`)
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
         * Formats phone number according to rule 1.1:
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
         * Builds `batch_id` according to Part 3 Step 4 rules:
         * - If className is "C11" or "C12" and year is "2027" -> "HSC 2027"
         * - If className is "C9" or "C10" and year is "2028" -> "SSC 2028"
         * - If C5–C8 -> year (e.g. "2026")
         */
        fun buildBatchId(className: String, year: String): String {
            val cleanYear = year.trim()
            if (cleanYear.isBlank()) return ""
            return when (className.uppercase().trim()) {
                "C11", "C12" -> {
                    if (cleanYear.uppercase().startsWith("HSC")) cleanYear else "HSC $cleanYear"
                }
                "C9", "C10" -> {
                    if (cleanYear.uppercase().startsWith("SSC")) cleanYear else "SSC $cleanYear"
                }
                else -> {
                    // C5 to C8 -> only year (strip SSC/HSC if accidentally present)
                    cleanYear.replace(Regex("^(SSC|HSC)\\s*", RegexOption.IGNORE_CASE), "").trim()
                }
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
     * Part 3 - Step 3: Real BatchOptions GraphQL API
     * `POST https://api.shikho.com/graphql`
     */
    suspend fun fetchBatchOptions(
        accessToken: String,
        classCode: String
    ): Result<List<ShikhoBatchExamYear>> = withContext(Dispatchers.IO) {
        try {
            val cleanToken = accessToken.trim()
            if (cleanToken.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("শিখো অ্যাকাউন্ট লগইন করা নেই।")
                )
            }

            val payload = JSONObject().apply {
                put("operationName", "BatchOptions")
                put(
                    "query",
                    "query BatchOptions(\$class: ClassEnumCommon!) { batchOptions(class: \$class) { exam_years { year label } } }"
                )
                put("variables", JSONObject().apply {
                    put("class", classCode.trim().uppercase())
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
                    return@withContext Result.failure(
                        Exception("ব্যাচ অপশন লোড করতে ব্যর্থ (HTTP ${response.code})")
                    )
                }

                val root = JSONObject(bodyStr)
                val errors = root.optJSONArray("errors")
                if (errors != null && errors.length() > 0 && root.optJSONObject("data") == null) {
                    val firstErr = errors.optJSONObject(0)?.optString("message") ?: "GraphQL Error"
                    return@withContext Result.failure(Exception(firstErr))
                }

                val examYearsArr = root
                    .optJSONObject("data")
                    ?.optJSONObject("batchOptions")
                    ?.optJSONArray("exam_years")
                    ?: JSONArray()

                val examYears = mutableListOf<ShikhoBatchExamYear>()
                for (i in 0 until examYearsArr.length()) {
                    val obj = examYearsArr.optJSONObject(i) ?: continue
                    val year = obj.optString("year").trim()
                    val label = obj.optString("label").trim().ifBlank { year }
                    if (year.isNotBlank()) {
                        examYears.add(ShikhoBatchExamYear(year = year, label = label))
                    }
                }

                Result.success(examYears.distinctBy { it.year })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Part 3 - Step 4: Real GetAcademicProgram GraphQL API
     * `POST https://api.shikho.com/graphql`
     */
    suspend fun fetchAcademicPrograms(
        accessToken: String,
        className: String,
        group: String,
        year: String
    ): Result<List<ShikhoAcademicProgram>> = withContext(Dispatchers.IO) {
        try {
            val cleanToken = accessToken.trim()
            if (cleanToken.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("শিখো অ্যাকাউন্ট লগইন করা নেই।")
                )
            }

            val normalizedClass = className.trim().uppercase()
            val effectiveGroup = if (normalizedClass in GROUP_REQUIRED_CLASSES) {
                group.trim().ifBlank { "Science" }
            } else {
                "None"
            }
            val batchId = buildBatchId(normalizedClass, year)

            val payload = JSONObject().apply {
                put("operationName", "GetAcademicProgram")
                put(
                    "query",
                    "query GetAcademicProgram(\$batch_id: String, \$className: AcademicProgramClassEnum, \$group: StudyGroupTypeEnum, \$vendor: VendorEnum) { listAcademicProgramByEnrollment(batch_id: \$batch_id, class: \$className, group: \$group, vendor: \$vendor) { enrolled_programs { id classes title_bn banner_url is_free } other_programs { id classes title_bn banner_url is_free } } }"
                )
                put("variables", JSONObject().apply {
                    put("batch_id", batchId)
                    put("className", normalizedClass)
                    put("group", effectiveGroup)
                    put("vendor", VENDOR_BD)
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
                    return@withContext Result.failure(
                        Exception("কোর্সের তালিকা লোড করতে ব্যর্থ (HTTP ${response.code})")
                    )
                }

                val root = JSONObject(bodyStr)
                val errors = root.optJSONArray("errors")
                if (errors != null && errors.length() > 0 && root.optJSONObject("data") == null) {
                    val firstErr = errors.optJSONObject(0)?.optString("message") ?: "GraphQL Error"
                    return@withContext Result.failure(Exception(firstErr))
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

                parseProgramArray(enrolledArr, isEnrolled = true)
                parseProgramArray(otherArr, isEnrolled = false)

                Result.success(combinedMap.values.toList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
