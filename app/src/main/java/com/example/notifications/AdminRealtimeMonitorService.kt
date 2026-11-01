package com.example.notifications

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.api.SupabaseClient
import com.example.data.preferences.AdminPreferences
import com.example.data.repository.MaxBirdRepository
import com.example.data.repository.SupabaseMaxBirdRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 24/7 Foreground & Background Real-time Monitor Service for MaxBird Admin.
 * Automatically checks Supabase every 15 seconds for:
 * 1. New Student Device Verification Requests (`public.devices` status = 'pending')
 * 2. New Course Access Requests (`public.course_access_enrollments` status = 'ACCESS_REQUESTED' / 'REQUESTED')
 * 3. New Course Payment / Installment Submissions (`public.course_payment_transactions` status = 'PENDING')
 *
 * Fires instant high-priority Android notifications with sound and vibration so the Admin
 * never has to manually open the app to check if someone sent a request or payment.
 */
class AdminRealtimeMonitorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitorJob: Job? = null

    private lateinit var adminPreferences: AdminPreferences
    private lateinit var repository: MaxBirdRepository
    private lateinit var notificationHelper: AdminNotificationHelper

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 8800
        private const val POLL_INTERVAL_MS = 15_000L // 15 seconds

        fun startIfEnabled(context: Context) {
            val helper = AdminNotificationHelper(context)
            if (!helper.isLiveMonitorEnabled() || !helper.hasNotificationPermission()) return
            try {
                val intent = Intent(context, AdminRealtimeMonitorService::class.java)
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Exception) {
                // Fallback if started from background on strict OEMs
                try {
                    context.startService(Intent(context, AdminRealtimeMonitorService::class.java))
                } catch (_: Exception) {}
            }
        }

        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, AdminRealtimeMonitorService::class.java))
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        adminPreferences = AdminPreferences(applicationContext)
        val supabaseClient = SupabaseClient(adminPreferences)
        repository = SupabaseMaxBirdRepository(supabaseClient, adminPreferences)
        notificationHelper = AdminNotificationHelper(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!notificationHelper.isLiveMonitorEnabled() || !notificationHelper.hasNotificationPermission()) {
            stopSelf()
            return START_NOT_STICKY
        }

        notificationHelper.ensureNotificationChannels()
        startForegroundSafely("লাইভ মনিটরিং চালু আছে • নতুন রিকোয়েস্ট ও পেমেন্ট অটো-চেক হচ্ছে")

        if (monitorJob?.isActive != true) {
            monitorJob = serviceScope.launch {
                while (isActive && notificationHelper.isLiveMonitorEnabled()) {
                    try {
                        val devices = repository.getUserDevices().getOrNull().orEmpty()
                        val enrollments = repository.getCourseAccessEnrollments().getOrNull().orEmpty()
                        val transactions = repository.getCoursePaymentTransactions().getOrNull().orEmpty()

                        notificationHelper.checkAndNotifyNewEvents(
                            devices = devices,
                            enrollments = enrollments,
                            transactions = transactions
                        )

                        val pendingDevs = devices.count { it.isPendingDevice }
                        val pendingCourses = enrollments.count { it.isAccessRequestedStatus }
                        val pendingPays = transactions.count { it.status.equals("PENDING", ignoreCase = true) }
                        val totalPending = pendingDevs + pendingCourses + pendingPays

                        val statusText = if (totalPending > 0) {
                            "অপেক্ষমান: ${pendingDevs}টি ডিভাইস • ${pendingCourses}টি কোর্স • ${pendingPays}টি পেমেন্ট"
                        } else {
                            "লাইভ মনিটর সক্রিয় • নতুন রিকোয়েস্ট বা টাকা আসলে সাথে সাথে জানাবো"
                        }
                        updateForegroundStatus(statusText)
                    } catch (_: Throwable) {
                        // Ignore transient network errors and retry on next cycle
                    }
                    delay(POLL_INTERVAL_MS)
                }
            }
        }

        return START_STICKY
    }

    private fun startForegroundSafely(statusText: String) {
        val notification = buildMonitorNotification(statusText)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    FOREGROUND_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(FOREGROUND_NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {
            // Ignore if foreground start is restricted
        }
    }

    private fun updateForegroundStatus(statusText: String) {
        if (!notificationHelper.hasNotificationPermission()) return
        try {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.notify(FOREGROUND_NOTIFICATION_ID, buildMonitorNotification(statusText))
        } catch (_: Exception) {}
    }

    private fun buildMonitorNotification(statusText: String) =
        NotificationCompat.Builder(this, AdminNotificationHelper.CHANNEL_ID_MONITOR)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MaxBird Admin • 24/7 লাইভ নোটিফিকেশন গার্ড")
            .setContentText(statusText)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                )
            )
            .build()

    override fun onDestroy() {
        monitorJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
