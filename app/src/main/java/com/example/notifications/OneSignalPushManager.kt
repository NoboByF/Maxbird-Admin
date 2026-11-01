package com.example.notifications

import android.content.Context
import android.content.Intent
import com.example.MainActivity
import com.example.data.preferences.AdminPreferences
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Full OneSignal Real Push Notification Manager for MaxBird Admin App.
 *
 * 1. Initializes the official OneSignal Android SDK v5 (`OneSignal.initWithContext(context, appId)`),
 *    binds external ID `"maxbird_admin"` and tag `"role" = "admin"`, and listens for push clicks
 *    to deep-link into Device Verification, Course Access Requests, or Payment Verification.
 * 2. Dispatches real cloud push notifications via the OneSignal REST API (`https://onesignal.com/api/v1/notifications`)
 *    so notifications arrive via FCM/OneSignal even when the app is completely killed/closed.
 * 3. Generates the ready-to-run Supabase `pg_net` Database Webhook Trigger SQL script pre-populated
 *    with the admin's OneSignal App ID and REST API Key so Supabase automatically pushes OneSignal
 *    alerts on `INSERT`/`UPDATE` of `public.devices`, `public.course_access_enrollments`, and
 *    `public.course_payment_transactions` even when the Admin phone is turned off or app is force-stopped.
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
     * Initializes the OneSignal SDK if a valid OneSignal App ID is configured.
     * Safe to call multiple times (e.g., on app startup and after saving new OneSignal credentials).
     */
    fun initializeIfConfigured(
        context: Context,
        explicitAppId: String? = null
    ): Boolean {
        val appContext = context.applicationContext
        val prefs = AdminPreferences(appContext)
        val appId = explicitAppId?.trim()?.takeIf { it.isNotEmpty() }
            ?: prefs.getOneSignalAppId().trim()
        if (!isValidOneSignalAppId(appId)) {
            return false
        }

        return try {
            OneSignal.Debug.logLevel = LogLevel.WARN
            if (initializedAppId != appId) {
                OneSignal.initWithContext(appContext, appId)
                initializedAppId = appId
            }

            // Tag & login this device as MaxBird Admin so server/webhook pushes reach it
            try {
                OneSignal.login("maxbird_admin")
                OneSignal.User.addTags(
                    mapOf(
                        "role" to "admin",
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
                            val targetScreen = data?.optString("target_screen", "")?.takeIf { it.isNotBlank() }
                                ?: "course_sales"
                            val subTab = data?.optInt("course_sales_tab", 0) ?: 0
                            val deviceQuery = data?.optString("device_id", "")?.takeIf { it.isNotBlank() }

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
     * Returns current OneSignal SDK subscription status summary for the UI.
     */
    fun getSubscriptionStatusSummary(
        context: Context,
        appIdOverride: String? = null
    ): String {
        val prefs = AdminPreferences(context.applicationContext)
        val appId = appIdOverride?.trim()?.takeIf { it.isNotEmpty() }
            ?: prefs.getOneSignalAppId().trim()
        val apiKey = prefs.getOneSignalRestApiKey().trim()
        if (!isValidOneSignalAppId(appId)) {
            return "OneSignal App ID সেট করা হয়নি (সেট করলে অ্যাপ বন্ধ থাকলেও ক্লাউড পুশ আসবে)"
        }
        return try {
            val subId = try {
                OneSignal.User.pushSubscription.id.takeIf { it.isNotBlank() }
            } catch (_: Throwable) {
                null
            }
            val optedIn = try {
                OneSignal.User.pushSubscription.optedIn
            } catch (_: Throwable) {
                false
            }
            buildString {
                append("✅ OneSignal SDK সংযুক্ত (App ID: ${appId.take(8)}...)")
                if (!subId.isNullOrBlank()) {
                    append(" • Sub ID: ${subId.take(10)}...")
                } else if (optedIn) {
                    append(" • Push Opted-In")
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
