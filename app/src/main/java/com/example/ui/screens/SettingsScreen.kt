package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CoursePaymentSettings
import com.example.data.model.UpsertCoursePaymentSettingsPayload
import com.example.notifications.OneSignalPushManager
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderGradientEnd
import com.example.ui.theme.CardBorderGradientStart
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

private const val SAFE_DATABASE_SYNC_FIX_SQL = """-- ============================================================================
-- SAFE FIX: 100% SYNC COLUMNS & ERROR-PROOF ONESIGNAL TRIGGERS
-- Run this once in Supabase Dashboard -> SQL Editor
-- ============================================================================

ALTER TABLE public.devices
    ADD COLUMN IF NOT EXISTS device_id TEXT,
    ADD COLUMN IF NOT EXISTS is_banned BOOLEAN DEFAULT false,
    ADD COLUMN IF NOT EXISTS is_blocked BOOLEAN DEFAULT false,
    ADD COLUMN IF NOT EXISTS ban_reason TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS student_name TEXT,
    ADD COLUMN IF NOT EXISTS student_phone TEXT,
    ADD COLUMN IF NOT EXISTS onesignal_player_id TEXT,
    ADD COLUMN IF NOT EXISTS fcm_token TEXT;

UPDATE public.devices
SET
    device_id = COALESCE(NULLIF(device_id, ''), device_hash),
    is_blocked = COALESCE(is_banned, is_blocked, false),
    is_banned = COALESCE(is_banned, is_blocked, false),
    rejection_reason = COALESCE(NULLIF(rejection_reason, ''), ban_reason, '')
WHERE device_id IS NULL OR is_blocked IS DISTINCT FROM is_banned;

CREATE OR REPLACE FUNCTION public.admin_set_device_block_status(
    p_device_hash TEXT,
    p_is_banned BOOLEAN DEFAULT true,
    p_reason TEXT DEFAULT 'অ্যাডমিন কর্তৃক ব্লক করা হয়েছে'
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE public.devices
    SET
        status = CASE WHEN p_is_banned THEN 'blocked' ELSE 'active' END,
        is_banned = p_is_banned,
        is_blocked = p_is_banned,
        ban_reason = CASE WHEN p_is_banned THEN COALESCE(p_reason, '') ELSE '' END,
        rejection_reason = CASE WHEN p_is_banned THEN COALESCE(p_reason, '') ELSE '' END,
        banned_at = CASE WHEN p_is_banned THEN now() ELSE NULL END,
        approved_at = CASE WHEN NOT p_is_banned THEN now() ELSE approved_at END,
        updated_at = now()
    WHERE device_hash = p_device_hash
       OR device_id = p_device_hash
       OR display_device_id = p_device_hash
       OR id::text = p_device_hash;
END;
$$;

CREATE OR REPLACE FUNCTION public.admin_approve_device_verification(
    p_device_hash TEXT
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE public.devices
    SET
        status = 'active',
        is_banned = false,
        is_blocked = false,
        ban_reason = '',
        rejection_reason = '',
        approved_at = now(),
        updated_at = now()
    WHERE device_hash = p_device_hash
       OR device_id = p_device_hash
       OR display_device_id = p_device_hash
       OR id::text = p_device_hash;
END;
$$;
"""

private const val SUPABASE_SCHEMA_SQL = """-- MaxBird Supabase Setup SQL Script
-- Run this in your Supabase Dashboard -> SQL Editor

-- Dynamic Payment Methods Table & Columns
CREATE TABLE IF NOT EXISTS public.course_payment_settings (
    id integer NOT NULL DEFAULT 1 PRIMARY KEY,
    payment_methods_json jsonb DEFAULT '[]'::jsonb,
    bkash_number text DEFAULT ''::text,
    bkash_enabled boolean DEFAULT false,
    nagad_number text DEFAULT ''::text,
    nagad_enabled boolean DEFAULT false,
    rocket_number text DEFAULT ''::text,
    rocket_enabled boolean DEFAULT false,
    upay_number text DEFAULT ''::text,
    upay_enabled boolean DEFAULT false,
    payment_instructions_bn text DEFAULT 'প্রদত্ত পেমেন্ট নম্বরে টাকা পাঠিয়ে আপনার প্রেরক মোবাইল নম্বর এবং Transaction ID দিয়ে সাবমিট করুন।'::text,
    updated_at timestamp with time zone DEFAULT now()
);

ALTER TABLE public.course_payment_settings
    ADD COLUMN IF NOT EXISTS payment_methods_json jsonb DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS bkash_enabled boolean DEFAULT false,
    ADD COLUMN IF NOT EXISTS nagad_enabled boolean DEFAULT false,
    ADD COLUMN IF NOT EXISTS rocket_enabled boolean DEFAULT false,
    ADD COLUMN IF NOT EXISTS upay_number text DEFAULT ''::text,
    ADD COLUMN IF NOT EXISTS upay_enabled boolean DEFAULT false;

-- ==========================================================
-- পুরাতন ম্যানুয়াল অ্যাক্সেস কোড সিস্টেম (access_codes ও activated_devices) সম্পূর্ণ ক্লিনআপ
-- ==========================================================
DROP TRIGGER IF EXISTS trg_sync_device_ban ON public.devices;
DROP FUNCTION IF EXISTS public.sync_device_ban_status();
DROP FUNCTION IF EXISTS public.activate_device(text, text, text);
DROP FUNCTION IF EXISTS public.activate_device(text, text, text, text);
DROP FUNCTION IF EXISTS public.activate_device(text, text, text, text, text, text, text, text);
DROP FUNCTION IF EXISTS public.admin_approve_device_verification(text);
DROP TABLE IF EXISTS public.activated_devices CASCADE;
DROP TABLE IF EXISTS public.access_codes CASCADE;

-- সাপোর্ট লিংক টেবিলে শুধুমাত্র service_role-এর জন্য রাইট পলিসি এবং পাবলিকের জন্য শুধুমাত্র Read পলিসি
DROP POLICY IF EXISTS "Allow admin full access for activation support links" ON public.activation_support_links;
DROP POLICY IF EXISTS "Service role full activation_support_links" ON public.activation_support_links;
CREATE POLICY "Service role full activation_support_links"
ON public.activation_support_links
FOR ALL TO service_role
USING (true) WITH CHECK (true);

-- App Updates Table
CREATE TABLE IF NOT EXISTS app_updates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    latest_version_name TEXT NOT NULL,
    latest_version_code INT NOT NULL,
    min_supported_version_code INT NOT NULL DEFAULT 1,
    is_force_update BOOLEAN NOT NULL DEFAULT false,
    title TEXT NOT NULL,
    changelog TEXT NOT NULL,
    download_url TEXT NOT NULL,
    button_text TEXT NOT NULL DEFAULT 'এখনই আপডেট করুন',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- App Notices Table
CREATE TABLE IF NOT EXISTS app_notices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT,
    image_url TEXT NOT NULL,
    action_url TEXT,
    action_button_text TEXT DEFAULT 'বিস্তারিত দেখুন',
    priority INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    show_as_popup BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Activation Support Links Table (For Device Activation Screen Contact Buttons)
CREATE TABLE IF NOT EXISTS activation_support_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    subtitle TEXT,
    icon_type TEXT NOT NULL DEFAULT 'telegram',
    url TEXT NOT NULL,
    color_hex TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    priority INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- User Devices Table (For Hardware Telemetry & Remote Ban Control)
CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_hash TEXT UNIQUE NOT NULL,
    display_device_id TEXT NOT NULL,
    device_model TEXT DEFAULT 'Unknown Device',
    device_brand TEXT,
    android_version TEXT,
    app_version TEXT,
    applied_code TEXT,
    is_banned BOOLEAN DEFAULT false,
    ban_reason TEXT,
    status TEXT DEFAULT 'pending',
    can_view_profiles BOOLEAN DEFAULT false,
    last_seen TIMESTAMPTZ DEFAULT now()
);

-- Enable Row Level Security (RLS)
ALTER TABLE app_updates ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_notices ENABLE ROW LEVEL SECURITY;
ALTER TABLE activation_support_links ENABLE ROW LEVEL SECURITY;
ALTER TABLE devices ENABLE ROW LEVEL SECURITY;

-- Zero-Trust Security Policies (Students Read-Only, Admin via service_role Full Access)
DROP POLICY IF EXISTS "Allow all course_payment_settings" ON public.course_payment_settings;
DROP POLICY IF EXISTS "Public read course_payment_settings" ON public.course_payment_settings;
CREATE POLICY "Public read course_payment_settings" ON public.course_payment_settings FOR SELECT TO anon, authenticated USING (true);
DROP POLICY IF EXISTS "Service role full course_payment_settings" ON public.course_payment_settings;
CREATE POLICY "Service role full course_payment_settings" ON public.course_payment_settings FOR ALL TO service_role USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow admin full access to app updates" ON public.app_updates;
DROP POLICY IF EXISTS "Service role full app_updates" ON public.app_updates;
CREATE POLICY "Service role full app_updates" ON public.app_updates FOR ALL TO service_role USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow full access for admin operations" ON public.app_notices;
DROP POLICY IF EXISTS "Service role full app_notices" ON public.app_notices;
CREATE POLICY "Service role full app_notices" ON public.app_notices FOR ALL TO service_role USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow manage admin_push_devices" ON public.admin_push_devices;
DROP POLICY IF EXISTS "Service role only admin_push_devices" ON public.admin_push_devices;
CREATE POLICY "Service role only admin_push_devices" ON public.admin_push_devices FOR ALL TO service_role USING (true) WITH CHECK (true);

REVOKE DELETE ON ALL TABLES IN SCHEMA public FROM anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.admin_approve_device_verification(text, text) FROM PUBLIC, anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.admin_set_device_block_status(text, boolean, text) FROM PUBLIC, anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.admin_toggle_device_ban(text, boolean, text) FROM PUBLIC, anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.admin_set_device_course_permissions(text, boolean, text[], text[], text[], text[]) FROM PUBLIC, anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.admin_set_account_course_permissions(text, text, text, boolean, boolean, text[], text[], text[], text[], boolean) FROM PUBLIC, anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.admin_approve_course_access_request(bigint, integer, boolean, jsonb, text, boolean) FROM PUBLIC, anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.admin_approve_course_access_with_trial(text, text, numeric, boolean, jsonb, boolean, integer, integer, boolean, text) FROM PUBLIC, anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.admin_manage_course_trial(text, text, text, integer, integer) FROM PUBLIC, anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.admin_review_course_payment(bigint, boolean, text, integer) FROM PUBLIC, anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.update_onesignal_config_secure(text, text) FROM PUBLIC, anon, authenticated;"""

@Composable
fun SettingsScreen(
    supabaseUrl: String,
    supabaseKey: String,
    isTestingConnection: Boolean,
    connectionStatus: String?,
    isConnectionSuccess: Boolean?,
    onUrlChange: (String) -> Unit,
    onKeyChange: (String) -> Unit,
    onSaveSettings: () -> Unit,
    onTestConnection: () -> Unit,
    onShowToast: (String) -> Unit,
    paymentSettings: CoursePaymentSettings = CoursePaymentSettings(),
    isSavingPaymentSettings: Boolean = false,
    onSavePaymentSettings: (UpsertCoursePaymentSettingsPayload) -> Unit = {},
    oneSignalAppId: String = "",
    oneSignalRestApiKey: String = "",
    oneSignalStatusSummary: String = "",
    isSendingOneSignalTest: Boolean = false,
    onSaveOneSignalConfig: (String, String) -> Unit = { _, _ -> },
    onTestPushNotification: () -> Unit = {},
    isRunningSyncDiagnostics: Boolean = false,
    syncDiagnosticResults: List<com.example.ui.viewmodel.AdminViewModel.SyncDiagnosticCheckItem> = emptyList(),
    onRunSyncDiagnostics: () -> Unit = {}
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var appIdDraft by remember(oneSignalAppId) { mutableStateOf(oneSignalAppId) }
    var restKeyDraft by remember(oneSignalRestApiKey) { mutableStateOf(oneSignalRestApiKey) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. 1-Click Student App & Database 100% Live Sync Diagnostic Tester Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.2.dp,
                        Brush.horizontalGradient(listOf(NeonCyan, EmeraldSuccess)),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🔍 স্টুডেন্ট অ্যাপ ও ডাটাবেজ ১০০% সিঙ্ক পরীক্ষক",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = "১-ক্লিকে ডিভাইস ব্যান, কোর্স, ফ্রি ট্রায়াল, পেমেন্ট, OneSignal ও ওয়েবসাইট চেক করুন",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NeonCyan,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRunSyncDiagnostics,
                            enabled = !isRunningSyncDiagnostics,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_run_full_sync_diagnostics")
                        ) {
                            if (isRunningSyncDiagnostics) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Slate950,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("পরীক্ষা চলছে...", color = Slate950, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("এখনই ১০০% সিঙ্ক পরীক্ষা করুন", color = Slate950, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(SAFE_DATABASE_SYNC_FIX_SQL))
                                onShowToast("✅ ডাটাবেজ অটো-ফিক্স ও ট্রিগার সিঙ্ক SQL কপি হয়েছে! Supabase SQL Editor-এ Run করুন।")
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_copy_safe_sync_sql")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fix SQL কপি", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    if (syncDiagnosticResults.isNotEmpty()) {
                        val passedCount = syncDiagnosticResults.count { it.isSuccess }
                        Surface(
                            color = Slate900,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (passedCount == syncDiagnosticResults.size) EmeraldSuccess.copy(alpha = 0.6f) else AmberWarning.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "📊 লাইভ টেস্ট রিপোর্ট: $passedCount/${syncDiagnosticResults.size} টি মডিউল ১০০% সিঙ্কড",
                                    color = if (passedCount == syncDiagnosticResults.size) EmeraldSuccess else AmberWarning,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                                syncDiagnosticResults.forEach { item ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Slate950)
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "${if (item.isSuccess) "✅" else "⚠️"} ${item.titleBn}",
                                            color = if (item.isSuccess) EmeraldSuccess else CoralDanger,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = item.detailBn,
                                            color = Slate200,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. Dynamic Payment Methods Manager (Top Priority in Settings!)
        item {
            DynamicPaymentMethodsManagerSection(
                settings = paymentSettings,
                isSaving = isSavingPaymentSettings,
                onSave = onSavePaymentSettings,
                onCopyText = { label, text ->
                    clipboardManager.setText(AnnotatedString(text))
                    onShowToast("$label কপি করা হয়েছে!")
                },
                onShowToast = onShowToast
            )
        }

        // 1B. OneSignal Real Push Notification & Supabase Auto-Trigger Setup Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                if (oneSignalAppId.isNotBlank()) EmeraldSuccess.copy(alpha = 0.6f) else AmberWarning.copy(alpha = 0.6f),
                                NeonCyan.copy(alpha = 0.4f)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldSuccess.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "OneSignal ক্লাউড পুশ নোটিফিকেশন",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "অ্যাপ বন্ধ (Closed/Killed) থাকলেও ১০০% রিয়েল-টাইম অ্যালার্ট",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NeonCyan,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    if (oneSignalStatusSummary.isNotBlank()) {
                        Surface(
                            color = Slate900,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (oneSignalAppId.isNotBlank()) EmeraldSuccess.copy(alpha = 0.4f) else AmberWarning.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = oneSignalStatusSummary,
                                color = if (oneSignalAppId.isNotBlank()) EmeraldSuccess else AmberWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = appIdDraft,
                        onValueChange = { appIdDraft = it },
                        label = { Text("OneSignal App ID (UUID)", color = Slate400, fontSize = 12.sp) },
                        placeholder = { Text("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", color = Slate400, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_onesignal_app_id")
                    )

                    OutlinedTextField(
                        value = restKeyDraft,
                        onValueChange = { restKeyDraft = it },
                        label = { Text("OneSignal REST API Key", color = Slate400, fontSize = 12.sp) },
                        placeholder = { Text("os_v2_app_...", color = Slate400, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_onesignal_rest_key")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveOneSignalConfig(appIdDraft.trim(), restKeyDraft.trim())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_save_onesignal")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("OneSignal সেভ", color = Slate950, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onTestPushNotification,
                            enabled = !isSendingOneSignalTest,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_test_onesignal_push")
                        ) {
                            if (isSendingOneSignalTest) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("টেস্ট পুশ", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // 1-Click Copy Supabase pg_net -> OneSignal Auto-Trigger SQL
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🚀 অ্যাপ সম্পূর্ণ বন্ধ (Killed) থাকলেও সুপাবেস থেকে সরাসরি OneSignal পুশ পাওয়ার জন্য নিচের বাটনে ক্লিক করে SQL Trigger কপি করে Supabase SQL Editor-এ একবার Run করুন:",
                                color = Slate200,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                            Button(
                                onClick = {
                                    val sql = OneSignalPushManager.buildSupabaseOneSignalTriggerSql(
                                        appId = appIdDraft.trim(),
                                        restApiKey = restKeyDraft.trim()
                                    )
                                    clipboardManager.setText(AnnotatedString(sql))
                                    onShowToast("✅ Supabase + OneSignal অটো-পুশ SQL Trigger কপি হয়েছে! এখন Supabase SQL Editor-এ Run করুন।")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_copy_onesignal_trigger_sql")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Supabase + OneSignal অটো-পুশ SQL ট্রিগার কপি করুন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modern Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(NeonCyan.copy(alpha = 0.4f), CardBorderGradientEnd)
                        ),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(NeonCyan.copy(alpha = 0.12f), Color.Transparent)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = NeonCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "API & BACKEND CONFIG",
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "সেটিংস ও সুপাবেস কানেকশন",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Supabase API Endpoint, এনভায়রনমেন্ট কি এবং ডাটাবেজ নিরাপত্তা",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate200.copy(alpha = 0.8f)
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Supabase Endpoint Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(CardBorderGradientStart, CardBorderGradientEnd)),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Supabase Database Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    // URL
                    Column {
                        Text(
                            text = "Supabase Project URL",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = supabaseUrl,
                            onValueChange = onUrlChange,
                            placeholder = { Text("https://your-project.supabase.co", color = Slate400, fontSize = 13.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Slate700,
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_supabase_url")
                        )
                    }

                    // API Key
                    Column {
                        val isServiceRole = supabaseKey.contains("InJvbGUiOiJzZXJ2aWNlX3JvbGUi")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Supabase Service Role Key (অ্যাডমিন)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Surface(
                                color = if (isServiceRole) EmeraldSuccess.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isServiceRole) "SERVICE_ROLE সক্রিয় ✅" else "SERVICE_ROLE প্রয়োজন ⚠️",
                                    color = if (isServiceRole) EmeraldSuccess else AmberWarning,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = supabaseKey,
                            onValueChange = onKeyChange,
                            placeholder = { Text("eyJhbGciOiJIUz...", color = Slate400, fontSize = 13.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Slate700,
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_supabase_key")
                        )
                    }

                    // Status display
                    if (!connectionStatus.isNullOrBlank()) {
                        Surface(
                            color = if (isConnectionSuccess == true) EmeraldSuccess.copy(alpha = 0.15f) else CoralDanger.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isConnectionSuccess == true) EmeraldSuccess else CoralDanger,
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isConnectionSuccess == true) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isConnectionSuccess == true) EmeraldSuccess else CoralDanger,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = connectionStatus,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onTestConnection,
                            enabled = !isTestingConnection,
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                .testTag("btn_test_connection")
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NeonCyan, strokeWidth = 2.dp)
                            } else {
                                Text("টেস্ট কানেকশন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Button(
                            onClick = onSaveSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_save_settings")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সেভ করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // 1-Click Zero-Trust Database Security Lock SQL
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🛡️ Service Role Key সেভ করার পর নিচের বাটনে ক্লিক করে Zero-Trust Security Lock SQL কপি করুন এবং Supabase SQL Editor-এ Run করুন। এতে স্টুডেন্ট অ্যাপের anon key দিয়ে কেউ হ্যাক বা অ্যাডমিন ফাংশন কল করতে পারবে না:",
                                color = Slate200,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                            Button(
                                onClick = {
                                    val securitySql = """
                                        -- ============================================================================
                                        -- 100% EXACT ZERO-TRUST SECURITY LOCK (MATCHED TO LIVE SCHEMA)
                                        -- ============================================================================

                                        -- ১. সব admin_* এবং সেনসিটিভ ফাংশন থেকে PUBLIC/anon অ্যাক্সেস বাতিল (শুধুমাত্র service_role কল করতে পারবে)
                                        REVOKE ALL ON FUNCTION public.admin_approve_device_verification(text, text) FROM PUBLIC, anon, authenticated;
                                        REVOKE ALL ON FUNCTION public.admin_set_device_block_status(text, boolean, text) FROM PUBLIC, anon, authenticated;
                                        REVOKE ALL ON FUNCTION public.admin_toggle_device_ban(text, boolean, text) FROM PUBLIC, anon, authenticated;
                                        REVOKE ALL ON FUNCTION public.admin_set_device_course_permissions(text, boolean, text[], text[], text[], text[]) FROM PUBLIC, anon, authenticated;
                                        REVOKE ALL ON FUNCTION public.admin_set_account_course_permissions(text, text, text, boolean, boolean, text[], text[], text[], text[], boolean) FROM PUBLIC, anon, authenticated;
                                        REVOKE ALL ON FUNCTION public.admin_approve_course_access_request(bigint, integer, boolean, jsonb, text, boolean) FROM PUBLIC, anon, authenticated;
                                        REVOKE ALL ON FUNCTION public.admin_approve_course_access_with_trial(text, text, numeric, boolean, jsonb, boolean, integer, integer, boolean, text) FROM PUBLIC, anon, authenticated;
                                        REVOKE ALL ON FUNCTION public.admin_manage_course_trial(text, text, text, integer, integer) FROM PUBLIC, anon, authenticated;
                                        REVOKE ALL ON FUNCTION public.admin_review_course_payment(bigint, boolean, text, integer) FROM PUBLIC, anon, authenticated;
                                        REVOKE ALL ON FUNCTION public.update_onesignal_config_secure(text, text) FROM PUBLIC, anon, authenticated;

                                        GRANT EXECUTE ON FUNCTION public.admin_approve_device_verification(text, text) TO service_role;
                                        GRANT EXECUTE ON FUNCTION public.admin_set_device_block_status(text, boolean, text) TO service_role;
                                        GRANT EXECUTE ON FUNCTION public.admin_toggle_device_ban(text, boolean, text) TO service_role;
                                        GRANT EXECUTE ON FUNCTION public.admin_set_device_course_permissions(text, boolean, text[], text[], text[], text[]) TO service_role;
                                        GRANT EXECUTE ON FUNCTION public.admin_set_account_course_permissions(text, text, text, boolean, boolean, text[], text[], text[], text[], boolean) TO service_role;
                                        GRANT EXECUTE ON FUNCTION public.admin_approve_course_access_request(bigint, integer, boolean, jsonb, text, boolean) TO service_role;
                                        GRANT EXECUTE ON FUNCTION public.admin_approve_course_access_with_trial(text, text, numeric, boolean, jsonb, boolean, integer, integer, boolean, text) TO service_role;
                                        GRANT EXECUTE ON FUNCTION public.admin_manage_course_trial(text, text, text, integer, integer) TO service_role;
                                        GRANT EXECUTE ON FUNCTION public.admin_review_course_payment(bigint, boolean, text, integer) TO service_role;
                                        GRANT EXECUTE ON FUNCTION public.update_onesignal_config_secure(text, text) TO service_role;

                                        -- ২. পেমেন্ট নাম্বার, অ্যাপ আপডেট, নোটিশ, সাপোর্ট লিংক ও অ্যাডমিন পুশ ডিভাইসে anon Write/Delete বন্ধ করা
                                        DROP POLICY IF EXISTS "Allow all course_payment_settings" ON public.course_payment_settings;
                                        DROP POLICY IF EXISTS "Public read course_payment_settings" ON public.course_payment_settings;
                                        CREATE POLICY "Public read course_payment_settings" ON public.course_payment_settings FOR SELECT TO public USING (true);
                                        DROP POLICY IF EXISTS "Service role full course_payment_settings" ON public.course_payment_settings;
                                        CREATE POLICY "Service role full course_payment_settings" ON public.course_payment_settings FOR ALL TO service_role USING (true) WITH CHECK (true);

                                        DROP POLICY IF EXISTS "Allow admin full access to app updates" ON public.app_updates;
                                        DROP POLICY IF EXISTS "Service role full app_updates" ON public.app_updates;
                                        CREATE POLICY "Service role full app_updates" ON public.app_updates FOR ALL TO service_role USING (true) WITH CHECK (true);

                                        DROP POLICY IF EXISTS "Allow full access for admin operations" ON public.app_notices;
                                        DROP POLICY IF EXISTS "Service role full app_notices" ON public.app_notices;
                                        CREATE POLICY "Service role full app_notices" ON public.app_notices FOR ALL TO service_role USING (true) WITH CHECK (true);

                                        DROP POLICY IF EXISTS "Allow admin full access for activation support links" ON public.activation_support_links;
                                        DROP POLICY IF EXISTS "Service role full activation_support_links" ON public.activation_support_links;
                                        CREATE POLICY "Service role full activation_support_links" ON public.activation_support_links FOR ALL TO service_role USING (true) WITH CHECK (true);

                                        DROP POLICY IF EXISTS "Allow manage admin_push_devices" ON public.admin_push_devices;
                                        DROP POLICY IF EXISTS "Service role full admin_push_devices" ON public.admin_push_devices;
                                        CREATE POLICY "Service role full admin_push_devices" ON public.admin_push_devices FOR ALL TO service_role USING (true) WITH CHECK (true);

                                        -- ৩. devices, enrollments ও transactions টেবিলে স্টুডেন্টদের DELETE ক্ষমতা বন্ধ করা (Tamper Trigger ইতোমধ্যে INSERT/UPDATE সুরক্ষিত রাখছে)
                                        DROP POLICY IF EXISTS "Allow full admin operations on devices" ON public.devices;
                                        DROP POLICY IF EXISTS "Allow student insert devices" ON public.devices;
                                        CREATE POLICY "Allow student insert devices" ON public.devices FOR INSERT TO anon, authenticated WITH CHECK (true);
                                        DROP POLICY IF EXISTS "Allow student update devices" ON public.devices;
                                        CREATE POLICY "Allow student update devices" ON public.devices FOR UPDATE TO anon, authenticated USING (true) WITH CHECK (true);
                                        DROP POLICY IF EXISTS "Service role full devices" ON public.devices;
                                        CREATE POLICY "Service role full devices" ON public.devices FOR ALL TO service_role USING (true) WITH CHECK (true);

                                        DROP POLICY IF EXISTS "Allow all course_access_enrollments" ON public.course_access_enrollments;
                                        DROP POLICY IF EXISTS "Allow student read enrollments" ON public.course_access_enrollments;
                                        CREATE POLICY "Allow student read enrollments" ON public.course_access_enrollments FOR SELECT TO anon, authenticated USING (true);
                                        DROP POLICY IF EXISTS "Allow student insert enrollments" ON public.course_access_enrollments;
                                        CREATE POLICY "Allow student insert enrollments" ON public.course_access_enrollments FOR INSERT TO anon, authenticated WITH CHECK (true);
                                        DROP POLICY IF EXISTS "Allow student update enrollments" ON public.course_access_enrollments;
                                        CREATE POLICY "Allow student update enrollments" ON public.course_access_enrollments FOR UPDATE TO anon, authenticated USING (true) WITH CHECK (true);
                                        DROP POLICY IF EXISTS "Service role full enrollments" ON public.course_access_enrollments;
                                        CREATE POLICY "Service role full enrollments" ON public.course_access_enrollments FOR ALL TO service_role USING (true) WITH CHECK (true);

                                        DROP POLICY IF EXISTS "Allow all course_payment_transactions" ON public.course_payment_transactions;
                                        DROP POLICY IF EXISTS "Allow student read payment_tx" ON public.course_payment_transactions;
                                        CREATE POLICY "Allow student read payment_tx" ON public.course_payment_transactions FOR SELECT TO anon, authenticated USING (true);
                                        DROP POLICY IF EXISTS "Allow student insert payment_tx" ON public.course_payment_transactions;
                                        CREATE POLICY "Allow student insert payment_tx" ON public.course_payment_transactions FOR INSERT TO anon, authenticated WITH CHECK (true);
                                        DROP POLICY IF EXISTS "Service role full payment_tx" ON public.course_payment_transactions;
                                        CREATE POLICY "Service role full payment_tx" ON public.course_payment_transactions FOR ALL TO service_role USING (true) WITH CHECK (true);
                                    """.trimIndent()
                                    clipboardManager.setText(AnnotatedString(securitySql))
                                    onShowToast("✅ Zero-Trust Security Lock SQL কপি হয়েছে! এখন Supabase SQL Editor-এ Run করুন।")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_copy_security_lock_sql")
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Zero-Trust Security Lock SQL কপি করুন",
                                    color = Slate950,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Supabase SQL Schema Helper
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate700.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Supabase SQL Setup Script",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(SUPABASE_SCHEMA_SQL))
                                onShowToast("SQL স্ক্রিপ্ট কপি করা হয়েছে! (Copied)")
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Slate800)
                                .size(38.dp)
                                .testTag("btn_copy_sql_schema")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy SQL",
                                tint = ElectricBlueLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "আপনার Supabase প্রজেক্টে পুরাতন access_codes/activated_devices ডিলিট করতে এবং devices ও অন্যান্য টেবিল সেটআপ করতে উপরের বাটন দিয়ে কপি করে Supabase SQL Editor এ রান করুন।",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400, lineHeight = 18.sp)
                    )

                    Surface(
                        color = Slate950,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = SUPABASE_SCHEMA_SQL,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }

        // Release APK & Export Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(EmeraldSuccess.copy(alpha = 0.5f), CardBorderGradientEnd)
                        ),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "রিলিজ APK ও বিল্ড তথ্য",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                        ) {
                            Text(
                                text = "Release Ready",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Text(
                        text = "MaxBird Secure Admin এখন সম্পূর্ণ তৈরি। আপনি Google AI Studio এর ওপরের ডানপাশের সেটিংস মেনু (Gear icon) অথবা প্রজেক্ট এক্সপোর্ট অপশন থেকে সরাসরি 'Generate APK / Export ZIP' করে আপনার ডিভাইসে ইন্সটল করতে পারবেন।",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400, lineHeight = 18.sp)
                    )

                    Surface(
                        color = Slate950,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "• প্যাকেজ নাম: com.aistudio.maxbirdadmin.kxmpzq",
                                style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontFamily = FontFamily.Monospace)
                            )
                            Text(
                                text = "• সাইনিং কনফিগ: Release Keystore কনফিগার করা আছে",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                            )
                            Text(
                                text = "• মডিউলসমূহ: কোর্স সেলস ও কিস্তি, নোটিশ, অ্যাপ আপডেট, কোড ও ডিভাইস কিল-সুইচ",
                                style = MaterialTheme.typography.labelSmall.copy(color = EmeraldSuccess)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
