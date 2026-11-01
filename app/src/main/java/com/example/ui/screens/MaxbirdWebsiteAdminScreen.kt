package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.MaxbirdAdminAuthSession
import com.example.data.model.MaxbirdPortalSettings
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PurpleRoyal
import com.example.ui.theme.Sky500
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlin.math.roundToLong

/**
 * Dedicated "Maxbird Admin Control Panel" Screen to manage the public Maxbird website
 * (`https://maxbird.pages.dev`) via Firebase Project `empirical-envelope-b0z9n`
 * and Named Firestore Database `ai-studio-edusmartappporta-6327637e-4b8d-4e4e-be1d-92bc33269fa0`,
 * document `portal_settings/main`.
 */
@Composable
fun MaxbirdWebsiteAdminScreen(
    settings: MaxbirdPortalSettings,
    authSession: MaxbirdAdminAuthSession,
    isLoading: Boolean,
    isSaving: Boolean,
    lastSyncMessage: String?,
    onUpdateDraft: (MaxbirdPortalSettings) -> Unit,
    onRefreshFromFirestore: () -> Unit,
    onSaveToFirestore: (MaxbirdPortalSettings) -> Unit,
    onSignInWithEmailPassword: (email: String, password: String) -> Unit,
    onCreateAdminWithEmailPassword: (email: String, password: String) -> Unit,
    onSendPasswordReset: (email: String) -> Unit,
    onVerifyManualIdToken: (idToken: String) -> Unit,
    onWebSdkAuthReceived: (uid: String, email: String, displayName: String, photoUrl: String, idToken: String, refreshToken: String) -> Unit,
    onSignOut: () -> Unit,
    onInspectLocalApkUri: (Uri) -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // 0 = Native Android Control Panel, 1 = Embedded Live Firebase Web SDK Mode
    var selectedViewMode by remember { mutableIntStateOf(0) }

    // Download count edit dialog state
    var showDownloadCountDialog by remember { mutableStateOf(false) }
    var downloadCountInput by remember(settings.downloadCount) {
        mutableStateOf(settings.downloadCount.toString())
    }

    // Auth form state
    var emailInput by remember { mutableStateOf(MaxbirdPortalSettings.ADMIN_EMAIL) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showTokenInputSection by remember { mutableStateOf(false) }
    var manualTokenInput by remember { mutableStateOf("") }
    var showAuthorizedDomainsHelp by remember { mutableStateOf(false) }

    // Local .APK File Picker Launcher
    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onInspectLocalApkUri(uri)
        }
    }

    // Dialog to edit or reset Download Counter
    if (showDownloadCountDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadCountDialog = false },
            containerColor = Slate850,
            title = {
                Text(
                    text = "ডাউনলোড কাউন্টার এডিট / রিসেট",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ওয়েবসাইটে দেখানো মোট ডাউনলোড সংখ্যা পরিবর্তন করুন বা 0-তে রিসেট করুন (0 থেকে 100,000,000):",
                        color = Slate200,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = downloadCountInput,
                        onValueChange = { raw ->
                            downloadCountInput = raw.filter { it.isDigit() }.take(9)
                        },
                        label = { Text("ডাউনলোড সংখ্যা (downloadCount)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { downloadCountInput = "0" },
                            border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = CoralDanger,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("রিসেট (0)", color = CoralDanger, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                val next = (downloadCountInput.toLongOrNull() ?: settings.downloadCount) + 100L
                                downloadCountInput = next.coerceAtMost(100_000_000L).toString()
                            },
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+১০০ যোগ", color = NeonCyan, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = (downloadCountInput.toDoubleOrNull() ?: 0.0)
                            .roundToLong()
                            .coerceIn(0L, 100_000_000L)
                        val updated = settings.copy(downloadCount = parsed)
                        onUpdateDraft(updated)
                        showDownloadCountDialog = false
                        if (authSession.isSignedIn) {
                            onSaveToFirestore(updated)
                        } else {
                            onShowToast("ডাউনলোড সংখ্যা $parsed সেট করা হয়েছে। লাইভ করতে সেভ বাটনে চাপুন।")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("আপডেট করুন", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadCountDialog = false }) {
                    Text("বাতিল", color = Slate400)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        // Top Mode Bar: Switch between Native Android Panel & Live Firebase Web SDK Panel
        Surface(
            color = Slate950,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedViewMode == 0) ElectricBlue else Slate800,
                    border = BorderStroke(
                        1.dp,
                        if (selectedViewMode == 0) NeonCyan else Slate700
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedViewMode = 0 }
                        .testTag("tab_maxbird_native_mode")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "নেটিভ কন্ট্রোল প্যানেল",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedViewMode == 1) PurpleRoyal else Slate800,
                    border = BorderStroke(
                        1.dp,
                        if (selectedViewMode == 1) NeonCyan else Slate700
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedViewMode = 1 }
                        .testTag("tab_maxbird_websdk_mode")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Web SDK + Google Login",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (selectedViewMode == 1) {
            // Embedded Live Firebase Web SDK Mode (uses exact firebase/app, firebase/auth, firebase/firestore)
            EmbeddedFirebaseWebSdkPanel(
                onAuthReceived = { uid, email, displayName, photoUrl, idToken, refreshToken ->
                    onWebSdkAuthReceived(uid, email, displayName, photoUrl, idToken, refreshToken)
                },
                onSwitchToNative = {
                    selectedViewMode = 0
                    onRefreshFromFirestore()
                }
            )
            return@Column
        }

        // Native Android Control Panel Mode
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Hero Banner: Maxbird Admin Control Panel & Named Firestore Database Status
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(ElectricBlue, NeonCyan)),
                            RoundedCornerShape(18.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(ElectricBlue.copy(alpha = 0.16f), Slate850)
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(ElectricBlue, NeonCyan))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Maxbird Admin Control Panel",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "ওয়েবসাইট: maxbird.pages.dev • Firestore Live Sync",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonCyan,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = onRefreshFromFirestore,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Slate800)
                                    .testTag("btn_refresh_maxbird_firestore")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = NeonCyan
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Database & Document Path Pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate950,
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "Project: ${MaxbirdPortalSettings.FIREBASE_PROJECT_ID} • Doc: portal_settings/main",
                                    color = EmeraldSuccess,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "DB: ${MaxbirdPortalSettings.FIRESTORE_DATABASE_ID}",
                                    color = Slate400,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (!lastSyncMessage.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = lastSyncMessage,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Authentication (Admin Login) Card — maiafahim1234@gmail.com
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (authSession.isSignedIn && authSession.isAuthorizedAdminEmail)
                                EmeraldSuccess.copy(alpha = 0.5f)
                            else
                                AmberWarning.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (authSession.isSignedIn)
                                        Icons.Default.VerifiedUser
                                    else
                                        Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (authSession.isSignedIn) EmeraldSuccess else AmberWarning,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "অ্যাডমিন অথেনটিকেশন (Firebase Auth)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "অনুমোদিত অ্যাডমিন ইমেইল: ${MaxbirdPortalSettings.ADMIN_EMAIL}",
                                        color = NeonCyan,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (authSession.isSignedIn) {
                                OutlinedButton(
                                    onClick = onSignOut,
                                    border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("btn_maxbird_admin_signout")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Sign Out",
                                        tint = CoralDanger,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sign Out", color = CoralDanger, fontSize = 12.sp)
                                }
                            }
                        }

                        if (authSession.isSignedIn) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Slate900,
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSuccess.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = authSession.email,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (authSession.isAuthorizedAdminEmail)
                                                "✅ ভেরিফাইড অ্যাডমিন • UID: ${authSession.uid.take(12)}..."
                                            else
                                                "⚠️ শুধুমাত্র ${MaxbirdPortalSettings.ADMIN_EMAIL} রাইট অ্যাক্সেস পাবে",
                                            color = if (authSession.isAuthorizedAdminEmail) EmeraldSuccess else AmberWarning,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            // Login Options:
                            // 1. Google Sign-In via Embedded Firebase Web SDK
                            Button(
                                onClick = { selectedViewMode = 1 },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_maxbird_google_signin")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Google Sign-In / Web SDK দিয়ে লগইন করুন",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            HorizontalDivider(color = Slate700)

                            // 2. Direct Email & Password Sign-In for maiafahim1234@gmail.com
                            Text(
                                text = "অথবা সরাসরি অ্যাডমিন ইমেইল ও পাসওয়ার্ড দিয়ে লগইন করুন:",
                                color = Slate200,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Admin Email") },
                                singleLine = true,
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_maxbird_admin_email")
                            )

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password (কমপক্ষে ৬ অক্ষর)") },
                                singleLine = true,
                                visualTransformation = if (passwordVisible)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible)
                                                Icons.Default.VisibilityOff
                                            else
                                                Icons.Default.Visibility,
                                            contentDescription = "Toggle Password",
                                            tint = Slate400
                                        )
                                    }
                                },
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_maxbird_admin_password")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (emailInput.isBlank() || passwordInput.isBlank()) {
                                            onShowToast("ইমেইল এবং পাসওয়ার্ড দিন।")
                                        } else {
                                            onSignInWithEmailPassword(emailInput, passwordInput)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_maxbird_email_login")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("লগইন করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (emailInput.isBlank() || passwordInput.length < 6) {
                                            onShowToast("ইমেইল এবং কমপক্ষে ৬ অক্ষরের পাসওয়ার্ড দিন।")
                                        } else {
                                            onCreateAdminWithEmailPassword(emailInput, passwordInput)
                                        }
                                    },
                                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("১ম বার পাসওয়ার্ড সেট", color = NeonCyan, fontSize = 11.sp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        if (emailInput.isNotBlank()) {
                                            onSendPasswordReset(emailInput)
                                        } else {
                                            onShowToast("পাসওয়ার্ড রিসেট করতে ইমেইল দিন।")
                                        }
                                    }
                                ) {
                                    Text("পাসওয়ার্ড রিসেট ইমেইল পাঠান", color = ElectricBlueLight, fontSize = 11.sp)
                                }

                                TextButton(
                                    onClick = { showTokenInputSection = !showTokenInputSection }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ID Token লগইন", color = Slate400, fontSize = 11.sp)
                                }
                            }

                            AnimatedVisibility(visible = showTokenInputSection) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = manualTokenInput,
                                        onValueChange = { manualTokenInput = it },
                                        label = { Text("Firebase Auth ID Token পেস্ট করুন") },
                                        singleLine = true,
                                        colors = adminTextFieldColors(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            if (manualTokenInput.isNotBlank()) {
                                                onVerifyManualIdToken(manualTokenInput)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PurpleRoyal),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("ID Token ভেরিফাই ও লগইন করুন", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Authorized Domains Helpful Info Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate900,
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAuthorizedDomainsHelp = !showAuthorizedDomainsHelp }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "গুরুত্বপূর্ণ নোট: External Domain (যেমন *.pages.dev) ও Google Sign-In সেটিংস",
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Text(
                                    text = "যদি ওয়েবসাইট বা প্যানেল কোনো এক্সটার্নাল ডোমেইনে (যেমন maxbird.pages.dev বা *.pages.dev) হোস্ট করা হয়, তবে Google Sign-In কাজ করার জন্য ডোমেইনটি অবশ্যই Firebase Console -> Authentication -> Settings -> Authorized domains-এ যুক্ত থাকতে হবে।",
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(MaxbirdPortalSettings.FIREBASE_AUTH_SETTINGS_URL)
                                                )
                                            )
                                        } catch (_: Exception) {
                                            clipboardManager.setText(
                                                AnnotatedString(MaxbirdPortalSettings.FIREBASE_AUTH_SETTINGS_URL)
                                            )
                                            onShowToast("লিঙ্ক কপি করা হয়েছে!")
                                        }
                                    },
                                    border = BorderStroke(1.dp, ElectricBlueLight.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = ElectricBlueLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Firebase Authorized Domains সেটিংস ওপেন করুন",
                                        color = ElectricBlueLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Section A: Live Status Overview Cards
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "A. লাইভ স্ট্যাটাস ওভারভিউ (Live Status Overview)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Live Download Counter Card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Slate900,
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        downloadCountInput = settings.downloadCount.toString()
                                        showDownloadCountDialog = true
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "মোট ডাউনলোড",
                                            color = Slate400,
                                            fontSize = 11.sp
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Download Count",
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = "${settings.downloadCount}",
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp
                                    )
                                    Text(
                                        text = "রিসেট / এডিট করতে ট্যাপ করুন",
                                        color = NeonCyan,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Current Live APK Version & File Size Card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Slate900,
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "লাইভ APK ভার্সন ও সাইজ",
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = settings.appVersion.ifBlank { "v1.0.0" },
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "সাইজ: ${settings.fileSize.ifBlank { "10 MB" }} • ${settings.minAndroidVersion}",
                                        color = NeonCyan,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Action Buttons: Test APK Download Link & Open Public Website (https://maxbird.pages.dev)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val url = settings.apkDownloadUrl.trim()
                                    if (url.isBlank()) {
                                        onShowToast("এখনো কোনো APK ডাউনলোড লিংক দেওয়া হয়নি!")
                                    } else {
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        } catch (e: Exception) {
                                            onShowToast("লিংক ওপেন করা যায়নি: ${e.message}")
                                        }
                                    }
                                },
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_test_apk_download_link")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Test APK Link",
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(MaxbirdPortalSettings.PUBLIC_WEBSITE_URL)
                                            )
                                        )
                                    } catch (e: Exception) {
                                        onShowToast("ওয়েবসাইট ওপেন করা যায়নি: ${e.message}")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_open_maxbird_public_website")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "maxbird.pages.dev",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. Section B: Cloudflare R2 APK Link Manager & Local .APK File Inspector
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "B. Cloudflare R2 APK লিংক ম্যানেজার",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "ডিরেক্ট R2 লিংক, অটো-বিল্ডার এবং লোকাল .apk ফাইল সাইজ ও নাম অটো-ডিটেক্টর",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Local .APK File Inspector Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElectricBlue.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    apkPickerLauncher.launch("*/*")
                                }
                                .testTag("btn_pick_local_apk_inspector")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ElectricBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ফোন থেকে .APK ফাইল সিলেক্ট করুন (Auto Inspector)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "ফাইলের সঠিক MB সাইজ (fileSize) এবং ফাইলের নাম (r2ObjectKey) অটোমেটিক বসে যাবে!",
                                        color = NeonCyan,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // R2 Bucket Public Domain
                        OutlinedTextField(
                            value = settings.r2BucketPublicDomain,
                            onValueChange = { newDomain ->
                                val cleanDom = newDomain.take(500)
                                val autoUrl = MaxbirdPortalSettings.autoBuildR2Url(cleanDom, settings.r2ObjectKey)
                                onUpdateDraft(
                                    settings.copy(
                                        r2BucketPublicDomain = cleanDom,
                                        apkDownloadUrl = if (autoUrl.isNotEmpty()) autoUrl else settings.apkDownloadUrl
                                    )
                                )
                            },
                            label = { Text("R2 Bucket Public Domain (ঐচ্ছিক, যেমন: pub-xxx.r2.dev)") },
                            supportingText = {
                                Text(
                                    text = "${settings.r2BucketPublicDomain.length}/500 chars",
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            },
                            singleLine = true,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_maxbird_r2_domain")
                        )

                        // R2 Object Key (APK Filename)
                        OutlinedTextField(
                            value = settings.r2ObjectKey,
                            onValueChange = { newKey ->
                                val cleanKey = newKey.take(300)
                                val autoUrl = MaxbirdPortalSettings.autoBuildR2Url(settings.r2BucketPublicDomain, cleanKey)
                                onUpdateDraft(
                                    settings.copy(
                                        r2ObjectKey = cleanKey,
                                        apkDownloadUrl = if (autoUrl.isNotEmpty()) autoUrl else settings.apkDownloadUrl
                                    )
                                )
                            },
                            label = { Text("R2 Object Key / APK ফাইলের নাম (যেমন: maxbird-v1.0.0.apk)") },
                            supportingText = {
                                Text(
                                    text = "${settings.r2ObjectKey.length}/300 chars • ডোমেইন ও ফাইলনেম দিলে নিচের লিংক অটো-তৈরি হবে",
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            },
                            singleLine = true,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_maxbird_r2_key")
                        )

                        // Direct Cloudflare R2 APK Download URL
                        OutlinedTextField(
                            value = settings.apkDownloadUrl,
                            onValueChange = { newUrl ->
                                onUpdateDraft(settings.copy(apkDownloadUrl = newUrl.take(1000)))
                            },
                            label = { Text("Direct Cloudflare R2 APK Download URL (apkDownloadUrl)") },
                            supportingText = {
                                Text(
                                    text = "${settings.apkDownloadUrl.length}/1000 chars • ওয়েবসাইটের Download APK বাটনে এই লিংক কাজ করবে",
                                    color = NeonCyan,
                                    fontSize = 10.sp
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (settings.apkDownloadUrl.isNotBlank()) {
                                            clipboardManager.setText(AnnotatedString(settings.apkDownloadUrl))
                                            onShowToast("APK Download URL কপি করা হয়েছে!")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy APK URL",
                                        tint = NeonCyan
                                    )
                                }
                            },
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_maxbird_apk_url")
                        )

                        if (settings.r2BucketPublicDomain.isNotBlank() && settings.r2ObjectKey.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    val built = MaxbirdPortalSettings.autoBuildR2Url(
                                        settings.r2BucketPublicDomain,
                                        settings.r2ObjectKey
                                    )
                                    onUpdateDraft(settings.copy(apkDownloadUrl = built))
                                    onShowToast("অটো-বিল্ড লিংক সেট হয়েছে: $built")
                                },
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "https://${settings.r2BucketPublicDomain.trim().trimEnd('/')}/${settings.r2ObjectKey.trim().trimStart('/')} সেট করুন",
                                    color = EmeraldSuccess,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 5. Section C: Website Content, Version Info & Notice Editor
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "C. ওয়েবসাইট কনটেন্ট ও নোটিশ এডিটর (17-Field Sync)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "অ্যাপের নাম, ভার্সন, ফাইল সাইজ, টপ নোটিশ ব্যানার, রিলিজ নোটস এবং টেলিগ্রাম সাপোর্ট",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Row 1: appName & appVersion
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = settings.appName,
                                onValueChange = { onUpdateDraft(settings.copy(appName = it.take(100))) },
                                label = { Text("App Name (appName)") },
                                singleLine = true,
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_maxbird_app_name")
                            )

                            OutlinedTextField(
                                value = settings.appVersion,
                                onValueChange = { onUpdateDraft(settings.copy(appVersion = it.take(60))) },
                                label = { Text("ভার্সন (appVersion)") },
                                singleLine = true,
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_maxbird_app_version")
                            )
                        }

                        // Row 2: fileSize & minAndroidVersion
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = settings.fileSize,
                                onValueChange = { onUpdateDraft(settings.copy(fileSize = it.take(40))) },
                                label = { Text("ফাইল সাইজ (fileSize)") },
                                singleLine = true,
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_maxbird_file_size")
                            )

                            OutlinedTextField(
                                value = settings.minAndroidVersion,
                                onValueChange = { onUpdateDraft(settings.copy(minAndroidVersion = it.take(50))) },
                                label = { Text("Min Android (যেমন Android 7.0+)") },
                                singleLine = true,
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_maxbird_min_android")
                            )
                        }

                        // Row 3: lastUpdated & sha256Short
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = settings.lastUpdated,
                                onValueChange = { onUpdateDraft(settings.copy(lastUpdated = it.take(60))) },
                                label = { Text("আপডেট তারিখ (lastUpdated)") },
                                singleLine = true,
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_maxbird_last_updated")
                            )

                            OutlinedTextField(
                                value = settings.sha256Short,
                                onValueChange = { onUpdateDraft(settings.copy(sha256Short = it.take(150))) },
                                label = { Text("সিকিউরিটি ব্যাজ (sha256Short)") },
                                singleLine = true,
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_maxbird_sha256")
                            )
                        }

                        // Top Announcement Notice Banner (noticeText)
                        OutlinedTextField(
                            value = settings.noticeText,
                            onValueChange = { onUpdateDraft(settings.copy(noticeText = it.take(500))) },
                            label = { Text("ওয়েবসাইটের টপ নোটিশ ব্যানার (noticeText)") },
                            supportingText = {
                                Text(
                                    text = "${settings.noticeText.length}/500 chars • ওয়েবসাইটের সবার উপরে এই নোটিশটি লাইভ দেখাবে",
                                    color = AmberWarning,
                                    fontSize = 10.sp
                                )
                            },
                            minLines = 2,
                            maxLines = 4,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_maxbird_notice_text")
                        )

                        // Release Notes (releaseNotes)
                        OutlinedTextField(
                            value = settings.releaseNotes,
                            onValueChange = { onUpdateDraft(settings.copy(releaseNotes = it.take(1000))) },
                            label = { Text("নতুন ভার্সনে কী কী থাকছে / Release Notes (releaseNotes)") },
                            supportingText = {
                                Text(
                                    text = "${settings.releaseNotes.length}/1000 chars",
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            },
                            minLines = 3,
                            maxLines = 6,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_maxbird_release_notes")
                        )

                        // Row 4: telegramHandle & telegramUrl
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = settings.telegramHandle,
                                onValueChange = { onUpdateDraft(settings.copy(telegramHandle = it.take(80))) },
                                label = { Text("Telegram Handle (@...)") },
                                singleLine = true,
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_maxbird_telegram_handle")
                            )

                            OutlinedTextField(
                                value = settings.telegramUrl,
                                onValueChange = { onUpdateDraft(settings.copy(telegramUrl = it.take(300))) },
                                label = { Text("Telegram URL (https://t.me/...)") },
                                singleLine = true,
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_maxbird_telegram_url")
                            )
                        }

                        // Metadata Read-only Summary (ownerId, createdAt, updatedAt)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate900,
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "সিস্টেম মেটাডাটা (১৭টি ফিল্ড অটো-সংরক্ষিত):",
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "• ownerId: ${settings.ownerId.ifBlank { authSession.uid.ifBlank { "(প্রথমবার সেভ করলে সেট হবে)" } }}",
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "• createdAt: ${settings.createdAtIso ?: "(সার্ভার টাইমস্ট্যাম্প — আপডেটে অপরিবর্তিত থাকবে)"}",
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "• updatedAt: ${settings.updatedAtIso ?: "(প্রতিটি সেভে অটো serverTimestamp হবে)"}",
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // 6. Section D: One-Click Save & Instant Sync Button
            item {
                Button(
                    onClick = { onSaveToFirestore(settings) },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSuccess,
                        disabledContainerColor = Slate700
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_save_maxbird_portal_settings")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "maxbird.pages.dev-এ লাইভ সেভ হচ্ছে...",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "১-ক্লিকে ওয়েবসাইটে লাইভ সেভ করুন (Save Changes)",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

/**
 * Embedded Web SDK Panel that runs the exact `firebase/app`, `firebase/auth`, and `firebase/firestore`
 * JS SDKs with named database `ai-studio-edusmartappporta-6327637e-4b8d-4e4e-be1d-92bc33269fa0`
 * on origin `https://empirical-envelope-b0z9n.firebaseapp.com`, allowing both Google Sign-In
 * and real-time `onSnapshot` / `setDoc` / `updateDoc` directly from the Web SDK.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun EmbeddedFirebaseWebSdkPanel(
    onAuthReceived: (uid: String, email: String, displayName: String, photoUrl: String, idToken: String, refreshToken: String) -> Unit,
    onSwitchToNative: () -> Unit
) {
    val htmlContent = remember { buildMaxbirdFirebaseWebDashboardHtml() }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = Slate900,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Firebase JS SDK (firebase/app, auth, firestore) • Live Mode",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onSwitchToNative) {
                    Text("নেটিভ প্যানেলে ফিরুন", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        var popupWebView by remember { mutableStateOf<WebView?>(null) }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        android.webkit.CookieManager.getInstance().setAcceptCookie(true)
                        android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.setSupportMultipleWindows(true)
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        settings.userAgentString =
                            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

                        addJavascriptInterface(
                            object {
                                @JavascriptInterface
                                fun onFirebaseUserSignedIn(
                                    uid: String,
                                    email: String,
                                    displayName: String,
                                    photoUrl: String,
                                    idToken: String,
                                    refreshToken: String
                                ) {
                                    post {
                                        popupWebView?.destroy()
                                        popupWebView = null
                                        onAuthReceived(uid, email, displayName, photoUrl, idToken, refreshToken)
                                    }
                                }
                            },
                            "AndroidBridge"
                        )

                        webChromeClient = object : WebChromeClient() {
                            override fun onCreateWindow(
                                view: WebView?,
                                isDialog: Boolean,
                                isUserGesture: Boolean,
                                resultMsg: android.os.Message?
                            ): Boolean {
                                val newWebView = WebView(ctx).apply {
                                    android.webkit.CookieManager.getInstance().setAcceptCookie(true)
                                    android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.databaseEnabled = true
                                    settings.javaScriptCanOpenWindowsAutomatically = true
                                    settings.setSupportMultipleWindows(true)
                                    settings.userAgentString =
                                        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean = false
                                    }
                                    webChromeClient = object : WebChromeClient() {
                                        override fun onCloseWindow(window: WebView?) {
                                            popupWebView?.destroy()
                                            popupWebView = null
                                        }
                                    }
                                }
                                popupWebView = newWebView
                                val transport = resultMsg?.obj as? WebView.WebViewTransport
                                transport?.webView = newWebView
                                resultMsg?.sendToTarget()
                                return true
                            }

                            override fun onCloseWindow(window: WebView?) {
                                popupWebView?.destroy()
                                popupWebView = null
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                return false
                            }
                        }

                        loadDataWithBaseURL(
                            "https://${MaxbirdPortalSettings.FIREBASE_AUTH_DOMAIN}/",
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                }
            )

            // Render OAuth Popup Window overlay when signInWithPopup opens a window
            popupWebView?.let { activePopup ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Slate900,
                    border = BorderStroke(1.5.dp, NeonCyan)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate950)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔐 Google Admin Sign-In (${MaxbirdPortalSettings.ADMIN_EMAIL})",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = {
                                    activePopup.destroy()
                                    popupWebView = null
                                }
                            ) {
                                Text("বন্ধ করুন ✕", color = CoralDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { activePopup }
                        )
                    }
                }
            }
        }
    }
}

private fun buildMaxbirdFirebaseWebDashboardHtml(): String = """
<!DOCTYPE html>
<html lang="bn">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Maxbird Admin Control Panel</title>
  <style>
    * { box-sizing: border-box; font-family: system-ui, -apple-system, sans-serif; }
    body { margin: 0; padding: 14px; background: #0f172a; color: #f8fafc; }
    .card { background: #1e293b; border: 1px solid #334155; border-radius: 14px; padding: 14px; margin-bottom: 14px; }
    .title { font-size: 16px; font-weight: 800; color: #38bdf8; margin-bottom: 6px; }
    .sub { font-size: 12px; color: #94a3b8; margin-bottom: 10px; }
    label { display: block; font-size: 12px; color: #cbd5e1; margin-top: 8px; margin-bottom: 4px; font-weight: 600; }
    input, textarea { width: 100%; padding: 10px; border-radius: 8px; border: 1px solid #475569; background: #0f172a; color: #fff; font-size: 13px; }
    button { width: 100%; padding: 11px; border-radius: 10px; border: none; font-weight: 700; font-size: 13px; cursor: pointer; margin-top: 8px; }
    .btn-blue { background: #2563eb; color: #fff; }
    .btn-green { background: #10b981; color: #fff; }
    .btn-red { background: #ef4444; color: #fff; }
    .status { padding: 10px; border-radius: 8px; background: #064e3b; color: #6ee7b7; font-size: 12px; margin-top: 8px; display: none; }
    .row { display: flex; gap: 8px; }
    .row > div { flex: 1; }
  </style>
</head>
<body>
  <div class="card">
    <div class="title">Maxbird Admin Control Panel (Web SDK)</div>
    <div class="sub">Database: ai-studio-edusmartappporta-6327637e-4b8d-4e4e-be1d-92bc33269fa0 • Doc: portal_settings/main</div>
    <div id="authStatus" class="sub">লগইন স্ট্যাটাস চেক করা হচ্ছে...</div>
    <button id="btnGoogle" class="btn-blue">Google দিয়ে লগইন করুন (maiafahim1234@gmail.com)</button>
    <div class="row" style="margin-top:8px;">
      <div><input id="authEmail" value="maiafahim1234@gmail.com" placeholder="Admin Email" /></div>
      <div><input id="authPass" type="password" placeholder="Password (কমপক্ষে ৬ অক্ষর)" /></div>
    </div>
    <button id="btnEmailLogin" class="btn-green">ইমেইল ও পাসওয়ার্ড দিয়ে লগইন / অটো সেটআপ (Sync Token to App)</button>
    <button id="btnSignOut" class="btn-red" style="display:none;">Sign Out</button>
    <div id="syncBanner" class="status"></div>
  </div>

  <div class="card">
    <div class="title">লাইভ ওয়েবসাইট সেটিংস (onSnapshot Live Sync)</div>
    <div class="row">
      <div><label>App Name</label><input id="f_appName" value="Maxbird" /></div>
      <div><label>Version</label><input id="f_appVersion" value="v1.0.0" /></div>
    </div>
    <div class="row">
      <div><label>File Size</label><input id="f_fileSize" value="10 MB" /></div>
      <div><label>Download Count</label><input id="f_downloadCount" type="number" value="0" /></div>
    </div>
    <label>R2 Bucket Public Domain</label>
    <input id="f_r2BucketPublicDomain" placeholder="pub-xxx.r2.dev" />
    <label>R2 Object Key (.apk filename)</label>
    <input id="f_r2ObjectKey" placeholder="maxbird-v1.0.0.apk" />
    <label>Direct APK Download URL (apkDownloadUrl)</label>
    <input id="f_apkDownloadUrl" placeholder="https://..." />
    <label>Top Notice Banner (noticeText)</label>
    <textarea id="f_noticeText" rows="2"></textarea>
    <label>Release Notes (releaseNotes)</label>
    <textarea id="f_releaseNotes" rows="3"></textarea>
    <div class="row">
      <div><label>Telegram Handle</label><input id="f_telegramHandle" value="@Fahim_Miya_F" /></div>
      <div><label>Telegram URL</label><input id="f_telegramUrl" value="https://t.me/Fahim_Miya_F" /></div>
    </div>
    <div class="row">
      <div><label>Min Android</label><input id="f_minAndroidVersion" value="Android 7.0+" /></div>
      <div><label>Last Updated</label><input id="f_lastUpdated" value="নভেম্বর ২০২৬" /></div>
    </div>
    <label>SHA-256 / Security Badge (sha256Short)</label>
    <input id="f_sha256Short" value="Play Protect Verified" />
    <button id="btnSaveDoc" class="btn-green">লাইভ ওয়েবসাইটে সেভ করুন (Save to portal_settings/main)</button>
  </div>

  <script type="module">
    import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
    import { getAuth, GoogleAuthProvider, signInWithPopup, signInWithEmailAndPassword, createUserWithEmailAndPassword, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
    import { getFirestore, doc, onSnapshot, getDoc, setDoc, updateDoc, serverTimestamp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";

    const firebaseConfig = {
      projectId: "empirical-envelope-b0z9n",
      appId: "1:164560756368:web:584b3905654b76c8e1c215",
      apiKey: "AIzaSyBzktkBCXHoSNaTfCJ39Lu3ibynDhX5Iu8",
      authDomain: "empirical-envelope-b0z9n.firebaseapp.com"
    };

    const app = initializeApp(firebaseConfig);
    const auth = getAuth(app);
    const db = getFirestore(app, "ai-studio-edusmartappporta-6327637e-4b8d-4e4e-be1d-92bc33269fa0");
    const settingsRef = doc(db, "portal_settings", "main");

    let currentSnapData = null;

    function showBanner(msg) {
      const el = document.getElementById("syncBanner");
      el.style.display = "block";
      el.textContent = msg;
    }

    onAuthStateChanged(auth, async (user) => {
      const statusEl = document.getElementById("authStatus");
      const btnOut = document.getElementById("btnSignOut");
      if (user) {
        statusEl.textContent = "✅ লগইনকৃত অ্যাডমিন: " + user.email + " (UID: " + user.uid + ")";
        btnOut.style.display = "block";
        const idToken = await user.getIdToken();
        if (window.AndroidBridge && window.AndroidBridge.onFirebaseUserSignedIn) {
          window.AndroidBridge.onFirebaseUserSignedIn(
            user.uid || "",
            user.email || "",
            user.displayName || "Maxbird Admin",
            user.photoURL || "",
            idToken || "",
            user.refreshToken || ""
          );
        }
      } else {
        statusEl.textContent = "🔒 এখনো লগইন করা হয়নি (maiafahim1234@gmail.com দিয়ে লগইন করুন)";
        btnOut.style.display = "none";
      }
    });

    onSnapshot(settingsRef, (snap) => {
      if (snap.exists()) {
        const d = snap.data();
        currentSnapData = d;
        document.getElementById("f_appName").value = d.appName || "Maxbird";
        document.getElementById("f_appVersion").value = d.appVersion || "v1.0.0";
        document.getElementById("f_fileSize").value = d.fileSize || "10 MB";
        document.getElementById("f_downloadCount").value = d.downloadCount ?? 0;
        document.getElementById("f_r2BucketPublicDomain").value = d.r2BucketPublicDomain || "";
        document.getElementById("f_r2ObjectKey").value = d.r2ObjectKey || "";
        document.getElementById("f_apkDownloadUrl").value = d.apkDownloadUrl || "";
        document.getElementById("f_noticeText").value = d.noticeText || "";
        document.getElementById("f_releaseNotes").value = d.releaseNotes || "";
        document.getElementById("f_telegramHandle").value = d.telegramHandle || "@Fahim_Miya_F";
        document.getElementById("f_telegramUrl").value = d.telegramUrl || "https://t.me/Fahim_Miya_F";
        document.getElementById("f_minAndroidVersion").value = d.minAndroidVersion || "Android 7.0+";
        document.getElementById("f_lastUpdated").value = d.lastUpdated || "নভেম্বর ২০২৬";
        document.getElementById("f_sha256Short").value = d.sha256Short || "Play Protect Verified";
      }
    });

    document.getElementById("btnGoogle").addEventListener("click", async () => {
      try {
        const provider = new GoogleAuthProvider();
        provider.setCustomParameters({ login_hint: "maiafahim1234@gmail.com", prompt: "select_account" });
        await signInWithPopup(auth, provider);
        showBanner("✅ Google লগইন সফল হয়েছে এবং অ্যাপের সাথে সিঙ্ক হয়েছে!");
      } catch (err) {
        showBanner("💡 Google Popup ব্লক হলে নিচের বক্সে পাসওয়ার্ড দিয়ে 'ইমেইল ও পাসওয়ার্ড দিয়ে লগইন' বাটনে চাপুন: " + err.message);
      }
    });

    document.getElementById("btnEmailLogin").addEventListener("click", async () => {
      const email = document.getElementById("authEmail").value.trim();
      const pass = document.getElementById("authPass").value;
      if (!email || !pass || pass.length < 6) {
        showBanner("⚠️ অনুগ্রহ করে ইমেইল এবং কমপক্ষে ৬ অক্ষরের পাসওয়ার্ড দিন।");
        return;
      }
      try {
        await signInWithEmailAndPassword(auth, email, pass);
        showBanner("✅ সফলভাবে লগইন হয়েছে এবং নেটিভ অ্যাপের সাথে টোকেন সিঙ্ক হয়েছে!");
      } catch (e) {
        if (email.toLowerCase() === "maiafahim1234@gmail.com" && (e.code === "auth/user-not-found" || e.code === "auth/invalid-credential")) {
          try {
            await createUserWithEmailAndPassword(auth, email, pass);
            showBanner("✅ অ্যাডমিন পাসওয়ার্ড সেটআপ ও লগইন সফল হয়েছে!");
            return;
          } catch (signupErr) {
            showBanner("⚠️ লগইন ত্রুটি: " + signupErr.message);
            return;
          }
        }
        showBanner("⚠️ লগইন ত্রুটি: " + e.message);
      }
    });

    document.getElementById("btnSignOut").addEventListener("click", async () => {
      await signOut(auth);
    });

    document.getElementById("btnSaveDoc").addEventListener("click", async () => {
      try {
        const user = auth.currentUser;
        if (!user) {
          showBanner("⚠️ সেভ করার আগে maiafahim1234@gmail.com দিয়ে লগইন করুন।");
          return;
        }
        const snap = await getDoc(settingsRef);
        const exists = snap.exists();
        const existingData = exists ? snap.data() : null;

        const payload = {
          appName: document.getElementById("f_appName").value.trim() || "Maxbird",
          apkDownloadUrl: document.getElementById("f_apkDownloadUrl").value.trim(),
          r2BucketPublicDomain: document.getElementById("f_r2BucketPublicDomain").value.trim(),
          r2ObjectKey: document.getElementById("f_r2ObjectKey").value.trim(),
          appVersion: document.getElementById("f_appVersion").value.trim() || "v1.0.0",
          fileSize: document.getElementById("f_fileSize").value.trim() || "10 MB",
          telegramHandle: document.getElementById("f_telegramHandle").value.trim() || "@Fahim_Miya_F",
          telegramUrl: document.getElementById("f_telegramUrl").value.trim() || "https://t.me/Fahim_Miya_F",
          noticeText: document.getElementById("f_noticeText").value.trim() || "Maxbird অফিশিয়াল ওয়েবসাইট",
          releaseNotes: document.getElementById("f_releaseNotes").value.trim(),
          downloadCount: Math.max(0, Math.min(100000000, Math.round(Number(document.getElementById("f_downloadCount").value || 0)))),
          minAndroidVersion: document.getElementById("f_minAndroidVersion").value.trim() || "Android 7.0+",
          lastUpdated: document.getElementById("f_lastUpdated").value.trim() || "নভেম্বর ২০২৬",
          sha256Short: document.getElementById("f_sha256Short").value.trim() || "Play Protect Verified",
          ownerId: (exists && existingData && existingData.ownerId) ? existingData.ownerId : user.uid,
          createdAt: (exists && existingData && existingData.createdAt) ? existingData.createdAt : serverTimestamp(),
          updatedAt: serverTimestamp()
        };

        if (exists) {
          await updateDoc(settingsRef, payload);
        } else {
          await setDoc(settingsRef, payload);
        }
        showBanner("✅ সফলভাবে portal_settings/main আপডেট হয়েছে এবং maxbird.pages.dev-এ লাইভ হয়েছে!");
      } catch (e) {
        showBanner("⚠️ সেভ ত্রুটি: " + e.message);
      }
    });
  </script>
</body>
</html>
""".trimIndent()

@Composable
private fun adminTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Slate900,
    unfocusedContainerColor = Slate900,
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = Slate700,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Slate400,
    cursorColor = NeonCyan
)
