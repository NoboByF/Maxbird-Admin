package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourseCatalogItem
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
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
import com.example.ui.theme.Violet500

data class SyllabusClassOption(
    val code: String,
    val labelBn: String,
    val shortBn: String
)

data class SyllabusGroupOption(
    val code: String,
    val labelBn: String
)

val ALL_CLASS_OPTIONS = listOf(
    SyllabusClassOption("C5", "পঞ্চম শ্রেণি (C5)", "৫ম (C5)"),
    SyllabusClassOption("C6", "ষষ্ঠ শ্রেণি (C6)", "৬ষ্ঠ (C6)"),
    SyllabusClassOption("C7", "সপ্তম শ্রেণি (C7)", "৭ম (C7)"),
    SyllabusClassOption("C8", "অষ্টম শ্রেণি (C8)", "৮ম (C8)"),
    SyllabusClassOption("C9", "নবম শ্রেণি (C9)", "৯ম (C9)"),
    SyllabusClassOption("C10", "দশম শ্রেণি (C10)", "১০ম (C10)"),
    SyllabusClassOption("C11", "একাদশ শ্রেণি (C11)", "১১শ (C11)"),
    SyllabusClassOption("C12", "দ্বাদশ শ্রেণি (C12)", "১২শ (C12)")
)

val ALL_GROUP_OPTIONS = listOf(
    SyllabusGroupOption("Science", "বিজ্ঞান (Science)"),
    SyllabusGroupOption("Humanities", "মানবিক (Humanities)"),
    SyllabusGroupOption("BusinessStudies", "ব্যবসায় শিক্ষা (Business)")
)

val DEFAULT_BATCH_OPTIONS = listOf("2026", "2027", "2028", "2029")

private val GROUP_APPLICABLE_CLASSES = setOf("C9", "C10", "C11", "C12")

fun formatClassCodeBn(code: String): String {
    return ALL_CLASS_OPTIONS.find { it.code.equals(code, ignoreCase = true) }?.shortBn ?: code
}

fun formatGroupCodeBn(code: String): String {
    return when (code) {
        "Science" -> "বিজ্ঞান"
        "Humanities" -> "মানবিক"
        "BusinessStudies" -> "ব্যবসায় শিক্ষা"
        else -> code
    }
}

/**
 * Reusable BottomSheet for configuring Device-Specific (or Access-Code-Specific)
 * Granular Syllabus & Course Access Control.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CourseAccessControlBottomSheet(
    targetTitle: String,
    targetSubtitle: String,
    targetBadge: String? = null,
    initialIsRestricted: Boolean,
    initialAllowedClasses: List<String>,
    initialAllowedGroups: List<String>,
    initialAllowedBatches: List<String>,
    initialAllowedPrograms: List<String>,
    coursesCatalog: List<CourseCatalogItem>,
    isLoadingCatalog: Boolean,
    isSaving: Boolean,
    onRefreshCatalog: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (
        isRestricted: Boolean,
        allowedClasses: List<String>,
        allowedGroups: List<String>,
        allowedBatches: List<String>,
        allowedPrograms: List<String>
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isRestricted by remember(initialIsRestricted) { mutableStateOf(initialIsRestricted) }
    var selectedClasses by remember(initialAllowedClasses) { mutableStateOf(initialAllowedClasses.toSet()) }
    var selectedGroups by remember(initialAllowedGroups) { mutableStateOf(initialAllowedGroups.toSet()) }
    var selectedBatches by remember(initialAllowedBatches) { mutableStateOf(initialAllowedBatches.toSet()) }
    var selectedPrograms by remember(initialAllowedPrograms) { mutableStateOf(initialAllowedPrograms.toSet()) }

    var customBatchInput by remember { mutableStateOf("") }
    var customProgramInput by remember { mutableStateOf("") }
    var catalogSearchQuery by remember { mutableStateOf("") }
    var showAllCatalogCourses by remember { mutableStateOf(false) }

    val hasHigherClassSelected = remember(selectedClasses, selectedGroups) {
        selectedClasses.any { it in GROUP_APPLICABLE_CLASSES } || selectedGroups.isNotEmpty()
    }

    // Filter courses from available_courses_catalog matching selected classes & groups
    val filteredCatalogCourses = remember(
        coursesCatalog,
        selectedClasses,
        selectedGroups,
        catalogSearchQuery,
        showAllCatalogCourses
    ) {
        coursesCatalog.filter { course ->
            val classMatches = if (showAllCatalogCourses || selectedClasses.isEmpty()) {
                true
            } else {
                val cCode = course.classCode?.trim()
                cCode.isNullOrBlank() || selectedClasses.any { it.equals(cCode, ignoreCase = true) }
            }

            val groupMatches = if (showAllCatalogCourses || selectedGroups.isEmpty()) {
                true
            } else {
                val gCode = course.groupCode?.trim()
                val cCode = course.groupCode?.let { course.classCode?.trim()?.uppercase() }
                // Only filter by group if the course has a specific group_code
                if (gCode.isNullOrBlank() || (cCode != null && cCode !in GROUP_APPLICABLE_CLASSES)) {
                    true
                } else {
                    selectedGroups.any { it.equals(gCode, ignoreCase = true) }
                }
            }

            val searchMatches = if (catalogSearchQuery.isBlank()) {
                true
            } else {
                course.titleBn.contains(catalogSearchQuery, ignoreCase = true) ||
                        course.programId.contains(catalogSearchQuery, ignoreCase = true) ||
                        (course.classCode?.contains(catalogSearchQuery, ignoreCase = true) == true) ||
                        (course.groupCode?.contains(catalogSearchQuery, ignoreCase = true) == true) ||
                        (course.batchId?.contains(catalogSearchQuery, ignoreCase = true) == true)
            }

            classMatches && groupMatches && searchMatches
        }
    }

    // All batch chips = default 2026..2029 + any custom batches already in selectedBatches
    val allBatchChips = remember(selectedBatches) {
        (DEFAULT_BATCH_OPTIONS + selectedBatches).distinct()
    }

    // Custom manually added programs (not matching programId of catalog or explicitly added)
    val catalogIdsAndTitles = remember(coursesCatalog) {
        coursesCatalog.flatMap { listOf(it.programId, it.titleBn) }.filter { it.isNotBlank() }.toSet()
    }
    val customAddedPrograms = remember(selectedPrograms, catalogIdsAndTitles) {
        selectedPrograms.filter { it.isNotBlank() && it !in catalogIdsAndTitles }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate900,
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Slate700)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
        ) {
            // Top Sheet Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(ElectricBlue, PurpleRoyal))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "কোর্স ও সিলেবাস এক্সেস কন্ট্রোল",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Granular Syllabus & Course Access Control",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Slate800)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Device / Access Code Info Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate950,
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = targetTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = targetSubtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontFamily = FontFamily.Monospace
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!targetBadge.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElectricBlue.copy(alpha = 0.15f),
                                border = BorderStroke(0.8.dp, ElectricBlue)
                            ) {
                                Text(
                                    text = targetBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ElectricBlueLight,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Slate800, thickness = 1.dp)

            // Scrollable Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Restrict Access Master Switch Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRestricted) Slate850 else Slate800
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.2.dp,
                            if (isRestricted) AmberWarning.copy(alpha = 0.7f) else EmeraldSuccess.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isRestricted) AmberWarning.copy(alpha = 0.2f)
                                            else EmeraldSuccess.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isRestricted) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (isRestricted) AmberWarning else EmeraldSuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "কাস্টম কোর্স লক সক্রিয় করুন (Restrict Access)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = if (isRestricted) {
                                            "অনুমোদিত ক্লাস, বিভাগ, ব্যাচ ও কোর্স ব্যতীত বাকি সব লক থাকবে"
                                        } else {
                                            "বর্তমানে সকল ক্লাস ও কোর্স এই ডিভাইসের জন্য উন্মুক্ত (Unrestricted)"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isRestricted) AmberWarning else EmeraldSuccess
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = isRestricted,
                                onCheckedChange = { isRestricted = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AmberWarning,
                                    uncheckedThumbColor = Slate200,
                                    uncheckedTrackColor = Slate700
                                ),
                                modifier = Modifier.testTag("switch_restrict_course_access")
                            )
                        }
                    }
                }

                // 2. Class Selection (C5 to C12)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "শ্রেণি নির্বাচন করুন (Allowed Classes)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "পঞ্চম থেকে দ্বাদশ শ্রেণি (Multi-select)",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = {
                                        selectedClasses = ALL_CLASS_OPTIONS.map { it.code }.toSet()
                                        isRestricted = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("সব", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                                }
                                if (selectedClasses.isNotEmpty()) {
                                    TextButton(
                                        onClick = { selectedClasses = emptySet() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("মুছুন", fontSize = 11.sp, color = CoralDanger)
                                    }
                                }
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ALL_CLASS_OPTIONS.forEach { option ->
                                val isSelected = option.code in selectedClasses
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedClasses = if (isSelected) {
                                            selectedClasses - option.code
                                        } else {
                                            isRestricted = true
                                            selectedClasses + option.code
                                        }
                                        // Clear groups if no C9-C12 class remains selected
                                        if (selectedClasses.none { it in GROUP_APPLICABLE_CLASSES }) {
                                            selectedGroups = emptySet()
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = option.labelBn,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricBlue,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                        containerColor = Slate900,
                                        labelColor = Slate200
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = Slate700,
                                        selectedBorderColor = NeonCyan
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("chip_class_${option.code}")
                                )
                            }
                        }
                    }
                }

                // 3. Group Selection (Science, Humanities, BusinessStudies — if C9-C12 selected)
                AnimatedVisibility(visible = hasHigherClassSelected) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate850),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PurpleRoyal.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = Violet500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "বিভাগ নির্বাচন করুন (Allowed Groups)",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "নবম-দ্বাদশ শ্রেণির জন্য প্রযোজ্য (বিজ্ঞান, মানবিক, ব্যবসায় শিক্ষা)",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                                        )
                                    }
                                }
                            }

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ALL_GROUP_OPTIONS.forEach { group ->
                                    val isSelected = group.code in selectedGroups
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedGroups = if (isSelected) {
                                                selectedGroups - group.code
                                            } else {
                                                isRestricted = true
                                                selectedGroups + group.code
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = group.labelBn,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PurpleRoyal,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White,
                                            containerColor = Slate900,
                                            labelColor = Slate200
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = Slate700,
                                            selectedBorderColor = Violet500
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("chip_group_${group.code}")
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Batch / Exam Year Selection (2026, 2027, 2028, 2029 + Custom Batch Input)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ব্যাচ / পরীক্ষার সাল নির্বাচন (Allowed Batches)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "চিপস সিলেক্ট করুন অথবা কাস্টম সাল লিখে অ্যাড করুন",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                                )
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allBatchChips.forEach { batch ->
                                val isSelected = batch in selectedBatches
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedBatches = if (isSelected) {
                                            selectedBatches - batch
                                        } else {
                                            isRestricted = true
                                            selectedBatches + batch
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = "ব্যাচ $batch",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldSuccess,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                        containerColor = Slate900,
                                        labelColor = Slate200
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = Slate700,
                                        selectedBorderColor = EmeraldSuccess
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("chip_batch_$batch")
                                )
                            }
                        }

                        // Custom Batch / Year Add Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customBatchInput,
                                onValueChange = { customBatchInput = it },
                                placeholder = {
                                    Text(
                                        text = "কাস্টম সাল/ব্যাচ লিখুন (যেমন: 2030)",
                                        fontSize = 12.sp,
                                        color = Slate400
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate900,
                                    unfocusedContainerColor = Slate900,
                                    focusedBorderColor = EmeraldSuccess,
                                    unfocusedBorderColor = Slate700,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_custom_batch")
                            )

                            Button(
                                onClick = {
                                    val trimmed = customBatchInput.trim()
                                    if (trimmed.isNotBlank()) {
                                        selectedBatches = selectedBatches + trimmed
                                        isRestricted = true
                                        customBatchInput = ""
                                    }
                                },
                                enabled = customBatchInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldSuccess,
                                    disabledContainerColor = Slate800
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                modifier = Modifier.testTag("btn_add_custom_batch")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("যোগ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // 5. Allowed Courses / Programs Selection (from `available_courses_catalog` + Custom Input)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
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
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = ElectricBlueLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "অনুমোদিত কোর্সসমূহ (Allowed Programs)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "নির্বাচিত কোর্স: ${selectedPrograms.size} টি • available_courses_catalog",
                                        style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onRefreshCatalog,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Slate900)
                            ) {
                                if (isLoadingCatalog) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = NeonCyan,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Catalog",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Manual Course Name / Program ID Input Box
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "ম্যানুয়ালি কোর্সের নাম বা Program ID যোগ করুন:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate200,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customProgramInput,
                                    onValueChange = { customProgramInput = it },
                                    placeholder = {
                                        Text(
                                            text = "Program ID বা কোর্সের বাংলা নাম...",
                                            fontSize = 12.sp,
                                            color = Slate400
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Slate900,
                                        unfocusedContainerColor = Slate900,
                                        focusedBorderColor = ElectricBlue,
                                        unfocusedBorderColor = Slate700,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_custom_program")
                                )

                                Button(
                                    onClick = {
                                        val trimmed = customProgramInput.trim()
                                        if (trimmed.isNotBlank()) {
                                            selectedPrograms = selectedPrograms + trimmed
                                            isRestricted = true
                                            customProgramInput = ""
                                        }
                                    },
                                    enabled = customProgramInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricBlue,
                                        disabledContainerColor = Slate800
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                    modifier = Modifier.testTag("btn_add_custom_program")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("যোগ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        // Display Custom Added Programs as removable chips
                        if (customAddedPrograms.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "ম্যানুয়ালি যুক্ত করা কোর্স/প্রোগ্রাম (${customAddedPrograms.size}):",
                                    style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan)
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    customAddedPrograms.forEach { prog ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = ElectricBlue.copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, ElectricBlue)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = prog,
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = CoralDanger,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable {
                                                            selectedPrograms = selectedPrograms - prog
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Slate700.copy(alpha = 0.6f), thickness = 0.8.dp)

                        // Search & Filter controls for Catalog Courses
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedClasses.isEmpty() || showAllCatalogCourses) {
                                    "ক্যাটালগের সকল কোর্স (${filteredCatalogCourses.size})"
                                } else {
                                    "নির্বাচিত শ্রেণির কোর্সসমূহ (${filteredCatalogCourses.size})"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (selectedClasses.isNotEmpty()) {
                                    TextButton(
                                        onClick = { showAllCatalogCourses = !showAllCatalogCourses },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (showAllCatalogCourses) "শ্রেণি অনুযায়ী ফিল্টার" else "সব ক্লাসের কোর্স দেখুন",
                                            fontSize = 11.sp,
                                            color = NeonCyan
                                        )
                                    }
                                }
                                if (filteredCatalogCourses.isNotEmpty()) {
                                    TextButton(
                                        onClick = {
                                            val allIds = filteredCatalogCourses.map {
                                                it.programId.ifBlank { it.titleBn }
                                            }.filter { it.isNotBlank() }
                                            selectedPrograms = selectedPrograms + allIds
                                            isRestricted = true
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("সব মার্ক", fontSize = 11.sp, color = EmeraldSuccess)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = catalogSearchQuery,
                            onValueChange = { catalogSearchQuery = it },
                            placeholder = {
                                Text(
                                    text = "ক্যাটালগ থেকে কোর্স খুঁজুন...",
                                    fontSize = 12.sp,
                                    color = Slate400
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = {
                                if (catalogSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { catalogSearchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Slate400,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Course Catalog Checkbox List
                        if (filteredCatalogCourses.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Slate900,
                                border = BorderStroke(1.dp, Slate800),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "নির্বাচিত ক্লাস/বিভাগে কোনো ক্যাটালগ কোর্স পাওয়া যায়নি",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate200,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = "উপরে ম্যানুয়ালি Program ID বা কোর্সের নাম লিখে যোগ করতে পারেন।",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                filteredCatalogCourses.forEach { course ->
                                    val primaryIdentifier = course.programId.ifBlank { course.titleBn }
                                    val isChecked = (course.programId.isNotBlank() && course.programId in selectedPrograms) ||
                                            (course.titleBn.isNotBlank() && course.titleBn in selectedPrograms)

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isChecked) ElectricBlue.copy(alpha = 0.14f) else Slate900,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isChecked) ElectricBlue else Slate800
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (isChecked) {
                                                    selectedPrograms = selectedPrograms - setOfNotNull(
                                                        course.programId.takeIf { it.isNotBlank() },
                                                        course.titleBn.takeIf { it.isNotBlank() }
                                                    )
                                                } else {
                                                    if (primaryIdentifier.isNotBlank()) {
                                                        selectedPrograms = selectedPrograms + primaryIdentifier
                                                        isRestricted = true
                                                    }
                                                }
                                            }
                                            .testTag("course_item_${course.programId}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    if (checked) {
                                                        if (primaryIdentifier.isNotBlank()) {
                                                            selectedPrograms = selectedPrograms + primaryIdentifier
                                                            isRestricted = true
                                                        }
                                                    } else {
                                                        selectedPrograms = selectedPrograms - setOfNotNull(
                                                            course.programId.takeIf { it.isNotBlank() },
                                                            course.titleBn.takeIf { it.isNotBlank() }
                                                        )
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = ElectricBlue,
                                                    uncheckedColor = Slate400,
                                                    checkmarkColor = Color.White
                                                )
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = course.titleBn.ifBlank { course.programId },
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        fontSize = 13.sp
                                                    )
                                                )

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (course.programId.isNotBlank()) {
                                                        Text(
                                                            text = "ID: ${course.programId}",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = NeonCyan,
                                                                fontFamily = FontFamily.Monospace,
                                                                fontSize = 10.sp
                                                            )
                                                        )
                                                    }
                                                    if (!course.classCode.isNullOrBlank()) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Slate800
                                                        ) {
                                                            Text(
                                                                text = formatClassCodeBn(course.classCode),
                                                                fontSize = 10.sp,
                                                                color = Slate200,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                    if (!course.groupCode.isNullOrBlank()) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = PurpleRoyal.copy(alpha = 0.25f)
                                                        ) {
                                                            Text(
                                                                text = formatGroupCodeBn(course.groupCode),
                                                                fontSize = 10.sp,
                                                                color = Violet500,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                    if (!course.batchId.isNullOrBlank()) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = EmeraldSuccess.copy(alpha = 0.2f)
                                                        ) {
                                                            Text(
                                                                text = course.batchId,
                                                                fontSize = 10.sp,
                                                                color = EmeraldSuccess,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                    if (course.isFree == true) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = EmeraldSuccess.copy(alpha = 0.25f)
                                                        ) {
                                                            Text(
                                                                text = "FREE",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = EmeraldSuccess,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
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
            }

            HorizontalDivider(color = Slate800, thickness = 1.dp)

            // Bottom Sticky Save Action Bar
            Surface(
                color = Slate950,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Slate700),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate200),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("বাতিল", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onSave(
                                isRestricted,
                                selectedClasses.toList(),
                                selectedGroups.toList(),
                                selectedBatches.toList(),
                                selectedPrograms.toList()
                            )
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            disabledContainerColor = Slate700
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_save_course_permissions")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("সেভ হচ্ছে...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "পারমিশন সেভ করুন",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact summary badge / row showing current Course & Syllabus Access state on a Device or AccessCode card.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseAccessSummarySection(
    isRestricted: Boolean,
    allowedClasses: List<String>?,
    allowedGroups: List<String>?,
    allowedBatches: List<String>?,
    allowedPrograms: List<String>?,
    onConfigureClick: () -> Unit,
    testTagPrefix: String = "device"
) {
    val classes = allowedClasses.orEmpty()
    val groups = allowedGroups.orEmpty()
    val batches = allowedBatches.orEmpty()
    val programs = allowedPrograms.orEmpty()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isRestricted) AmberWarning.copy(alpha = 0.1f) else Slate950,
        border = BorderStroke(
            1.dp,
            if (isRestricted) AmberWarning.copy(alpha = 0.5f) else Slate700
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (isRestricted) Icons.Default.Lock else Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (isRestricted) AmberWarning else EmeraldSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRestricted) {
                            "কাস্টম কোর্স ও সিলেবাস লক সক্রিয়"
                        } else {
                            "সকল ক্লাস ও কোর্স উন্মুক্ত (All Courses Open)"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isRestricted) AmberWarning else EmeraldSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Button(
                    onClick = onConfigureClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRestricted) AmberWarning.copy(alpha = 0.2f) else ElectricBlue.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .border(
                            1.dp,
                            if (isRestricted) AmberWarning else ElectricBlue,
                            RoundedCornerShape(8.dp)
                        )
                        .testTag("btn_course_access_$testTagPrefix")
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = if (isRestricted) AmberWarning else NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "কোর্স ও সিলেবাস এক্সেস",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (isRestricted) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (classes.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElectricBlue.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, ElectricBlue)
                        ) {
                            Text(
                                text = "শ্রেণি: ${classes.joinToString(", ") { formatClassCodeBn(it) }}",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (groups.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PurpleRoyal.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, PurpleRoyal)
                        ) {
                            Text(
                                text = "বিভাগ: ${groups.joinToString(", ") { formatGroupCodeBn(it) }}",
                                fontSize = 10.sp,
                                color = Violet500,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (batches.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldSuccess.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, EmeraldSuccess)
                        ) {
                            Text(
                                text = "ব্যাচ: ${batches.joinToString(", ")}",
                                fontSize = 10.sp,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (programs.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonCyan.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, NeonCyan)
                        ) {
                            Text(
                                text = "কোর্স: ${programs.size} টি অনুমোদিত",
                                fontSize = 10.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (classes.isEmpty() && groups.isEmpty() && batches.isEmpty() && programs.isEmpty()) {
                        Text(
                            text = "⚠️ লক সক্রিয় কিন্তু কোনো ক্লাস বা কোর্স নির্বাচন করা হয়নি",
                            fontSize = 10.sp,
                            color = CoralDanger
                        )
                    }
                }
            }
        }
    }
}
