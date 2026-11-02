package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.AsyncImage
import com.example.data.api.CloudflareR2Client
import com.example.data.model.MaxbirdAdminAuthSession
import com.example.data.model.MaxbirdPortalSettings
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PurpleRoyal
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Upgraded Maxbird Website Admin Panel (`https://maxbird.pages.dev`).
 * Controls:
 * 1. Direct Cloudflare R2 (`maxbird` bucket) APK file upload, replace & delete with live progress bar
 * 2. Play Store-style App Icon (`appIconUrl`) & Screenshots Gallery (`appScreenshots`, up to 15 images)
 *    with upload, replace, delete (from both R2 and Firestore), reorder, and manual URL entry
 * 3. All 18 required `portal_settings/main` fields saved atomically via `writeBatch` + Secret Key (`Maxbird_Secret_Fahim_2026`)
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
    val coroutineScope = rememberCoroutineScope()
    val r2Client = remember(context) { CloudflareR2Client(context) }

    // Secret key visibility toggle
    var secretKeyVisible by remember { mutableStateOf(false) }

    // R2 APK Upload State
    var isUploadingApk by remember { mutableStateOf(false) }
    var apkUploadProgress by remember { mutableIntStateOf(0) }
    var apkUploadStatusText by remember { mutableStateOf("") }
    var autoReplaceOldApkInR2 by remember { mutableStateOf(true) }

    // R2 Icon & Screenshot Upload State
    var isUploadingMedia by remember { mutableStateOf(false) }
    var mediaUploadStatusText by remember { mutableStateOf("") }
    var screenshotIndexToReplace by remember { mutableIntStateOf(-1) }
    var manualScreenshotUrlInput by remember { mutableStateOf("") }

    // 1. Launcher: Direct APK Upload to Cloudflare R2 (`apk/Maxbird-vX.X.apk`)
    val r2ApkUploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isUploadingApk = true
                apkUploadProgress = 0
                val meta = r2Client.inspectUriMetadata(uri)
                val cleanVersion = settings.appVersion
                    .trim()
                    .ifEmpty { "v1.0.0" }
                    .replace(Regex("[^a-zA-Z0-9._-]"), "_")
                val targetKey = "apk/Maxbird-$cleanVersion-${System.currentTimeMillis() / 1000}.apk"
                val previousApkUrl = settings.apkDownloadUrl

                apkUploadStatusText = "প্রস্তুত করা হচ্ছে: ${meta.fileName} (${meta.fileSizeFormatted})..."

                val uploadRes = r2Client.uploadUriToR2(
                    uri = uri,
                    targetFileKey = targetKey,
                    contentTypeOverride = "application/vnd.android.package-archive",
                    onProgress = { pct, uploaded, total ->
                        apkUploadProgress = pct
                        val upMb = String.format(Locale.US, "%.1f", uploaded / (1024.0 * 1024.0))
                        val totMb = String.format(Locale.US, "%.1f", total / (1024.0 * 1024.0))
                        apkUploadStatusText = "R2-তে APK আপলোড হচ্ছে: $pct% ($upMb MB / $totMb MB)"
                    }
                )

                if (uploadRes.isSuccess) {
                    val info = uploadRes.getOrNull()!!
                    // If autoReplaceOldApkInR2 is enabled and old URL was on R2, delete old APK object from R2
                    if (autoReplaceOldApkInR2 &&
                        previousApkUrl.isNotBlank() &&
                        previousApkUrl != info.publicUrl &&
                        CloudflareR2Client.extractFileKeyFromPublicUrl(previousApkUrl) != null
                    ) {
                        apkUploadStatusText = "পুরনো APK ফাইল R2 থেকে ডিলিট করা হচ্ছে..."
                        r2Client.deleteObjectFromR2(previousApkUrl)
                    }

                    val updatedSettings = settings.copy(
                        apkDownloadUrl = info.publicUrl,
                        appSize = info.fileSizeFormatted,
                        r2ObjectKey = info.fileKey
                    )
                    onUpdateDraft(updatedSettings)
                    onSaveToFirestore(updatedSettings)
                    apkUploadStatusText = "✅ APK সফলভাবে R2-তে আপলোড ও ওয়েবসাইটে লাইভ সেভ হয়েছে!"
                    onShowToast("✅ নতুন APK আপলোড সম্পন্ন (${info.fileSizeFormatted})!")
                } else {
                    val err = uploadRes.exceptionOrNull()?.message ?: "APK আপলোড ব্যর্থ হয়েছে"
                    apkUploadStatusText = "❌ $err"
                    onShowToast("⚠️ $err")
                }
                isUploadingApk = false
            }
        }
    }

    // 2. Launcher: Upload App Icon (`icons/icon-xxx.png`) to Cloudflare R2
    val r2IconUploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isUploadingMedia = true
                val meta = r2Client.inspectUriMetadata(uri)
                val ext = meta.fileName.substringAfterLast('.', "png").lowercase(Locale.US).take(4)
                val targetKey = "icons/app-icon-${System.currentTimeMillis()}.$ext"
                val oldIconUrl = settings.appIconUrl
                mediaUploadStatusText = "অ্যাপ আইকন R2-তে আপলোড হচ্ছে..."

                val res = r2Client.uploadUriToR2(
                    uri = uri,
                    targetFileKey = targetKey,
                    onProgress = { pct, _, _ ->
                        mediaUploadStatusText = "অ্যাপ আইকন আপলোড হচ্ছে ($pct%)..."
                    }
                )
                if (res.isSuccess) {
                    val info = res.getOrNull()!!
                    if (oldIconUrl.isNotBlank() &&
                        oldIconUrl != info.publicUrl &&
                        CloudflareR2Client.extractFileKeyFromPublicUrl(oldIconUrl) != null
                    ) {
                        r2Client.deleteObjectFromR2(oldIconUrl)
                    }
                    val updated = settings.copy(appIconUrl = info.publicUrl)
                    onUpdateDraft(updated)
                    onSaveToFirestore(updated)
                    mediaUploadStatusText = "✅ অ্যাপ আইকন আপলোড ও সেভ সম্পন্ন!"
                    onShowToast("✅ নতুন অ্যাপ আইকন আপলোড হয়েছে!")
                } else {
                    val err = res.exceptionOrNull()?.message ?: "আইকন আপলোড ব্যর্থ"
                    mediaUploadStatusText = "❌ $err"
                    onShowToast("⚠️ $err")
                }
                isUploadingMedia = false
            }
        }
    }

    // 3. Launcher: Upload Multiple Screenshots (`screenshots/screen-xxx.jpg`) to Cloudflare R2
    val r2MultipleScreenshotsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch {
                val currentList = settings.appScreenshots.toMutableList()
                val remainingSlots = (MaxbirdPortalSettings.MAX_SCREENSHOTS - currentList.size).coerceAtLeast(0)
                if (remainingSlots <= 0) {
                    onShowToast("⚠️ সর্বোচ্চ ১৫টি স্ক্রিনশট যোগ করা যায়।")
                    return@launch
                }
                val toUpload = uris.take(remainingSlots)
                isUploadingMedia = true
                var uploadedCount = 0

                for ((idx, uri) in toUpload.withIndex()) {
                    val meta = r2Client.inspectUriMetadata(uri)
                    val ext = meta.fileName.substringAfterLast('.', "jpg").lowercase(Locale.US).take(4)
                    val targetKey = "screenshots/screenshot-${System.currentTimeMillis()}-$idx.$ext"
                    mediaUploadStatusText = "স্ক্রিনশট আপলোড হচ্ছে (${idx + 1}/${toUpload.size})..."

                    val res = r2Client.uploadUriToR2(uri = uri, targetFileKey = targetKey)
                    if (res.isSuccess) {
                        currentList.add(res.getOrNull()!!.publicUrl)
                        uploadedCount++
                    }
                }

                if (uploadedCount > 0) {
                    val updated = settings.copy(appScreenshots = currentList.take(MaxbirdPortalSettings.MAX_SCREENSHOTS))
                    onUpdateDraft(updated)
                    onSaveToFirestore(updated)
                    mediaUploadStatusText = "✅ $uploadedCount টি স্ক্রিনশট R2-তে আপলোড ও সেভ হয়েছে!"
                    onShowToast("✅ $uploadedCount টি স্ক্রিনশট যুক্ত হয়েছে!")
                } else {
                    mediaUploadStatusText = "❌ স্ক্রিনশট আপলোড ব্যর্থ হয়েছে।"
                }
                isUploadingMedia = false
            }
        }
    }

    // 4. Launcher: Replace a Single Screenshot at `screenshotIndexToReplace`
    val r2ReplaceSingleScreenshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val replaceIdx = screenshotIndexToReplace
        if (uri != null && replaceIdx in settings.appScreenshots.indices) {
            coroutineScope.launch {
                isUploadingMedia = true
                val oldUrl = settings.appScreenshots[replaceIdx]
                val meta = r2Client.inspectUriMetadata(uri)
                val ext = meta.fileName.substringAfterLast('.', "jpg").lowercase(Locale.US).take(4)
                val targetKey = "screenshots/screenshot-${System.currentTimeMillis()}-replace-$replaceIdx.$ext"
                mediaUploadStatusText = "স্ক্রিনশট #${replaceIdx + 1} রিপ্লেস হচ্ছে..."

                val res = r2Client.uploadUriToR2(uri = uri, targetFileKey = targetKey)
                if (res.isSuccess) {
                    val newUrl = res.getOrNull()!!.publicUrl
                    if (CloudflareR2Client.extractFileKeyFromPublicUrl(oldUrl) != null) {
                        r2Client.deleteObjectFromR2(oldUrl)
                    }
                    val mutable = settings.appScreenshots.toMutableList()
                    if (replaceIdx in mutable.indices) {
                        mutable[replaceIdx] = newUrl
                    }
                    val updated = settings.copy(appScreenshots = mutable)
                    onUpdateDraft(updated)
                    onSaveToFirestore(updated)
                    mediaUploadStatusText = "✅ স্ক্রিনশট #${replaceIdx + 1} সফলভাবে রিপ্লেস হয়েছে!"
                    onShowToast("✅ স্ক্রিনশট #${replaceIdx + 1} রিপ্লেস সম্পন্ন!")
                } else {
                    val err = res.exceptionOrNull()?.message ?: "রিপ্লেস ব্যর্থ"
                    mediaUploadStatusText = "❌ $err"
                    onShowToast("⚠️ $err")
                }
                screenshotIndexToReplace = -1
                isUploadingMedia = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // =====================================================================
        // TOP HERO CARD + BIG "💾 সব পরিবর্তন ওয়েবসাইটে সেভ করুন" BUTTON
        // =====================================================================
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(EmeraldSuccess, NeonCyan)),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(EmeraldSuccess.copy(alpha = 0.15f), Slate850)
                            )
                        )
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(listOf(EmeraldSuccess, ElectricBlue))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Maxbird ওয়েবসাইট ও R2 কন্ট্রোল",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = EmeraldSuccess.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldSuccess)
                                        )
                                        Text(
                                            text = "ডাটাবেজ ও R2 কানেক্টেড (Live Sync)",
                                            color = EmeraldSuccess,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = onRefreshFromFirestore,
                            modifier = Modifier
                                .size(38.dp)
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

                    // Prominent Top Save Button: "💾 সব পরিবর্তন ওয়েবসাইটে সেভ করুন"
                    Button(
                        onClick = { onSaveToFirestore(settings) },
                        enabled = !isSaving && !isUploadingApk && !isUploadingMedia,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            disabledContainerColor = Slate700
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_top_save_maxbird_portal_settings")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ওয়েবসাইটে লাইভ সেভ হচ্ছে...",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "💾 সব পরিবর্তন ওয়েবসাইটে সেভ করুন",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Live Website Preview Link Button
                    OutlinedButton(
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(MaxbirdPortalSettings.PUBLIC_WEBSITE_URL))
                                )
                            } catch (_: Exception) {
                                onShowToast("ব্রাউজার ওপেন করা যায়নি")
                            }
                        },
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_open_maxbird_website")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "পাবলিক ওয়েবসাইট দেখুন (maxbird.pages.dev)",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
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

        // =====================================================================
        // SECTION 1: APK ফাইল ম্যানেজার (Cloudflare R2 + Firebase)
        // =====================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
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
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "১. APK ফাইল ম্যানেজার (Cloudflare R2 + Firebase)",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "সরাসরি R2 বাকেটে (apk/Maxbird-vX.X.apk) নতুন APK আপলোড, রিপ্লেস ও ডিলিট করুন",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Toggle: Auto-delete old APK from R2 when uploading a new one
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate900,
                        border = BorderStroke(1.dp, Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "পুরনো APK অটো-রিপ্লেস (R2 থেকে ডিলিট)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "নতুন APK আপলোড হলে আগের APK ফাইলটি R2 বাকেট থেকে মুছে যাবে",
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = autoReplaceOldApkInR2,
                                onCheckedChange = { autoReplaceOldApkInR2 = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldSuccess
                                )
                            )
                        }
                    }

                    // Primary R2 APK Upload Button
                    Button(
                        onClick = { r2ApkUploadLauncher.launch("*/*") },
                        enabled = !isUploadingApk,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_upload_apk_to_r2")
                    ) {
                        if (isUploadingApk) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "R2-তে APK আপলোড হচ্ছে ($apkUploadProgress%)...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📤 নতুন .APK সিলেক্ট ও সরাসরি R2-তে আপলোড করুন",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Live Progress Bar & Status Message for APK Upload
                    AnimatedVisibility(visible = isUploadingApk || apkUploadStatusText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate950,
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = apkUploadStatusText,
                                    color = NeonCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isUploadingApk) {
                                    LinearProgressIndicator(
                                        progress = { (apkUploadProgress / 100f).coerceIn(0f, 1f) },
                                        color = EmeraldSuccess,
                                        trackColor = Slate800,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }

                    // Manual APK Download URL Field
                    OutlinedTextField(
                        value = settings.apkDownloadUrl,
                        onValueChange = { onUpdateDraft(settings.copy(apkDownloadUrl = it)) },
                        label = { Text("পাবলিক APK ডাউনলোড লিংক (apkDownloadUrl)") },
                        placeholder = { Text("${CloudflareR2Client.PUBLIC_BASE_URL}/apk/Maxbird-v1.0.apk") },
                        singleLine = true,
                        trailingIcon = {
                            if (settings.apkDownloadUrl.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(settings.apkDownloadUrl))
                                        onShowToast("APK লিংক কপি হয়েছে!")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy APK URL",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_maxbird_apk_download_url")
                    )

                    // Row: Test Download Link + Delete APK from R2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val url = settings.apkDownloadUrl.trim()
                                if (url.startsWith("http://") || url.startsWith("https://")) {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    } catch (_: Exception) {
                                        onShowToast("লিংক ওপেন করা যায়নি")
                                    }
                                } else {
                                    onShowToast("কোনো APK লিংক সেট করা নেই")
                                }
                            },
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ডাউনলোড টেস্ট", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (CloudflareR2Client.extractFileKeyFromPublicUrl(settings.apkDownloadUrl) != null) {
                            OutlinedButton(
                                onClick = {
                                    val urlToDelete = settings.apkDownloadUrl
                                    coroutineScope.launch {
                                        isUploadingApk = true
                                        apkUploadStatusText = "R2 বাকেট থেকে APK ডিলিট করা হচ্ছে..."
                                        val delRes = r2Client.deleteObjectFromR2(urlToDelete)
                                        if (delRes.isSuccess) {
                                            val updated = settings.copy(apkDownloadUrl = "")
                                            onUpdateDraft(updated)
                                            onSaveToFirestore(updated)
                                            apkUploadStatusText = "🗑️ APK ফাইলটি R2 বাকেট থেকে ডিলিট করা হয়েছে।"
                                            onShowToast("🗑️ R2 থেকে APK ডিলিট সম্পন্ন!")
                                        } else {
                                            apkUploadStatusText = "❌ ডিলিট ব্যর্থ: ${delRes.exceptionOrNull()?.message}"
                                        }
                                        isUploadingApk = false
                                    }
                                },
                                border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.65f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = CoralDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("R2 থেকে APK ডিলিট", color = CoralDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // SECTION 2: পেইজ ও প্লে-স্টোর স্ক্রিনশট গ্যালারি ম্যানেজার (appIconUrl & appScreenshots)
        // =====================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PurpleRoyal.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "২. অ্যাপ আইকন ও প্লে-স্টোর স্ক্রিনশট গ্যালারি (R2)",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "লোগো (appIconUrl) এবং সর্বোচ্চ ১৫টি অ্যাপ স্ক্রিনশট (appScreenshots) নিয়ন্ত্রণ করুন",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (isUploadingMedia || mediaUploadStatusText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate950,
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isUploadingMedia) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = NeonCyan
                                    )
                                }
                                Text(
                                    text = mediaUploadStatusText,
                                    color = NeonCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // 2A. App Icon (`appIconUrl`) Manager
                    // ---------------------------------------------------------
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Slate900,
                        border = BorderStroke(1.dp, Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "অ্যাপ লোগো / আইকন (appIconUrl)",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Icon Preview Box
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Slate950)
                                        .border(1.dp, Slate700, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (settings.appIconUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = settings.appIconUrl,
                                            contentDescription = "App Icon",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Android,
                                            contentDescription = null,
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { r2IconUploadLauncher.launch("image/*") },
                                        enabled = !isUploadingMedia,
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (settings.appIconUrl.isBlank()) "আইকন আপলোড (R2)" else "আইকন পরিবর্তন (Replace)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (settings.appIconUrl.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                val oldIcon = settings.appIconUrl
                                                coroutineScope.launch {
                                                    isUploadingMedia = true
                                                    if (CloudflareR2Client.extractFileKeyFromPublicUrl(oldIcon) != null) {
                                                        r2Client.deleteObjectFromR2(oldIcon)
                                                    }
                                                    val updated = settings.copy(appIconUrl = "")
                                                    onUpdateDraft(updated)
                                                    onSaveToFirestore(updated)
                                                    mediaUploadStatusText = "🗑️ অ্যাপ আইকন ডিলিট করা হয়েছে।"
                                                    isUploadingMedia = false
                                                }
                                            },
                                            border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.65f)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = CoralDanger,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("আইকন ডিলিট করুন", color = CoralDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = settings.appIconUrl,
                                onValueChange = { onUpdateDraft(settings.copy(appIconUrl = it)) },
                                label = { Text("অ্যাপ আইকন লিংক (appIconUrl)") },
                                placeholder = { Text("${CloudflareR2Client.PUBLIC_BASE_URL}/icons/...") },
                                singleLine = true,
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_maxbird_app_icon_url")
                            )
                        }
                    }

                    HorizontalDivider(color = Slate700)

                    // ---------------------------------------------------------
                    // 2B. Play Store Screenshots Gallery (`appScreenshots` - Max 15)
                    // ---------------------------------------------------------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "স্ক্রিনশট গ্যালারি (${settings.appScreenshots.size}/${MaxbirdPortalSettings.MAX_SCREENSHOTS})",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Play Store স্টাইল প্রিভিউ",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Upload 1 or Multiple Screenshots to R2 Button
                    Button(
                        onClick = { r2MultipleScreenshotsLauncher.launch("image/*") },
                        enabled = !isUploadingMedia && settings.appScreenshots.size < MaxbirdPortalSettings.MAX_SCREENSHOTS,
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleRoyal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_upload_screenshots_r2")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📸 এক বা একাধিক স্ক্রিনশট সিলেক্ট ও R2-তে আপলোড করুন",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Manual Screenshot URL Paste Input + Add Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualScreenshotUrlInput,
                            onValueChange = { manualScreenshotUrlInput = it },
                            label = { Text("সরাসরি ছবির লিংক পেস্ট করে যোগ করুন") },
                            placeholder = { Text("https://.../screenshot.jpg") },
                            singleLine = true,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_manual_screenshot_url")
                        )

                        Button(
                            onClick = {
                                val clean = manualScreenshotUrlInput.trim()
                                if (clean.startsWith("http://") || clean.startsWith("https://")) {
                                    if (settings.appScreenshots.size >= MaxbirdPortalSettings.MAX_SCREENSHOTS) {
                                        onShowToast("সর্বোচ্চ ১৫টি স্ক্রিনশট যোগ করা যাবে")
                                    } else {
                                        val updated = settings.copy(
                                            appScreenshots = (settings.appScreenshots + clean).take(MaxbirdPortalSettings.MAX_SCREENSHOTS)
                                        )
                                        onUpdateDraft(updated)
                                        manualScreenshotUrlInput = ""
                                        onShowToast("✅ স্ক্রিনশট লিংক যুক্ত হয়েছে!")
                                    }
                                } else {
                                    onShowToast("সঠিক ছবির লিংক (https://...) দিন")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add URL",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("যোগ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // List of Screenshot Cards with Preview, Replace, Delete & Up/Down Reorder
                    if (settings.appScreenshots.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Slate900,
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "এখনো কোনো স্ক্রিনশট যোগ করা হয়নি",
                                    color = Slate200,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "উপরের বাটনে ট্যাপ করে অ্যাপের স্ক্রিনশট R2-তে আপলোড করুন",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            settings.appScreenshots.forEachIndexed { index, screenshotUrl ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Slate900,
                                    border = BorderStroke(1.dp, Slate700),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Play Store Style Vertical Phone Screenshot Thumbnail
                                        Box(
                                            modifier = Modifier
                                                .width(68.dp)
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Slate950)
                                                .border(1.dp, Slate700, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = screenshotUrl,
                                                contentDescription = "Screenshot #${index + 1}",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(bottomEnd = 8.dp),
                                                color = Slate950.copy(alpha = 0.85f),
                                                modifier = Modifier.align(Alignment.TopStart)
                                            ) {
                                                Text(
                                                    text = "#${index + 1}",
                                                    color = NeonCyan,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        // Right side controls: URL preview, Reorder Up/Down, Replace, Delete
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = screenshotUrl.substringAfterLast('/'),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = screenshotUrl,
                                                color = Slate400,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // Row 1: Replace & Delete Buttons
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        screenshotIndexToReplace = index
                                                        r2ReplaceSingleScreenshotLauncher.launch("image/*")
                                                    },
                                                    enabled = !isUploadingMedia,
                                                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.65f)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Autorenew,
                                                        contentDescription = "Replace",
                                                        tint = NeonCyan,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "রিপ্লেস",
                                                        color = NeonCyan,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        val urlToRemove = screenshotUrl
                                                        coroutineScope.launch {
                                                            isUploadingMedia = true
                                                            mediaUploadStatusText = "স্ক্রিনশট #${index + 1} ডিলিট হচ্ছে..."
                                                            if (CloudflareR2Client.extractFileKeyFromPublicUrl(urlToRemove) != null) {
                                                                r2Client.deleteObjectFromR2(urlToRemove)
                                                            }
                                                            val updatedList = settings.appScreenshots.toMutableList().apply {
                                                                if (index in indices) removeAt(index)
                                                            }
                                                            val updated = settings.copy(appScreenshots = updatedList)
                                                            onUpdateDraft(updated)
                                                            onSaveToFirestore(updated)
                                                            mediaUploadStatusText = "🗑️ স্ক্রিনশট #${index + 1} R2 ও ডাটাবেজ থেকে ডিলিট হয়েছে।"
                                                            onShowToast("🗑️ স্ক্রিনশট ডিলিট সম্পন্ন!")
                                                            isUploadingMedia = false
                                                        }
                                                    },
                                                    enabled = !isUploadingMedia,
                                                    border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.65f)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = CoralDanger,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "ডিলিট",
                                                        color = CoralDanger,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // Row 2: Reorder Up / Down Buttons
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        if (index > 0) {
                                                            val mutable = settings.appScreenshots.toMutableList()
                                                            val tmp = mutable[index - 1]
                                                            mutable[index - 1] = mutable[index]
                                                            mutable[index] = tmp
                                                            onUpdateDraft(settings.copy(appScreenshots = mutable))
                                                        }
                                                    },
                                                    enabled = index > 0,
                                                    border = BorderStroke(1.dp, Slate700),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowUpward,
                                                        contentDescription = "Move Up",
                                                        tint = if (index > 0) Color.White else Slate600,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "উপরে",
                                                        color = if (index > 0) Color.White else Slate600,
                                                        fontSize = 11.sp
                                                    )
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        if (index < settings.appScreenshots.lastIndex) {
                                                            val mutable = settings.appScreenshots.toMutableList()
                                                            val tmp = mutable[index + 1]
                                                            mutable[index + 1] = mutable[index]
                                                            mutable[index] = tmp
                                                            onUpdateDraft(settings.copy(appScreenshots = mutable))
                                                        }
                                                    },
                                                    enabled = index < settings.appScreenshots.lastIndex,
                                                    border = BorderStroke(1.dp, Slate700),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowDownward,
                                                        contentDescription = "Move Down",
                                                        tint = if (index < settings.appScreenshots.lastIndex) Color.White else Slate600,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "নিচে",
                                                        color = if (index < settings.appScreenshots.lastIndex) Color.White else Slate600,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // SECTION 3: ওয়েবসাইটের সব টেক্সট ও সেটিংস কন্ট্রোল (portal_settings/main)
        // =====================================================================
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
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "৩. ওয়েবসাইটের সব টেক্সট ও সেটিংস (portal_settings/main)",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "সবগুলো ফিল্ড একসাথে লোড ও writeBatch দিয়ে সেভ হবে",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Row 1: appName & developerName
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = settings.appName,
                            onValueChange = { onUpdateDraft(settings.copy(appName = it)) },
                            label = { Text("অ্যাপের নাম (appName)") },
                            placeholder = { Text("Maxbird") },
                            singleLine = true,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_maxbird_app_name")
                        )

                        OutlinedTextField(
                            value = settings.developerName,
                            onValueChange = { onUpdateDraft(settings.copy(developerName = it)) },
                            label = { Text("ডেভেলপার (developerName)") },
                            placeholder = { Text("Maxbird Official") },
                            singleLine = true,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_maxbird_developer_name")
                        )
                    }

                    // Row 2: appTagline
                    OutlinedTextField(
                        value = settings.appTagline,
                        onValueChange = { onUpdateDraft(settings.copy(appTagline = it)) },
                        label = { Text("অ্যাপের ট্যাগলাইন / সাবটাইটেল (appTagline)") },
                        placeholder = { Text("এইচএসসি ও এসএসসি শিক্ষার্থীদের স্মার্ট লার্নিং অ্যাপ") },
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_maxbird_app_tagline")
                    )

                    // Row 3: appVersion & appSize
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = settings.appVersion,
                            onValueChange = { onUpdateDraft(settings.copy(appVersion = it)) },
                            label = { Text("ভার্সন (appVersion)") },
                            placeholder = { Text("v1.0.5") },
                            singleLine = true,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_maxbird_app_version")
                        )

                        OutlinedTextField(
                            value = settings.appSize,
                            onValueChange = { onUpdateDraft(settings.copy(appSize = it)) },
                            label = { Text("অ্যাপ সাইজ (appSize)") },
                            placeholder = { Text("24.5 MB") },
                            singleLine = true,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_maxbird_file_size")
                        )
                    }

                    // Row 4: androidRequirement & lastUpdated
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = settings.androidRequirement,
                            onValueChange = { onUpdateDraft(settings.copy(androidRequirement = it)) },
                            label = { Text("Android ভার্সন (androidRequirement)") },
                            placeholder = { Text("Android 7.0+") },
                            singleLine = true,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_maxbird_min_android")
                        )

                        OutlinedTextField(
                            value = settings.lastUpdated,
                            onValueChange = { onUpdateDraft(settings.copy(lastUpdated = it)) },
                            label = { Text("আপডেটের তারিখ (lastUpdated)") },
                            placeholder = { Text("নভেম্বর ২০২৬") },
                            singleLine = true,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_maxbird_last_updated")
                        )
                    }

                    // Row 5: totalDownloads with +1 and Reset helpers
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Slate900,
                        border = BorderStroke(1.dp, Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "মোট ডাউনলোড (totalDownloads)",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = settings.totalDownloads,
                                    onValueChange = { onUpdateDraft(settings.copy(totalDownloads = it)) },
                                    label = { Text("totalDownloads (যেমন: 10,000+ বা 520)") },
                                    singleLine = true,
                                    colors = adminTextFieldColors(),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("input_maxbird_download_count")
                                )

                                Button(
                                    onClick = {
                                        val currentNum = settings.totalDownloads.filter { it.isDigit() }.toLongOrNull() ?: 0L
                                        onUpdateDraft(settings.copy(totalDownloads = (currentNum + 1L).toString()))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                                    modifier = Modifier.testTag("btn_maxbird_download_plus_one")
                                ) {
                                    Text("+1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onUpdateDraft(settings.copy(totalDownloads = "0"))
                                    },
                                    border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.65f)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                                    modifier = Modifier.testTag("btn_maxbird_download_reset")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = "Reset",
                                        tint = CoralDanger,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Row 6: Notice Banner Switch (`showNoticeBanner`) + Text (`noticeBannerText`)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Slate900,
                        border = BorderStroke(
                            1.dp,
                            if (settings.showNoticeBanner) EmeraldSuccess.copy(alpha = 0.5f) else Slate700
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = if (settings.showNoticeBanner) EmeraldSuccess else Slate400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "ওয়েবসাইট নোটিশ ব্যানার (showNoticeBanner)",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (settings.showNoticeBanner) "চালু আছে (ওয়েবসাইটে নোটিশ দেখাবে)" else "বন্ধ আছে (নোটিশ লুকানো থাকবে)",
                                            color = if (settings.showNoticeBanner) EmeraldSuccess else Slate400,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = settings.showNoticeBanner,
                                    onCheckedChange = { onUpdateDraft(settings.copy(showNoticeBanner = it)) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = EmeraldSuccess
                                    ),
                                    modifier = Modifier.testTag("switch_maxbird_show_notice_banner")
                                )
                            }

                            OutlinedTextField(
                                value = settings.noticeBannerText,
                                onValueChange = { onUpdateDraft(settings.copy(noticeBannerText = it)) },
                                label = { Text("নোটিশ ব্যানার টেক্সট (noticeBannerText)") },
                                placeholder = { Text("Maxbird অফিশিয়াল অ্যাপের নতুন ভার্সন এখন ডাউনলোডের জন্য উন্মুক্ত!") },
                                minLines = 2,
                                maxLines = 4,
                                colors = adminTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_maxbird_notice_text")
                            )
                        }
                    }

                    // Row 7: Release Notes (`releaseNotes`)
                    OutlinedTextField(
                        value = settings.releaseNotes,
                        onValueChange = { onUpdateDraft(settings.copy(releaseNotes = it)) },
                        label = { Text("রিলিজ নোটস / নতুন কী কী যুক্ত হয়েছে (releaseNotes)") },
                        placeholder = { Text("• দ্রুতগতির ভিডিও প্লেয়ার\n• নতুন কোর্স ও বাগ ফিক্স") },
                        minLines = 3,
                        maxLines = 6,
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_maxbird_release_notes")
                    )

                    HorizontalDivider(color = Slate700)

                    // Row 8: Support Contact Fields (`supportPhone`, `supportWhatsApp`, `supportEmail`)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "সাপোর্ট ও যোগাযোগের তথ্য (Support Info)",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedTextField(
                        value = settings.supportPhone,
                        onValueChange = { onUpdateDraft(settings.copy(supportPhone = it)) },
                        label = { Text("সাপোর্ট ফোন নম্বর (supportPhone)") },
                        placeholder = { Text("+8801XXXXXXXXX") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_maxbird_support_phone")
                    )

                    OutlinedTextField(
                        value = settings.supportWhatsApp,
                        onValueChange = { onUpdateDraft(settings.copy(supportWhatsApp = it)) },
                        label = { Text("হোয়াটসঅ্যাপ লিংক বা নম্বর (supportWhatsApp)") },
                        placeholder = { Text("https://wa.me/8801XXXXXXXXX") },
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_maxbird_support_whatsapp")
                    )

                    OutlinedTextField(
                        value = settings.supportEmail,
                        onValueChange = { onUpdateDraft(settings.copy(supportEmail = it)) },
                        label = { Text("সাপোর্ট ইমেইল (supportEmail)") },
                        placeholder = { Text("maiafahim1234@gmail.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_maxbird_support_email")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = settings.telegramHandle,
                            onValueChange = { onUpdateDraft(settings.copy(telegramHandle = it)) },
                            label = { Text("Telegram ID") },
                            placeholder = { Text("@Fahim_Miya_F") },
                            singleLine = true,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_maxbird_telegram_handle")
                        )

                        OutlinedTextField(
                            value = settings.telegramUrl,
                            onValueChange = { onUpdateDraft(settings.copy(telegramUrl = it)) },
                            label = { Text("Telegram লিংক") },
                            placeholder = { Text("https://t.me/Fahim_Miya_F") },
                            singleLine = true,
                            colors = adminTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_maxbird_telegram_url")
                        )
                    }

                    HorizontalDivider(color = Slate700)

                    // Admin Secret Key Input (`Maxbird_Secret_Fahim_2026` pre-filled by default)
                    OutlinedTextField(
                        value = settings.adminSecretKey,
                        onValueChange = { onUpdateDraft(settings.copy(adminSecretKey = it)) },
                        label = { Text("Admin Secret Key (স্বয়ংক্রিয়ভাবে সেট করা আছে)") },
                        placeholder = { Text(MaxbirdPortalSettings.DEFAULT_ADMIN_SECRET_KEY) },
                        singleLine = true,
                        visualTransformation = if (secretKeyVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { secretKeyVisible = !secretKeyVisible }) {
                                Icon(
                                    imageVector = if (secretKeyVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = "Show/Hide Secret Key",
                                    tint = NeonCyan
                                )
                            }
                        },
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_maxbird_admin_secret_key")
                    )
                }
            }
        }

        // =====================================================================
        // BOTTOM BIG SAVE BUTTON: "💾 সব পরিবর্তন ওয়েবসাইটে সেভ করুন"
        // =====================================================================
        item {
            Button(
                onClick = { onSaveToFirestore(settings) },
                enabled = !isSaving && !isUploadingApk && !isUploadingMedia,
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
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ওয়েবসাইটে লাইভ সেভ হচ্ছে...",
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
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "💾 সব পরিবর্তন ওয়েবসাইটে সেভ করুন",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}

@Composable
private fun adminTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Slate900,
    unfocusedContainerColor = Slate900,
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = Slate600,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Slate400,
    cursorColor = NeonCyan
)
