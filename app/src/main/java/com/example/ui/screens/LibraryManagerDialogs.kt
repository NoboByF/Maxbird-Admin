package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.CloudflareR2Client
import com.example.data.model.LibraryChapter
import com.example.data.model.LibraryClass
import com.example.data.model.LibraryHtmlFile
import com.example.data.model.LibrarySubject
import com.example.data.model.LibraryUtils
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

internal fun parseSafeHexColor(hex: String, fallback: Color = EmeraldSuccess): Color {
    return try {
        val cleaned = hex.trim().let { if (it.startsWith("#")) it else "#$it" }
        Color(android.graphics.Color.parseColor(cleaned))
    } catch (_: Exception) {
        fallback
    }
}

@Composable
private fun libraryTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = Slate700,
    focusedContainerColor = Slate900,
    unfocusedContainerColor = Slate900,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Slate400,
    cursorColor = NeonCyan
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorHexPickerRow(
    selectedHex: String,
    onSelectHex: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "থিম কালার (color_hex)",
            color = Slate200,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LibraryUtils.PRESET_COLORS.forEach { preset ->
                val color = parseSafeHexColor(preset)
                val isSelected = selectedHex.equals(preset, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Color.White else Slate700,
                            shape = CircleShape
                        )
                        .clickable { onSelectHex(preset) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected color",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            value = selectedHex,
            onValueChange = onSelectHex,
            label = { Text("কাস্টম Hex কালার", fontSize = 11.sp) },
            placeholder = { Text("#10B981", fontSize = 12.sp) },
            singleLine = true,
            colors = libraryTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun ClassFormDialog(
    editingClass: LibraryClass?,
    onDismiss: () -> Unit,
    onSave: (
        customId: String,
        title: String,
        subtitle: String,
        badge: String,
        colorHex: String,
        isActive: Boolean
    ) -> Unit
) {
    var title by remember(editingClass) { mutableStateOf(editingClass?.title.orEmpty()) }
    var subtitle by remember(editingClass) { mutableStateOf(editingClass?.subtitle.orEmpty()) }
    var badge by remember(editingClass) { mutableStateOf(editingClass?.badge.orEmpty()) }
    var customId by remember(editingClass) { mutableStateOf(editingClass?.id.orEmpty()) }
    var colorHex by remember(editingClass) { mutableStateOf(editingClass?.colorHex ?: "#10B981") }
    var isActive by remember(editingClass) { mutableStateOf(editingClass?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate850,
        title = {
            Text(
                text = if (editingClass == null) "নতুন শ্রেণী যোগ করুন" else "শ্রেণী এডিট করুন",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("শ্রেণীর নাম * (যেমন: HSC মানবিক)", fontSize = 12.sp) },
                    singleLine = true,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_class_title")
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("বিবরণ / সাবটাইটেল", fontSize = 12.sp) },
                    placeholder = { Text("একাদশ-দ্বাদশ শ্রেণীর পাঠ্যবই ও নোট", fontSize = 11.sp) },
                    maxLines = 2,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = badge,
                    onValueChange = { badge = it },
                    label = { Text("ব্যাজ (যেমন: HSC, SSC, Class 7)", fontSize = 12.sp) },
                    singleLine = true,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (editingClass == null) {
                    OutlinedTextField(
                        value = customId,
                        onValueChange = { customId = it },
                        label = { Text("ফোল্ডার ID (ঐচ্ছিক, যেমন: hsc-humanities)", fontSize = 11.sp) },
                        supportingText = {
                            Text(
                                text = "খালি রাখলে অটোমেটিক URL-safe ID তৈরি হবে।",
                                color = Slate400,
                                fontSize = 10.sp
                            )
                        },
                        singleLine = true,
                        colors = libraryTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                ColorHexPickerRow(
                    selectedHex = colorHex,
                    onSelectHex = { colorHex = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("স্টুডেন্ট অ্যাপে দৃশ্যমান (is_active)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("বন্ধ করলে স্টুডেন্টরা এই শ্রেণী দেখতে পাবে না", color = Slate400, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldSuccess
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(customId, title, subtitle, badge, colorHex, isActive) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("btn_save_class")
            ) {
                Text("সেভ করুন", color = Slate950, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = Slate400)
            }
        }
    )
}

@Composable
internal fun SubjectFormDialog(
    editingSubject: LibrarySubject?,
    onUploadCover: (Uri, (String) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onSave: (
        customId: String,
        title: String,
        subtitle: String,
        coverUrl: String,
        colorHex: String,
        isActive: Boolean
    ) -> Unit
) {
    var title by remember(editingSubject) { mutableStateOf(editingSubject?.title.orEmpty()) }
    var subtitle by remember(editingSubject) { mutableStateOf(editingSubject?.subtitle.orEmpty()) }
    var coverUrl by remember(editingSubject) { mutableStateOf(editingSubject?.coverUrl.orEmpty()) }
    var customId by remember(editingSubject) { mutableStateOf(editingSubject?.id.orEmpty()) }
    var colorHex by remember(editingSubject) { mutableStateOf(editingSubject?.colorHex ?: "#0284C7") }
    var isActive by remember(editingSubject) { mutableStateOf(editingSubject?.isActive ?: true) }

    val coverPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUploadCover(uri) { uploadedUrl ->
                coverUrl = uploadedUrl
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate850,
        title = {
            Text(
                text = if (editingSubject == null) "নতুন সাবজেক্ট যোগ করুন" else "সাবজেক্ট এডিট করুন",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("সাবজেক্টের নাম * (যেমন: সমাজবিজ্ঞান ১ম পত্র)", fontSize = 12.sp) },
                    singleLine = true,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_subject_title")
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("সাবটাইটেল", fontSize = 12.sp) },
                    placeholder = { Text("সম্পূর্ণ অধ্যায়ভিত্তিক বই", fontSize = 11.sp) },
                    maxLines = 2,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = coverUrl,
                    onValueChange = { coverUrl = it },
                    label = { Text("কভার ছবি URL (ঐচ্ছিক)", fontSize = 12.sp) },
                    placeholder = { Text("https://...", fontSize = 11.sp) },
                    singleLine = true,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        coverPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("গ্যালারি থেকে কভার R2-তে আপলোড", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (editingSubject == null) {
                    OutlinedTextField(
                        value = customId,
                        onValueChange = { customId = it },
                        label = { Text("ফোল্ডার ID (ঐচ্ছিক, যেমন: sociology-1st)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = libraryTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                ColorHexPickerRow(
                    selectedHex = colorHex,
                    onSelectHex = { colorHex = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("সক্রিয় (is_active)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldSuccess
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(customId, title, subtitle, coverUrl, colorHex, isActive) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("btn_save_subject")
            ) {
                Text("সেভ করুন", color = Slate950, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = Slate400)
            }
        }
    )
}

@Composable
internal fun ChapterFormDialog(
    editingChapter: LibraryChapter?,
    suggestedChapterNumber: Int,
    onDismiss: () -> Unit,
    onSave: (
        customId: String,
        chapterNumber: Int,
        title: String,
        subtitle: String,
        isActive: Boolean
    ) -> Unit
) {
    var chapterNumberText by remember(editingChapter, suggestedChapterNumber) {
        mutableStateOf((editingChapter?.chapterNumber ?: suggestedChapterNumber).toString())
    }
    var title by remember(editingChapter) { mutableStateOf(editingChapter?.title.orEmpty()) }
    var subtitle by remember(editingChapter) { mutableStateOf(editingChapter?.subtitle.orEmpty()) }
    var customId by remember(editingChapter) { mutableStateOf(editingChapter?.id.orEmpty()) }
    var isActive by remember(editingChapter) { mutableStateOf(editingChapter?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate850,
        title = {
            Text(
                text = if (editingChapter == null) "নতুন অধ্যায় যোগ করুন" else "অধ্যায় এডিট করুন",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = chapterNumberText,
                    onValueChange = { value -> chapterNumberText = value.filter { it.isDigit() } },
                    label = { Text("অধ্যায় নম্বর (chapter_number)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("অধ্যায়ের শিরোনাম *", fontSize = 12.sp) },
                    placeholder = { Text("প্রথম অধ্যায়: সমাজবিজ্ঞানের উৎপত্তি ও বিকাশ", fontSize = 11.sp) },
                    maxLines = 2,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_chapter_title")
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("সাবটাইটেল (ঐচ্ছিক)", fontSize = 12.sp) },
                    maxLines = 2,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (editingChapter == null) {
                    OutlinedTextField(
                        value = customId,
                        onValueChange = { customId = it },
                        label = { Text("ফোল্ডার ID (ঐচ্ছিক, যেমন: ch-1)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = libraryTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("সক্রিয় (is_active)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldSuccess
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = chapterNumberText.toIntOrNull()?.coerceAtLeast(1) ?: suggestedChapterNumber
                    onSave(customId, num, title, subtitle, isActive)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("btn_save_chapter")
            ) {
                Text("সেভ করুন", color = Slate950, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = Slate400)
            }
        }
    )
}

@Composable
internal fun AddHtmlFileDialog(
    inspectUri: (Uri) -> CloudflareR2Client.UriMetadata,
    onDismiss: () -> Unit,
    onUpload: (
        uri: Uri,
        customId: String,
        title: String,
        description: String,
        isActive: Boolean
    ) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSize by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var customId by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    val htmlPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val meta = inspectUri(uri)
            selectedUri = uri
            selectedFileName = meta.fileName
            selectedFileSize = LibraryUtils.formatFileSize(meta.sizeBytes)
            if (title.isBlank()) {
                title = LibraryUtils.cleanTitleFromFileName(meta.fileName)
            }
            if (customId.isBlank()) {
                customId = LibraryUtils.slugify(meta.fileName.substringBeforeLast('.'), "lesson")
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate850,
        title = {
            Text(
                text = "নতুন HTML ফাইল আপলোড",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate900,
                    border = BorderStroke(
                        1.dp,
                        if (selectedUri != null) EmeraldSuccess.copy(alpha = 0.7f) else NeonCyan.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            htmlPicker.launch(arrayOf("text/html", "application/xhtml+xml", "*/*"))
                        }
                        .testTag("btn_pick_single_html")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedUri != null) Icons.Default.Description else Icons.Default.CloudUpload,
                            contentDescription = "Select HTML file",
                            tint = if (selectedUri != null) EmeraldSuccess else NeonCyan,
                            modifier = Modifier.size(26.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedUri != null) selectedFileName else ".html ফাইল সিলেক্ট করুন",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (selectedUri != null) {
                                    "সাইজ: $selectedFileSize • পরিবর্তন করতে ট্যাপ করুন"
                                } else {
                                    "ট্যাপ করে ডিভাইস থেকে .html ফাইল বাছাই করুন"
                                },
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("পাঠের শিরোনাম *", fontSize = 12.sp) },
                    placeholder = { Text("পাঠ ১: সমাজবিজ্ঞানের ধারণা", fontSize = 11.sp) },
                    singleLine = true,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_html_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিবরণ (ঐচ্ছিক)", fontSize = 12.sp) },
                    placeholder = { Text("মূল পাঠ ও গুরুত্বপূর্ণ প্রশ্নোত্তর", fontSize = 11.sp) },
                    maxLines = 3,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customId,
                    onValueChange = { customId = it },
                    label = { Text("ফাইল ID (ঐচ্ছিক, যেমন: soc1-ch1-p1)", fontSize = 11.sp) },
                    singleLine = true,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("সক্রিয় (is_active)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldSuccess
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val uri = selectedUri ?: return@Button
                    onUpload(uri, customId, title, description, isActive)
                },
                enabled = selectedUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier.testTag("btn_upload_single_html_confirm")
            ) {
                Text("R2-তে আপলোড", color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = Slate400)
            }
        }
    )
}

@Composable
internal fun EditHtmlFileDialog(
    file: LibraryHtmlFile,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, isActive: Boolean) -> Unit
) {
    var title by remember(file) { mutableStateOf(file.title) }
    var description by remember(file) { mutableStateOf(file.description) }
    var isActive by remember(file) { mutableStateOf(file.isActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate850,
        title = {
            Text(
                text = "HTML ফাইলের তথ্য এডিট",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate900,
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "R2 Key: ${file.fileUrl}",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "ভার্শন: v${file.version} • সাইজ: ${LibraryUtils.formatFileSize(file.fileSizeBytes)}",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("পাঠের শিরোনাম *", fontSize = 12.sp) },
                    singleLine = true,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিবরণ", fontSize = 12.sp) },
                    maxLines = 3,
                    colors = libraryTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("সক্রিয় (is_active)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldSuccess
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, description, isActive) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                Text("আপডেট করুন", color = Slate950, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = Slate400)
            }
        }
    )
}

@Composable
internal fun LibraryDeleteConfirmDialog(
    title: String,
    message: String,
    r2FilesCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate850,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = CoralDanger,
                modifier = Modifier.size(30.dp)
            )
        },
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = message, color = Slate200, fontSize = 13.sp)
                if (r2FilesCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CoralDanger.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ এর ভেতরের মোট ${r2FilesCount}টি ফাইল Cloudflare R2 বাকেট (maxbird) থেকেও স্থায়ীভাবে ডিলিট হয়ে যাবে।",
                            color = AmberWarning,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CoralDanger),
                modifier = Modifier.testTag("btn_confirm_library_delete")
            ) {
                Text("হ্যাঁ, ডিলিট করুন", color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = Slate400)
            }
        }
    )
}
