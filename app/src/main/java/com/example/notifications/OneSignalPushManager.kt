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
     * Generates the complete Supabase PostgreSQL Trigger + `pg_net` script that automatically pushes
     * real OneSignal notifications whenever a student submits:
     * 1) A Device Verification Request (`public.devices`)
     * 2) A Course Access Request (`public.course_access_enrollments`)
     * 3) A Payment / Installment Verification Request (`public.course_payment_transactions`)
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
        val authPrefix = if (cleanKey.startsWith("os_v2_")) "Key" else "Basic"

        return """
-- ============================================================================
-- MaxBird Admin: 24/7 Real OneSignal Push Notification Triggers (Supabase SQL)
-- এই SQL কোডটি আপনার Supabase Dashboard -> SQL Editor-এ Run করলেই
-- আপনার Admin App সম্পূর্ণ বন্ধ (Force Closed / Killed) থাকলেও সাথে সাথে
-- OneSignal-এর মাধ্যমে ফোনে রিয়েল পুশ নোটিফিকেশন চলে আসবে!
-- ============================================================================

-- ১. pg_net এক্সটেনশন চালু করুন (Supabase থেকে OneSignal API কল করার জন্য)
CREATE EXTENSION IF NOT EXISTS pg_net;

-- ২. OneSignal-এ পুশ পাঠানোর কমন ফাংশন
CREATE OR REPLACE FUNCTION public.fn_send_onesignal_admin_push(
    p_title TEXT,
    p_body TEXT,
    p_target_screen TEXT DEFAULT 'course_sales',
    p_sub_tab INT DEFAULT 0,
    p_device_id TEXT DEFAULT ''
) RETURNS VOID AS $$
DECLARE
    v_app_id TEXT := '$safeAppId';
    v_api_key TEXT := '$authPrefix $cleanKey';
BEGIN
    PERFORM net.http_post(
        url := 'https://onesignal.com/api/v1/notifications',
        headers := jsonb_build_object(
            'Content-Type', 'application/json; charset=utf-8',
            'Authorization', v_api_key
        ),
        body := jsonb_build_object(
            'app_id', v_app_id,
            'included_segments', jsonb_build_array('Total Subscriptions'),
            'priority', 10,
            'android_visibility', 1,
            'headings', jsonb_build_object('en', p_title, 'bn', p_title),
            'contents', jsonb_build_object('en', p_body, 'bn', p_body),
            'data', jsonb_build_object(
                'target_screen', p_target_screen,
                'course_sales_tab', p_sub_tab,
                'device_id', p_device_id
            )
        )
    );
EXCEPTION WHEN OTHERS THEN
    -- Ignore HTTP errors so student database transactions never fail
    NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ৩. নতুন ডিভাইস ভেরিফিকেশন রিকোয়েস্ট ট্রিগার (public.devices)
CREATE OR REPLACE FUNCTION public.trg_fn_onesignal_device_request()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT' AND LOWER(COALESCE(NEW.status, 'pending')) = 'pending') OR
       (TG_OP = 'UPDATE' AND LOWER(COALESCE(OLD.status, '')) <> 'pending' AND LOWER(COALESCE(NEW.status, '')) = 'pending') THEN
        PERFORM public.fn_send_onesignal_admin_push(
            '📱 নতুন ডিভাইস ভেরিফিকেশন রিকোয়েস্ট!',
            COALESCE(NULLIF(NEW.student_note, ''), 'নতুন শিক্ষার্থী') || ' (' || COALESCE(NEW.device_model, 'Phone') || ') • ID: ' || COALESCE(NEW.display_device_id, LEFT(NEW.device_hash, 10)),
            'devices',
            0,
            COALESCE(NEW.display_device_id, NEW.device_hash)
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_onesignal_device_request ON public.devices;
CREATE TRIGGER trg_onesignal_device_request
AFTER INSERT OR UPDATE ON public.devices
FOR EACH ROW EXECUTE FUNCTION public.trg_fn_onesignal_device_request();

-- ৪. নতুন কোর্স এক্সেস রিকোয়েস্ট ট্রিগার (public.course_access_enrollments)
CREATE OR REPLACE FUNCTION public.trg_fn_onesignal_course_request()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT' AND UPPER(COALESCE(NEW.status, '')) IN ('REQUESTED', 'ACCESS_REQUESTED')) OR
       (TG_OP = 'UPDATE' AND UPPER(COALESCE(OLD.status, '')) NOT IN ('REQUESTED', 'ACCESS_REQUESTED') AND UPPER(COALESCE(NEW.status, '')) IN ('REQUESTED', 'ACCESS_REQUESTED')) THEN
        PERFORM public.fn_send_onesignal_admin_push(
            '📚 নতুন কোর্স এক্সেস রিকোয়েস্ট!',
            COALESCE(NULLIF(NEW.student_name, ''), 'শিক্ষার্থী') || ' (' || COALESCE(NEW.student_phone, NEW.shikho_account_key, '') || ') - ' || COALESCE(NEW.program_title, NEW.program_id, 'কোর্স'),
            'course_sales',
            0,
            COALESCE(NEW.display_device_id, NEW.device_id, NEW.student_phone, '')
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_onesignal_course_request ON public.course_access_enrollments;
CREATE TRIGGER trg_onesignal_course_request
AFTER INSERT OR UPDATE ON public.course_access_enrollments
FOR EACH ROW EXECUTE FUNCTION public.trg_fn_onesignal_course_request();

-- ৫. নতুন পেমেন্ট / কিস্তি ভেরিফিকেশন রিকোয়েস্ট ট্রিগার (public.course_payment_transactions)
CREATE OR REPLACE FUNCTION public.trg_fn_onesignal_payment_request()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT' AND UPPER(COALESCE(NEW.status, 'PENDING')) = 'PENDING') THEN
        PERFORM public.fn_send_onesignal_admin_push(
            '💰 নতুন পেমেন্ট ভেরিফিকেশন! (৳' || COALESCE(NEW.amount::TEXT, '0') || ' - ' || COALESCE(NEW.payment_method, 'Payment') || ')',
            COALESCE(NULLIF(NEW.student_name, ''), COALESCE(NEW.student_phone, 'শিক্ষার্থী')) || ' • TrxID: ' || COALESCE(NEW.transaction_id, 'N/A') || ' • প্রেরক: ' || COALESCE(NEW.sender_mobile, 'N/A'),
            'course_sales',
            1,
            COALESCE(NEW.display_device_id, NEW.device_id, NEW.student_phone, '')
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_onesignal_payment_request ON public.course_payment_transactions;
CREATE TRIGGER trg_onesignal_payment_request
AFTER INSERT ON public.course_payment_transactions
FOR EACH ROW EXECUTE FUNCTION public.trg_fn_onesignal_payment_request();
""".trimIndent()
    }
}
