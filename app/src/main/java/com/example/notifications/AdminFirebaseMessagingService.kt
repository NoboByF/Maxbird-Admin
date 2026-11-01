package com.example.notifications

import com.example.data.preferences.AdminPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.util.Collections

/**
 * Firebase Cloud Messaging Service for MaxBird Admin App.
 *
 * 1. Receives refreshed FCM tokens (`onNewToken`) and automatically syncs with OneSignal REST API
 *    (`POST https://onesignal.com/api/v1/players`) + Supabase `public.admin_push_devices`.
 * 2. Handles incoming OneSignal & FCM push notifications (`onMessageReceived`), parsing
 *    `remoteMessage.data["alert"]`, `remoteMessage.data["title"]`, `remoteMessage.data["custom"]`,
 *    and standard FCM fields to display high-priority notifications with sound even when the app
 *    is in the background or killed.
 */
class AdminFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private val recentNotificationIds: MutableSet<String> =
            Collections.synchronizedSet( LinkedHashSet<String>() )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val cleanToken = token.trim()
        if (cleanToken.isNotEmpty()) {
            val prefs = AdminPreferences(applicationContext)
            prefs.setSavedFcmToken(cleanToken)
            OneSignalPushManager.syncAdminPushRegistration(
                context = applicationContext,
                fcmTokenOverride = cleanToken
            )
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val customJsonStr = data["custom"]?.trim().orEmpty()
        var customObj: JSONObject? = null
        var additionalDataObj: JSONObject? = null
        var oneSignalNotificationId = ""

        if (customJsonStr.startsWith("{")) {
            try {
                customObj = JSONObject(customJsonStr)
                oneSignalNotificationId = customObj.optString("i", "").trim()
                additionalDataObj = customObj.optJSONObject("a")
            } catch (_: Exception) {
            }
        }

        // Deduplicate if OneSignal SDK and FCM service both process the same notification ID
        if (oneSignalNotificationId.isNotEmpty()) {
            synchronized(recentNotificationIds) {
                if (recentNotificationIds.contains(oneSignalNotificationId)) {
                    return
                }
                recentNotificationIds.add(oneSignalNotificationId)
                if (recentNotificationIds.size > 200) {
                    val first = recentNotificationIds.iterator().next()
                    recentNotificationIds.remove(first)
                }
            }
        }

        val title = data["title"]?.takeIf { it.isNotBlank() }
            ?: data["nt"]?.takeIf { it.isNotBlank() }
            ?: additionalDataObj?.optString("title", "")?.takeIf { it.isNotBlank() }
            ?: remoteMessage.notification?.title?.takeIf { it.isNotBlank() }
            ?: "🔔 MaxBird Admin নোটিফিকেশন"

        val body = data["alert"]?.takeIf { it.isNotBlank() }
            ?: data["body"]?.takeIf { it.isNotBlank() }
            ?: data["message"]?.takeIf { it.isNotBlank() }
            ?: additionalDataObj?.optString("message", "")?.takeIf { it.isNotBlank() }
            ?: additionalDataObj?.optString("body", "")?.takeIf { it.isNotBlank() }
            ?: remoteMessage.notification?.body?.takeIf { it.isNotBlank() }
            ?: ""

        if (body.isBlank() && remoteMessage.notification == null && data.isEmpty()) {
            return
        }

        val eventType = additionalDataObj?.optString("type", "")?.takeIf { it.isNotBlank() }
            ?: additionalDataObj?.optString("event_type", "")?.takeIf { it.isNotBlank() }
            ?: data["type"]?.takeIf { it.isNotBlank() }
            ?: data["event_type"]?.takeIf { it.isNotBlank() }
            ?: ""

        val rawTargetScreen = additionalDataObj?.optString("target_screen", "")?.takeIf { it.isNotBlank() }
            ?: data["target_screen"]?.takeIf { it.isNotBlank() }
            ?: ""

        val subTab = additionalDataObj?.optInt("course_sales_tab", -1)?.takeIf { it >= 0 }
            ?: data["course_sales_tab"]?.toIntOrNull()
            ?: -1

        val deviceQuery = additionalDataObj?.optString("display_device_id", "")?.takeIf { it.isNotBlank() }
            ?: additionalDataObj?.optString("device_id", "")?.takeIf { it.isNotBlank() }
            ?: additionalDataObj?.optString("device_hash", "")?.takeIf { it.isNotBlank() }
            ?: data["display_device_id"]?.takeIf { it.isNotBlank() }
            ?: data["device_id"]?.takeIf { it.isNotBlank() }
            ?: data["device_hash"]?.takeIf { it.isNotBlank() }

        val target = when {
            rawTargetScreen.equals("devices", ignoreCase = true) ||
                eventType.equals("ADMIN_DEVICE_ACCESS_REQUEST", ignoreCase = true) ||
                eventType.contains("device", ignoreCase = true) ||
                title.contains("ডিভাইস") -> {
                AdminNotificationHelper.TARGET_DEVICES
            }
            rawTargetScreen.equals("course_payments", ignoreCase = true) ||
                subTab == 1 ||
                eventType.equals("ADMIN_COURSE_PAYMENT_SUBMITTED", ignoreCase = true) ||
                eventType.contains("payment", ignoreCase = true) ||
                eventType.contains("trx", ignoreCase = true) ||
                title.contains("পেমেন্ট") ||
                title.contains("টাকা") -> {
                AdminNotificationHelper.TARGET_COURSE_PAYMENTS
            }
            else -> {
                AdminNotificationHelper.TARGET_COURSE_REQUESTS
            }
        }

        val notificationId = if (oneSignalNotificationId.isNotEmpty()) {
            oneSignalNotificationId.hashCode()
        } else {
            ((System.currentTimeMillis() % 100000).toInt())
        }

        AdminNotificationHelper(applicationContext).showAlertNotification(
            notificationId = notificationId,
            title = title,
            body = body.ifBlank { title },
            target = target,
            deviceQuery = deviceQuery
        )
    }
}
