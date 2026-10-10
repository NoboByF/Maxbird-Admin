package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.AppNotice
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun NoticeManagementScreen(
    notices: List<AppNotice>,
    isLoading: Boolean,
    searchQuery: String,
    filter: String,
    isDialogOpen: Boolean,
    editingId: String?,
    title: String,
    description: String,
    imageUrl: String,
    actionUrl: String,
    actionButtonText: String,
    priority: String,
    isActive: Boolean,
    showAsPopup: Boolean,
    previewNotice: AppNotice?,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onOpenCreateDialog: () -> Unit,
    onOpenEditDialog: (AppNotice) -> Unit,
    onCloseDialog: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onActionUrlChange: (String) -> Unit,
    onActionButtonTextChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onShowAsPopupChange: (Boolean) -> Unit,
    onOpenPreview: (AppNotice) -> Unit,
    onClosePreview: () -> Unit,
    onSaveNotice: () -> Unit,
    onToggleStatus: (AppNotice) -> Unit,
    onDeleteNotice: (AppNotice) -> Unit,
    onRefresh: () -> Unit,
    onShowToast: (String) -> Unit
) {
    var noticeToDelete by remember { mutableStateOf<AppNotice?>(null) }

    val filteredNotices = remember(notices, searchQuery, filter) {
        notices.filter { notice ->
            val matchesSearch = searchQuery.isBlank() ||
                notice.title.contains(searchQuery, ignoreCase = true) ||
                (notice.description?.contains(searchQuery, ignoreCase = true) == true) ||
                (notice.actionUrl?.contains(searchQuery, ignoreCase = true) == true)

            val matchesFilter = when (filter) {
                "ACTIVE" -> notice.isActive
                "INACTIVE" -> !notice.isActive
                "POPUP" -> notice.showAsPopup
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        containerColor = Slate900,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenCreateDialog,
                containerColor = ElectricBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_create_notice")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Notice")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("নতুন নোটিশ", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Screen Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "নোটিশ কন্ট্রোল সেন্টার",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Text(
                            text = "Supabase app_notices • লাইভ ব্যানার ও পপ-আপ বার্তা",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Slate800)
                            .border(1.dp, Slate700, CircleShape)
                            .testTag("btn_refresh_notices")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Notices",
                                tint = Slate400
                            )
                        }
                    }
                }
            }

            // Quick Stats Row
            item {
                val totalCount = notices.size
                val activeCount = notices.count { it.isActive }
                val popupCount = notices.count { it.showAsPopup && it.isActive }
                val highestPriority = notices.maxOfOrNull { it.priority } ?: 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NoticeStatBox(
                        title = "মোট নোটিশ",
                        value = totalCount.toString(),
                        accentColor = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    NoticeStatBox(
                        title = "সক্রিয় নোটিশ",
                        value = activeCount.toString(),
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    NoticeStatBox(
                        title = "পপ-আপ অ্যালার্ট",
                        value = popupCount.toString(),
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    NoticeStatBox(
                        title = "টপ প্রায়োরিটি",
                        value = highestPriority.toString(),
                        accentColor = CoralDanger,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_notices"),
                        placeholder = { Text("নোটিশ খুঁজুন (শিরোনাম বা বিবরণ)...", color = Slate400) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Slate400)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate400)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Filter chips row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = filter == "ALL",
                                onClick = { onFilterChange("ALL") },
                                label = { Text("সকল (${notices.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = Slate800,
                                    labelColor = Slate400
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = filter == "ALL",
                                    borderColor = Slate700,
                                    selectedBorderColor = ElectricBlue
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = filter == "ACTIVE",
                                onClick = { onFilterChange("ACTIVE") },
                                label = { Text("সক্রিয় (${notices.count { it.isActive }})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldSuccess,
                                    selectedLabelColor = Color.White,
                                    containerColor = Slate800,
                                    labelColor = Slate400
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = filter == "ACTIVE",
                                    borderColor = Slate700,
                                    selectedBorderColor = EmeraldSuccess
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = filter == "POPUP",
                                onClick = { onFilterChange("POPUP") },
                                label = { Text("পপ-আপ (${notices.count { it.showAsPopup }})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Slate950,
                                    containerColor = Slate800,
                                    labelColor = Slate400
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = filter == "POPUP",
                                    borderColor = Slate700,
                                    selectedBorderColor = NeonCyan
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = filter == "INACTIVE",
                                onClick = { onFilterChange("INACTIVE") },
                                label = { Text("নিষ্ক্রিয় (${notices.count { !it.isActive }})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Slate700,
                                    selectedLabelColor = Color.White,
                                    containerColor = Slate800,
                                    labelColor = Slate400
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = filter == "INACTIVE",
                                    borderColor = Slate700,
                                    selectedBorderColor = Slate400
                                )
                            )
                        }
                    }
                }
            }

            // Notice List Section
            if (filteredNotices.isEmpty()) {
                item {
                    EmptyNoticeCard(
                        searchQuery = searchQuery,
                        onOpenCreateDialog = onOpenCreateDialog
                    )
                }
            } else {
                items(filteredNotices, key = { it.id }) { notice ->
                    NoticeCard(
                        notice = notice,
                        onToggleStatus = { onToggleStatus(notice) },
                        onEdit = { onOpenEditDialog(notice) },
                        onDelete = { noticeToDelete = notice },
                        onPreview = { onOpenPreview(notice) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Create / Edit Notice Dialog
    if (isDialogOpen) {
        NoticeFormDialog(
            editingId = editingId,
            title = title,
            description = description,
            imageUrl = imageUrl,
            actionUrl = actionUrl,
            actionButtonText = actionButtonText,
            priority = priority,
            isActive = isActive,
            showAsPopup = showAsPopup,
            onClose = onCloseDialog,
            onTitleChange = onTitleChange,
            onDescriptionChange = onDescriptionChange,
            onImageUrlChange = onImageUrlChange,
            onActionUrlChange = onActionUrlChange,
            onActionButtonTextChange = onActionButtonTextChange,
            onPriorityChange = onPriorityChange,
            onIsActiveChange = onIsActiveChange,
            onShowAsPopupChange = onShowAsPopupChange,
            onSave = onSaveNotice
        )
    }

    // Live Student App Popup Simulation Modal
    if (previewNotice != null) {
        StudentPopupPreviewDialog(
            notice = previewNotice,
            onClose = onClosePreview,
            onShowToast = onShowToast
        )
    }

    // Delete Confirmation Dialog
    if (noticeToDelete != null) {
        val target = noticeToDelete!!
        AlertDialog(
            onDismissRequest = { noticeToDelete = null },
            containerColor = Slate800,
            titleContentColor = Color.White,
            textContentColor = Slate400,
            title = { Text("নোটিশ মুছে ফেলতে চান?", fontWeight = FontWeight.Bold) },
            text = {
                Text("আপনি কি নিশ্চিত যে '${target.title}' নোটিশটি ডাটাবেজ থেকে মুছে ফেলবেন? এটি পুনরুদ্ধার করা যাবে না।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNotice(target)
                        noticeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger)
                ) {
                    Text("হ্যাঁ, ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { noticeToDelete = null }) {
                    Text("বাতিল", color = Slate400)
                }
            }
        )
    }
}

@Composable
private fun NoticeStatBox(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, Slate700, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = Slate400),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NoticeCard(
    notice: AppNotice,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (notice.isActive) Slate700 else Slate700.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("notice_card_${notice.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Banner Thumbnail & Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Banner Image Preview Box
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 75.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate900)
                        .border(1.dp, Slate700, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (notice.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = notice.imageUrl,
                            contentDescription = notice.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Priority Tag Overlay
                    Surface(
                        color = Slate950.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "P: ${notice.priority}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }
                    }
                }

                // Title & Badges
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badges Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (notice.showAsPopup) {
                                Surface(
                                    color = NeonCyan.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "📱 পপ-আপ",
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Surface(
                                color = if (notice.isActive) EmeraldSuccess.copy(alpha = 0.15f) else Slate700,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (notice.isActive) "সক্রিয় (Active)" else "বন্ধ (Inactive)",
                                    color = if (notice.isActive) EmeraldSuccess else Slate400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Instant Active Switch
                        Switch(
                            checked = notice.isActive,
                            onCheckedChange = { onToggleStatus() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldSuccess,
                                uncheckedThumbColor = Slate400,
                                uncheckedTrackColor = Slate900
                            ),
                            modifier = Modifier.testTag("switch_notice_active_${notice.id}")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notice.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Description preview
            if (!notice.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notice.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action URL / Button Text Row
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!notice.actionUrl.isNullOrBlank()) {
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = notice.actionUrl,
                                fontSize = 11.sp,
                                color = ElectricBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    color = Slate700,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "বাটন: ${notice.actionButtonText}",
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Preview, Edit, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Preview Button
                OutlinedButton(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue))),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("প্রিভিউ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Edit Button
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সম্পাদনা", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CoralDanger.copy(alpha = 0.15f))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Notice",
                        tint = CoralDanger,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNoticeCard(
    searchQuery: String,
    onOpenCreateDialog: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Campaign,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (searchQuery.isNotBlank()) "কোনো নোটিশ পাওয়া যায়নি" else "এখনো কোনো নোটিশ যুক্ত করা হয়নি",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (searchQuery.isNotBlank())
                    "'$searchQuery' দিয়ে কোনো ফলাফল মেলেনি।"
                else
                    "শিক্ষার্থী অ্যাপে জরুরি ব্যানার ও পপ-আপ মেসেজ দেখাতে নতুন নোটিশ প্রকাশ করুন।",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenCreateDialog,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("প্রথম নোটিশ তৈরি করুন")
            }
        }
    }
}

@Composable
private fun NoticeFormDialog(
    editingId: String?,
    title: String,
    description: String,
    imageUrl: String,
    actionUrl: String,
    actionButtonText: String,
    priority: String,
    isActive: Boolean,
    showAsPopup: Boolean,
    onClose: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onActionUrlChange: (String) -> Unit,
    onActionButtonTextChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onShowAsPopupChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Form, 1: Live Preview

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate800)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (editingId == null) "নতুন নোটিশ প্রকাশ করুন" else "নোটিশ সম্পাদনা করুন",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Supabase app_notices টেবিলে রিয়েল-টাইম সংরক্ষণ",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )
                    }

                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                // Tabs: ফর্ম (Form) vs লাইভ প্রিভিউ (Live Preview)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Slate800,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonCyan
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("ইনপুট ফর্ম (Form)", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedTab == 1) NeonCyan else Slate400
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("লাইভ প্রিভিউ (Preview)", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                // Content Area
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        // Form Inputs
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Notice Title (Required)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "নোটিশের শিরোনাম (Title) *",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = onTitleChange,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_notice_title"),
                                    placeholder = { Text("যেমন: 🎉 স্পেশাল লাইভ সেশন নোটিশ", color = Slate400) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Slate800,
                                        unfocusedContainerColor = Slate800,
                                        focusedBorderColor = ElectricBlue,
                                        unfocusedBorderColor = Slate700,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                            }

                            // Notice Image URL (Required)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "নোটিশ ব্যানার ছবির URL (Image URL) *",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                OutlinedTextField(
                                    value = imageUrl,
                                    onValueChange = onImageUrlChange,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_notice_image_url"),
                                    placeholder = { Text("https://example.com/banner.jpg বা Supabase Storage লিংক", color = Slate400) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = Slate400)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Slate800,
                                        unfocusedContainerColor = Slate800,
                                        focusedBorderColor = ElectricBlue,
                                        unfocusedBorderColor = Slate700,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                // Quick Image URL Suggestions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "নমুনা ছবি:",
                                        fontSize = 10.sp,
                                        color = Slate400,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                    Surface(
                                        color = Slate800,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.clickable {
                                            onImageUrlChange("https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800")
                                        }
                                    ) {
                                        Text(
                                            text = "ব্যানার ১ (Class)",
                                            fontSize = 10.sp,
                                            color = ElectricBlue,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        color = Slate800,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.clickable {
                                            onImageUrlChange("https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=800")
                                        }
                                    ) {
                                        Text(
                                            text = "ব্যানার ২ (Server)",
                                            fontSize = 10.sp,
                                            color = ElectricBlue,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Notice Description (Optional)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "নোটিশের বিস্তারিত বিবরণ (Description) [ঐচ্ছিক]",
                                    style = MaterialTheme.typography.labelMedium.copy(color = Slate400)
                                )
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = onDescriptionChange,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .testTag("input_notice_description"),
                                    placeholder = { Text("নোটিশের মূল বিষয়বস্তু, প্রয়োজনীয় নির্দেশনা ইত্যাদি লিখুন...", color = Slate400) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Slate800,
                                        unfocusedContainerColor = Slate800,
                                        focusedBorderColor = ElectricBlue,
                                        unfocusedBorderColor = Slate700,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    maxLines = 5
                                )
                            }

                            // Action URL (Optional) & Button Text
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1.2f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "অ্যাকশন লিংক (Action URL)",
                                        style = MaterialTheme.typography.labelMedium.copy(color = Slate400)
                                    )
                                    OutlinedTextField(
                                        value = actionUrl,
                                        onValueChange = onActionUrlChange,
                                        placeholder = { Text("https://t.me/...", color = Slate400) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Link, contentDescription = null, tint = Slate400)
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Slate800,
                                            unfocusedContainerColor = Slate800,
                                            focusedBorderColor = ElectricBlue,
                                            unfocusedBorderColor = Slate700,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        modifier = Modifier.testTag("input_notice_action_url")
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(0.8f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "বাটনের নাম",
                                        style = MaterialTheme.typography.labelMedium.copy(color = Slate400)
                                    )
                                    OutlinedTextField(
                                        value = actionButtonText,
                                        onValueChange = onActionButtonTextChange,
                                        placeholder = { Text("বিস্তারিত দেখুন", color = Slate400) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Slate800,
                                            unfocusedContainerColor = Slate800,
                                            focusedBorderColor = ElectricBlue,
                                            unfocusedBorderColor = Slate700,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        modifier = Modifier.testTag("input_notice_button_text")
                                    )
                                }
                            }

                            // Priority (Integer)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "প্রায়োরিটি নম্বর (Priority, e.g. 0, 5, 10) *",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                OutlinedTextField(
                                    value = priority,
                                    onValueChange = { if (it.all { char -> char.isDigit() }) onPriorityChange(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_notice_priority"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    placeholder = { Text("০ (উচ্চ সংখ্যা আগে দেখাবে)", color = Slate400) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Slate800,
                                        unfocusedContainerColor = Slate800,
                                        focusedBorderColor = ElectricBlue,
                                        unfocusedBorderColor = Slate700,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                                Text(
                                    text = "💡 টিপস: উচ্চ প্রায়োরিটি (যেমন 10, 50) নোটিশগুলো সবার উপরে পপ-আপ আকারে প্রদর্শিত হবে।",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }

                            // Checkbox for Show As Popup & Switch for Is Active
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Slate800),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.border(1.dp, Slate700, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onShowAsPopupChange(!showAsPopup) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = showAsPopup,
                                            onCheckedChange = { onShowAsPopupChange(it) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = NeonCyan,
                                                uncheckedColor = Slate400
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "অ্যাপ খোলার সময় পপ-আপ হিসেবে দেখান",
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "শিক্ষার্থী অ্যাপ ওপেন করলে অটোমেটিক পূর্ণাঙ্গ পপ-আপ কার্ড ভেসে উঠবে।",
                                                fontSize = 11.sp,
                                                color = Slate400
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "নোটিশটি সক্রিয় রাখুন (Is Active)",
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = if (isActive) "সক্রিয়: ইউজাররা এই নোটিশ দেখতে পাবে" else "বন্ধ: আপাতত লুকানো থাকবে",
                                                fontSize = 11.sp,
                                                color = if (isActive) EmeraldSuccess else Slate400
                                            )
                                        }

                                        Switch(
                                            checked = isActive,
                                            onCheckedChange = onIsActiveChange,
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = EmeraldSuccess,
                                                uncheckedThumbColor = Slate400,
                                                uncheckedTrackColor = Slate900
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Live Preview Tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📱 শিক্ষার্থী অ্যাপে যেমন দেখাবে (Live Preview):",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Simulated Popup Card
                            SimulatedStudentPopupCard(
                                title = title.ifBlank { "নোটিশের শিরোনাম এখানে প্রদর্শিত হবে" },
                                description = description.ifBlank { "নোটিশের বিস্তারিত বিবরণ এখানে প্রদর্শিত হবে..." },
                                imageUrl = imageUrl,
                                actionButtonText = actionButtonText.ifBlank { "বিস্তারিত দেখুন" },
                                actionUrl = actionUrl,
                                priority = priority.toIntOrNull() ?: 0,
                                showAsPopup = showAsPopup
                            )
                        }
                    }
                }

                // Dialog Bottom Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate800)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onClose) {
                        Text("বাতিল", color = Slate400)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_save_notice")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (editingId == null) "নোটিশ প্রকাশ করুন" else "আপডেট সেভ করুন",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatedStudentPopupCard(
    title: String,
    description: String,
    imageUrl: String,
    actionButtonText: String,
    actionUrl: String,
    priority: Int,
    showAsPopup: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, ElectricBlue, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Simulated Phone Status & Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate950)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldSuccess)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MaxBird Student App • Notice",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                }

                Surface(
                    color = ElectricBlue.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Priority: $priority",
                        fontSize = 9.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Banner Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Slate900),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Notice Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ছবির URL দিলে এখানে ব্যানার দেখাবে",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }
                }
            }

            // Body
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button
                Button(
                    onClick = { /* Simulated Click */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = actionButtonText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (actionUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "পরবর্তীতে (Dismiss)",
                    fontSize = 12.sp,
                    color = Slate400,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun StudentPopupPreviewDialog(
    notice: AppNotice,
    onClose: () -> Unit,
    onShowToast: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "শিক্ষার্থী অ্যাপে পপ-আপ সিমুলেশন",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Slate800)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SimulatedStudentPopupCard(
                    title = notice.title,
                    description = notice.description ?: "",
                    imageUrl = notice.imageUrl,
                    actionButtonText = notice.actionButtonText,
                    actionUrl = notice.actionUrl ?: "",
                    priority = notice.priority,
                    showAsPopup = notice.showAsPopup
                )
            }
        }
    }
}
