package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

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

-- Enable Row Level Security (RLS)
ALTER TABLE access_codes ENABLE ROW LEVEL SECURITY;
ALTER TABLE activated_devices ENABLE ROW LEVEL SECURITY;

-- Allow Admin API Access (Service Role or Public Anon Key)
CREATE POLICY "Public Read/Write Access" ON access_codes FOR ALL USING (true);
CREATE POLICY "Public Devices Read/Write Access" ON activated_devices FOR ALL USING (true);"""

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
    onShowToast: (String) -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "সেটিংস ও সুপাবেস কানেকশন",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Supabase API & Admin Security Configuration",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NeonCyan,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        // Supabase Endpoint Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = supabaseUrl,
                            onValueChange = onUrlChange,
                            placeholder = { Text("https://your-project.supabase.co", color = Slate400) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Slate600,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
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
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = supabaseKey,
                            onValueChange = onKeyChange,
                            placeholder = { Text("eyJhbGciOiJIUz...", color = Slate400) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Slate600,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_supabase_key")
                        )
                    }

                    // Status display
                    if (!connectionStatus.isNullOrBlank()) {
                        Surface(
                            color = if (isConnectionSuccess == true) EmeraldSuccess.copy(alpha = 0.15f) else CoralDanger.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isConnectionSuccess == true) EmeraldSuccess else CoralDanger,
                                    RoundedCornerShape(10.dp)
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
                            colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_test_connection")
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NeonCyan, strokeWidth = 2.dp)
                            } else {
                                Text("টেস্ট কানেকশন", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = onSaveSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_save_settings")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সেভ করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Supabase SQL Schema Helper
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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
                                .background(Slate700)
                                .size(36.dp)
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
                        text = "আপনার Supabase প্রজেক্টে access_codes ও activated_devices টেবিল না থাকলে উপরের বাটন দিয়ে কপি করে Supabase SQL Editor এ রান করুন।",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                    )

                    Surface(
                        color = Slate950,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = SUPABASE_SCHEMA_SQL,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
