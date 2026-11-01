package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Ensures OneSignal SDK & 24/7 Live Notification Monitor automatically start when the device boots.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            OneSignalPushManager.initializeIfConfigured(context)
            AdminRealtimeMonitorService.startIfEnabled(context)
        }
    }
}
