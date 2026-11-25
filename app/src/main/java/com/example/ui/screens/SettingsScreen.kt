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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.example.data.preferences.AdminPreferences
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
SET search_path = public
AS $$
BEGIN
    IF NOT public.is_admin_user() THEN
        RAISE EXCEPTION 'Unauthorized: admin access required';
    END IF;

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
REVOKE ALL ON FUNCTION public.admin_set_device_block_status(TEXT, BOOLEAN, TEXT) FROM PUBLIC, anon;
GRANT EXECUTE ON FUNCTION public.admin_set_device_block_status(TEXT, BOOLEAN, TEXT) TO authenticated;

CREATE OR REPLACE FUNCTION public.admin_approve_device_verification(
    p_device_hash TEXT,
    p_student_note TEXT DEFAULT ''
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    IF NOT public.is_admin_user() THEN
        RAISE EXCEPTION 'Unauthorized: admin access required';
    END IF;

    UPDATE public.devices
    SET
        status = 'active',
        is_banned = false,
        is_blocked = false,
        ban_reason = '',
        rejection_reason = '',
        student_note = COALESCE(NULLIF(BTRIM(p_student_note), ''), student_note),
        approved_at = now(),
        updated_at = now()
    WHERE device_hash = p_device_hash
       OR device_id = p_device_hash
       OR display_device_id = p_device_hash
       OR id::text = p_device_hash;
END;
$$;
REVOKE ALL ON FUNCTION public.admin_approve_device_verification(TEXT, TEXT) FROM PUBLIC, anon;
GRANT EXECUTE ON FUNCTION public.admin_approve_device_verification(TEXT, TEXT) TO authenticated;
"""

private const val SUPABASE_SCHEMA_SQL = """-- MaxBird Live Zero-Trust Authenticated RLS Reference SQL (16 Live Tables)
-- Admin App authenticates via Supabase Email/Password JWT (`authenticated` + `public.is_admin_user() = true`).
-- Note: `access_codes` and `activated_devices` are permanently removed from the live database.

-- 1. Admin Auth & Security Config Tables
CREATE TABLE IF NOT EXISTS public.admin_users (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    role TEXT DEFAULT 'admin',
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.app_security_config (
    id INT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    allowed_release_signatures TEXT[] NOT NULL DEFAULT '{}'::TEXT[],
    max_admin_push_per_minute INTEGER NOT NULL DEFAULT 20,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Admin Verification RPCs (`authenticated` role -> BOOLEAN)
CREATE OR REPLACE FUNCTION public.is_admin_user()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.admin_users WHERE user_id = auth.uid()
    );
$$;

GRANT EXECUTE ON FUNCTION public.is_admin_user() TO authenticated;
GRANT EXECUTE ON FUNCTION public.is_admin() TO authenticated;
GRANT EXECUTE ON FUNCTION public.check_is_admin() TO authenticated;

-- 3. Authenticated Admin RLS Policy Pattern (All 16 Live Tables)
ALTER TABLE public.admin_users ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Admin self read admin_users" ON public.admin_users;
CREATE POLICY "Admin self read admin_users"
ON public.admin_users FOR SELECT TO authenticated
USING (user_id = auth.uid());

ALTER TABLE public.app_security_config ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Admin full app_security_config" ON public.app_security_config;
CREATE POLICY "Admin full app_security_config"
ON public.app_security_config FOR ALL TO authenticated
USING (public.is_admin_user()) WITH CHECK (public.is_admin_user());

ALTER TABLE public.course_payment_settings ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Admin full course_payment_settings" ON public.course_payment_settings;
CREATE POLICY "Admin full course_payment_settings"
ON public.course_payment_settings FOR ALL TO authenticated
USING (public.is_admin_user()) WITH CHECK (public.is_admin_user());

ALTER TABLE public.app_updates ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Admin full app_updates" ON public.app_updates;
CREATE POLICY "Admin full app_updates"
ON public.app_updates FOR ALL TO authenticated
USING (public.is_admin_user()) WITH CHECK (public.is_admin_user());

ALTER TABLE public.app_notices ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Admin full app_notices" ON public.app_notices;
CREATE POLICY "Admin full app_notices"
ON public.app_notices FOR ALL TO authenticated
USING (public.is_admin_user()) WITH CHECK (public.is_admin_user());

ALTER TABLE public.activation_support_links ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Admin full activation_support_links" ON public.activation_support_links;
CREATE POLICY "Admin full activation_support_links"
ON public.activation_support_links FOR ALL TO authenticated
USING (public.is_admin_user()) WITH CHECK (public.is_admin_user());

ALTER TABLE public.admin_push_devices ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Admin full admin_push_devices" ON public.admin_push_devices;
CREATE POLICY "Admin full admin_push_devices"
ON public.admin_push_devices FOR ALL TO authenticated
USING (public.is_admin_user()) WITH CHECK (public.is_admin_user());

ALTER TABLE public.student_notifications ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Admin full student_notifications" ON public.student_notifications;
CREATE POLICY "Admin full student_notifications"
ON public.student_notifications FOR ALL TO authenticated
USING (public.is_admin_user()) WITH CHECK (public.is_admin_user());"""

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
    onRunSyncDiagnostics: () -> Unit = {},
    adminEmail: String = "",
    adminUserId: String = "",
    onLogout: () -> Unit = {},
    allowedReleaseSignatures: List<String> = emptyList(),
    maxAdminPushPerMinute: Int = 20,
    isLoadingSecurityConfig: Boolean = false,
    isSavingSecurityConfig: Boolean = false,
    onRefreshSecurityConfig: () -> Unit = {},
    onAddReleaseSignature: (String) -> Unit = {},
    onRemoveReleaseSignature: (String) -> Unit = {},
    onSaveMaxAdminPushPerMinute: (Int) -> Unit = {}
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var appIdDraft by remember(oneSignalAppId) { mutableStateOf(oneSignalAppId) }
    var restKeyDraft by remember(oneSignalRestApiKey) { mutableStateOf(oneSignalRestApiKey) }
    var newSignatureDraft by remember { mutableStateOf("") }
    var maxPushDraft by remember(maxAdminPushPerMinute) { mutableStateOf(maxAdminPushPerMinute.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0A. Authenticated Admin Session & Logout Card (Zero-Trust RLS)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.2.dp,
                        Brush.horizontalGradient(listOf(EmeraldSuccess.copy(alpha = 0.7f), ElectricBlue.copy(alpha = 0.6f))),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldSuccess.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Admin",
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Authenticated Admin সেশন",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = adminEmail.ifBlank { "Verified Supabase Admin" },
                                    color = NeonCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "ZERO-TRUST RLS",
                                color = EmeraldSuccess,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (adminUserId.isNotBlank()) {
                        Surface(
                            color = Slate900,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "User UID: $adminUserId • EncryptedSharedPreferences Active",
                                color = Slate400,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onLogout,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralDanger.copy(alpha = 0.8f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CoralDanger.copy(alpha = 0.10f),
                            contentColor = CoralDanger
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_settings_logout")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = CoralDanger,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "অ্যাডমিন অ্যাকাউন্ট থেকে লগআউট করুন",
                            color = CoralDanger,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 0B. App Security Config (`public.app_security_config`, id = 1) — Allowed Release Signatures
        item {
            val cleanedSigDraft = newSignatureDraft.trim().replace(":", "").replace(" ", "")
            val isSigDraftValid = cleanedSigDraft.isEmpty() || Regex("^[0-9a-fA-F]{64}$").matches(cleanedSigDraft)
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.2.dp,
                        Brush.horizontalGradient(listOf(ElectricBlue.copy(alpha = 0.7f), NeonCyan.copy(alpha = 0.5f))),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricBlue.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Release Signatures",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "App Security Config (Release Signatures)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = "public.app_security_config (id = 1) • allowed_release_signatures",
                                    color = Slate400,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        IconButton(
                            onClick = onRefreshSecurityConfig,
                            enabled = !isLoadingSecurityConfig,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate800)
                                .testTag("btn_refresh_security_config")
                        ) {
                            if (isLoadingSecurityConfig) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Security Config",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "অফিশিয়াল স্টুডেন্ট APK-এর SHA-256 সাইনিং সিগনেচারগুলো এখানে যুক্ত রাখুন। অনুমোদিত সিগনেচার ছাড়া মোডিফায়েড/ক্লোন APK ব্লক হবে।",
                        color = Slate200,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    // Add New Signature Row
                    OutlinedTextField(
                        value = newSignatureDraft,
                        onValueChange = { newSignatureDraft = it },
                        label = { Text("নতুন SHA-256 Release Signature", color = Slate400, fontSize = 12.sp) },
                        placeholder = { Text("64-char hex অথবা AA:BB:CC:... ফরম্যাট", color = Slate400, fontSize = 11.sp) },
                        singleLine = true,
                        isError = !isSigDraftValid,
                        supportingText = if (!isSigDraftValid) {
                            {
                                Text(
                                    text = "⛔ ৬৪ অক্ষরের SHA-256 Hex (কোলনসহ বা ছাড়া) দিন (${cleanedSigDraft.length}/64)",
                                    color = CoralDanger,
                                    fontSize = 10.sp
                                )
                            }
                        } else null,
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
                            .testTag("input_release_signature")
                    )

                    Button(
                        onClick = {
                            if (cleanedSigDraft.length == 64 && isSigDraftValid) {
                                onAddReleaseSignature(newSignatureDraft.trim())
                                newSignatureDraft = ""
                            }
                        },
                        enabled = !isSavingSecurityConfig && cleanedSigDraft.length == 64 && isSigDraftValid,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_add_release_signature")
                    ) {
                        if (isSavingSecurityConfig) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("সেভ হচ্ছে...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Release Signature যুক্ত করুন", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                    }

                    // Existing Allowed Signatures List
                    if (allowedReleaseSignatures.isEmpty()) {
                        Surface(
                            color = Slate900,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "এখনো কোনো allowed_release_signatures যুক্ত করা হয়নি।",
                                color = Slate400,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "অনুমোদিত সিগনেচার তালিকা (${allowedReleaseSignatures.size} টি):",
                                color = EmeraldSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            allowedReleaseSignatures.forEachIndexed { index, sig ->
                                Surface(
                                    color = Slate900,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${index + 1}. $sig",
                                            color = NeonCyan,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(sig))
                                                    onShowToast("সিগনেচার কপি করা হয়েছে!")
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Signature",
                                                    tint = Slate400,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onRemoveReleaseSignature(sig) },
                                                enabled = !isSavingSecurityConfig,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .testTag("btn_remove_release_signature_$index")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove Signature",
                                                    tint = CoralDanger,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Slate700.copy(alpha = 0.5f))
                    )

                    // max_admin_push_per_minute setting (`public.app_security_config`, id = 1)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "অ্যাডমিন পুশ রেট লিমিট (max_admin_push_per_minute)",
                            color = Slate200,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = maxPushDraft,
                                onValueChange = { input ->
                                    maxPushDraft = input.filter { it.isDigit() }.take(3)
                                },
                                label = { Text("প্রতি মিনিটে সর্বোচ্চ পুশ", color = Slate400, fontSize = 11.sp) },
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
                                    .weight(1f)
                                    .testTag("input_max_admin_push_per_minute")
                            )
                            Button(
                                onClick = {
                                    val parsed = maxPushDraft.toIntOrNull()
                                    if (parsed != null && parsed in 1..600) {
                                        onSaveMaxAdminPushPerMinute(parsed)
                                    } else {
                                        onShowToast("⚠️ ১ থেকে ৬০০ এর মধ্যে একটি সংখ্যা দিন।")
                                    }
                                },
                                enabled = !isSavingSecurityConfig && (maxPushDraft.toIntOrNull() in 1..600),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("btn_save_max_admin_push_per_minute")
                            ) {
                                Text(
                                    text = "সেভ",
                                    color = Slate950,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

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

                    // API Key (Zero-Trust Public Anon Key ONLY — service_role strictly prohibited)
                    Column {
                        val isServiceRole = AdminPreferences.isServiceRoleJwt(supabaseKey)
                        val isValidAnonKey = supabaseKey.isNotBlank() && !isServiceRole
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Supabase Anon Key (Public • Zero-Trust)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Surface(
                                color = when {
                                    isServiceRole -> CoralDanger.copy(alpha = 0.18f)
                                    isValidAnonKey -> EmeraldSuccess.copy(alpha = 0.15f)
                                    else -> AmberWarning.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = when {
                                        isServiceRole -> "SERVICE_ROLE নিষিদ্ধ ⛔"
                                        isValidAnonKey -> "ANON KEY + AUTH ✅"
                                        else -> "ANON KEY প্রয়োজন ⚠️"
                                    },
                                    color = when {
                                        isServiceRole -> CoralDanger
                                        isValidAnonKey -> EmeraldSuccess
                                        else -> AmberWarning
                                    },
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
                            placeholder = { Text("eyJhbGciOiJIUz... (public anon key)", color = Slate400, fontSize = 13.sp) },
                            singleLine = true,
                            isError = isServiceRole,
                            supportingText = if (isServiceRole) {
                                {
                                    Text(
                                        text = "⛔ নিরাপত্তার জন্য service_role key এখানে সেভ করা যাবে না। শুধুমাত্র public anon key দিন।",
                                        color = CoralDanger,
                                        fontSize = 11.sp
                                    )
                                }
                            } else null,
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
                            enabled = !AdminPreferences.isServiceRoleJwt(supabaseKey),
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

                    // Zero-Trust Security Architecture Status Banner
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
                                text = "🛡️ Zero-Trust Authenticated RLS সক্রিয়: প্রতিটি Supabase API ও RPC কল-এ শুধুমাত্র public anon key (`apikey`) এবং আপনার লগইন করা অ্যাডমিন JWT (`Authorization: Bearer <access_token>`) পাঠানো হয়। অ্যাপের ভেতরে কোনো `service_role` key সংরক্ষণ করা হয় না।",
                                color = Slate200,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                            Button(
                                onClick = {
                                    val securitySql = """
                                        -- ============================================================================
                                        -- ZERO-TRUST AUTHENTICATED ADMIN SECURITY LOCK (LIVE 16 TABLES + 14 ADMIN RPCs)
                                        -- ============================================================================
                                        -- 1. Revoke anon/PUBLIC access from all Admin RPCs; grant ONLY to authenticated
                                        --    (each RPC internally enforces `public.is_admin_user() = true`).
                                        REVOKE ALL ON FUNCTION public.admin_approve_device_verification(text, text) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.admin_set_device_block_status(text, boolean, text) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.admin_toggle_device_ban(text, boolean, text) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.admin_set_device_course_permissions(text, boolean, text[], text[], text[], text[]) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.admin_set_account_course_permissions(text, text, text, boolean, boolean, text[], text[], text[], text[], boolean) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.admin_approve_course_access_request(bigint, integer, boolean, jsonb, text, boolean) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.admin_approve_course_access_with_trial(text, text, numeric, boolean, jsonb, boolean, integer, integer, boolean, text) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.admin_manage_course_trial(text, text, text, integer, integer) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.admin_review_course_payment(bigint, boolean, text, integer) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.admin_publish_app_update(text, integer, text, text, text, text, text) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.admin_set_kill_switch(boolean, text, text, text) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.notify_student_via_onesignal(text, text, text, text, text, text, jsonb) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.notify_admin_via_onesignal(text, text, text, jsonb) FROM PUBLIC, anon;
                                        REVOKE ALL ON FUNCTION public.update_onesignal_config_secure(text, text) FROM PUBLIC, anon;

                                        GRANT EXECUTE ON FUNCTION public.admin_approve_device_verification(text, text) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.admin_set_device_block_status(text, boolean, text) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.admin_toggle_device_ban(text, boolean, text) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.admin_set_device_course_permissions(text, boolean, text[], text[], text[], text[]) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.admin_set_account_course_permissions(text, text, text, boolean, boolean, text[], text[], text[], text[], boolean) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.admin_approve_course_access_request(bigint, integer, boolean, jsonb, text, boolean) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.admin_approve_course_access_with_trial(text, text, numeric, boolean, jsonb, boolean, integer, integer, boolean, text) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.admin_manage_course_trial(text, text, text, integer, integer) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.admin_review_course_payment(bigint, boolean, text, integer) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.admin_publish_app_update(text, integer, text, text, text, text, text) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.admin_set_kill_switch(boolean, text, text, text) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.notify_student_via_onesignal(text, text, text, text, text, text, jsonb) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.notify_admin_via_onesignal(text, text, text, jsonb) TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.update_onesignal_config_secure(text, text) TO authenticated;

                                        -- 2. Verify Admin Auth RPCs for `authenticated`
                                        GRANT EXECUTE ON FUNCTION public.is_admin_user() TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.is_admin() TO authenticated;
                                        GRANT EXECUTE ON FUNCTION public.check_is_admin() TO authenticated;
                                    """.trimIndent()
                                    clipboardManager.setText(AnnotatedString(securitySql))
                                    onShowToast("✅ Zero-Trust Authenticated RLS SQL কপি হয়েছে!")
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
                                    text = "Zero-Trust Authenticated RLS SQL কপি করুন",
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
                        text = "আপনার Supabase প্রজেক্টে লাইভ Zero-Trust Authenticated Admin RLS রেফারেন্স স্ক্রিপ্ট দেখতে বা কপি করতে উপরের বাটন ব্যবহার করুন।",
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
