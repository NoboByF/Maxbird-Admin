package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.CardBorderGradientEnd
import com.example.ui.theme.CardBorderGradientStart
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.Violet500

private const val SUPABASE_SCHEMA_SQL = """-- MaxBird Supabase Setup SQL Script
-- Run this in your Supabase Dashboard -> SQL Editor

CREATE TABLE IF NOT EXISTS access_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT UNIQUE NOT NULL,
    student_name TEXT NOT NULL,
    max_devices INT DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    note TEXT
);

CREATE TABLE IF NOT EXISTS activated_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code_id UUID REFERENCES access_codes(id) ON DELETE CASCADE,
    device_id TEXT NOT NULL,
    device_model TEXT DEFAULT 'Unknown Device',
    is_blocked BOOLEAN DEFAULT false,
    activated_at TIMESTAMPTZ DEFAULT now(),
    last_active_at TIMESTAMPTZ DEFAULT now()
);

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
    status TEXT DEFAULT 'active',
    last_seen TIMESTAMPTZ DEFAULT now()
);

-- Enable Row Level Security (RLS)
ALTER TABLE access_codes ENABLE ROW LEVEL SECURITY;
ALTER TABLE activated_devices ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_updates ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_notices ENABLE ROW LEVEL SECURITY;
ALTER TABLE activation_support_links ENABLE ROW LEVEL SECURITY;
ALTER TABLE devices ENABLE ROW LEVEL SECURITY;

-- Allow Public/Admin Read and Write
CREATE POLICY "Public Access access_codes" ON access_codes FOR ALL USING (true);
CREATE POLICY "Public Access activated_devices" ON activated_devices FOR ALL USING (true);
CREATE POLICY "Public Access app_updates" ON app_updates FOR ALL USING (true);
CREATE POLICY "Public Access app_notices" ON app_notices FOR ALL USING (true);
CREATE POLICY "Public Access activation_support_links" ON activation_support_links FOR ALL USING (true);
CREATE POLICY "Public Access devices" ON devices FOR ALL USING (true);"""

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
    isShikhoConnected: Boolean = false,
    shikhoConnectedPhone: String = "",
    shikhoConnectedUserId: String = "",
    onOpenShikhoLogin: () -> Unit = {},
    onLogoutShikho: () -> Unit = {}
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shikho Account API Connection Card
        item {
            ShikhoConnectionBannerCard(
                isConnected = isShikhoConnected,
                connectedPhone = shikhoConnectedPhone,
                connectedUserId = shikhoConnectedUserId,
                onOpenLoginDialog = onOpenShikhoLogin,
                onLogout = onLogoutShikho
            )
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
                        Text(
                            text = "Supabase Anon / Service Role Key",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
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
                        text = "আপনার Supabase প্রজেক্টে access_codes, devices ও অন্যান্য টেবিল না থাকলে উপরের বাটন দিয়ে কপি করে Supabase SQL Editor এ রান করুন।",
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
                                text = "• মডিউলসমূহ: নোটিশ ম্যানেজমেন্ট, অ্যাপ আপডেট, কোড ও ডিভাইস কিল-সুইচ",
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
