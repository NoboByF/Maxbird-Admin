package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.CloudflareR2Client
import com.example.data.model.LibraryChapter
import com.example.data.model.LibraryClass
import com.example.data.model.LibraryHtmlFile
import com.example.data.model.LibrarySubject
import com.example.data.repository.LibraryR2Repository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PurpleRoyal
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.LibraryLevel
import com.example.ui.viewmodel.LibraryUiState

private sealed class LibraryPendingDelete {
    data class ClassItem(val item: LibraryClass) : LibraryPendingDelete()
    data class SubjectItem(val item: LibrarySubject) : LibraryPendingDelete()
    data class ChapterItem(val item: LibraryChapter) : LibraryPendingDelete()
    data class FileItem(val item: LibraryHtmlFile) : LibraryPendingDelete()
}

@Composable
fun LibraryManagerScreen(
    uiState: LibraryUiState,
    inspectUri: (Uri) -> CloudflareR2Client.UriMetadata,
    onLoadIndex: (forceRefresh: Boolean) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenClass: (String) -> Unit,
    onOpenSubject: (String) -> Unit,
    onOpenChapter: (String) -> Unit,
    onNavigateToRoot: () -> Unit,
    onNavigateToClassLevel: () -> Unit,
    onNavigateToSubjectLevel: () -> Unit,
    onNavigateUp: () -> Boolean,
    onSaveClass: (String?, String, String, String, String, String, Boolean) -> Unit,
    onToggleClassActive: (String) -> Unit,
    onMoveClass: (String, Boolean) -> Unit,
    onDeleteClass: (String) -> Unit,
    onUploadSubjectCover: (Uri, (String) -> Unit) -> Unit,
    onSaveSubject: (String?, String, String, String, String, String, Boolean) -> Unit,
    onToggleSubjectActive: (String) -> Unit,
    onMoveSubject: (String, Boolean) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onSaveChapter: (String?, String, Int, String, String, Boolean) -> Unit,
    onToggleChapterActive: (String) -> Unit,
    onMoveChapter: (String, Boolean) -> Unit,
    onDeleteChapter: (String) -> Unit,
    onUploadSingleHtml: (Uri, String, String, String, Boolean) -> Unit,
    onUploadMultipleHtml: (List<Uri>) -> Unit,
    onUpdateHtmlMetadata: (String, String, String, Boolean) -> Unit,
    onReplaceHtmlFile: (String, Uri) -> Unit,
    onToggleHtmlActive: (String) -> Unit,
    onMoveHtmlFile: (String, Boolean) -> Unit,
    onDeleteHtmlFile: (String) -> Unit,
    onShowToast: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current

    // Dialog states
    var showClassDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<LibraryClass?>(null) }

    var showSubjectDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<LibrarySubject?>(null) }

    var showChapterDialog by remember { mutableStateOf(false) }
    var editingChapter by remember { mutableStateOf<LibraryChapter?>(null) }

    var showAddHtmlDialog by remember { mutableStateOf(false) }
    var editingHtmlFile by remember { mutableStateOf<LibraryHtmlFile?>(null) }
    var replacingFileId by remember { mutableStateOf<String?>(null) }

    var pendingDelete by remember { mutableStateOf<LibraryPendingDelete?>(null) }

    // Initial load when entering Library Manager
    LaunchedEffect(Unit) {
        onLoadIndex(false)
    }

    // Handle Back button to step up 1 folder level before leaving Library Manager
    BackHandler(enabled = uiState.currentLevel != LibraryLevel.CLASSES) {
        onNavigateUp()
    }

    // Multiple HTML file picker
    val multiHtmlPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onUploadMultipleHtml(uris)
        }
    }

    // Single HTML file picker for Replace (+1 version)
    val replaceHtmlPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val targetId = replacingFileId
        replacingFileId = null
        if (uri != null && targetId != null) {
            onReplaceHtmlFile(targetId, uri)
        }
    }

    val isBusy = uiState.isLoading || uiState.isSaving
    val query = uiState.searchQuery.trim()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .testTag("library_manager_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. R2 Status & Stats Hero Banner
        item {
            LibraryR2HeroCard(
                uiState = uiState,
                onRefresh = { onLoadIndex(true) },
                onCopyIndexUrl = {
                    clipboard.setText(AnnotatedString(LibraryR2Repository.INDEX_PUBLIC_URL))
                    onShowToast("✅ library_index.json পাবলিক লিংক কপি হয়েছে!")
                }
            )
        }

        // 2. Live R2 Upload / Sync Progress Banner
        item {
            AnimatedVisibility(visible = uiState.isSaving && !uiState.uploadStatusText.isNullOrBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = NeonCyan
                            )
                            Text(
                                text = uiState.uploadStatusText.orEmpty(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (uiState.uploadProgressPercent > 0) {
                            LinearProgressIndicator(
                                progress = { (uiState.uploadProgressPercent / 100f).coerceIn(0f, 1f) },
                                color = NeonCyan,
                                trackColor = Slate700,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        } else {
                            LinearProgressIndicator(
                                color = NeonCyan,
                                trackColor = Slate700,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }

        // 3. Error Banner (if any)
        if (!uiState.errorMessage.isNullOrBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CoralDanger.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = CoralDanger,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = { onLoadIndex(true) },
                            border = BorderStroke(1.dp, CoralDanger)
                        ) {
                            Text("রিস্টার্ট", color = CoralDanger, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 4. Interactive 4-Level Breadcrumb Bar
        item {
            LibraryBreadcrumbBar(
                uiState = uiState,
                onClickRoot = onNavigateToRoot,
                onClickClass = onNavigateToClassLevel,
                onClickSubject = onNavigateToSubjectLevel
            )
        }

        // 5. Level Header, Search & Primary Create / Upload Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (uiState.currentLevel) {
                                LibraryLevel.CLASSES -> "ধাপ ১: শ্রেণী তালিকা (Classes)"
                                LibraryLevel.SUBJECTS -> "ধাপ ২: সাবজেক্ট তালিকা (Subjects)"
                                LibraryLevel.CHAPTERS -> "ধাপ ৩: অধ্যায় তালিকা (Chapters)"
                                LibraryLevel.FILES -> "ধাপ ৪: HTML বই ও পাঠ (Files)"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = when (uiState.currentLevel) {
                                LibraryLevel.CLASSES -> "যেকোনো শ্রেণীতে ট্যাপ করে সাবজেক্ট ম্যানেজ করুন"
                                LibraryLevel.SUBJECTS -> "${uiState.selectedClass?.title.orEmpty()} এর সাবজেক্টসমূহ"
                                LibraryLevel.CHAPTERS -> "${uiState.selectedSubject?.title.orEmpty()} এর অধ্যায়সমূহ"
                                LibraryLevel.FILES -> "${uiState.selectedChapter?.title.orEmpty()} এর HTML ফাইলসমূহ"
                            },
                            color = Slate400,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (uiState.currentLevel != LibraryLevel.FILES) {
                        Button(
                            onClick = {
                                when (uiState.currentLevel) {
                                    LibraryLevel.CLASSES -> {
                                        editingClass = null
                                        showClassDialog = true
                                    }
                                    LibraryLevel.SUBJECTS -> {
                                        editingSubject = null
                                        showSubjectDialog = true
                                    }
                                    LibraryLevel.CHAPTERS -> {
                                        editingChapter = null
                                        showChapterDialog = true
                                    }
                                    LibraryLevel.FILES -> Unit
                                }
                            },
                            enabled = !isBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier.testTag("btn_library_add_primary")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add new item",
                                tint = Slate950,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (uiState.currentLevel) {
                                    LibraryLevel.CLASSES -> "নতুন শ্রেণী"
                                    LibraryLevel.SUBJECTS -> "নতুন সাবজেক্ট"
                                    LibraryLevel.CHAPTERS -> "নতুন অধ্যায়"
                                    LibraryLevel.FILES -> ""
                                },
                                color = Slate950,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Special HTML Upload Action Strip at Level 4
                if (uiState.currentLevel == LibraryLevel.FILES) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAddHtmlDialog = true },
                            enabled = !isBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_upload_single_html")
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "১টি HTML আপলোড",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                multiHtmlPicker.launch(arrayOf("text/html", "application/xhtml+xml", "*/*"))
                            },
                            enabled = !isBusy,
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_upload_multi_html")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "একাধিক HTML",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = when (uiState.currentLevel) {
                                LibraryLevel.CLASSES -> "শ্রেণী খুঁজুন..."
                                LibraryLevel.SUBJECTS -> "সাবজেক্ট খুঁজুন..."
                                LibraryLevel.CHAPTERS -> "অধ্যায় খুঁজুন..."
                                LibraryLevel.FILES -> "HTML পাঠ খুঁজুন..."
                            },
                            color = Slate400,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate850,
                        unfocusedContainerColor = Slate850,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_library_search")
                )
            }
        }

        // 6. Content Area (Loading / 4-Level Lists / Empty States)
        if (uiState.isLoading && !uiState.hasLoadedOnce) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = NeonCyan)
                        Text("Cloudflare R2 থেকে লাইব্রেরি লোড হচ্ছে...", color = Slate400, fontSize = 13.sp)
                    }
                }
            }
        } else {
            when (uiState.currentLevel) {
                LibraryLevel.CLASSES -> {
                    val allClasses = uiState.index.classes.sortedBy { it.order }
                    val filtered = if (query.isBlank()) {
                        allClasses
                    } else {
                        allClasses.filter {
                            it.title.contains(query, ignoreCase = true) ||
                                it.subtitle.contains(query, ignoreCase = true) ||
                                it.badge.contains(query, ignoreCase = true) ||
                                it.id.contains(query, ignoreCase = true)
                        }
                    }

                    if (filtered.isEmpty()) {
                        item {
                            LibraryEmptyStateCard(
                                icon = Icons.Default.LibraryBooks,
                                title = if (allClasses.isEmpty()) "এখনো কোনো শ্রেণী যোগ করা হয়নি" else "কোনো শ্রেণী পাওয়া যায়নি",
                                subtitle = if (allClasses.isEmpty()) {
                                    "“নতুন শ্রেণী” বাটনে ট্যাপ করে HSC মানবিক, ক্লাস ৭ ইত্যাদি তৈরি করুন। সাথে সাথে R2-তে library_index.json সেভ হয়ে যাবে।"
                                } else {
                                    "অন্য কীওয়ার্ড দিয়ে খুঁজে দেখুন।"
                                }
                            )
                        }
                    } else {
                        itemsIndexed(filtered, key = { _, item -> item.id }) { idx, cls ->
                            LibraryClassCard(
                                libraryClass = cls,
                                canMoveUp = query.isBlank() && idx > 0,
                                canMoveDown = query.isBlank() && idx < filtered.lastIndex,
                                isBusy = isBusy,
                                onOpen = { onOpenClass(cls.id) },
                                onMoveUp = { onMoveClass(cls.id, true) },
                                onMoveDown = { onMoveClass(cls.id, false) },
                                onToggleActive = { onToggleClassActive(cls.id) },
                                onEdit = {
                                    editingClass = cls
                                    showClassDialog = true
                                },
                                onDelete = { pendingDelete = LibraryPendingDelete.ClassItem(cls) }
                            )
                        }
                    }
                }

                LibraryLevel.SUBJECTS -> {
                    val allSubjects = uiState.selectedClass?.subjects.orEmpty().sortedBy { it.order }
                    val filtered = if (query.isBlank()) {
                        allSubjects
                    } else {
                        allSubjects.filter {
                            it.title.contains(query, ignoreCase = true) ||
                                it.subtitle.contains(query, ignoreCase = true) ||
                                it.id.contains(query, ignoreCase = true)
                        }
                    }

                    if (filtered.isEmpty()) {
                        item {
                            LibraryEmptyStateCard(
                                icon = Icons.Default.FolderOpen,
                                title = if (allSubjects.isEmpty()) "এই শ্রেণীতে কোনো সাবজেক্ট নেই" else "কোনো সাবজেক্ট পাওয়া যায়নি",
                                subtitle = "“নতুন সাবজেক্ট” বাটনে ট্যাপ করে সমাজবিজ্ঞান ১ম পত্র, ইতিহাস ইত্যাদি যোগ করুন।"
                            )
                        }
                    } else {
                        itemsIndexed(filtered, key = { _, item -> item.id }) { idx, sub ->
                            LibrarySubjectCard(
                                subject = sub,
                                canMoveUp = query.isBlank() && idx > 0,
                                canMoveDown = query.isBlank() && idx < filtered.lastIndex,
                                isBusy = isBusy,
                                onOpen = { onOpenSubject(sub.id) },
                                onMoveUp = { onMoveSubject(sub.id, true) },
                                onMoveDown = { onMoveSubject(sub.id, false) },
                                onToggleActive = { onToggleSubjectActive(sub.id) },
                                onEdit = {
                                    editingSubject = sub
                                    showSubjectDialog = true
                                },
                                onDelete = { pendingDelete = LibraryPendingDelete.SubjectItem(sub) }
                            )
                        }
                    }
                }

                LibraryLevel.CHAPTERS -> {
                    val subjectAccent = parseSafeHexColor(
                        uiState.selectedSubject?.colorHex ?: "#0284C7",
                        NeonCyan
                    )
                    val allChapters = uiState.selectedSubject?.chapters.orEmpty().sortedBy { it.order }
                    val filtered = if (query.isBlank()) {
                        allChapters
                    } else {
                        allChapters.filter {
                            it.title.contains(query, ignoreCase = true) ||
                                it.subtitle.contains(query, ignoreCase = true) ||
                                it.id.contains(query, ignoreCase = true)
                        }
                    }

                    if (filtered.isEmpty()) {
                        item {
                            LibraryEmptyStateCard(
                                icon = Icons.Default.MenuBook,
                                title = if (allChapters.isEmpty()) "এই সাবজেক্টে কোনো অধ্যায় নেই" else "কোনো অধ্যায় পাওয়া যায়নি",
                                subtitle = "“নতুন অধ্যায়” বাটনে ট্যাপ করে প্রথম অধ্যায়, দ্বিতীয় অধ্যায় ইত্যাদি তৈরি করুন।"
                            )
                        }
                    } else {
                        itemsIndexed(filtered, key = { _, item -> item.id }) { idx, ch ->
                            LibraryChapterCard(
                                chapter = ch,
                                accentColor = subjectAccent,
                                canMoveUp = query.isBlank() && idx > 0,
                                canMoveDown = query.isBlank() && idx < filtered.lastIndex,
                                isBusy = isBusy,
                                onOpen = { onOpenChapter(ch.id) },
                                onMoveUp = { onMoveChapter(ch.id, true) },
                                onMoveDown = { onMoveChapter(ch.id, false) },
                                onToggleActive = { onToggleChapterActive(ch.id) },
                                onEdit = {
                                    editingChapter = ch
                                    showChapterDialog = true
                                },
                                onDelete = { pendingDelete = LibraryPendingDelete.ChapterItem(ch) }
                            )
                        }
                    }
                }

                LibraryLevel.FILES -> {
                    val allFiles = uiState.selectedChapter?.files.orEmpty().sortedBy { it.order }
                    val filtered = if (query.isBlank()) {
                        allFiles
                    } else {
                        allFiles.filter {
                            it.title.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true) ||
                                it.id.contains(query, ignoreCase = true)
                        }
                    }

                    if (filtered.isEmpty()) {
                        item {
                            LibraryEmptyStateCard(
                                icon = Icons.Default.Description,
                                title = if (allFiles.isEmpty()) "এই অধ্যায়ে কোনো HTML ফাইল নেই" else "কোনো ফাইল পাওয়া যায়নি",
                                subtitle = "উপরের বাটন থেকে এক বা একাধিক .html ফাইল আপলোড করুন। প্রতিটি ফাইল সরাসরি R2 বাকেটে সংরক্ষিত হবে।"
                            )
                        }
                    } else {
                        itemsIndexed(filtered, key = { _, item -> item.id }) { idx, file ->
                            LibraryHtmlFileCard(
                                file = file,
                                canMoveUp = query.isBlank() && idx > 0,
                                canMoveDown = query.isBlank() && idx < filtered.lastIndex,
                                isBusy = isBusy,
                                onReplaceHtml = {
                                    replacingFileId = file.id
                                    replaceHtmlPicker.launch(arrayOf("text/html", "application/xhtml+xml", "*/*"))
                                },
                                onCopyPublicUrl = {
                                    val fullUrl = CloudflareR2Client.buildPublicUrl(file.fileUrl)
                                    clipboard.setText(AnnotatedString(fullUrl))
                                    onShowToast("✅ HTML পাবলিক লিংক কপি হয়েছে!")
                                },
                                onMoveUp = { onMoveHtmlFile(file.id, true) },
                                onMoveDown = { onMoveHtmlFile(file.id, false) },
                                onToggleActive = { onToggleHtmlActive(file.id) },
                                onEdit = { editingHtmlFile = file },
                                onDelete = { pendingDelete = LibraryPendingDelete.FileItem(file) }
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // =========================================================================
    // Dialogs
    // =========================================================================

    if (showClassDialog) {
        ClassFormDialog(
            editingClass = editingClass,
            onDismiss = { showClassDialog = false },
            onSave = { customId, title, subtitle, badge, colorHex, isActive ->
                onSaveClass(editingClass?.id, customId, title, subtitle, badge, colorHex, isActive)
                showClassDialog = false
            }
        )
    }

    if (showSubjectDialog) {
        SubjectFormDialog(
            editingSubject = editingSubject,
            onUploadCover = onUploadSubjectCover,
            onDismiss = { showSubjectDialog = false },
            onSave = { customId, title, subtitle, coverUrl, colorHex, isActive ->
                onSaveSubject(editingSubject?.id, customId, title, subtitle, coverUrl, colorHex, isActive)
                showSubjectDialog = false
            }
        )
    }

    if (showChapterDialog) {
        val nextNum = (uiState.selectedSubject?.chapters?.maxOfOrNull { it.chapterNumber } ?: 0) + 1
        ChapterFormDialog(
            editingChapter = editingChapter,
            suggestedChapterNumber = nextNum,
            onDismiss = { showChapterDialog = false },
            onSave = { customId, num, title, subtitle, isActive ->
                onSaveChapter(editingChapter?.id, customId, num, title, subtitle, isActive)
                showChapterDialog = false
            }
        )
    }

    if (showAddHtmlDialog) {
        AddHtmlFileDialog(
            inspectUri = inspectUri,
            onDismiss = { showAddHtmlDialog = false },
            onUpload = { uri, customId, title, description, isActive ->
                onUploadSingleHtml(uri, customId, title, description, isActive)
                showAddHtmlDialog = false
            }
        )
    }

    editingHtmlFile?.let { file ->
        EditHtmlFileDialog(
            file = file,
            onDismiss = { editingHtmlFile = null },
            onSave = { title, description, isActive ->
                onUpdateHtmlMetadata(file.id, title, description, isActive)
                editingHtmlFile = null
            }
        )
    }

    pendingDelete?.let { target ->
        when (target) {
            is LibraryPendingDelete.ClassItem -> {
                LibraryDeleteConfirmDialog(
                    title = "শ্রেণী ডিলিট নিশ্চিত করুন",
                    message = "“${target.item.title}” শ্রেণী এবং এর ভেতরের ${target.item.subjects.size}টি সাবজেক্ট ও ${target.item.totalChapters}টি অধ্যায় মুছে ফেলবেন?",
                    r2FilesCount = target.item.collectAllR2Keys().size,
                    onDismiss = { pendingDelete = null },
                    onConfirm = {
                        onDeleteClass(target.item.id)
                        pendingDelete = null
                    }
                )
            }
            is LibraryPendingDelete.SubjectItem -> {
                LibraryDeleteConfirmDialog(
                    title = "সাবজেক্ট ডিলিট নিশ্চিত করুন",
                    message = "“${target.item.title}” সাবজেক্ট এবং এর ভেতরের ${target.item.chapters.size}টি অধ্যায় মুছে ফেলবেন?",
                    r2FilesCount = target.item.collectAllR2Keys().size,
                    onDismiss = { pendingDelete = null },
                    onConfirm = {
                        onDeleteSubject(target.item.id)
                        pendingDelete = null
                    }
                )
            }
            is LibraryPendingDelete.ChapterItem -> {
                LibraryDeleteConfirmDialog(
                    title = "অধ্যায় ডিলিট নিশ্চিত করুন",
                    message = "“${target.item.title}” অধ্যায় এবং এর সব HTML ফাইল মুছে ফেলবেন?",
                    r2FilesCount = target.item.collectAllR2Keys().size,
                    onDismiss = { pendingDelete = null },
                    onConfirm = {
                        onDeleteChapter(target.item.id)
                        pendingDelete = null
                    }
                )
            }
            is LibraryPendingDelete.FileItem -> {
                LibraryDeleteConfirmDialog(
                    title = "HTML ফাইল ডিলিট নিশ্চিত করুন",
                    message = "“${target.item.title}” ফাইলটি মুছে ফেলবেন?",
                    r2FilesCount = 1,
                    onDismiss = { pendingDelete = null },
                    onConfirm = {
                        onDeleteHtmlFile(target.item.id)
                        pendingDelete = null
                    }
                )
            }
        }
    }
}

@Composable
private fun LibraryR2HeroCard(
    uiState: LibraryUiState,
    onRefresh: () -> Unit,
    onCopyIndexUrl: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, PurpleRoyal.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            PurpleRoyal.copy(alpha = 0.22f),
                            ElectricBlue.copy(alpha = 0.14f),
                            Slate850
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                .background(PurpleRoyal.copy(alpha = 0.24f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Cloudflare R2 ডিজিটাল লাইব্রেরি",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Bucket: maxbird • library/library_index.json",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    SmallTagPill(
                        text = if (uiState.existedOnR2) "LIVE R2" else "NEW",
                        color = if (uiState.existedOnR2) EmeraldSuccess else AmberWarning
                    )
                }

                // 4-Stat Counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LibraryStatMiniBox(
                        label = "শ্রেণী",
                        value = "${uiState.index.classes.size}",
                        color = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    LibraryStatMiniBox(
                        label = "সাবজেক্ট",
                        value = "${uiState.index.totalSubjects}",
                        color = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    LibraryStatMiniBox(
                        label = "অধ্যায়",
                        value = "${uiState.index.totalChapters}",
                        color = AmberWarning,
                        modifier = Modifier.weight(1f)
                    )
                    LibraryStatMiniBox(
                        label = "HTML বই",
                        value = "${uiState.index.totalFiles}",
                        color = PurpleRoyal,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.existedOnR2) {
                            "শেষ আপডেট: ${uiState.index.updatedAt}"
                        } else {
                            "প্রথম শ্রেণী যোগ করলেই R2-তে ইনডেক্স তৈরি হবে"
                        },
                        color = Slate400,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopyIndexUrl,
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_copy_library_index_url")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Index URL কপি", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onRefresh,
                        enabled = !uiState.isLoading && !uiState.isSaving,
                        border = BorderStroke(1.dp, Slate700),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_refresh_library_index")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Slate200, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("R2 সিঙ্ক রিফ্রেশ", color = Slate200, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryStatMiniBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Slate900.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
            Text(
                text = label,
                color = Slate400,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun LibraryBreadcrumbBar(
    uiState: LibraryUiState,
    onClickRoot: () -> Unit,
    onClickClass: () -> Unit,
    onClickSubject: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Slate850,
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BreadcrumbChip(
                text = "লাইব্রেরি",
                isActive = uiState.currentLevel == LibraryLevel.CLASSES,
                onClick = onClickRoot
            )

            uiState.selectedClass?.let { cls ->
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(16.dp)
                )
                BreadcrumbChip(
                    text = cls.title,
                    isActive = uiState.currentLevel == LibraryLevel.SUBJECTS,
                    onClick = onClickClass
                )
            }

            uiState.selectedSubject?.let { sub ->
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(16.dp)
                )
                BreadcrumbChip(
                    text = sub.title,
                    isActive = uiState.currentLevel == LibraryLevel.CHAPTERS,
                    onClick = onClickSubject
                )
            }

            uiState.selectedChapter?.let { ch ->
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(16.dp)
                )
                BreadcrumbChip(
                    text = "অধ্যায় ${ch.chapterNumber}",
                    isActive = true,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun BreadcrumbChip(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) NeonCyan.copy(alpha = 0.18f) else Slate800,
        border = BorderStroke(1.dp, if (isActive) NeonCyan.copy(alpha = 0.6f) else Slate700),
        modifier = Modifier.clickable(enabled = !isActive, onClick = onClick)
    ) {
        Text(
            text = text,
            color = if (isActive) NeonCyan else Slate200,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun LibraryEmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = Slate400,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
