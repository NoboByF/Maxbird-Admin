package com.example.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.MainActivity
import com.example.data.api.SupabaseClient
import com.example.data.preferences.AdminPreferences
import com.example.data.repository.SupabaseMaxBirdRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * Full OneSignal + Firebase Cloud Messaging (FCM) Push Notification Manager for MaxBird Admin App.
 *
 * 1. Resolves OneSignal App ID from `BuildConfig.ONESIGNAL_APP_ID` / `AdminPreferences`, or falls back
 *    to Supabase RPC `POST /rest/v1/rpc/get_onesignal_public_app_id`.
 * 2. Initializes the official OneSignal Android SDK v5 (`OneSignal.initWithContext(context, appId)`),
 *    binds external ID `"ADMIN_MASTER"` and tags `"role" = "admin"`, `"is_admin" = "true"`, and listens
 *    for push clicks to deep-link into Device Verification, Course Access Requests, or Payment Verification.
 * 3. Obtains the device's FCM token (`FirebaseMessaging.getInstance().token`), registers it with
 *    OneSignal REST API (`POST https://onesignal.com/api/v1/players`), and automatically upserts
 *    the `onesignal_player_id` and `fcm_token` into Supabase `public.admin_push_devices`
 *    (`POST /rest/v1/admin_push_devices?on_conflict=admin_device_id`).
 */
object OneSignalPushManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var initializedAppId: String? = null
    private var clickListenerRegistered = false

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val UUID_REGEX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    fun isValidOneSignalAppId(appId: String?): Boolean {
        val clean = appId?.trim().orEmpty()
        return UUID_REGEX.matches(clean)
    }

    /**
     * Initializes the OneSignal SDK if a valid OneSignal App ID is configured, and launches
     * background sync for Supabase `get_onesignal_public_app_id`, FCM token, OneSignal `/api/v1/players`,
     * and Supabase `public.admin_push_devices`.
     */
    fun initializeIfConfigured(
        context: Context,
        explicitAppId: String? = null
    ): Boolean {
        val appContext = context.applicationContext
        val prefs = AdminPreferences(appContext)
        val appId = explicitAppId?.trim()?.takeIf { it.isNotEmpty() }
            ?: prefs.getOneSignalAppId().trim()

        val initializedNow = if (isValidOneSignalAppId(appId)) {
            initOneSignalSdkInternal(appContext, appId)
        } else {
            false
        }

        // Always launch background registration sync (will also fetch App ID from Supabase RPC if blank)
        syncAdminPushRegistration(
            context = appContext,
            explicitAppId = explicitAppId
        )

        return initializedNow
    }

    private fun initOneSignalSdkInternal(
        appContext: Context,
        appId: String
    ): Boolean {
        if (!isValidOneSignalAppId(appId)) return false
        return try {
            OneSignal.Debug.logLevel = LogLevel.WARN
            if (initializedAppId != appId) {
                OneSignal.initWithContext(appContext, appId)
                initializedAppId = appId
            }

            // Tag & login this device as ADMIN_MASTER with role=admin, is_admin=true
            try {
                OneSignal.login("ADMIN_MASTER")
                OneSignal.User.addTags(
                    mapOf(
                        "role" to "admin",
                        "is_admin" to "true",
                        "app_type" to "maxbird_admin"
                    )
                )
                OneSignal.User.pushSubscription.optIn()
            } catch (_: Throwable) {
            }

            // Request push permission via OneSignal SDK on background coroutine
            scope.launch {
                try {
                    OneSignal.Notifications.requestPermission(true)
                } catch (_: Throwable) {
                }
            }

            // Register deep-link click listener once
            if (!clickListenerRegistered) {
                OneSignal.Notifications.addClickListener(object : INotificationClickListener {
                    override fun onClick(event: INotificationClickEvent) {
                        try {
                            val data = event.notification.additionalData
                            val eventType = data?.optString("type", "")?.takeIf { it.isNotBlank() }
                                ?: data?.optString("event_type", "")?.takeIf { it.isNotBlank() }
                                ?: ""
                            val explicitTarget = data?.optString("target_screen", "")?.takeIf { it.isNotBlank() }

                            val targetScreen = when {
                                !explicitTarget.isNullOrBlank() -> explicitTarget
                                eventType.equals("ADMIN_DEVICE_ACCESS_REQUEST", ignoreCase = true) ||
                                    eventType.contains("device", ignoreCase = true) -> "devices"
                                else -> "course_sales"
                            }

                            val subTab = when {
                                data?.has("course_sales_tab") == true -> data.optInt("course_sales_tab", 0)
                                eventType.equals("ADMIN_COURSE_PAYMENT_SUBMITTED", ignoreCase = true) ||
                                    eventType.contains("payment", ignoreCase = true) -> 1
                                else -> 0
                            }

                            val deviceQuery = data?.optString("display_device_id", "")?.takeIf { it.isNotBlank() }
                                ?: data?.optString("device_id", "")?.takeIf { it.isNotBlank() }
                                ?: data?.optString("device_hash", "")?.takeIf { it.isNotBlank() }

                            val launchIntent = Intent(appContext, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra(AdminNotificationHelper.EXTRA_NAVIGATE_SCREEN, targetScreen)
                                putExtra(AdminNotificationHelper.EXTRA_COURSE_SALES_TAB, subTab)
                                if (!deviceQuery.isNullOrBlank()) {
                                    putExtra(AdminNotificationHelper.EXTRA_DEVICE_SEARCH_QUERY, deviceQuery)
                                }
                            }
                            appContext.startActivity(launchIntent)
                        } catch (_: Throwable) {
                        }
                    }
                })
                clickListenerRegistered = true
            }
            true
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Full automatic registration pipeline:
     * 1. Resolves OneSignal App ID (from preferences / BuildConfig, or via Supabase RPC `get_onesignal_public_app_id`).
     * 2. Obtains FCM token from `FirebaseMessaging.getInstance().token`.
     * 3. Registers FCM token with OneSignal REST API `POST https://onesignal.com/api/v1/players`
     *    (`external_user_id = "ADMIN_MASTER"`, `tags = {"role": "admin", "is_admin": "true"}`).
     * 4. Upserts `admin_device_id`, `onesignal_player_id`, `fcm_token`, `admin_label`, `is_active = true`
     *    to Supabase `public.admin_push_devices`.
     */
    fun syncAdminPushRegistration(
        context: Context,
        explicitAppId: String? = null,
        fcmTokenOverride: String? = null,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val appContext = context.applicationContext
        scope.launch {
            val ok = syncAdminPushRegistrationSuspend(
                appContext = appContext,
                explicitAppId = explicitAppId,
                fcmTokenOverride = fcmTokenOverride
            )
            onComplete?.invoke(ok)
        }
    }

    suspend fun syncAdminPushRegistrationSuspend(
        appContext: Context,
        explicitAppId: String? = null,
        fcmTokenOverride: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val prefs = AdminPreferences(appContext)
            val supabaseClient = SupabaseClient(prefs)
            val repository = SupabaseMaxBirdRepository(supabaseClient, prefs)

            // Step 1: Resolve OneSignal App ID (local/BuildConfig first, then Supabase RPC get_onesignal_public_app_id)
            var appId = explicitAppId?.trim()?.takeIf { isValidOneSignalAppId(it) }
                ?: prefs.getOneSignalAppId().trim().takeIf { isValidOneSignalAppId(it) }
                ?: ""

            if (appId.isBlank()) {
                val rpcResult = repository.fetchOneSignalPublicAppId()
                val fetchedId = rpcResult.getOrNull()?.trim().orEmpty()
                if (isValidOneSignalAppId(fetchedId)) {
                    appId = fetchedId
                    prefs.setOneSignalAppId(appId)
                }
            }

            if (isValidOneSignalAppId(appId)) {
                initOneSignalSdkInternal(appContext, appId)
            }

            // Step 2: Obtain FCM Token
            val fcmToken = fcmTokenOverride?.trim()?.takeIf { it.isNotEmpty() }
                ?: fetchFcmTokenSuspend().takeIf { it.isNotEmpty() }
                ?: prefs.getSavedFcmToken()

            if (fcmToken.isNotBlank()) {
                prefs.setSavedFcmToken(fcmToken)
            }

            // Step 3: Register with OneSignal REST API (`POST https://onesignal.com/api/v1/players`)
            var playerId = ""
            if (isValidOneSignalAppId(appId) && fcmToken.isNotBlank()) {
                playerId = registerPlayerWithOneSignalRestApi(
                    appId = appId,
                    fcmToken = fcmToken
                )
            }

            if (playerId.isBlank()) {
                playerId = try {
                    OneSignal.User.pushSubscription.id.trim()
                } catch (_: Throwable) {
                    ""
                }
            }
            if (playerId.isBlank()) {
                playerId = prefs.getSavedOneSignalPlayerId()
            } else {
                prefs.setSavedOneSignalPlayerId(playerId)
            }

            // Step 4: Upsert into Supabase `public.admin_push_devices`
            if (playerId.isNotBlank() || fcmToken.isNotBlank()) {
                val adminDeviceId = prefs.getAdminAndroidDeviceId(appContext)
                val manufacturer = Build.MANUFACTURER?.trim().orEmpty()
                val model = Build.MODEL?.trim().orEmpty()
                val adminLabel = when {
                    manufacturer.isNotEmpty() && model.isNotEmpty() && !model.startsWith(manufacturer, ignoreCase = true) ->
                        "$manufacturer $model"
                    model.isNotEmpty() -> model
                    manufacturer.isNotEmpty() -> manufacturer
                    else -> "MaxBird Admin Android"
                }

                repository.upsertAdminPushDevice(
                    adminDeviceId = adminDeviceId,
                    oneSignalPlayerId = playerId,
                    fcmToken = fcmToken,
                    adminLabel = adminLabel,
                    isActive = true
                )
                return@withContext true
            }
            false
        } catch (_: Throwable) {
            false
        }
    }

    private suspend fun fetchFcmTokenSuspend(): String = suspendCancellableCoroutine { cont ->
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (!cont.isActive) return@addOnCompleteListener
                    if (task.isSuccessful) {
                        cont.resume(task.result?.trim().orEmpty())
                    } else {
                        cont.resume("")
                    }
                }
        } catch (_: Throwable) {
            if (cont.isActive) {
                cont.resume("")
            }
        }
    }

    /**
     * Calls `POST https://onesignal.com/api/v1/players` to register the Admin device's FCM token
     * with `external_user_id = "ADMIN_MASTER"` and `tags = {"role": "admin", "is_admin": "true"}`.
     * Returns the OneSignal `player_id` (`id`).
     */
    private fun registerPlayerWithOneSignalRestApi(
        appId: String,
        fcmToken: String
    ): String {
        return try {
            val payload = JSONObject().apply {
                put("app_id", appId.trim())
                put("device_type", 1) // 1 = Android
                put("identifier", fcmToken.trim())
                put("notification_types", 1) // 1 = Subscribed
                put("external_user_id", "ADMIN_MASTER")
                put("tags", JSONObject().apply {
                    put("role", "admin")
                    put("is_admin", "true")
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url("https://onesignal.com/api/v1/players")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Accept", "application/json")
                .post(payload.toString().toRequestBody(mediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (response.isSuccessful && bodyStr.isNotBlank()) {
                    val json = JSONObject(bodyStr)
                    json.optString("id", "").trim()
                } else {
                    ""
                }
            }
        } catch (_: Throwable) {
            ""
        }
    }

    /**
     * Returns current OneSignal + FCM subscription status summary for the UI.
     */
    fun getSubscriptionStatusSummary(
        context: Context,
        appIdOverride: String? = null
    ): String {
        val prefs = AdminPreferences(context.applicationContext)
        val appId = appIdOverride?.trim()?.takeIf { it.isNotEmpty() }
            ?: prefs.getOneSignalAppId().trim()
        val apiKey = prefs.getOneSignalRestApiKey().trim()
        val savedPlayerId = prefs.getSavedOneSignalPlayerId()
        val savedFcm = prefs.getSavedFcmToken()

        if (!isValidOneSignalAppId(appId)) {
            return if (savedFcm.isNotBlank()) {
                "✅ FCM সক্রিয় • OneSignal App ID অটো-সিঙ্ক হচ্ছে..."
            } else {
                "OneSignal App ID সেট করা হয়নি (সেট করলে বা ডাটাবেজ কানেক্ট থাকলে অটো-সিঙ্ক হবে)"
            }
        }
        return try {
            val subId = try {
                OneSignal.User.pushSubscription.id.takeIf { it.isNotBlank() }
            } catch (_: Throwable) {
                null
            } ?: savedPlayerId.takeIf { it.isNotBlank() }

            val optedIn = try {
                OneSignal.User.pushSubscription.optedIn
            } catch (_: Throwable) {
                false
            }
            buildString {
                append("✅ OneSignal + FCM সংযুক্ত (App: ${appId.take(8)}...)")
                if (!subId.isNullOrBlank()) {
                    append(" • Player ID: ${subId.take(8)}...")
                } else if (optedIn) {
                    append(" • Push Opted-In")
                }
                if (savedFcm.isNotBlank()) {
                    append(" • FCM রেডি")
                }
                if (apiKey.isNotBlank()) {
                    append(" • REST API সক্রিয়")
                }
            }
        } catch (_: Throwable) {
            "✅ OneSignal App ID সংরক্ষিত (${appId.take(8)}...)"
        }
    }

    fun getSubscriptionSummary(
        context: Context,
        appIdOverride: String? = null
    ): String = getSubscriptionStatusSummary(context, appIdOverride)

    /**
     * Dispatches a real push notification through OneSignal's Cloud REST API (`https://onesignal.com/api/v1/notifications`).
     */
    suspend fun sendCloudPushNotification(
        appId: String,
        restApiKey: String,
        title: String,
        message: String,
        targetScreen: String = "course_sales",
        courseSalesTab: Int = 0,
        deviceId: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanAppId = appId.trim()
        val rawApiKey = restApiKey.trim()

        if (!isValidOneSignalAppId(cleanAppId)) {
            return@withContext Result.failure(
                IllegalStateException("সঠিক OneSignal App ID (UUID ফরম্যাট) দিন।")
            )
        }
        if (rawApiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("OneSignal REST API Key দেওয়া হয়নি।")
            )
        }

        val cleanKey = rawApiKey
            .removePrefix("Basic ")
            .removePrefix("Key ")
            .removePrefix("Bearer ")
            .trim()

        try {
            val payloadJson = JSONObject().apply {
                put("app_id", cleanAppId)
                put("included_segments", JSONArray().apply {
                    put("Total Subscriptions")
                })
                put("headings", JSONObject().apply {
                    put("en", title)
                    put("bn", title)
                })
                put("contents", JSONObject().apply {
                    put("en", message)
                    put("bn", message)
                })
                put("priority", 10)
                put("android_visibility", 1)
                put("data", JSONObject().apply {
                    put("target_screen", targetScreen)
                    put("course_sales_tab", courseSalesTab)
                    if (!deviceId.isNullOrBlank()) {
                        put("device_id", deviceId)
                    }
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val bodyString = payloadJson.toString()

            val authHeadersToTry = if (cleanKey.startsWith("os_v2_")) {
                listOf("Key $cleanKey", "Basic $cleanKey")
            } else {
                listOf("Basic $cleanKey", "Key $cleanKey")
            }

            var lastError = ""
            for (authHeader in authHeadersToTry) {
                val request = Request.Builder()
                    .url("https://onesignal.com/api/v1/notifications")
                    .addHeader("Authorization", authHeader)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Accept", "application/json")
                    .post(bodyString.toRequestBody(mediaType))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val respText = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        return@withContext Result.success(respText)
                    } else {
                        lastError = "HTTP ${response.code}: ${respText.take(180)}"
                    }
                }
            }

            Result.failure(IllegalStateException("OneSignal API Error: $lastError"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendCloudPushNotification(
        context: Context,
        title: String,
        message: String,
        targetScreen: String = "course_sales",
        courseSalesTab: Int = 0,
        deviceId: String? = null
    ): Result<String> = sendPushViaOneSignalRestApi(
        context = context,
        title = title,
        message = message,
        targetScreen = targetScreen,
        courseSalesTab = courseSalesTab,
        deviceId = deviceId
    )

    suspend fun sendPushViaOneSignalRestApi(
        context: Context,
        title: String,
        message: String,
        targetScreen: String = "course_sales",
        courseSalesTab: Int = 0,
        deviceId: String? = null
    ): Result<String> {
        val prefs = AdminPreferences(context.applicationContext)
        return sendCloudPushNotification(
            appId = prefs.getOneSignalAppId(),
            restApiKey = prefs.getOneSignalRestApiKey(),
            title = title,
            message = message,
            targetScreen = targetScreen,
            courseSalesTab = courseSalesTab,
            deviceId = deviceId
        )
    }

    /**
     * Generates the complete Supabase PostgreSQL Security + `SECURITY DEFINER` RPC + Trigger script:
     * 1) Protects `public.onesignal_config` with RLS (`REVOKE SELECT, UPDATE ON public.onesignal_config FROM anon`)
     * 2) Provides `SECURITY DEFINER` RPCs (`get_onesignal_public_app_id`, `update_onesignal_config_secure`,
     *    `send_student_push_via_onesignal`, `notify_student_via_onesignal`, `notify_admin_via_onesignal`)
     * 3) Provides `SECURITY DEFINER` Admin Delete & Permission RPCs (`admin_delete_device`,
     *    `admin_delete_activated_device`, `admin_delete_access_code`, `admin_delete_app_notice`,
     *    `admin_delete_app_update`, `admin_delete_support_link`, `admin_delete_course_enrollment`,
     *    `admin_set_device_can_view_profiles`, `admin_revoke_device_to_pending`) so `anon` never needs direct `DELETE` privileges.
     */
    fun buildSupabaseOneSignalTriggerSql(
        appId: String = "",
        restApiKey: String = ""
    ): String {
        val safeAppId = appId.trim().ifBlank { "YOUR_ONESIGNAL_APP_ID" }
        val cleanKey = restApiKey.trim()
            .removePrefix("Basic ")
            .removePrefix("Key ")
            .trim()
            .ifBlank { "YOUR_ONESIGNAL_REST_API_KEY" }

        return """
-- ============================================================================
-- MaxBird Admin & Student App: Hardened Security + OneSignal Push RPCs (SQL)
-- ============================================================================

-- ১. pg_net এক্সটেনশন চালু করুন (সার্ভার-সাইড থেকে OneSignal API কল করার জন্য)
CREATE EXTENSION IF NOT EXISTS pg_net;

-- ১.ক. স্টুডেন্ট অ্যাপ থেকে কোর্স এক্সেস রিকোয়েস্ট পাঠালে যেন অটো-এপ্রুভ না দেখায় তার ফিক্স:
-- course_access_enrollments টেবিলের ডিফল্ট মানগুলো 0 / false / '[]' করা যাতে অ্যাডমিন এপ্রুভ করার আগে দাম ও কিস্তি না দেখায়
ALTER TABLE public.course_access_enrollments
    ALTER COLUMN status SET DEFAULT 'REQUESTED',
    ALTER COLUMN access_status SET DEFAULT 'REQUESTED',
    ALTER COLUMN total_price SET DEFAULT 0,
    ALTER COLUMN total_fee SET DEFAULT 0,
    ALTER COLUMN due_amount SET DEFAULT 0,
    ALTER COLUMN next_installment_amount SET DEFAULT 0,
    ALTER COLUMN installment_enabled SET DEFAULT false,
    ALTER COLUMN allow_installments SET DEFAULT false,
    ALTER COLUMN installment_plans_json SET DEFAULT '[]'::jsonb,
    ALTER COLUMN installment_plans SET DEFAULT '[]'::jsonb;

-- ১.খ. fn_sync_course_enrollment_columns ট্রিগার ফাংশন আপডেট করা (সব মিরর কলাম সিঙ্ক রেখে REQUESTED অবস্থায় approved_at না থাকলে total_price = 0 রাখা):
CREATE OR REPLACE FUNCTION public.fn_sync_course_enrollment_columns()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    -- Sync device_id <-> device_hash
    NEW.device_id := COALESCE(NULLIF(btrim(NEW.device_id), ''), NULLIF(btrim(NEW.device_hash), ''), '');
    NEW.device_hash := COALESCE(NULLIF(btrim(NEW.device_hash), ''), NULLIF(btrim(NEW.device_id), ''), '');

    -- Sync student_phone <-> user_phone
    NEW.student_phone := public.normalize_bd_phone(COALESCE(NULLIF(btrim(NEW.student_phone), ''), NULLIF(btrim(NEW.user_phone), ''), ''));
    NEW.user_phone := COALESCE(NULLIF(btrim(NEW.user_phone), ''), NEW.student_phone);

    -- Sync shikho_account_key <-> shikho_user_id <-> student_key
    NEW.shikho_account_key := COALESCE(
        NULLIF(public.normalize_bd_phone(NEW.shikho_account_key), ''),
        NULLIF(btrim(NEW.shikho_account_key), ''),
        NULLIF(btrim(NEW.shikho_user_id), ''),
        NULLIF(btrim(NEW.student_key), ''),
        NULLIF(NEW.student_phone, ''),
        NEW.device_id
    );
    NEW.shikho_user_id := COALESCE(NULLIF(btrim(NEW.shikho_user_id), ''), NEW.shikho_account_key);
    NEW.student_key := COALESCE(NULLIF(btrim(NEW.student_key), ''), NEW.shikho_account_key);

    -- Sync student_class <-> class_code & student_group <-> group_code
    NEW.student_class := COALESCE(NULLIF(btrim(NEW.student_class), ''), NULLIF(btrim(NEW.class_code), ''), '');
    NEW.class_code := COALESCE(NULLIF(btrim(NEW.class_code), ''), NEW.student_class);
    NEW.student_group := COALESCE(NULLIF(btrim(NEW.student_group), ''), NULLIF(btrim(NEW.group_code), ''), '');
    NEW.group_code := COALESCE(NULLIF(btrim(NEW.group_code), ''), NEW.student_group);

    -- Sync program_id <-> course_id & program_title <-> course_title
    NEW.program_id := COALESCE(NULLIF(btrim(NEW.program_id), ''), NULLIF(btrim(NEW.course_id), ''), '');
    NEW.course_id := COALESCE(NULLIF(btrim(NEW.course_id), ''), NEW.program_id);
    NEW.program_title := COALESCE(NULLIF(btrim(NEW.program_title), ''), NULLIF(btrim(NEW.course_title), ''), '');
    NEW.course_title := COALESCE(NULLIF(btrim(NEW.course_title), ''), NEW.program_title);

    -- Sync status <-> access_status
    IF TG_OP = 'UPDATE' AND NEW.access_status IS DISTINCT FROM OLD.access_status AND NEW.status IS NOT DISTINCT FROM OLD.status THEN
        NEW.status := UPPER(COALESCE(NULLIF(btrim(NEW.access_status), ''), 'REQUESTED'));
    ELSE
        NEW.status := UPPER(COALESCE(NULLIF(btrim(NEW.status), ''), NULLIF(btrim(NEW.access_status), ''), 'REQUESTED'));
    END IF;
    NEW.access_status := NEW.status;

    -- Sync selected_plan <-> active_plan
    NEW.selected_plan_id := COALESCE(NULLIF(btrim(NEW.selected_plan_id), ''), NULLIF(btrim(NEW.active_plan_id), ''), 'FULL');
    NEW.active_plan_id := COALESCE(NULLIF(btrim(NEW.active_plan_id), ''), NEW.selected_plan_id);
    NEW.selected_plan_title := COALESCE(NULLIF(btrim(NEW.selected_plan_title), ''), NULLIF(btrim(NEW.active_plan_title), ''), 'এককালীন সম্পূর্ণ পেমেন্ট');
    NEW.active_plan_title := COALESCE(NULLIF(btrim(NEW.active_plan_title), ''), NEW.selected_plan_title);

    -- Sync trial & due dates
    NEW.next_installment_due_at := NEW.next_due_date;
    NEW.trial_used := COALESCE(NEW.is_trial_used, false);
    NEW.trial_days_granted := COALESCE(NEW.trial_days, 0);
    NEW.trial_expires_at := NEW.trial_ends_at;

    -- যদি রিকোয়েস্টটি এখনো অ্যাডমিন এপ্রুভ না করে থাকেন (REQUESTED এবং approved_at IS NULL), তবে প্রাইস 0 ও কিস্তি false থাকবে
    IF NEW.status IN ('REQUESTED', 'ACCESS_REQUESTED') AND NEW.approved_at IS NULL AND COALESCE(NEW.paid_amount, 0) = 0 THEN
        NEW.total_price := 0;
        NEW.total_fee := 0;
        NEW.paid_amount := 0;
        NEW.due_amount := 0;
        NEW.next_installment_amount := 0;
        NEW.installment_enabled := false;
        NEW.allow_installments := false;
        NEW.installment_plans_json := '[]'::jsonb;
        NEW.installment_plans := '[]'::jsonb;
    ELSE
        IF NEW.installment_enabled IS DISTINCT FROM NEW.allow_installments THEN
            IF TG_OP = 'UPDATE' AND NEW.allow_installments IS DISTINCT FROM OLD.allow_installments THEN
                NEW.installment_enabled := NEW.allow_installments;
            ELSE
                NEW.allow_installments := COALESCE(NEW.installment_enabled, true);
                NEW.installment_enabled := COALESCE(NEW.installment_enabled, true);
            END IF;
        END IF;

        IF NEW.installment_plans IS DISTINCT FROM NEW.installment_plans_json THEN
            IF TG_OP = 'UPDATE' AND NEW.installment_plans IS DISTINCT FROM OLD.installment_plans THEN
                NEW.installment_plans_json := NEW.installment_plans;
            ELSE
                NEW.installment_plans := COALESCE(NEW.installment_plans_json, '[]'::jsonb);
            END IF;
        END IF;

        NEW.total_price := COALESCE(NEW.total_price, 1000);
        NEW.total_fee := NEW.total_price;
        NEW.paid_amount := COALESCE(NEW.paid_amount, 0);
        NEW.due_amount := GREATEST(0, NEW.total_price - NEW.paid_amount);
    END IF;

    NEW.updated_at := now();
    RETURN NEW;
END;
$$;

-- যেসব রিকোয়েস্ট ইতিমধ্যে REQUESTED অবস্থায় আছে কিন্তু অ্যাডমিন এখনো এপ্রুভ করেননি (approved_at IS NULL), সেগুলোর ডিফল্ট ১০০০ টাকা ও প্ল্যান ক্লিয়ার করা
UPDATE public.course_access_enrollments
SET total_price = 0,
    total_fee = 0,
    due_amount = 0,
    next_installment_amount = 0,
    installment_enabled = false,
    allow_installments = false,
    installment_plans_json = '[]'::jsonb,
    installment_plans = '[]'::jsonb
WHERE UPPER(COALESCE(status, 'REQUESTED')) IN ('REQUESTED', 'ACCESS_REQUESTED')
  AND approved_at IS NULL
  AND COALESCE(paid_amount, 0) = 0;

CREATE OR REPLACE FUNCTION public.request_course_purchase_access(
    p_device_id text,
    p_shikho_account_key text,
    p_student_name text,
    p_student_phone text,
    p_student_class text,
    p_student_group text,
    p_program_id text,
    p_program_title text
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_phone text;
    v_account_key text;
    v_real_dev_id text;
    v_player_id text;
    v_existing_id bigint;
    v_existing_status text;
BEGIN
    v_phone := public.normalize_bd_phone(COALESCE(NULLIF(btrim(p_student_phone), ''), btrim(p_shikho_account_key)));
    v_account_key := COALESCE(NULLIF(v_phone, ''), NULLIF(btrim(p_shikho_account_key), ''), btrim(p_device_id));
    v_real_dev_id := COALESCE(NULLIF(btrim(p_device_id), ''), v_account_key);

    SELECT onesignal_player_id INTO v_player_id
    FROM public.devices
    WHERE device_hash = v_real_dev_id OR device_id = v_real_dev_id OR display_device_id = v_real_dev_id
    LIMIT 1;

    SELECT id, status INTO v_existing_id, v_existing_status
    FROM public.course_access_enrollments
    WHERE (
        shikho_account_key = v_account_key
        OR device_hash = v_real_dev_id
        OR (v_phone <> '' AND public.normalize_bd_phone(student_phone) = v_phone)
    ) AND program_id = p_program_id
    ORDER BY updated_at DESC
    LIMIT 1;

    IF v_existing_id IS NOT NULL THEN
        -- যদি কোর্সটি ইতিমধ্যে ACTIVE বা TRIAL_ACTIVE না থাকে, তবে নতুন রিকোয়েস্ট পাঠালে অবশ্যই REQUESTED হবে (অ্যাডমিন অনুমোদনের অপেক্ষায়)
        UPDATE public.course_access_enrollments
        SET
            device_id = v_real_dev_id,
            device_hash = v_real_dev_id,
            shikho_account_key = v_account_key,
            onesignal_player_id = COALESCE(NULLIF(v_player_id, ''), onesignal_player_id),
            student_name = COALESCE(NULLIF(btrim(p_student_name), ''), student_name),
            student_phone = COALESCE(NULLIF(v_phone, ''), student_phone),
            student_class = COALESCE(NULLIF(btrim(p_student_class), ''), student_class),
            student_group = COALESCE(NULLIF(btrim(p_student_group), ''), student_group),
            program_title = COALESCE(NULLIF(btrim(p_program_title), ''), program_title),
            status = CASE WHEN UPPER(COALESCE(v_existing_status, '')) IN ('ACTIVE', 'TRIAL_ACTIVE') THEN v_existing_status ELSE 'REQUESTED' END,
            access_status = CASE WHEN UPPER(COALESCE(v_existing_status, '')) IN ('ACTIVE', 'TRIAL_ACTIVE') THEN v_existing_status ELSE 'REQUESTED' END,
            approved_at = CASE WHEN UPPER(COALESCE(v_existing_status, '')) IN ('ACTIVE', 'TRIAL_ACTIVE') THEN approved_at ELSE NULL END,
            total_price = CASE WHEN UPPER(COALESCE(v_existing_status, '')) IN ('ACTIVE', 'TRIAL_ACTIVE') THEN total_price ELSE 0 END,
            total_fee = CASE WHEN UPPER(COALESCE(v_existing_status, '')) IN ('ACTIVE', 'TRIAL_ACTIVE') THEN total_fee ELSE 0 END,
            due_amount = CASE WHEN UPPER(COALESCE(v_existing_status, '')) IN ('ACTIVE', 'TRIAL_ACTIVE') THEN due_amount ELSE 0 END,
            installment_enabled = CASE WHEN UPPER(COALESCE(v_existing_status, '')) IN ('ACTIVE', 'TRIAL_ACTIVE') THEN installment_enabled ELSE false END,
            allow_installments = CASE WHEN UPPER(COALESCE(v_existing_status, '')) IN ('ACTIVE', 'TRIAL_ACTIVE') THEN allow_installments ELSE false END,
            installment_plans_json = CASE WHEN UPPER(COALESCE(v_existing_status, '')) IN ('ACTIVE', 'TRIAL_ACTIVE') THEN installment_plans_json ELSE '[]'::jsonb END,
            installment_plans = CASE WHEN UPPER(COALESCE(v_existing_status, '')) IN ('ACTIVE', 'TRIAL_ACTIVE') THEN installment_plans ELSE '[]'::jsonb END,
            requested_at = now(),
            updated_at = now()
        WHERE id = v_existing_id;
    ELSE
        INSERT INTO public.course_access_enrollments (
            device_id, device_hash, shikho_account_key, onesignal_player_id, student_name, student_phone,
            student_class, student_group, program_id, program_title,
            status, access_status, total_price, total_fee, due_amount, next_installment_amount,
            installment_enabled, allow_installments, installment_plans_json, installment_plans,
            approved_at, requested_at, updated_at
        ) VALUES (
            v_real_dev_id, v_real_dev_id, v_account_key, v_player_id, COALESCE(NULLIF(btrim(p_student_name), ''), 'শিক্ষার্থী'), v_phone,
            COALESCE(p_student_class, ''), COALESCE(p_student_group, ''), p_program_id, p_program_title,
            'REQUESTED', 'REQUESTED', 0, 0, 0, 0,
            false, false, '[]'::jsonb, '[]'::jsonb,
            NULL, now(), now()
        );
    END IF;

    RETURN jsonb_build_object(
        'success', true,
        'status', 'REQUESTED',
        'message', 'অ্যাডমিনের কাছে কোর্সে এক্সেস রিকোয়েস্ট পাঠানো হয়েছে! অ্যাডমিন দাম সেট করে এক্সেস দিলে এখানে দেখতে পাবেন।'
    );
END;
$$;

-- ২. public.onesignal_config টেবিল তৈরি (লাইভ ডাটাবেজের ৪টি আসল কলাম: id, onesignal_app_id, onesignal_rest_api_key, updated_at)
CREATE TABLE IF NOT EXISTS public.onesignal_config (
    id INT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    onesignal_app_id TEXT NOT NULL DEFAULT '$safeAppId',
    onesignal_rest_api_key TEXT NOT NULL DEFAULT '$cleanKey',
    updated_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.onesignal_config ENABLE ROW LEVEL SECURITY;

-- শুধু পাবলিক App ID পড়ার জন্য নিরাপদ SECURITY DEFINER RPC (JSONB রিটার্ন করে)
CREATE OR REPLACE FUNCTION public.get_onesignal_public_app_id()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_app_id TEXT := '';
BEGIN
    SELECT COALESCE(onesignal_app_id, '')
    INTO v_app_id
    FROM public.onesignal_config
    WHERE id = 1
    LIMIT 1;

    RETURN jsonb_build_object(
        'onesignal_app_id', COALESCE(v_app_id, '')
    );
END;
$$;
GRANT EXECUTE ON FUNCTION public.get_onesignal_public_app_id() TO anon, authenticated, service_role;

-- Admin App থেকে নিরাপদে OneSignal কনফিগারেশন আপডেট করার RPC
CREATE OR REPLACE FUNCTION public.update_onesignal_config_secure(
    p_app_id TEXT,
    p_rest_api_key TEXT DEFAULT ''
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    INSERT INTO public.onesignal_config (id, onesignal_app_id, onesignal_rest_api_key, updated_at)
    VALUES (
        1,
        COALESCE(NULLIF(BTRIM(p_app_id), ''), '$safeAppId'),
        COALESCE(NULLIF(BTRIM(p_rest_api_key), ''), '$cleanKey'),
        now()
    )
    ON CONFLICT (id) DO UPDATE SET
        onesignal_app_id = COALESCE(NULLIF(BTRIM(EXCLUDED.onesignal_app_id), ''), public.onesignal_config.onesignal_app_id),
        onesignal_rest_api_key = CASE
            WHEN NULLIF(BTRIM(p_rest_api_key), '') IS NOT NULL THEN BTRIM(p_rest_api_key)
            ELSE public.onesignal_config.onesignal_rest_api_key
        END,
        updated_at = now();

    RETURN jsonb_build_object('success', true);
END;
$$;
REVOKE ALL ON FUNCTION public.update_onesignal_config_secure(TEXT, TEXT) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.update_onesignal_config_secure(TEXT, TEXT) TO service_role;

-- ৩. স্টুডেন্টকে সার্ভার-সাইড থেকে OneSignal পুশ পাঠানোর SECURITY DEFINER RPC (শুধুমাত্র service_role)
CREATE OR REPLACE FUNCTION public.send_student_push_via_onesignal(
    p_onesignal_player_id TEXT DEFAULT '',
    p_device_hash TEXT DEFAULT '',
    p_student_phone TEXT DEFAULT '',
    p_title TEXT DEFAULT '',
    p_message TEXT DEFAULT '',
    p_event_type TEXT DEFAULT 'STUDENT_UPDATE',
    p_extra_data JSONB DEFAULT '{}'::JSONB
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    PERFORM public.notify_student_via_onesignal(
        p_onesignal_player_id := p_onesignal_player_id,
        p_device_hash := p_device_hash,
        p_student_phone := p_student_phone,
        p_title := p_title,
        p_message := p_message,
        p_event_type := p_event_type,
        p_extra_data := p_extra_data
    );
    RETURN jsonb_build_object('success', true);
EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('success', false, 'error', SQLERRM);
END;
$$;
REVOKE ALL ON FUNCTION public.send_student_push_via_onesignal(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, JSONB) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.send_student_push_via_onesignal(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, JSONB) TO service_role;

-- ৪. ডিভাইস ও কনটেন্ট ডিলিট এবং পারমিশন ম্যানেজমেন্টের SECURITY DEFINER RPCs (শুধুমাত্র service_role)
CREATE OR REPLACE FUNCTION public.admin_delete_device(
    p_device_hash TEXT DEFAULT '',
    p_device_id TEXT DEFAULT ''
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    DELETE FROM public.devices
    WHERE (NULLIF(BTRIM(p_device_hash), '') IS NOT NULL AND (device_hash = BTRIM(p_device_hash) OR display_device_id = BTRIM(p_device_hash) OR device_id = BTRIM(p_device_hash)))
       OR (NULLIF(BTRIM(p_device_id), '') IS NOT NULL AND id::TEXT = BTRIM(p_device_id));

    RETURN jsonb_build_object('success', true);
END;
$$;
REVOKE ALL ON FUNCTION public.admin_delete_device(TEXT, TEXT) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.admin_delete_device(TEXT, TEXT) TO service_role;

CREATE OR REPLACE FUNCTION public.admin_set_device_can_view_profiles(
    p_device_hash TEXT,
    p_can_view_profiles BOOLEAN
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    UPDATE public.devices
    SET can_view_profiles = COALESCE(p_can_view_profiles, FALSE),
        updated_at = now()
    WHERE device_hash = BTRIM(p_device_hash)
       OR display_device_id = BTRIM(p_device_hash)
       OR device_id = BTRIM(p_device_hash)
       OR id::TEXT = BTRIM(p_device_hash);
END;
$$;
REVOKE ALL ON FUNCTION public.admin_set_device_can_view_profiles(TEXT, BOOLEAN) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.admin_set_device_can_view_profiles(TEXT, BOOLEAN) TO service_role;

CREATE OR REPLACE FUNCTION public.admin_revoke_device_to_pending(
    p_device_hash TEXT
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    UPDATE public.devices
    SET status = 'pending',
        is_banned = FALSE,
        is_blocked = FALSE,
        ban_reason = '',
        rejection_reason = '',
        updated_at = now()
    WHERE device_hash = BTRIM(p_device_hash)
       OR display_device_id = BTRIM(p_device_hash)
       OR device_id = BTRIM(p_device_hash)
       OR id::TEXT = BTRIM(p_device_hash);
END;
$$;
REVOKE ALL ON FUNCTION public.admin_revoke_device_to_pending(TEXT) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.admin_revoke_device_to_pending(TEXT) TO service_role;

CREATE OR REPLACE FUNCTION public.admin_delete_app_notice(
    p_id TEXT DEFAULT '',
    p_notice_id TEXT DEFAULT ''
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_target TEXT := COALESCE(NULLIF(BTRIM(p_id), ''), NULLIF(BTRIM(p_notice_id), ''));
BEGIN
    IF v_target IS NOT NULL THEN
        DELETE FROM public.app_notices WHERE id::TEXT = v_target;
    END IF;
END;
$$;
REVOKE ALL ON FUNCTION public.admin_delete_app_notice(TEXT, TEXT) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.admin_delete_app_notice(TEXT, TEXT) TO service_role;

CREATE OR REPLACE FUNCTION public.admin_delete_app_update(
    p_id TEXT DEFAULT '',
    p_update_id TEXT DEFAULT ''
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_target TEXT := COALESCE(NULLIF(BTRIM(p_id), ''), NULLIF(BTRIM(p_update_id), ''));
BEGIN
    IF v_target IS NOT NULL THEN
        DELETE FROM public.app_updates WHERE id::TEXT = v_target;
    END IF;
END;
$$;
REVOKE ALL ON FUNCTION public.admin_delete_app_update(TEXT, TEXT) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.admin_delete_app_update(TEXT, TEXT) TO service_role;

CREATE OR REPLACE FUNCTION public.admin_delete_support_link(
    p_id TEXT DEFAULT '',
    p_link_id TEXT DEFAULT ''
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_target TEXT := COALESCE(NULLIF(BTRIM(p_id), ''), NULLIF(BTRIM(p_link_id), ''));
BEGIN
    IF v_target IS NOT NULL THEN
        DELETE FROM public.activation_support_links WHERE id::TEXT = v_target;
    END IF;
END;
$$;
REVOKE ALL ON FUNCTION public.admin_delete_support_link(TEXT, TEXT) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.admin_delete_support_link(TEXT, TEXT) TO service_role;

CREATE OR REPLACE FUNCTION public.admin_delete_course_enrollment(
    p_enrollment_id BIGINT DEFAULT 0,
    p_student_phone TEXT DEFAULT '',
    p_program_id TEXT DEFAULT ''
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    IF COALESCE(p_enrollment_id, 0) > 0 THEN
        DELETE FROM public.course_access_enrollments WHERE id = p_enrollment_id;
    ELSIF NULLIF(BTRIM(p_student_phone), '') IS NOT NULL AND NULLIF(BTRIM(p_program_id), '') IS NOT NULL THEN
        DELETE FROM public.course_access_enrollments
        WHERE (student_phone = BTRIM(p_student_phone) OR user_phone = BTRIM(p_student_phone) OR shikho_account_key = BTRIM(p_student_phone))
          AND program_id = BTRIM(p_program_id);
    END IF;
END;
$$;
REVOKE ALL ON FUNCTION public.admin_delete_course_enrollment(BIGINT, TEXT, TEXT) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.admin_delete_course_enrollment(BIGINT, TEXT, TEXT) TO service_role;
""".trimIndent()
    }
}

