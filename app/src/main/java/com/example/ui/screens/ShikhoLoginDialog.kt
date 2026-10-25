package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ShikhoApiClient
import com.example.ui.theme.AmberWarning
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

/**
 * Reusable Banner Card for Shikho API Login / Connection Status.
 * Can be placed on DeviceManagementScreen, SettingsScreen, or inside BottomSheets.
 */
@Composable
fun ShikhoConnectionBannerCard(
    isConnected: Boolean,
    connectedPhone: String,
    connectedUserId: String,
    onOpenLoginDialog: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isConnected) EmeraldSuccess else AmberWarning

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(accentColor.copy(alpha = 0.65f), ElectricBlue.copy(alpha = 0.4f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onOpenLoginDialog() }
            .testTag("card_shikho_api_status")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Key,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isConnected) {
                                "শিখো API কানেক্টেড (Live)"
                            } else {
                                "শিখো অ্যাকাউন্ট কানেক্ট করুন (Shikho API Login)"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Text(
                        text = if (isConnected) {
                            "ফোন: ${connectedPhone.ifBlank { "সংযুক্ত" }}${if (connectedUserId.isNotBlank()) " • ID: ${connectedUserId.take(10)}" else ""}"
                        } else {
                            "অফিসিয়াল সার্ভার থেকে রিয়েল শ্রেণি, ব্যাচ ও কোর্স লোড করতে লগইন করুন"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isConnected) EmeraldSuccess else Slate400,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onOpenLoginDialog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) Slate800 else ElectricBlue
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_open_shikho_login")
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.AccountCircle else Icons.Default.Link,
                    contentDescription = null,
                    tint = if (isConnected) NeonCyan else Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isConnected) "অ্যাকাউন্ট" else "লগইন",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Dialog for Admin to log into their own Shikho account via `POST https://api.shikho.com/auth/v2/login`
 * using phone number (`017xxxxxxxx` -> `88017xxxxxxxx`) and PIN (`otp`).
 */
@Composable
fun ShikhoAccountLoginDialog(
    isOpen: Boolean,
    isConnected: Boolean,
    connectedPhone: String,
    connectedUserId: String,
    accessTokenPreview: String,
    isLoggingIn: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onLoginSubmit: (phone: String, pin: String) -> Unit,
    onLogout: () -> Unit
) {
    if (!isOpen) return

    var phoneInput by remember(connectedPhone) {
        mutableStateOf(
            if (connectedPhone.startsWith("8801") && connectedPhone.length == 13) {
                connectedPhone.removePrefix("88")
            } else {
                connectedPhone
            }
        )
    }
    var pinInput by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }

    val formattedPreview = remember(phoneInput) {
        ShikhoApiClient.formatPhoneNumber(phoneInput)
    }

    AlertDialog(
        onDismissRequest = { if (!isLoggingIn) onDismiss() },
        containerColor = Slate900,
        titleContentColor = Color.White,
        textContentColor = Slate200,
        shape = RoundedCornerShape(22.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isConnected) EmeraldSuccess.copy(alpha = 0.2f)
                        else ElectricBlue.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Key,
                    contentDescription = null,
                    tint = if (isConnected) EmeraldSuccess else NeonCyan,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "শিখো অ্যাকাউন্ট কানেক্ট করুন",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Shikho Official API (https://api.shikho.com)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isConnected) {
                    Surface(
                        color = EmeraldSuccess.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✅ শিখো অ্যাকাউন্ট সংযুক্ত আছে",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                OutlinedButton(
                                    onClick = onLogout,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralDanger),
                                    border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.6f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("btn_shikho_logout")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LinkOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("লগআউট", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (connectedPhone.isNotBlank()) {
                                Text(
                                    text = "📱 ফোন: $connectedPhone",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (connectedUserId.isNotBlank()) {
                                Text(
                                    text = "🆔 User ID: $connectedUserId",
                                    fontSize = 11.sp,
                                    color = NeonCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (accessTokenPreview.isNotBlank()) {
                                Text(
                                    text = "🔑 Token: ${accessTokenPreview.take(22)}...",
                                    fontSize = 10.sp,
                                    color = Slate400,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Slate700)

                    Text(
                        text = "অন্য শিখো অ্যাকাউন্ট বা নতুন টোকেন রিফ্রেশ করতে নিচে পুনরায় লগইন করুন:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                    )
                } else {
                    Text(
                        text = "আপনার নিজের শিখো অ্যাকাউন্টের ফোন নাম্বার ও ৪ ডিজিটের পিন (PIN) দিয়ে লগইন করুন। এই টোকেন দিয়ে শিখোর অফিসিয়াল সার্ভার থেকে রিয়েল শ্রেণি, ব্যাচ এবং কোর্সের তালিকা লোড হবে।",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate200,
                            lineHeight = 18.sp
                        )
                    )
                }

                // Phone Number Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোবাইল নাম্বার (Phone Number)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (formattedPreview.isNotBlank()) {
                            Text(
                                text = "API ফরম্যাট: $formattedPreview",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        placeholder = {
                            Text(
                                text = "017XXXXXXXX বা 88017XXXXXXXX",
                                color = Slate400,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = NeonCyan
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_shikho_phone")
                    )
                }

                // PIN Input (sent in "otp" field)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "শিখো পিন (PIN - ৪ ডিজিট)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        placeholder = {
                            Text(
                                text = "যেমন: 1234",
                                color = Slate400,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = NeonCyan
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle PIN Visibility",
                                    tint = Slate400
                                )
                            }
                        },
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_shikho_pin")
                    )
                }

                // Error Message
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = CoralDanger.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CoralDanger),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ $errorMessage",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onLoginSubmit(phoneInput, pinInput) },
                enabled = !isLoggingIn && phoneInput.isNotBlank() && pinInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_submit_shikho_login")
            ) {
                if (isLoggingIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("লগইন হচ্ছে...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("লগইন ও টোকেন সেভ করুন", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoggingIn,
                colors = ButtonDefaults.textButtonColors(contentColor = Slate400)
            ) {
                Text("বন্ধ করুন")
            }
        }
    )
}
