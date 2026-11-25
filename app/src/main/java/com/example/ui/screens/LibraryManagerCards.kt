package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LibraryChapter
import com.example.data.model.LibraryClass
import com.example.data.model.LibraryHtmlFile
import com.example.data.model.LibrarySubject
import com.example.data.model.LibraryUtils
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900

@Composable
internal fun LibraryClassCard(
    libraryClass: LibraryClass,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    isBusy: Boolean,
    onOpen: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = parseSafeHexColor(libraryClass.colorHex, EmeraldSuccess)

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (libraryClass.isActive) accent.copy(alpha = 0.45f) else Slate700
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isBusy, onClick = onOpen)
            .testTag("card_library_class_${libraryClass.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = libraryClass.title,
                            color = if (libraryClass.isActive) Color.White else Slate400,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (libraryClass.badge.isNotBlank()) {
                            SmallTagPill(text = libraryClass.badge, color = accent)
                        }
                    }

                    if (libraryClass.subtitle.isNotBlank()) {
                        Text(
                            text = libraryClass.subtitle,
                            color = Slate400,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${libraryClass.subjects.size} সাবজেক্ট • ${libraryClass.totalChapters} অধ্যায় • ${libraryClass.totalFiles} HTML",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open subjects",
                    tint = Slate400,
                    modifier = Modifier.size(24.dp)
                )
            }

            LibraryCardActionBar(
                idText = "ID: ${libraryClass.id} • #${libraryClass.order}",
                isActive = libraryClass.isActive,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                isBusy = isBusy,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onToggleActive = onToggleActive,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    }
}

@Composable
internal fun LibrarySubjectCard(
    subject: LibrarySubject,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    isBusy: Boolean,
    onOpen: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = parseSafeHexColor(subject.colorHex, NeonCyan)

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (subject.isActive) accent.copy(alpha = 0.45f) else Slate700
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isBusy, onClick = onOpen)
            .testTag("card_library_subject_${subject.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = subject.title,
                            color = if (subject.isActive) Color.White else Slate400,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (subject.coverUrl.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Has cover image",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    if (subject.subtitle.isNotBlank()) {
                        Text(
                            text = subject.subtitle,
                            color = Slate400,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${subject.chapters.size} অধ্যায় • ${subject.totalFiles} HTML ফাইল",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open chapters",
                    tint = Slate400,
                    modifier = Modifier.size(24.dp)
                )
            }

            LibraryCardActionBar(
                idText = "ID: ${subject.id} • #${subject.order}",
                isActive = subject.isActive,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                isBusy = isBusy,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onToggleActive = onToggleActive,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    }
}

@Composable
internal fun LibraryChapterCard(
    chapter: LibraryChapter,
    accentColor: Color,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    isBusy: Boolean,
    onOpen: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (chapter.isActive) accentColor.copy(alpha = 0.42f) else Slate700
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isBusy, onClick = onOpen)
            .testTag("card_library_chapter_${chapter.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${chapter.chapterNumber}",
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = chapter.title,
                        color = if (chapter.isActive) Color.White else Slate400,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (chapter.subtitle.isNotBlank()) {
                        Text(
                            text = chapter.subtitle,
                            color = Slate400,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "অধ্যায় ${chapter.chapterNumber} • ${chapter.files.size}টি HTML ফাইল",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open HTML files",
                    tint = Slate400,
                    modifier = Modifier.size(24.dp)
                )
            }

            LibraryCardActionBar(
                idText = "ID: ${chapter.id} • #${chapter.order}",
                isActive = chapter.isActive,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                isBusy = isBusy,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onToggleActive = onToggleActive,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    }
}

@Composable
internal fun LibraryHtmlFileCard(
    file: LibraryHtmlFile,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    isBusy: Boolean,
    onReplaceHtml: () -> Unit,
    onCopyPublicUrl: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (file.isActive) NeonCyan.copy(alpha = 0.4f) else Slate700
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_library_file_${file.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonCyan.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = file.title,
                            color = if (file.isActive) Color.White else Slate400,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        SmallTagPill(
                            text = "v${file.version}",
                            color = if (file.version > 1) AmberWarning else EmeraldSuccess
                        )
                    }

                    if (file.description.isNotBlank()) {
                        Text(
                            text = file.description,
                            color = Slate200,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${LibraryUtils.formatFileSize(file.fileSizeBytes)} • ${file.fileUrl}",
                        color = Slate400,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Primary HTML File Actions: Replace (+1 Version) & Copy Public URL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReplaceHtml,
                    enabled = !isBusy,
                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_replace_html_${file.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Replace HTML file",
                        tint = AmberWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "রিপ্লেস (v${file.version + 1})",
                        color = AmberWarning,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onCopyPublicUrl,
                    enabled = !isBusy,
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_copy_html_url_${file.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy public link",
                        tint = NeonCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "R2 লিংক কপি",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            LibraryCardActionBar(
                idText = "ID: ${file.id} • #${file.order}",
                isActive = file.isActive,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                isBusy = isBusy,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onToggleActive = onToggleActive,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun LibraryCardActionBar(
    idText: String,
    isActive: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    isBusy: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Slate900,
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isActive) EmeraldSuccess else Slate400)
                )
                Text(
                    text = idText,
                    color = Slate400,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp && !isBusy,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move up",
                        tint = if (canMoveUp && !isBusy) Slate200 else Slate700,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown && !isBusy,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move down",
                        tint = if (canMoveDown && !isBusy) Slate200 else Slate700,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onToggleActive,
                    enabled = !isBusy,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isActive) "Disable" else "Enable",
                        tint = if (isActive) EmeraldSuccess else Slate400,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    enabled = !isBusy,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit item",
                        tint = NeonCyan,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    enabled = !isBusy,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = CoralDanger,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun SmallTagPill(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}
