package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ActivationSupportLink
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

private data class SupportPlatform(
    val type: String,
    val nameEn: String,
    val nameBn: String,
    val defaultPrefix: String,
    val defaultColor: String,
    val icon: ImageVector
)

private val SUPPORT_PLATFORMS = listOf(
    SupportPlatform("telegram", "Telegram", "টেলিগ্রাম", "https://t.me/", "#229ED9", Icons.Default.Send),
    SupportPlatform("whatsapp", "WhatsApp", "হোয়াটসঅ্যাপ", "https://wa.me/", "#25D366", Icons.Default.Forum),
    SupportPlatform("facebook", "Facebook", "ফেসবুক", "https://facebook.com/", "#1877F2", Icons.Default.ThumbUp),
    SupportPlatform("phone", "Phone Call", "ফোন কল", "tel:+880", "#10B981", Icons.Default.Call),
    SupportPlatform("website", "Website", "ওয়েবসাইট", "https://", "#0EA5E9", Icons.Default.Language)
)

private fun getPlatformInfo(type: String): SupportPlatform {
    return SUPPORT_PLATFORMS.firstOrNull { it.type.equals(type, ignoreCase = true) }
        ?: SupportPlatform("website", "Link", "লিংক", "https://", "#3B82F6", Icons.Default.Link)
}

private fun parseColorSafely(hex: String?, fallback: Color = ElectricBlue): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val cleanHex = hex.trim().removePrefix("#")
        val colorInt = when (cleanHex.length) {
            6 -> (0xFF000000 or cleanHex.toLong(16)).toInt()
            8 -> cleanHex.toLong(16).toInt()
            else -> return fallback
        }
        Color(colorInt)
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun ActivationSupportLinksScreen(
    supportLinks: List<ActivationSupportLink>,
    isLoading: Boolean,
    searchQuery: String,
    filter: String,
    isDialogOpen: Boolean,
    editingId: String?,
    title: String,
    subtitle: String,
    iconType: String,
    url: String,
    colorHex: String,
    priority: String,
    isActive: Boolean,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onOpenCreateDialog: () -> Unit,
    onOpenEditDialog: (ActivationSupportLink) -> Unit,
    onCloseDialog: () -> Unit,
    onTitleChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onIconTypeChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onColorHexChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSaveLink: () -> Unit,
    onToggleStatus: (ActivationSupportLink) -> Unit,
    onDeleteLink: (ActivationSupportLink) -> Unit,
    onRefresh: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var deleteCandidate by remember { mutableStateOf<ActivationSupportLink?>(null) }

    // Filter and Search logic
    val filteredLinks = remember(supportLinks, searchQuery, filter) {
        supportLinks.filter { link ->
            val matchesSearch = searchQuery.isBlank() ||
                link.title.contains(searchQuery, ignoreCase = true) ||
                (link.subtitle?.contains(searchQuery, ignoreCase = true) == true) ||
                link.url.contains(searchQuery, ignoreCase = true) ||
                link.iconType.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (filter) {
                "ACTIVE" -> link.isActive
                "INACTIVE" -> !link.isActive
                "TELEGRAM" -> link.iconType.equals("telegram", ignoreCase = true)
                "WHATSAPP" -> link.iconType.equals("whatsapp", ignoreCase = true)
                "FACEBOOK" -> link.iconType.equals("facebook", ignoreCase = true)
                "PHONE" -> link.iconType.equals("phone", ignoreCase = true)
                "WEBSITE" -> link.iconType.equals("website", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesFilter
        }.sortedWith(
            compareByDescending<ActivationSupportLink> { it.priority }
                .thenByDescending { it.createdAt ?: "" }
        )
    }

    val activeCount = supportLinks.count { it.isActive }
    val totalCount = supportLinks.size

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenCreateDialog,
                containerColor = NeonCyan,
                contentColor = Slate950,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_create_support_link")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Support Link",
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        containerColor = Slate900
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate900)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HeadsetMic,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Device Activation Support",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                        Text(
                            text = "সাপোর্ট লিংক ম্যানেজমেন্ট",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "শিক্ষার্থীদের অ্যাপে প্রদর্শিত হেল্প ও কন্টাক্ট লিংকসমূহ",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Slate800)
                                .testTag("btn_refresh_support_links")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = NeonCyan
                                )
                            }
                        }

                        Button(
                            onClick = onOpenCreateDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("btn_add_support_link_header")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("নতুন লিংক", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Stats Metrics Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalCount",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "মোট লিংক",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Slate700)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$activeCount",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldSuccess
                                )
                            )
                            Text(
                                text = "সক্রিয় (Active)",
                                style = MaterialTheme.typography.labelSmall.copy(color = EmeraldSuccess)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Slate700)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${totalCount - activeCount}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (totalCount - activeCount > 0) CoralDanger else Slate400
                                )
                            )
                            Text(
                                text = "বন্ধ (Inactive)",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_support_links"),
                    placeholder = {
                        Text(
                            text = "নাম, বিবরণ বা URL দিয়ে খুঁজুন...",
                            color = Slate600,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Slate400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Slate700
                    )
                )
            }

            // Quick Filter Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filterOptions = listOf(
                        "ALL" to "সকল লিংক (${totalCount})",
                        "ACTIVE" to "সক্রিয় (${activeCount})",
                        "INACTIVE" to "নিষ্ক্রিয় (${totalCount - activeCount})",
                        "TELEGRAM" to "টেলিগ্রাম",
                        "WHATSAPP" to "হোয়াটসঅ্যাপ",
                        "FACEBOOK" to "ফেসবুক",
                        "PHONE" to "ফোন",
                        "WEBSITE" to "ওয়েবসাইট"
                    )

                    items(filterOptions) { (key, label) ->
                        val isSelected = filter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterChange(key) },
                            label = { Text(text = label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Slate800,
                                labelColor = Slate400,
                                selectedContainerColor = ElectricBlue,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NeonCyan else Slate700,
                                selectedBorderColor = NeonCyan
                            )
                        )
                    }
                }
            }

            // Link Items List
            if (filteredLinks.isEmpty()) {
                item {
                    EmptyLinksCard(
                        searchQuery = searchQuery,
                        onOpenCreateDialog = onOpenCreateDialog
                    )
                }
            } else {
                items(filteredLinks, key = { it.id }) { link ->
                    SupportLinkCard(
                        link = link,
                        onToggle = { onToggleStatus(link) },
                        onEdit = { onOpenEditDialog(link) },
                        onDelete = { deleteCandidate = link },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(link.url))
                            onShowToast("লিংক কপি করা হয়েছে: ${link.url}")
                        },
                        onTest = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                onShowToast("লিংক ওপেন করা যায়নি: ${e.message}")
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add / Edit Modal Dialog
    if (isDialogOpen) {
        SupportLinkEditDialog(
            isEditing = editingId != null,
            title = title,
            subtitle = subtitle,
            iconType = iconType,
            url = url,
            colorHex = colorHex,
            priority = priority,
            isActive = isActive,
            onTitleChange = onTitleChange,
            onSubtitleChange = onSubtitleChange,
            onIconTypeChange = onIconTypeChange,
            onUrlChange = onUrlChange,
            onColorHexChange = onColorHexChange,
            onPriorityChange = onPriorityChange,
            onIsActiveChange = onIsActiveChange,
            onSave = onSaveLink,
            onDismiss = onCloseDialog
        )
    }

    // Delete Confirmation Dialog
    deleteCandidate?.let { link ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = {
                Text(
                    text = "সাপোর্ট লিংক ডিলিট করবেন?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            text = {
                Text(
                    text = "'${link.title}' লিংকটি সম্পূর্ণ মুছে ফেলা হবে। আপনি কি নিশ্চিত?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = deleteCandidate
                        deleteCandidate = null
                        target?.let { onDeleteLink(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger)
                ) {
                    Text("হ্যাঁ, ডিলিট করুন", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { deleteCandidate = null }
                ) {
                    Text("বাতিল", color = Slate400)
                }
            },
            containerColor = Slate800,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SupportLinkCard(
    link: ActivationSupportLink,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onTest: () -> Unit
) {
    val platform = getPlatformInfo(link.iconType)
    val brandColor = parseColorSafely(link.colorHex, parseColorSafely(platform.defaultColor))

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (link.isActive) 1.dp else 1.dp,
                color = if (link.isActive) brandColor.copy(alpha = 0.4f) else Slate700,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("support_link_card_${link.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Platform Icon Badge, Title/Subtitle, and Active Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Distinct Brand Icon Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(brandColor.copy(alpha = if (link.isActive) 0.18f else 0.08f))
                            .border(1.5.dp, brandColor.copy(alpha = if (link.isActive) 0.8f else 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = platform.icon,
                            contentDescription = platform.nameEn,
                            tint = if (link.isActive) brandColor else Slate400,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = link.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (link.isActive) Color.White else Slate400
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (!link.subtitle.isNullOrBlank()) {
                            Text(
                                text = link.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (link.isActive) Slate400 else Slate600
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Switch and Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Switch(
                        checked = link.isActive,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldSuccess,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate900
                        ),
                        modifier = Modifier.testTag("switch_link_${link.id}")
                    )
                }
            }

            // Target URL Pill
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCopy() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = link.url,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy URL",
                                tint = Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        IconButton(
                            onClick = onTest,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Test link",
                                tint = ElectricBlueLight,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Badges & Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type Badge
                    Surface(
                        color = brandColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = platform.nameBn,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = brandColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    // Priority Badge
                    Surface(
                        color = ElectricBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "প্রায়োরিটি: ${link.priority}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Status Badge
                    Surface(
                        color = if (link.isActive) EmeraldSuccess.copy(alpha = 0.15f) else Slate700,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (link.isActive) "সক্রিয়" else "বন্ধ",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (link.isActive) EmeraldSuccess else Slate400,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Edit & Delete Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Slate700)
                            .testTag("btn_edit_link_${link.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = ElectricBlueLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Slate700)
                            .testTag("btn_delete_link_${link.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = CoralDanger,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportLinkEditDialog(
    isEditing: Boolean,
    title: String,
    subtitle: String,
    iconType: String,
    url: String,
    colorHex: String,
    priority: String,
    isActive: Boolean,
    onTitleChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onIconTypeChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onColorHexChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val activePlatform = getPlatformInfo(iconType)
    val previewColor = parseColorSafely(colorHex, parseColorSafely(activePlatform.defaultColor))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dialog Header
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
                                .background(previewColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = activePlatform.icon,
                                contentDescription = null,
                                tint = previewColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEditing) "সাপোর্ট লিংক এডিট করুন" else "নতুন সাপোর্ট লিংক যোগ করুন",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "শিক্ষার্থী অ্যাপের অ্যাক্টিভেশন স্ক্রিনে দৃশ্যমান হবে",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Slate800)
                            .size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                // Platform / Icon Type Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "প্ল্যাটফর্ম / আইকন টাইপ নির্বাচন করুন:*",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SUPPORT_PLATFORMS) { plat ->
                            val isSelected = plat.type.equals(iconType, ignoreCase = true)
                            val chipColor = parseColorSafely(plat.defaultColor)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) chipColor else Slate800,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) chipColor else Slate700
                                ),
                                modifier = Modifier
                                    .clickable {
                                        onIconTypeChange(plat.type)
                                        // If url is empty or was using other prefix, update with default
                                        if (url.isBlank() || SUPPORT_PLATFORMS.any { url == it.defaultPrefix }) {
                                            onUrlChange(plat.defaultPrefix)
                                        }
                                        // Auto suggest title if blank
                                        if (title.isBlank()) {
                                            onTitleChange("${plat.nameBn} সাপোর্ট")
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = plat.icon,
                                        contentDescription = plat.nameEn,
                                        tint = if (isSelected) Color.White else chipColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = plat.nameBn,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Slate400
                                    )
                                }
                            }
                        }
                    }
                }

                // Title Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("শিরোনাম (Title):*", style = MaterialTheme.typography.labelMedium.copy(color = Color.White))
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_support_title"),
                        placeholder = { Text("উদা: Telegram সাপোর্ট বা WhatsApp গ্রুপ", color = Slate600) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Slate700
                        )
                    )
                }

                // Subtitle Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("সাবটাইটেল / বিবরণ (Subtitle):", style = MaterialTheme.typography.labelMedium.copy(color = Color.White))
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = onSubtitleChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_support_subtitle"),
                        placeholder = { Text("উদা: @fahim017740 এ মেসেজ দিন", color = Slate600) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Slate700
                        )
                    )
                }

                // Target URL Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("টার্গেট URL বা লিংক (Target URL):*", style = MaterialTheme.typography.labelMedium.copy(color = Color.White))
                    OutlinedTextField(
                        value = url,
                        onValueChange = onUrlChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_support_url"),
                        placeholder = { Text("উদা: https://t.me/fahim017740", color = Slate600) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Slate700
                        )
                    )
                }

                // Priority and Color Palette Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Priority Field
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("প্রায়োরিটি নম্বর:*", style = MaterialTheme.typography.labelMedium.copy(color = Color.White))
                        OutlinedTextField(
                            value = priority,
                            onValueChange = onPriorityChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_support_priority"),
                            placeholder = { Text("10", color = Slate600) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Slate700
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("1", "5", "10", "20").forEach { pVal ->
                                Surface(
                                    color = Slate800,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable { onPriorityChange(pVal) }
                                ) {
                                    Text(
                                        text = "+$pVal",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        color = NeonCyan
                                    )
                                }
                            }
                        }
                    }

                    // Color Hex
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("রং হেক্স কোড (Hex):", style = MaterialTheme.typography.labelMedium.copy(color = Color.White))
                        OutlinedTextField(
                            value = colorHex,
                            onValueChange = onColorHexChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_support_color"),
                            placeholder = { Text("#229ED9", color = Slate600) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(previewColor)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Slate700
                            )
                        )
                        // Preset color swatch dots
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("#229ED9", "#25D366", "#1877F2", "#10B981", "#0EA5E9", "#EC4899").forEach { col ->
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(parseColorSafely(col))
                                        .border(
                                            1.dp,
                                            if (colorHex.equals(col, ignoreCase = true)) Color.White else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { onColorHexChange(col) }
                                )
                            }
                        }
                    }
                }

                // Active Switch
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "লিংক স্ট্যাটাস (Active / Inactive)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (isActive) "শিক্ষার্থীদের স্ক্রিনে প্রদর্শিত হবে" else "এখন বন্ধ থাকবে",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isActive) EmeraldSuccess else Slate400
                                )
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

                // REAL-TIME LIVE PREVIEW
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "শিক্ষার্থী অ্যাপে যেমন দেখাবে (Live Preview):",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // User App Device Activation Screen Card Preview
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate950),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, previewColor, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(previewColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = activePlatform.icon,
                                        contentDescription = null,
                                        tint = previewColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = title.ifBlank { "${activePlatform.nameBn} সাপোর্ট" },
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = subtitle.ifBlank { "যোগাযোগ করতে এখানে ট্যাপ করুন" },
                                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = previewColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate400)
                    ) {
                        Text("বাতিল")
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_support_link"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Slate950, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditing) "আপডেট করুন" else "সেভ করুন",
                            color = Slate950,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLinksCard(
    searchQuery: String,
    onOpenCreateDialog: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Slate700),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HeadsetMic,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = if (searchQuery.isNotBlank()) "কোনো লিংক পাওয়া যায়নি" else "কোনো সাপোর্ট লিংক যোগ করা হয়নি",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = if (searchQuery.isNotBlank()) {
                    "'$searchQuery' এর সাথে মিল থাকা কোনো লিংক নেই। দয়া করে অন্য কিছু লিখুন।"
                } else {
                    "শিক্ষার্থীদের ডিভাইস অ্যাক্টিভেশন পেজে টেলিগ্রাম, হোয়াটসঅ্যাপ বা হেল্পলাইন লিংক প্রদর্শনের জন্য নতুন লিংক যোগ করুন।"
                },
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Button(
                onClick = onOpenCreateDialog,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Slate950)
                Spacer(modifier = Modifier.width(6.dp))
                Text("নতুন সাপোর্ট লিংক যোগ করুন", color = Slate950, fontWeight = FontWeight.Bold)
            }
        }
    }
}
