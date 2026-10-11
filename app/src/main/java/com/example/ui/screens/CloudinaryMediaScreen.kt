package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.CloudinaryClient
import com.example.data.model.CloudinaryHistoryItem
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

/**
 * Shikho Cloudinary Media Uploader Screen
 * Direct unrestricted upload tool to Shikho's Cloudinary storage
 * Supports Image, Video, and PDF with permanent direct links.
 */
@Composable
fun CloudinaryMediaScreen(
    selectedUri: Uri?,
    fileMeta: CloudinaryClient.FileMeta?,
    isUploading: Boolean,
    lastUploadedUrl: String?,
    uploadHistory: List<CloudinaryHistoryItem>,
    onSelectFile: (Uri?) -> Unit,
    onClearSelectedFile: () -> Unit,
    onUploadClick: () -> Unit,
    onRemoveHistoryItem: (CloudinaryHistoryItem) -> Unit,
    onShowToast: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    // File picker launcher supporting Image, Video, and PDF (auto mime types)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onSelectFile(uri)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Shikho Cloudinary Media Uploader",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "আনলিমিটেড ও সরাসরি মিডিয়া আপলোডার টুল",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldSuccess.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                        ) {
                            Text(
                                text = "Active API",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = "কোনো লগইন বা অ্যাকাউন্ট ছাড়াই Shikho-এর অফিশিয়াল ক্লাউডনারিতে ছবি, ভিডিও বা পিডিএফ আপলোড করুন এবং যেকোনো ব্যানার বা নোটিশে ব্যবহারের জন্য চিরস্থায়ী ডিরেক্ট লিংক সংগ্রহ করুন।",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate400,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // 2. File Selection & Upload Card
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
                    Text(
                        text = "১. ফাইল নির্বাচন করুন",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    if (selectedUri == null) {
                        // Empty State: Choose File Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate900)
                                .border(1.5.dp, Slate700, RoundedCornerShape(12.dp))
                                .clickable {
                                    // Open file picker (*/* allows images, pdf, mp4, etc.)
                                    filePickerLauncher.launch("*/*")
                                }
                                .testTag("btn_select_media_picker"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Select File",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "ফাইল সিলেক্ট করুন (Image, PDF, Video)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "এখানে ট্যাপ করে গ্যালারি বা ফাইল ম্যানেজার থেকে ফাইল বেছে নিন",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    } else {
                        // File Selected State with Preview & Info Card
                        val isImage = fileMeta?.mimeType?.startsWith("image") == true
                        val isPdf = fileMeta?.mimeType?.contains("pdf") == true || fileMeta?.name?.endsWith(".pdf", ignoreCase = true) == true
                        val isVideo = fileMeta?.mimeType?.startsWith("video") == true

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ElectricBlue, RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                        val icon = when {
                                            isImage -> Icons.Default.Image
                                            isPdf -> Icons.Default.PictureAsPdf
                                            isVideo -> Icons.Default.VideoFile
                                            else -> Icons.Default.Description
                                        }
                                        val iconColor = when {
                                            isImage -> NeonCyan
                                            isPdf -> CoralDanger
                                            isVideo -> ElectricBlueLight
                                            else -> Slate400
                                        }

                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = fileMeta?.name ?: "নির্বাচিত ফাইল",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                ),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${fileMeta?.mimeType ?: "মিডিয়া"} • ${formatBytes(fileMeta?.size ?: 0L)}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Slate400
                                                )
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = onClearSelectedFile,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Selection",
                                            tint = Slate400
                                        )
                                    }
                                }

                                // Image Preview if selected file is image
                                if (isImage) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Slate950)
                                            .border(1.dp, Slate700, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = selectedUri,
                                            contentDescription = "Image Preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { filePickerLauncher.launch("*/*") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Slate200
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("অন্য ফাইল নিন", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = onUploadClick,
                                        enabled = !isUploading,
                                        modifier = Modifier
                                            .weight(1.4f)
                                            .testTag("btn_upload_to_cloud"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricBlue,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        if (isUploading) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("আপলোড হচ্ছে...", fontSize = 13.sp)
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CloudUpload,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Upload to Cloud", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Last Uploaded Direct Link Result Card (Permanent secure_url)
        val currentUploadedUrl = lastUploadedUrl
        if (!currentUploadedUrl.isNullOrBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, EmeraldSuccess, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "আপলোড সফল হয়েছে! (Permanent Direct Link)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                            )
                        }

                        Text(
                            text = "এই লিংকটি সরাসরি যেকোনো নোটিশ ব্যানার, সাপোর্ট লিংক বা ব্রাউজারে ব্যবহার করুন:",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )

                        // Read-Only Direct URL Box
                        OutlinedTextField(
                            value = currentUploadedUrl,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("field_secure_url_result"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = NeonCyan
                                )
                            },
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900,
                                focusedBorderColor = EmeraldSuccess,
                                unfocusedBorderColor = Slate700
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Big Prominent "Copy Link" Button
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentUploadedUrl))
                                onShowToast("ডিরেক্ট লিংক কপি হয়েছে!")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_copy_direct_link"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Link",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Copy Link (ডিরেক্ট লিংক কপি করুন)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Recent Upload History Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "রিসেন্ট আপলোড হিস্ট্রি (${uploadHistory.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                if (uploadHistory.isNotEmpty()) {
                    Text(
                        text = "সর্বশেষ আপলোডসমূহ",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                    )
                }
            }
        }

        if (uploadHistory.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "এখনো কোনো ফাইল আপলোড করা হয়নি",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Slate200,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "উপরের বাটন দিয়ে ফাইল সিলেক্ট করে আপলোড করলেই হিস্ট্রি এখানে জমা হবে।",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        } else {
            items(uploadHistory, key = { it.id }) { item ->
                UploadHistoryItemCard(
                    item = item,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(item.secureUrl))
                        onShowToast("ডিরেক্ট লিংক কপি হয়েছে!")
                    },
                    onDelete = { onRemoveHistoryItem(item) }
                )
            }
        }
    }
}

@Composable
private fun UploadHistoryItemCard(
    item: CloudinaryHistoryItem,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
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
                    modifier = Modifier.weight(1f)
                ) {
                    val icon = when {
                        item.resourceType == "image" || item.mimeType.startsWith("image") -> Icons.Default.Image
                        item.resourceType == "video" || item.mimeType.startsWith("video") -> Icons.Default.VideoFile
                        item.mimeType.contains("pdf") -> Icons.Default.PictureAsPdf
                        else -> Icons.Default.Description
                    }
                    val iconTint = when {
                        item.resourceType == "image" || item.mimeType.startsWith("image") -> NeonCyan
                        item.resourceType == "video" || item.mimeType.startsWith("video") -> ElectricBlueLight
                        item.mimeType.contains("pdf") -> CoralDanger
                        else -> Slate400
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate900),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = item.fileName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "${item.fileSizeFormatted} • ${item.uploadedAtFormatted}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Slate700)
                            .testTag("btn_copy_history_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Link",
                            tint = ElectricBlueLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Slate700)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove History Item",
                            tint = CoralDanger,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Url preview pill
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Slate900,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCopy() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.secureUrl,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate200,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(java.util.Locale.US, "%.2f MB", mb)
    } else if (kb >= 1.0) {
        String.format(java.util.Locale.US, "%.1f KB", kb)
    } else {
        "$bytes B"
    }
}
