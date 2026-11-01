package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.CourseAccessEnrollment
import com.example.data.model.CoursePaymentTransaction
import com.example.data.model.UserDevice

/**
 * Manages real-time Android system notifications for the MaxBird Admin App:
 * - New Device Verification Requests (`public.devices` with status = 'pending')
 * - New Course Access Requests (`public.course_access_enrollments` with status = 'ACCESS_REQUESTED' / 'REQUESTED')
 * - New Course Payment / Installment Submissions (`public.course_payment_transactions` with status = 'PENDING')
 *
 * Tracks seen IDs in SharedPreferences so the admin gets instant notifications for every new request/payment
 * without duplicate alerts on app restart.
 */
class AdminNotificationHelper(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "maxbird_admin_notifications_prefs"
        private const val KEY_MONITOR_ENABLED = "live_monitor_enabled"
        private const val KEY_INITIAL_SNAPSHOT_DONE = "initial_snapshot_done"
        private const val KEY_SEEN_PENDING_DEVICES = "seen_pending_devices"
        private const val KEY_SEEN_COURSE_REQUESTS = "seen_course_requests"
        private const val KEY_SEEN_PENDING_PAYMENTS = "seen_pending_payments"

        const val CHANNEL_ID_ALERTS = "maxbird_admin_high_alerts"
        const val CHANNEL_ID_MONITOR = "maxbird_admin_live_monitor"

        const val EXTRA_NAVIGATE_SCREEN = "extra_navigate_screen"
        const val EXTRA_COURSE_SALES_TAB = "extra_course_sales_tab"
        const val EXTRA_DEVICE_SEARCH_QUERY = "extra_device_search_query"

        const val TARGET_DEVICES = "devices"
        const val TARGET_COURSE_REQUESTS = "course_requests"
        const val TARGET_COURSE_PAYMENTS = "course_payments"
    }

    init {
        ensureNotificationChannels()
    }

    fun ensureNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "নতুন রিকোয়েস্ট ও পেমেন্ট অ্যালার্ট (Admin Alerts)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "নতুন ডিভাইস ভেরিফিকেশন, কোর্স রিকোয়েস্ট এবং পেমেন্ট আসলে তাৎক্ষণিক সাউন্ড ও পপ-আপ নোটিফিকেশন"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 150, 350)
                setSound(defaultSoundUri, audioAttrs)
            }

            val monitorChannel = NotificationChannel(
                CHANNEL_ID_MONITOR,
                "লাইভ ব্যাকগ্রাউন্ড মনিটর (24/7 Live Monitor)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "অ্যাপ মিনিমাইজ বা স্ক্রিন লক থাকলেও নতুন রিকোয়েস্ট ও পেমেন্ট চেক করার সার্ভিস"
                setShowBadge(false)
            }

            manager.createNotificationChannel(alertsChannel)
            manager.createNotificationChannel(monitorChannel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isLiveMonitorEnabled(): Boolean {
        return prefs.getBoolean(KEY_MONITOR_ENABLED, true)
    }

    fun setLiveMonitorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MONITOR_ENABLED, enabled).apply()
    }

    /**
     * Evaluates the latest lists of devices, course enrollments, and payment transactions.
     * Fires high-priority Android notifications for any newly arrived pending items!
     */
    fun checkAndNotifyNewEvents(
        devices: List<UserDevice>,
        enrollments: List<CourseAccessEnrollment>,
        transactions: List<CoursePaymentTransaction>
    ): Int {
        val pendingDevices = devices.filter { it.isPendingDevice }
        val requestedCourses = enrollments.filter { it.isAccessRequestedStatus }
        val pendingPayments = transactions.filter { it.status.equals("PENDING", ignoreCase = true) }

        val currentDeviceKeys = pendingDevices.map { it.effectiveKey }.filter { it.isNotBlank() }.toSet()
        val currentCourseKeys = requestedCourses.map { "${it.id}_${it.effectivePhone}_${it.programId}" }.toSet()
        val currentPaymentKeys = pendingPayments.map { "${it.id}_${it.transactionId.orEmpty()}" }.toSet()

        val initialDone = prefs.getBoolean(KEY_INITIAL_SNAPSHOT_DONE, false)
        val seenDevices = prefs.getStringSet(KEY_SEEN_PENDING_DEVICES, emptySet()).orEmpty()
        val seenCourses = prefs.getStringSet(KEY_SEEN_COURSE_REQUESTS, emptySet()).orEmpty()
        val seenPayments = prefs.getStringSet(KEY_SEEN_PENDING_PAYMENTS, emptySet()).orEmpty()

        var notificationsSent = 0

        if (!initialDone) {
            // First time ever initializing: save current baseline and if there are pending items right now,
            // send a summary notification so the admin immediately knows what's waiting!
            prefs.edit()
                .putBoolean(KEY_INITIAL_SNAPSHOT_DONE, true)
                .putStringSet(KEY_SEEN_PENDING_DEVICES, currentDeviceKeys)
                .putStringSet(KEY_SEEN_COURSE_REQUESTS, currentCourseKeys)
                .putStringSet(KEY_SEEN_PENDING_PAYMENTS, currentPaymentKeys)
                .apply()

            val totalWaiting = pendingDevices.size + requestedCourses.size + pendingPayments.size
            if (totalWaiting > 0) {
                val summaryParts = mutableListOf<String>()
                if (pendingDevices.isNotEmpty()) summaryParts.add("${pendingDevices.size}টি ডিভাইস রিকোয়েস্ট")
                if (requestedCourses.isNotEmpty()) summaryParts.add("${requestedCourses.size}টি কোর্স রিকোয়েস্ট")
                if (pendingPayments.isNotEmpty()) summaryParts.add("${pendingPayments.size}টি পেমেন্ট ভেরিফিকেশন")

                val target = when {
                    pendingPayments.isNotEmpty() -> TARGET_COURSE_PAYMENTS
                    requestedCourses.isNotEmpty() -> TARGET_COURSE_REQUESTS
                    else -> TARGET_DEVICES
                }
                showAlertNotification(
                    notificationId = 9001,
                    title = "🔔 ${totalWaiting}টি পেন্ডিং রিকোয়েস্ট আপনার অনুমোদনের অপেক্ষায়!",
                    body = summaryParts.joinToString(" • ") + " চেক করতে ট্যাপ করুন।",
                    target = target
                )
                notificationsSent++
            }
            return notificationsSent
        }

        // 1. Check for newly arrived Pending Device Verification requests
        for (device in pendingDevices) {
            val key = device.effectiveKey
            if (key.isNotBlank() && key !in seenDevices) {
                val studentLabel = device.studentNote?.trim()?.takeIf { it.isNotEmpty() } ?: "নতুন শিক্ষার্থী"
                val dispId = device.displayDeviceId.ifBlank { key.take(12) }
                showAlertNotification(
                    notificationId = ("dev_$key").hashCode(),
                    title = "📱 নতুন ডিভাইস ভেরিফিকেশন রিকোয়েস্ট!",
                    body = "$studentLabel (${device.fullPhoneModel}) • ডিভাইস আইডি: $dispId ভেরিফিকেশন চেয়েছে।",
                    target = TARGET_DEVICES,
                    deviceQuery = dispId
                )
                notificationsSent++
            }
        }

        // 2. Check for newly arrived Course Access Requests
        for (enrollment in requestedCourses) {
            val key = "${enrollment.id}_${enrollment.effectivePhone}_${enrollment.programId}"
            if (key !in seenCourses) {
                val studentLabel = enrollment.studentName?.takeIf { it.isNotBlank() }
                    ?: enrollment.effectivePhone.ifBlank { "শিক্ষার্থী" }
                val courseTitle = enrollment.programTitle?.takeIf { it.isNotBlank() } ?: "কোর্স"
                val resolvedDev = com.example.util.StudentDeviceMatcher.resolveForEnrollment(
                    enrollment = enrollment,
                    devices = devices,
                    allEnrollments = enrollments,
                    allTransactions = transactions
                )
                val devBadge = resolvedDev.displayDeviceIdText.takeIf { it.isNotBlank() } ?: "N/A"
                showAlertNotification(
                    notificationId = ("crs_$key").hashCode(),
                    title = "📚 নতুন কোর্স এক্সেস রিকোয়েস্ট!",
                    body = "$studentLabel (${enrollment.effectivePhone}) • ডিভাইস: $devBadge • '$courseTitle' কোর্সে এক্সেস চেয়েছে।",
                    target = TARGET_COURSE_REQUESTS
                )
                notificationsSent++
            }
        }

        // 3. Check for newly arrived Pending Course Payment Transactions
        for (tx in pendingPayments) {
            val key = "${tx.id}_${tx.transactionId.orEmpty()}"
            if (key !in seenPayments) {
                val studentLabel = tx.studentName?.takeIf { it.isNotBlank() }
                    ?: tx.studentPhone?.takeIf { it.isNotBlank() }
                    ?: tx.senderMobile?.takeIf { it.isNotBlank() }
                    ?: "শিক্ষার্থী"
                val method = tx.paymentMethod ?: "পেমেন্ট"
                val trxId = tx.transactionId ?: "N/A"
                val courseTitle = tx.programTitle?.takeIf { it.isNotBlank() } ?: "কোর্স"
                val resolvedDev = com.example.util.StudentDeviceMatcher.resolveForTransaction(
                    transaction = tx,
                    devices = devices,
                    allEnrollments = enrollments,
                    allTransactions = transactions
                )
                val devBadge = resolvedDev.displayDeviceIdText.takeIf { it.isNotBlank() } ?: "N/A"
                showAlertNotification(
                    notificationId = ("pay_$key").hashCode(),
                    title = "💰 নতুন টাকা জমা হয়েছে! (৳${tx.amount} - $method)",
                    body = "$studentLabel (${tx.effectiveStudentPhone}) • ডিভাইস: $devBadge • '$courseTitle'-এর জন্য ৳${tx.amount} পাঠিয়েছে (TrxID: $trxId)।",
                    target = TARGET_COURSE_PAYMENTS
                )
                notificationsSent++
            }
        }

        // Update seen sets (keep union with currently pending so resolved ones can re-trigger if re-requested,
        // and also retain up to 500 historical IDs so transient network glitches don't re-notify)
        val updatedSeenDevices = (seenDevices + currentDeviceKeys).toList().takeLast(500).toSet()
        val updatedSeenCourses = (seenCourses + currentCourseKeys).toList().takeLast(500).toSet()
        val updatedSeenPayments = (seenPayments + currentPaymentKeys).toList().takeLast(500).toSet()

        prefs.edit()
            .putStringSet(KEY_SEEN_PENDING_DEVICES, updatedSeenDevices)
            .putStringSet(KEY_SEEN_COURSE_REQUESTS, updatedSeenCourses)
            .putStringSet(KEY_SEEN_PENDING_PAYMENTS, updatedSeenPayments)
            .apply()

        return notificationsSent
    }

    /**
     * Triggers an immediate test notification so the admin can verify sound, banner, and tap navigation.
     */
    fun sendTestNotification() {
        showAlertNotification(
            notificationId = (System.currentTimeMillis() % 100000).toInt(),
            title = "✅ MaxBird Admin পুশ নোটিফিকেশন সক্রিয়!",
            body = "নতুন ডিভাইস রিকোয়েস্ট, কোর্স এক্সেস রিকোয়েস্ট বা কিস্তির টাকা পাঠালে সাথে সাথে ডিভাইস আইডিসহ নোটিফিকেশন আসবে।",
            target = TARGET_COURSE_PAYMENTS
        )
    }

    fun showAlertNotification(
        notificationId: Int,
        title: String,
        body: String,
        target: String,
        deviceQuery: String? = null
    ) {
        if (!hasNotificationPermission()) return
        ensureNotificationChannels()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!deviceQuery.isNullOrBlank()) {
                putExtra(EXTRA_DEVICE_SEARCH_QUERY, deviceQuery)
            }
            when (target) {
                TARGET_DEVICES -> {
                    putExtra(EXTRA_NAVIGATE_SCREEN, "devices")
                }
                TARGET_COURSE_REQUESTS -> {
                    putExtra(EXTRA_NAVIGATE_SCREEN, "course_sales")
                    putExtra(EXTRA_COURSE_SALES_TAB, 0)
                }
                TARGET_COURSE_PAYMENTS -> {
                    putExtra(EXTRA_NAVIGATE_SCREEN, "course_sales")
                    putExtra(EXTRA_COURSE_SALES_TAB, 1)
                }
            }
        }

        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            pendingIntentFlags
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 350, 150, 350))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Ignore if permission was revoked
        }
    }
}
