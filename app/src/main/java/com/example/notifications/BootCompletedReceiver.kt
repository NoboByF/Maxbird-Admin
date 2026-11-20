package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.preferences.AdminPreferences

/**
 * Ensures OneSignal SDK & 24/7 Live Notification Monitor automatically start when the device boots,
 * strictly gated behind a verified Zero-Trust Supabase Admin session.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            if (!AdminPreferences(context).hasValidAdminSession()) return
            OneSignalPushManager.initializeIfConfigured(context)
            AdminRealtimeMonitorService.startIfEnabled(context)
        }
    }
}
