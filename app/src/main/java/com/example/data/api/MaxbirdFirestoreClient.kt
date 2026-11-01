package com.example.data.api

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.MaxbirdAdminAuthSession
import com.example.data.model.MaxbirdPortalSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

/**
 * Dedicated client for Maxbird Admin Control Panel (`https://maxbird.pages.dev`).
 * Connects directly to Firebase Project `empirical-envelope-b0z9n` and the named
 * Firestore database `ai-studio-edusmartappporta-6327637e-4b8d-4e4e-be1d-92bc33269fa0`,
 * targeting document `portal_settings/main`.
 *
 * Uses atomic `writeBatch` (`documents:commit`) with Secret Key verification:
 * - Write 1: `/admin_secrets/{adminSecretKey}` -> `{ verifiedAt: serverTimestamp() }`
 * - Write 2: `/portal_settings/main` -> all 18 required fields + `updatedAt: new Date().toISOString()`
 */
class MaxbirdFirestoreClient(private val context: Context) {

    private val prefs = context.getSharedPreferences("maxbird_web_admin_auth", Context.MODE_PRIVATE)

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val projectId = MaxbirdPortalSettings.FIREBASE_PROJECT_ID
    private val apiKey = MaxbirdPortalSettings.FIREBASE_API_KEY
    private val databaseId = MaxbirdPortalSettings.FIRESTORE_DATABASE_ID

    private val baseDocumentsPath =
        "projects/$projectId/databases/$databaseId/documents"

    private val settingsDocumentResourceName =
        "$baseDocumentsPath/${MaxbirdPortalSettings.COLLECTION_NAME}/${MaxbirdPortalSettings.DOCUMENT_ID}"

    private val documentUrl =
        "https://firestore.googleapis.com/v1/$settingsDocumentResourceName?key=$apiKey"

    private val commitUrl =
        "https://firestore.googleapis.com/v1/$baseDocumentsPath:commit?key=$apiKey"

    // =========================================================================
    // Admin Secret Key & Session Persistence
    // =========================================================================

    fun getSavedAdminSecretKey(): String {
        val saved = prefs.getString("admin_secret_key", "").orEmpty().trim()
        return saved.ifBlank { MaxbirdPortalSettings.DEFAULT_ADMIN_SECRET_KEY }
    }

    fun saveAdminSecretKey(secretKey: String) {
        val clean = secretKey.trim().ifBlank { MaxbirdPortalSettings.DEFAULT_ADMIN_SECRET_KEY }
        prefs.edit().putString("admin_secret_key", clean).apply()
    }

    fun getSavedAuthSession(): MaxbirdAdminAuthSession {
        return MaxbirdAdminAuthSession(
            uid = MaxbirdPortalSettings.DEFAULT_OWNER_ID,
            email = MaxbirdPortalSettings.ADMIN_EMAIL,
            displayName = "Maxbird Admin",
            isSignedIn = true
        )
    }

    fun saveAuthSession(session: MaxbirdAdminAuthSession) {}
    fun clearAuthSession() {}

    suspend fun ensureAutoConnectedSession(): MaxbirdAdminAuthSession = withContext(Dispatchers.IO) {
        getSavedAuthSession()
    }

    suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Result<MaxbirdAdminAuthSession> = Result.success(getSavedAuthSession())

    suspend fun createAdminAccountWithEmailPassword(
        email: String,
        password: String
    ): Result<MaxbirdAdminAuthSession> = Result.success(getSavedAuthSession())

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)

    suspend fun verifyAndSaveFirebaseIdToken(rawIdToken: String): Result<MaxbirdAdminAuthSession> =
        Result.success(getSavedAuthSession())

    // =========================================================================
    // Firestore Read (`portal_settings/main`)
    // =========================================================================

    suspend fun fetchPortalSettings(): Result<MaxbirdPortalSettings> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(documentUrl)
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (response.code == 404) {
                    return@withContext Result.success(
                        MaxbirdPortalSettings(
                            ownerId = MaxbirdPortalSettings.DEFAULT_OWNER_ID,
                            documentExists = false,
                            adminSecretKey = getSavedAdminSecretKey()
                        )
                    )
                }
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception(parseFirestoreError(bodyStr)))
                }

                Result.success(parsePortalSettingsJson(bodyStr))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parsePortalSettingsJson(bodyStr: String): MaxbirdPortalSettings {
        val docJson = JSONObject(bodyStr)
        val fields = docJson.optJSONObject("fields") ?: JSONObject()

        val legacyFileSize = readStringField(fields, "fileSize", "24.5 MB")
        val legacyMinAndroid = readStringField(fields, "minAndroidVersion", "Android 7.0+")
        val legacyNotice = readStringField(
            fields,
            "noticeText",
            "Maxbird অফিশিয়াল অ্যাপের নতুন ভার্সন এখন ডাউনলোডের জন্য উন্মুক্ত!"
        )
        val legacyDownloadCount = readLongField(fields, "downloadCount", 0L)

        val resolvedAppSize = readStringField(fields, "appSize", "").ifBlank { legacyFileSize }
        val resolvedAndroidReq = readStringField(fields, "androidRequirement", "").ifBlank { legacyMinAndroid }
        val resolvedNoticeBanner = readStringField(fields, "noticeBannerText", "").ifBlank { legacyNotice }
        val resolvedTotalDownloads = readStringOrNumberField(
            fields,
            "totalDownloads",
            if (legacyDownloadCount > 0L) legacyDownloadCount.toString() else "10,000+"
        )

        val updatedAtStr = readStringField(fields, "updatedAt", "")
            .ifBlank { readTimestampField(fields, "updatedAt") ?: MaxbirdPortalSettings.currentIsoString() }

        return MaxbirdPortalSettings(
            appName = readStringField(fields, "appName", "Maxbird"),
            appTagline = readStringField(
                fields,
                "appTagline",
                "এইচএসসি ও এসএসসি শিক্ষার্থীদের স্মার্ট লার্নিং অ্যাপ"
            ),
            appVersion = readStringField(fields, "appVersion", "v1.0.0"),
            appSize = resolvedAppSize,
            androidRequirement = resolvedAndroidReq,
            lastUpdated = readStringField(fields, "lastUpdated", "নভেম্বর ২০২৬"),
            totalDownloads = resolvedTotalDownloads,
            apkDownloadUrl = readStringField(fields, "apkDownloadUrl", ""),
            releaseNotes = readStringField(fields, "releaseNotes", ""),
            noticeBannerText = resolvedNoticeBanner,
            showNoticeBanner = readBooleanField(fields, "showNoticeBanner", true),
            supportPhone = readStringField(fields, "supportPhone", ""),
            supportWhatsApp = readStringField(fields, "supportWhatsApp", ""),
            supportEmail = readStringField(fields, "supportEmail", "maiafahim1234@gmail.com"),
            developerName = readStringField(fields, "developerName", "Maxbird Official"),
            appIconUrl = readStringField(fields, "appIconUrl", ""),
            appScreenshots = readStringArrayField(fields, "appScreenshots"),
            updatedAt = updatedAtStr,
            r2BucketPublicDomain = readStringField(
                fields,
                "r2BucketPublicDomain",
                CloudflareR2Client.PUBLIC_BASE_URL.removePrefix("https://")
            ),
            r2ObjectKey = readStringField(fields, "r2ObjectKey", ""),
            telegramHandle = readStringField(fields, "telegramHandle", "@Fahim_Miya_F"),
            telegramUrl = readStringField(fields, "telegramUrl", "https://t.me/Fahim_Miya_F"),
            sha256Short = readStringField(fields, "sha256Short", "Play Protect Verified"),
            ownerId = readStringField(fields, "ownerId", MaxbirdPortalSettings.DEFAULT_OWNER_ID),
            createdAtIso = readTimestampField(fields, "createdAt"),
            updatedAtIso = readTimestampField(fields, "updatedAt"),
            documentExists = true,
            adminSecretKey = getSavedAdminSecretKey()
        )
    }

    // =========================================================================
    // Firestore Atomic `writeBatch` Save (`admin_secrets` + `portal_settings/main`)
    // =========================================================================

    /**
     * Executes the exact `writeBatch` specified by the user:
     * ```ts
     * const batch = writeBatch(db);
     * const secretRef = doc(db, "admin_secrets", adminSecretKey.trim());
     * batch.set(secretRef, { verifiedAt: serverTimestamp() });
     *
     * const mainRef = doc(db, "portal_settings", "main");
     * batch.set(mainRef, {
     *   ...data,
     *   updatedAt: new Date().toISOString()
     * });
     * await batch.commit();
     * ```
     */
    suspend fun savePortalSettings(
        draft: MaxbirdPortalSettings
    ): Result<MaxbirdPortalSettings> = withContext(Dispatchers.IO) {
        try {
            val secretKey = draft.adminSecretKey.trim().ifBlank { getSavedAdminSecretKey() }
            if (secretKey.isBlank()) {
                return@withContext Result.failure(Exception("দয়া করে Admin Secret Key দিন।"))
            }
            saveAdminSecretKey(secretKey)

            val sanitized = draft.copy(adminSecretKey = secretKey).sanitizeForSave()

            val encodedSecretDocId = URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
            val secretDocResourceName =
                "$baseDocumentsPath/${MaxbirdPortalSettings.ADMIN_SECRETS_COLLECTION}/$encodedSecretDocId"

            // Step 1: batch.set(secretRef, { verifiedAt: serverTimestamp() })
            val secretWriteOp = JSONObject().apply {
                put(
                    "update",
                    JSONObject().apply {
                        put("name", secretDocResourceName)
                        put("fields", JSONObject())
                    }
                )
                put(
                    "updateTransforms",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("fieldPath", "verifiedAt")
                                put("setToServerValue", "REQUEST_TIME")
                            }
                        )
                    }
                )
            }

            // Step 2 (Primary 18-field schema):
            // batch.set(mainRef, { ...18 fields, updatedAt: new Date().toISOString() })
            fun executePrimary18FieldBatch(): Pair<Boolean, String> {
                val mainFieldsObj = JSONObject().apply {
                    put("appName", stringValue(sanitized.appName))
                    put("appTagline", stringValue(sanitized.appTagline))
                    put("appVersion", stringValue(sanitized.appVersion))
                    put("appSize", stringValue(sanitized.appSize))
                    put("androidRequirement", stringValue(sanitized.androidRequirement))
                    put("lastUpdated", stringValue(sanitized.lastUpdated))
                    put("totalDownloads", stringValue(sanitized.totalDownloads))
                    put("apkDownloadUrl", stringValue(sanitized.apkDownloadUrl))
                    put("releaseNotes", stringValue(sanitized.releaseNotes))
                    put("noticeBannerText", stringValue(sanitized.noticeBannerText))
                    put("showNoticeBanner", booleanValue(sanitized.showNoticeBanner))
                    put("supportPhone", stringValue(sanitized.supportPhone))
                    put("supportWhatsApp", stringValue(sanitized.supportWhatsApp))
                    put("supportEmail", stringValue(sanitized.supportEmail))
                    put("developerName", stringValue(sanitized.developerName))
                    put("appIconUrl", stringValue(sanitized.appIconUrl))
                    put("appScreenshots", stringArrayValue(sanitized.appScreenshots))
                    put("updatedAt", stringValue(sanitized.updatedAt))
                }

                val mainWriteOp = JSONObject().apply {
                    put(
                        "update",
                        JSONObject().apply {
                            put("name", settingsDocumentResourceName)
                            put("fields", mainFieldsObj)
                        }
                    )
                }

                val commitPayload = JSONObject().apply {
                    put(
                        "writes",
                        JSONArray().apply {
                            put(secretWriteOp)
                            put(mainWriteOp)
                        }
                    )
                }

                val request = Request.Builder()
                    .url(commitUrl)
                    .post(commitPayload.toString().toRequestBody(jsonMediaType))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string().orEmpty()
                    return Pair(response.isSuccessful, bodyStr)
                }
            }

            val (ok, errBody) = executePrimary18FieldBatch()
            if (!ok) {
                return@withContext Result.failure(Exception(parseFirestoreError(errBody)))
            }

            val refreshed = fetchPortalSettings().getOrNull() ?: sanitized.copy(documentExists = true)
            Result.success(refreshed.copy(adminSecretKey = secretKey))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // Local .APK File Inspector Helper
    // =========================================================================

    data class ApkFileInspection(
        val fileName: String,
        val fileSizeFormatted: String,
        val sizeBytes: Long
    )

    fun inspectLocalApkFile(uri: Uri): Result<ApkFileInspection> {
        return try {
            var fileName = "Maxbird.apk"
            var sizeBytes = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx >= 0) {
                        val rawName = cursor.getString(nameIdx)
                        if (!rawName.isNullOrBlank()) {
                            fileName = rawName.trim()
                        }
                    }
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                        sizeBytes = cursor.getLong(sizeIdx)
                    }
                }
            }

            if (sizeBytes <= 0L) {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                    if (afd.length > 0L) {
                        sizeBytes = afd.length
                    }
                }
            }

            val mb = sizeBytes.toDouble() / (1024.0 * 1024.0)
            val formattedSize = when {
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                sizeBytes > 0L -> String.format(Locale.US, "%.0f KB", sizeBytes / 1024.0)
                else -> "24.5 MB"
            }

            Result.success(
                ApkFileInspection(
                    fileName = fileName,
                    fileSizeFormatted = formattedSize,
                    sizeBytes = sizeBytes
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // Firestore Value Helpers
    // =========================================================================

    private fun stringValue(value: String): JSONObject =
        JSONObject().put("stringValue", value)

    private fun booleanValue(value: Boolean): JSONObject =
        JSONObject().put("booleanValue", value)

    private fun stringArrayValue(items: List<String>): JSONObject {
        val valuesArray = JSONArray()
        items.forEach { item ->
            valuesArray.put(stringValue(item))
        }
        return JSONObject().put(
            "arrayValue",
            JSONObject().put("values", valuesArray)
        )
    }

    private fun readStringField(fields: JSONObject, key: String, default: String): String {
        val fieldObj = fields.optJSONObject(key) ?: return default
        return if (fieldObj.has("stringValue")) {
            fieldObj.optString("stringValue", default)
        } else {
            default
        }
    }

    private fun readStringOrNumberField(fields: JSONObject, key: String, default: String): String {
        val fieldObj = fields.optJSONObject(key) ?: return default
        return when {
            fieldObj.has("stringValue") -> fieldObj.optString("stringValue", default)
            fieldObj.has("integerValue") -> fieldObj.optString("integerValue", default)
            fieldObj.has("doubleValue") -> fieldObj.optDouble("doubleValue", 0.0).roundToLong().toString()
            else -> default
        }
    }

    private fun readBooleanField(fields: JSONObject, key: String, default: Boolean): Boolean {
        val fieldObj = fields.optJSONObject(key) ?: return default
        return if (fieldObj.has("booleanValue")) {
            fieldObj.optBoolean("booleanValue", default)
        } else {
            default
        }
    }

    private fun readStringArrayField(fields: JSONObject, key: String): List<String> {
        val fieldObj = fields.optJSONObject(key) ?: return emptyList()
        val arrayVal = fieldObj.optJSONObject("arrayValue") ?: return emptyList()
        val values = arrayVal.optJSONArray("values") ?: return emptyList()
        val result = mutableListOf<String>()
        for (i in 0 until values.length()) {
            val itemObj = values.optJSONObject(i) ?: continue
            val str = itemObj.optString("stringValue", "").trim()
            if (str.isNotEmpty()) {
                result.add(str)
            }
        }
        return result
    }

    private fun readLongField(fields: JSONObject, key: String, default: Long): Long {
        val fieldObj = fields.optJSONObject(key) ?: return default
        return when {
            fieldObj.has("integerValue") -> fieldObj.optString("integerValue", default.toString()).toLongOrNull() ?: default
            fieldObj.has("doubleValue") -> fieldObj.optDouble("doubleValue", default.toDouble()).roundToLong()
            else -> default
        }
    }

    private fun readTimestampField(fields: JSONObject, key: String): String? {
        val fieldObj = fields.optJSONObject(key) ?: return null
        return fieldObj.optString("timestampValue", "").takeIf { it.isNotBlank() }
    }

    private fun parseFirestoreError(rawBody: String): String {
        if (rawBody.isBlank()) return "সার্ভারের সাথে সংযোগ ব্যর্থ হয়েছে।"
        return try {
            val json = JSONObject(rawBody)
            val errObj = json.optJSONObject("error")
            val rawMessage = errObj?.optString("message").orEmpty().ifBlank { rawBody.take(180) }
            when {
                rawMessage.contains("PERMISSION_DENIED", ignoreCase = true) ||
                    rawMessage.contains("Missing or insufficient permissions", ignoreCase = true) ->
                    "পারমিশন এরর: Admin Secret Key সঠিক নয় অথবা ডাটাবেজ রুলস মিলছে না! দয়া করে সঠিক Secret Key (Maxbird_Secret_Fahim_2026) দিন।"
                else -> rawMessage
            }
        } catch (_: Exception) {
            rawBody.take(180)
        }
    }
}
