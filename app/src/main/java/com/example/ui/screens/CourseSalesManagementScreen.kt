package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
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
import com.example.data.model.ApproveCourseAccessRequestRpcPayload
import com.example.data.model.CourseAccessEnrollment
import com.example.data.model.CoursePaymentSettings
import com.example.data.model.CoursePaymentTransaction
import com.example.data.model.InstallmentPlanItem
import com.example.data.model.ReviewCoursePaymentRpcPayload
import com.example.data.model.UpsertCoursePaymentSettingsPayload
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
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun CourseSalesManagementScreen(
    enrollments: List<CourseAccessEnrollment>,
    transactions: List<CoursePaymentTransaction>,
    paymentSettings: CoursePaymentSettings,
    isLoading: Boolean,
    isProcessingAction: Boolean,
    onRefreshAll: () -> Unit,
    onApproveAccessRequest: (ApproveCourseAccessRequestRpcPayload, onSuccess: () -> Unit) -> Unit,
    onReviewPayment: (ReviewCoursePaymentRpcPayload, onSuccess: () -> Unit) -> Unit,
    onEnforceOverdueInstallments: () -> Unit,
    onExtendDueDate: (CourseAccessEnrollment, Int) -> Unit,
    onUpdateEnrollmentStatus: (CourseAccessEnrollment, String) -> Unit,
    onSavePaymentSettings: (UpsertCoursePaymentSettingsPayload) -> Unit,
    onShowToast: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    // 4 Primary Sub-tabs:
    // 0 -> এক্সেস রিকোয়েস্ট (REQUESTED)
    // 1 -> পেমেন্ট ভেরিফিকেশন (PENDING Transactions)
    // 2 -> চলমান কিস্তি ও বকেয়া (ACTIVE & OVERDUE)
    // 3 -> পেমেন্ট নম্বর সেটিংস (course_payment_settings)
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Sub-filters per tab
    var requestTabFilter by remember { mutableStateOf("REQUESTED") } // REQUESTED, APPROVED_TO_BUY, ALL
    var paymentTabFilter by remember { mutableStateOf("PENDING") } // PENDING, APPROVED, REJECTED, ALL
    var installmentTabFilter by remember { mutableStateOf("DUE_AND_OVERDUE") } // DUE_AND_OVERDUE, OVERDUE, ACTIVE_DUE, ALL_ACTIVE, REVOKED

    // Dialog states
    var enrollmentForPricingDialog by remember { mutableStateOf<CourseAccessEnrollment?>(null) }
    var transactionForApproveDialog by remember { mutableStateOf<CoursePaymentTransaction?>(null) }
    var transactionForRejectDialog by remember { mutableStateOf<CoursePaymentTransaction?>(null) }
    var enrollmentForCustomDaysDialog by remember { mutableStateOf<CourseAccessEnrollment?>(null) }

    // Counts for badges
    val requestedCount = remember(enrollments) {
        enrollments.count { it.status.equals("REQUESTED", ignoreCase = true) }
    }
    val pendingPaymentsCount = remember(transactions) {
        transactions.count { it.status.equals("PENDING", ignoreCase = true) }
    }
    val overdueCount = remember(enrollments) {
        enrollments.count { it.status.equals("OVERDUE", ignoreCase = true) }
    }
    val activeDueCount = remember(enrollments) {
        enrollments.count {
            (it.status.equals("ACTIVE", ignoreCase = true) && it.dueAmount > 0) ||
                it.status.equals("OVERDUE", ignoreCase = true)
        }
    }

    // Pricing & Installment Plan Builder Dialog
    enrollmentForPricingDialog?.let { targetEnrollment ->
        ApproveCourseAccessPricingDialog(
            enrollment = targetEnrollment,
            defaultCoursePrice = paymentSettings.defaultCoursePrice ?: 1000,
            isProcessing = isProcessingAction,
            onDismiss = { enrollmentForPricingDialog = null },
            onSubmit = { payload ->
                onApproveAccessRequest(payload) {
                    enrollmentForPricingDialog = null
                }
            }
        )
    }

    // Approve Payment Confirmation Dialog (with optional custom next due days)
    transactionForApproveDialog?.let { targetTx ->
        ApprovePaymentDialog(
            transaction = targetTx,
            isProcessing = isProcessingAction,
            onDismiss = { transactionForApproveDialog = null },
            onConfirmApprove = { customDays ->
                onReviewPayment(
                    ReviewCoursePaymentRpcPayload(
                        transactionId = targetTx.id,
                        approve = true,
                        rejectionReason = "",
                        customNextDueDays = customDays
                    )
                ) {
                    transactionForApproveDialog = null
                }
            }
        )
    }

    // Reject Payment Reason Dialog
    transactionForRejectDialog?.let { targetTx ->
        RejectPaymentDialog(
            transaction = targetTx,
            isProcessing = isProcessingAction,
            onDismiss = { transactionForRejectDialog = null },
            onConfirmReject = { reason ->
                onReviewPayment(
                    ReviewCoursePaymentRpcPayload(
                        transactionId = targetTx.id,
                        approve = false,
                        rejectionReason = reason,
                        customNextDueDays = null
                    )
                ) {
                    transactionForRejectDialog = null
                }
            }
        )
    }

    // Custom Due Date Extension Dialog
    enrollmentForCustomDaysDialog?.let { targetEnrollment ->
        ExtendDueDateCustomDialog(
            enrollment = targetEnrollment,
            onDismiss = { enrollmentForCustomDaysDialog = null },
            onConfirmDays = { days ->
                onExtendDueDate(targetEnrollment, days)
                enrollmentForCustomDaysDialog = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        // Top Executive Summary & Sub-Tab Selector
        Surface(
            color = Slate950,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Live Counters Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniSummaryPill(
                        modifier = Modifier.weight(1f),
                        label = "নতুন রিকোয়েস্ট",
                        count = requestedCount,
                        accentColor = ElectricBlue,
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    MiniSummaryPill(
                        modifier = Modifier.weight(1f),
                        label = "পেমেন্ট যাচাই",
                        count = pendingPaymentsCount,
                        accentColor = AmberWarning,
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    MiniSummaryPill(
                        modifier = Modifier.weight(1f),
                        label = "চলমান কিস্তি",
                        count = activeDueCount,
                        accentColor = if (overdueCount > 0) CoralDanger else EmeraldSuccess,
                        isSelected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                }

                // 4 Scrollable Sub-Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubTabChip(
                        title = "এক্সেস রিকোয়েস্ট",
                        badgeCount = requestedCount,
                        icon = Icons.Default.NotificationsActive,
                        selected = selectedTab == 0,
                        accentColor = ElectricBlue,
                        onClick = { selectedTab = 0 },
                        testTag = "subtab_requests"
                    )
                    SubTabChip(
                        title = "পেমেন্ট ভেরিফিকেশন",
                        badgeCount = pendingPaymentsCount,
                        icon = Icons.Default.PriceCheck,
                        selected = selectedTab == 1,
                        accentColor = AmberWarning,
                        onClick = { selectedTab = 1 },
                        testTag = "subtab_payments"
                    )
                    SubTabChip(
                        title = "চলমান কিস্তি ও বকেয়া",
                        badgeCount = overdueCount,
                        icon = Icons.Default.EventRepeat,
                        selected = selectedTab == 2,
                        accentColor = if (overdueCount > 0) CoralDanger else EmeraldSuccess,
                        onClick = { selectedTab = 2 },
                        testTag = "subtab_installments"
                    )
                    SubTabChip(
                        title = "পেমেন্ট নম্বর সেটিংস",
                        badgeCount = 0,
                        icon = Icons.Default.AccountBalanceWallet,
                        selected = selectedTab == 3,
                        accentColor = NeonCyan,
                        onClick = { selectedTab = 3 },
                        testTag = "subtab_settings"
                    )
                }

                // Search bar for Tabs 0, 1, 2
                if (selectedTab in 0..2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "শিক্ষার্থীর নাম, ফোন, ডিভাইস বা কোর্সের নাম খুঁজুন..."
                                        1 -> "Transaction ID, প্রেরক নম্বর বা শিক্ষার্থীর নাম খুঁজুন..."
                                        else -> "শিক্ষার্থীর নাম, ফোন বা কোর্স খুঁজুন..."
                                    },
                                    color = Slate400,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
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
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Slate700,
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("course_sales_search_input")
                        )

                        IconButton(
                            onClick = onRefreshAll,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate800)
                                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                .testTag("btn_refresh_course_sales")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = NeonCyan
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = NeonCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // Main Content Area based on selected sub-tab
        when (selectedTab) {
            0 -> {
                AccessRequestsSubTab(
                    enrollments = enrollments,
                    searchQuery = searchQuery,
                    selectedFilter = requestTabFilter,
                    onFilterChange = { requestTabFilter = it },
                    onOpenApproveDialog = { enrollmentForPricingDialog = it },
                    onCopyText = { label, text ->
                        clipboardManager.setText(AnnotatedString(text))
                        onShowToast("$label কপি করা হয়েছে!")
                    }
                )
            }

            1 -> {
                PaymentVerificationSubTab(
                    transactions = transactions,
                    searchQuery = searchQuery,
                    selectedFilter = paymentTabFilter,
                    onFilterChange = { paymentTabFilter = it },
                    onApproveClick = { transactionForApproveDialog = it },
                    onRejectClick = { transactionForRejectDialog = it },
                    onCopyText = { label, text ->
                        clipboardManager.setText(AnnotatedString(text))
                        onShowToast("$label কপি করা হয়েছে!")
                    }
                )
            }

            2 -> {
                ActiveInstallmentsSubTab(
                    enrollments = enrollments,
                    searchQuery = searchQuery,
                    selectedFilter = installmentTabFilter,
                    isProcessing = isProcessingAction,
                    onFilterChange = { installmentTabFilter = it },
                    onEnforceOverdue = onEnforceOverdueInstallments,
                    onExtendDueDays = { enrollment, days -> onExtendDueDate(enrollment, days) },
                    onOpenCustomDaysDialog = { enrollmentForCustomDaysDialog = it },
                    onSetStatus = { enrollment, targetStatus ->
                        onUpdateEnrollmentStatus(enrollment, targetStatus)
                    },
                    onOpenEditPlans = { enrollmentForPricingDialog = it },
                    onCopyText = { label, text ->
                        clipboardManager.setText(AnnotatedString(text))
                        onShowToast("$label কপি করা হয়েছে!")
                    }
                )
            }

            3 -> {
                PaymentGatewaySettingsSubTab(
                    settings = paymentSettings,
                    isSaving = isProcessingAction,
                    onSave = onSavePaymentSettings
                )
            }
        }
    }
}

// =============================================================================
// Sub-Tab 1: এক্সেস রিকোয়েস্ট (Access Requests)
// =============================================================================
@Composable
private fun AccessRequestsSubTab(
    enrollments: List<CourseAccessEnrollment>,
    searchQuery: String,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onOpenApproveDialog: (CourseAccessEnrollment) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val filteredList = remember(enrollments, searchQuery, selectedFilter) {
        enrollments.filter { item ->
            val statusMatch = when (selectedFilter) {
                "REQUESTED" -> item.status.equals("REQUESTED", ignoreCase = true)
                "APPROVED_TO_BUY" -> item.status.equals("APPROVED_TO_BUY", ignoreCase = true) ||
                    item.status.equals("PAYMENT_REJECTED", ignoreCase = true)
                else -> true
            }
            val query = searchQuery.trim().lowercase()
            val queryMatch = query.isEmpty() ||
                (item.studentName?.lowercase()?.contains(query) == true) ||
                (item.effectivePhone?.lowercase()?.contains(query) == true) ||
                (item.effectiveDeviceId?.lowercase()?.contains(query) == true) ||
                (item.effectiveAccountKey?.lowercase()?.contains(query) == true) ||
                (item.programTitle?.lowercase()?.contains(query) == true) ||
                (item.effectiveClass?.lowercase()?.contains(query) == true)
            statusMatch && queryMatch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "REQUESTED",
                    onClick = { onFilterChange("REQUESTED") },
                    label = {
                        Text(
                            "নতুন রিকোয়েস্ট (${enrollments.count { it.status.equals("REQUESTED", true) }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBlue.copy(alpha = 0.25f),
                        selectedLabelColor = NeonCyan,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
                FilterChip(
                    selected = selectedFilter == "APPROVED_TO_BUY",
                    onClick = { onFilterChange("APPROVED_TO_BUY") },
                    label = {
                        Text(
                            "পেমেন্টের অপেক্ষায় (${enrollments.count { it.status.equals("APPROVED_TO_BUY", true) || it.status.equals("PAYMENT_REJECTED", true) }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberWarning.copy(alpha = 0.25f),
                        selectedLabelColor = AmberWarning,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { onFilterChange("ALL") },
                    label = {
                        Text(
                            "সব রিকোয়েস্ট (${enrollments.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleRoyal.copy(alpha = 0.25f),
                        selectedLabelColor = Color.White,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
            }
        }

        if (filteredList.isEmpty()) {
            item {
                EmptyStateBox(
                    icon = Icons.Default.NotificationsActive,
                    title = "কোনো কোর্স এক্সেস রিকোয়েস্ট পাওয়া যায়নি",
                    subtitle = "শিক্ষার্থী অ্যাপ থেকে 'কোর্সে এক্সেস পান' বাটনে ক্লিক করলে সাথে সাথে এখানে রিকোয়েস্ট চলে আসবে।"
                )
            }
        } else {
            items(filteredList, key = { it.id }) { enrollment ->
                EnrollmentRequestCard(
                    enrollment = enrollment,
                    onConfigureClick = { onOpenApproveDialog(enrollment) },
                    onCopyText = onCopyText
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun EnrollmentRequestCard(
    enrollment: CourseAccessEnrollment,
    onConfigureClick: () -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val statusColor = statusBadgeColor(enrollment.status)
    val statusLabel = statusBadgeLabelBn(enrollment.status)
    val plans = remember(enrollment.installmentPlansJson) { enrollment.parsedInstallmentPlans() }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .testTag("enrollment_request_card_${enrollment.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Student Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = enrollment.studentName?.takeIf { it.isNotBlank() } ?: "অজ্ঞাত শিক্ষার্থী",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = buildString {
                                append(enrollment.effectivePhone?.takeIf { it.isNotBlank() } ?: "ফোন নম্বর নেই")
                                if (!enrollment.effectiveClass.isNullOrBlank()) {
                                    append(" • ${formatClassBn(enrollment.effectiveClass)}")
                                }
                                if (!enrollment.effectiveGroup.isNullOrBlank()) {
                                    append(" (${formatGroupBn(enrollment.effectiveGroup)})")
                                }
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                StatusPill(label = statusLabel, color = statusColor)
            }

            // Requested Course Banner
            Surface(
                color = Slate950,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "অনুরোধকৃত কোর্স:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = enrollment.programTitle?.takeIf { it.isNotBlank() }
                                ?: enrollment.programId
                                ?: "কোর্সের নাম উল্লেখ নেই",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Device ID & Account Key chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val devId = enrollment.effectiveDeviceId
                val accKey = enrollment.effectiveAccountKey
                if (!devId.isNullOrBlank()) {
                    CopyableMetaChip(
                        modifier = Modifier.weight(1f),
                        label = "ডিভাইস",
                        value = devId,
                        onCopy = { onCopyText("ডিভাইস আইডি", devId) }
                    )
                }
                if (!accKey.isNullOrBlank()) {
                    CopyableMetaChip(
                        modifier = Modifier.weight(1f),
                        label = "অ্যাকাউন্ট কী",
                        value = accKey,
                        onCopy = { onCopyText("অ্যাকাউন্ট কী", accKey) }
                    )
                }
            }

            // Show already configured price & plans if approved to buy
            if (enrollment.totalPrice > 0 || plans.isNotEmpty()) {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "নির্ধারিত কোর্স ফি: ৳${enrollment.totalPrice}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = if (enrollment.effectiveInstallmentsEnabled) "কিস্তি চালু (${plans.size}টি প্ল্যান)" else "শুধু এককালীন পেমেন্ট",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        if (!enrollment.adminNote.isNullOrBlank()) {
                            Text(
                                text = "নোট: ${enrollment.adminNote}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                            )
                        }
                    }
                }
            }

            // Timestamp + Primary Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সময়: ${formatIsoTimestampBn(enrollment.effectiveTimestamp)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate400,
                        fontSize = 11.sp
                    )
                )

                Button(
                    onClick = onConfigureClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (enrollment.status.equals("REQUESTED", true)) ElectricBlue else Slate700
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("btn_configure_request_${enrollment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (enrollment.status.equals("REQUESTED", true)) {
                            "দাম ও কিস্তি সেট করে অনুমোদন দিন"
                        } else {
                            "দাম ও কিস্তি পরিবর্তন করুন"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// =============================================================================
// Sub-Tab 2: পেমেন্ট ভেরিফিকেশন (Payment Verification)
// =============================================================================
@Composable
private fun PaymentVerificationSubTab(
    transactions: List<CoursePaymentTransaction>,
    searchQuery: String,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onApproveClick: (CoursePaymentTransaction) -> Unit,
    onRejectClick: (CoursePaymentTransaction) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val filteredList = remember(transactions, searchQuery, selectedFilter) {
        transactions.filter { tx ->
            val statusMatch = when (selectedFilter) {
                "PENDING" -> tx.status.equals("PENDING", ignoreCase = true)
                "APPROVED" -> tx.status.equals("APPROVED", ignoreCase = true)
                "REJECTED" -> tx.status.equals("REJECTED", ignoreCase = true)
                else -> true
            }
            val query = searchQuery.trim().lowercase()
            val queryMatch = query.isEmpty() ||
                (tx.transactionId?.lowercase()?.contains(query) == true) ||
                (tx.senderMobile?.lowercase()?.contains(query) == true) ||
                (tx.studentName?.lowercase()?.contains(query) == true) ||
                (tx.programTitle?.lowercase()?.contains(query) == true) ||
                (tx.paymentMethod?.lowercase()?.contains(query) == true)
            statusMatch && queryMatch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "PENDING",
                    onClick = { onFilterChange("PENDING") },
                    label = {
                        Text(
                            "অপেক্ষমাণ (${transactions.count { it.status.equals("PENDING", true) }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberWarning.copy(alpha = 0.25f),
                        selectedLabelColor = AmberWarning,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
                FilterChip(
                    selected = selectedFilter == "APPROVED",
                    onClick = { onFilterChange("APPROVED") },
                    label = {
                        Text(
                            "অনুমোদিত (${transactions.count { it.status.equals("APPROVED", true) }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldSuccess.copy(alpha = 0.25f),
                        selectedLabelColor = EmeraldSuccess,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
                FilterChip(
                    selected = selectedFilter == "REJECTED",
                    onClick = { onFilterChange("REJECTED") },
                    label = {
                        Text(
                            "বাতিলকৃত (${transactions.count { it.status.equals("REJECTED", true) }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CoralDanger.copy(alpha = 0.25f),
                        selectedLabelColor = CoralDanger,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { onFilterChange("ALL") },
                    label = {
                        Text(
                            "সব লেনদেন (${transactions.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBlue.copy(alpha = 0.25f),
                        selectedLabelColor = NeonCyan,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
            }
        }

        if (filteredList.isEmpty()) {
            item {
                EmptyStateBox(
                    icon = Icons.Default.PriceCheck,
                    title = "কোনো পেমেন্ট ট্রানজেকশন পাওয়া যায়নি",
                    subtitle = "শিক্ষার্থীরা বিকাশ, নগদ বা রকেটে টাকা পাঠিয়ে ট্রানজেকশন আইডি সাবমিট করলে এখানে ভেরিফিকেশনের জন্য দেখা যাবে।"
                )
            }
        } else {
            items(filteredList, key = { it.id }) { tx ->
                PaymentTransactionCard(
                    transaction = tx,
                    onApproveClick = { onApproveClick(tx) },
                    onRejectClick = { onRejectClick(tx) },
                    onCopyText = onCopyText
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun PaymentTransactionCard(
    transaction: CoursePaymentTransaction,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val isPending = transaction.status.equals("PENDING", ignoreCase = true)
    val isApproved = transaction.status.equals("APPROVED", ignoreCase = true)
    val statusColor = when {
        isPending -> AmberWarning
        isApproved -> EmeraldSuccess
        else -> CoralDanger
    }
    val statusText = when {
        isPending -> "ভেরিফিকেশন অপেক্ষমাণ"
        isApproved -> "পেমেন্ট অনুমোদিত"
        else -> "পেমেন্ট বাতিলকৃত"
    }

    val methodColor = paymentMethodColor(transaction.paymentMethod)

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .testTag("payment_tx_card_${transaction.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Student Name + Payment Method + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.studentName?.takeIf { it.isNotBlank() } ?: "অজ্ঞাত শিক্ষার্থী",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = transaction.programTitle?.takeIf { it.isNotBlank() }
                            ?: transaction.programId
                            ?: "কোর্স",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusPill(label = statusText, color = statusColor)
            }

            // Highlight Box: Payment Method, Amount, Installment Number
            Surface(
                color = Slate950,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, methodColor.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = methodColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, methodColor.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = formatPaymentMethodLabel(transaction.paymentMethod),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = methodColor,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                            }

                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "কিস্তি ${transaction.installmentNumber}/${transaction.totalInstallments.coerceAtLeast(1)}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate200,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        val pTitle = transaction.effectivePlanTitle
                        if (!pTitle.isNullOrBlank()) {
                            Text(
                                text = pTitle,
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                            )
                        }
                    }

                    Text(
                        text = "৳${transaction.amount}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            // Copyable Sender Mobile & Transaction ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CopyableMetaChip(
                    modifier = Modifier.weight(1f),
                    label = "প্রেরক নম্বর (Sender)",
                    value = transaction.senderMobile?.takeIf { it.isNotBlank() } ?: "N/A",
                    accentColor = NeonCyan,
                    onCopy = {
                        transaction.senderMobile?.takeIf { it.isNotBlank() }?.let {
                            onCopyText("প্রেরক নম্বর", it)
                        }
                    }
                )
                CopyableMetaChip(
                    modifier = Modifier.weight(1f),
                    label = "TrxID (ট্রানজেকশন আইডি)",
                    value = transaction.transactionId?.takeIf { it.isNotBlank() } ?: "N/A",
                    accentColor = AmberWarning,
                    onCopy = {
                        transaction.transactionId?.takeIf { it.isNotBlank() }?.let {
                            onCopyText("Transaction ID", it)
                        }
                    }
                )
            }

            // Rejection reason if rejected
            if (!transaction.rejectionReason.isNullOrBlank()) {
                Surface(
                    color = CoralDanger.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "বাতিলের কারণ: ${transaction.rejectionReason}",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CoralDanger,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Submitted Time
            Text(
                text = "জমা দেওয়ার সময়: ${formatIsoTimestampBn(transaction.effectiveSubmittedAt)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate400,
                    fontSize = 11.sp
                )
            )

            // Approve / Reject Buttons for PENDING transactions
            if (isPending) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRejectClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralDanger),
                        border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_reject_tx_${transaction.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GppBad,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reject (বাতিল)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onApproveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_approve_tx_${transaction.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Slate950,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Approve & Unlock",
                            color = Slate950,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Sub-Tab 3: চলমান কিস্তি ও বকেয়া (Active Installments & Overdue)
// =============================================================================
@Composable
private fun ActiveInstallmentsSubTab(
    enrollments: List<CourseAccessEnrollment>,
    searchQuery: String,
    selectedFilter: String,
    isProcessing: Boolean,
    onFilterChange: (String) -> Unit,
    onEnforceOverdue: () -> Unit,
    onExtendDueDays: (CourseAccessEnrollment, Int) -> Unit,
    onOpenCustomDaysDialog: (CourseAccessEnrollment) -> Unit,
    onSetStatus: (CourseAccessEnrollment, String) -> Unit,
    onOpenEditPlans: (CourseAccessEnrollment) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val filteredList = remember(enrollments, searchQuery, selectedFilter) {
        enrollments.filter { item ->
            val statusMatch = when (selectedFilter) {
                "DUE_AND_OVERDUE" ->
                    item.status.equals("OVERDUE", ignoreCase = true) ||
                        (item.status.equals("ACTIVE", ignoreCase = true) && item.dueAmount > 0) ||
                        item.status.equals("PAYMENT_PENDING", ignoreCase = true)
                "OVERDUE" -> item.status.equals("OVERDUE", ignoreCase = true)
                "ACTIVE_DUE" -> item.status.equals("ACTIVE", ignoreCase = true) && item.dueAmount > 0
                "ALL_ACTIVE" -> item.status.equals("ACTIVE", ignoreCase = true)
                "REVOKED" -> item.status.equals("REVOKED", ignoreCase = true)
                else -> !item.status.equals("REQUESTED", ignoreCase = true)
            }
            val query = searchQuery.trim().lowercase()
            val queryMatch = query.isEmpty() ||
                (item.studentName?.lowercase()?.contains(query) == true) ||
                (item.effectivePhone?.lowercase()?.contains(query) == true) ||
                (item.effectiveDeviceId?.lowercase()?.contains(query) == true) ||
                (item.programTitle?.lowercase()?.contains(query) == true)
            statusMatch && queryMatch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Overdue Enforcement Banner Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CoralDanger.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "কিস্তির মেয়াদ ও অটো-লক চেক",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "যাদের কিস্তির শেষ তারিখ পার হয়ে গেছে তাদের কোর্স অটোমেটিক OVERDUE (লক) করতে ক্লিক করুন।",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onEnforceOverdue,
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = CoralDanger),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("btn_enforce_overdue")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventRepeat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ওভারডিউ চেক", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "DUE_AND_OVERDUE",
                    onClick = { onFilterChange("DUE_AND_OVERDUE") },
                    label = { Text("বকেয়া ও ওভারডিউ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberWarning.copy(alpha = 0.25f),
                        selectedLabelColor = AmberWarning,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
                FilterChip(
                    selected = selectedFilter == "OVERDUE",
                    onClick = { onFilterChange("OVERDUE") },
                    label = {
                        Text(
                            "মেয়াদোত্তীর্ণ (${enrollments.count { it.status.equals("OVERDUE", true) }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CoralDanger.copy(alpha = 0.25f),
                        selectedLabelColor = CoralDanger,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
                FilterChip(
                    selected = selectedFilter == "ALL_ACTIVE",
                    onClick = { onFilterChange("ALL_ACTIVE") },
                    label = {
                        Text(
                            "সকল সক্রিয় কোর্স (${enrollments.count { it.status.equals("ACTIVE", true) }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldSuccess.copy(alpha = 0.25f),
                        selectedLabelColor = EmeraldSuccess,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
                FilterChip(
                    selected = selectedFilter == "REVOKED",
                    onClick = { onFilterChange("REVOKED") },
                    label = {
                        Text(
                            "লকড / বাতিল (${enrollments.count { it.status.equals("REVOKED", true) }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleRoyal.copy(alpha = 0.25f),
                        selectedLabelColor = Color.White,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
            }
        }

        if (filteredList.isEmpty()) {
            item {
                EmptyStateBox(
                    icon = Icons.Default.EventRepeat,
                    title = "এই ফিল্টারে কোনো কিস্তি বা এনরোলমেন্ট নেই",
                    subtitle = "চলমান কিস্তি, বকেয়া টাকা এবং মেয়াদোত্তীর্ণ (Overdue) শিক্ষার্থীদের তালিকা এখানে দেখা যাবে।"
                )
            }
        } else {
            items(filteredList, key = { it.id }) { enrollment ->
                InstallmentLedgerCard(
                    enrollment = enrollment,
                    onExtend7Days = { onExtendDueDays(enrollment, 7) },
                    onExtend15Days = { onExtendDueDays(enrollment, 15) },
                    onOpenCustomDays = { onOpenCustomDaysDialog(enrollment) },
                    onSetStatus = { targetStatus -> onSetStatus(enrollment, targetStatus) },
                    onOpenEditPlans = { onOpenEditPlans(enrollment) },
                    onCopyText = onCopyText
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun InstallmentLedgerCard(
    enrollment: CourseAccessEnrollment,
    onExtend7Days: () -> Unit,
    onExtend15Days: () -> Unit,
    onOpenCustomDays: () -> Unit,
    onSetStatus: (String) -> Unit,
    onOpenEditPlans: () -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val isOverdue = enrollment.status.equals("OVERDUE", ignoreCase = true)
    val isActive = enrollment.status.equals("ACTIVE", ignoreCase = true)
    val statusColor = statusBadgeColor(enrollment.status)
    val statusLabel = statusBadgeLabelBn(enrollment.status)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) Color(0xFF2A1218) else Slate800
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isOverdue) 1.5.dp else 1.dp,
                color = if (isOverdue) CoralDanger else statusColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("installment_card_${enrollment.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Student Info + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = enrollment.studentName?.takeIf { it.isNotBlank() } ?: "অজ্ঞাত শিক্ষার্থী",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = buildString {
                            append(enrollment.effectivePhone?.takeIf { it.isNotBlank() } ?: "ফোন নেই")
                            if (!enrollment.effectiveClass.isNullOrBlank()) {
                                append(" • ${formatClassBn(enrollment.effectiveClass)}")
                            }
                        },
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = enrollment.programTitle?.takeIf { it.isNotBlank() }
                            ?: enrollment.programId
                            ?: "কোর্স",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                StatusPill(label = statusLabel, color = statusColor)
            }

            // Financial Summary Grid: Total | Paid | Due
            Surface(
                color = Slate950,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FinancialStatColumn(
                            label = "মোট মূল্য",
                            amount = "৳${enrollment.totalPrice}",
                            color = Color.White
                        )
                        FinancialStatColumn(
                            label = "পরিশোধিত (Paid)",
                            amount = "৳${enrollment.paidAmount}",
                            color = EmeraldSuccess
                        )
                        FinancialStatColumn(
                            label = "বকেয়া (Due)",
                            amount = "৳${enrollment.dueAmount}",
                            color = if (enrollment.dueAmount > 0) AmberWarning else Slate400
                        )
                        FinancialStatColumn(
                            label = "কিস্তি সম্পন্ন",
                            amount = "${enrollment.completedInstallments}/${enrollment.totalInstallments.coerceAtLeast(1)}",
                            color = NeonCyan
                        )
                    }

                    val progress = if (enrollment.totalPrice > 0) {
                        (enrollment.paidAmount.toFloat() / enrollment.totalPrice.toFloat()).coerceIn(0f, 1f)
                    } else 1f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isOverdue) CoralDanger else EmeraldSuccess,
                        trackColor = Slate800
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (enrollment.nextInstallmentAmount > 0 && enrollment.dueAmount > 0) {
                                "পরবর্তী কিস্তি: ৳${enrollment.nextInstallmentAmount}"
                            } else {
                                "কোনো বকেয়া কিস্তি নেই"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (enrollment.dueAmount > 0) AmberWarning else EmeraldSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = "শেষ তারিখ: ${formatIsoTimestampBn(enrollment.nextDueDate)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isOverdue) CoralDanger else Slate200,
                                fontWeight = if (isOverdue) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Quick Due-Date Extension Buttons (+7 days, +15 days, Custom)
            if (enrollment.dueAmount > 0 || isOverdue) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মেয়াদ বাড়ান:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate200,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    SmallActionChip(
                        label = "+৭ দিন",
                        color = ElectricBlueLight,
                        onClick = onExtend7Days
                    )
                    SmallActionChip(
                        label = "+১৫ দিন",
                        color = NeonCyan,
                        onClick = onExtend15Days
                    )
                    SmallActionChip(
                        label = "কাস্টম দিন",
                        color = AmberWarning,
                        onClick = onOpenCustomDays
                    )
                }
            }

            // Quick Manual Status Control: ACTIVE / OVERDUE / REVOKED
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "স্ট্যাটাস:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate400,
                        fontWeight = FontWeight.Bold
                    )
                )
                SmallActionChip(
                    label = "ACTIVE (আনলক)",
                    color = if (isActive) EmeraldSuccess else Slate400,
                    onClick = { onSetStatus("ACTIVE") }
                )
                SmallActionChip(
                    label = "OVERDUE (লক)",
                    color = if (isOverdue) AmberWarning else Slate400,
                    onClick = { onSetStatus("OVERDUE") }
                )
                SmallActionChip(
                    label = "REVOKED (বাতিল)",
                    color = if (enrollment.status.equals("REVOKED", true)) CoralDanger else Slate400,
                    onClick = { onSetStatus("REVOKED") }
                )
            }

            // Bottom Row: Manual Lock/Unlock + Re-configure Price/Plans
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenEditPlans,
                    border = BorderStroke(1.dp, Slate600),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = null,
                        tint = Slate200,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("প্ল্যান ও দাম এডিট", color = Slate200, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onSetStatus(if (isActive) "REVOKED" else "ACTIVE")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) CoralDanger.copy(alpha = 0.85f) else EmeraldSuccess
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_toggle_enrollment_lock_${enrollment.id}")
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (isActive) Color.White else Slate950,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isActive) "কোর্স লক করুন" else "কোর্স আনলক করুন",
                        color = if (isActive) Color.White else Slate950,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

// =============================================================================
// Sub-Tab 4: পেমেন্ট নম্বর সেটিংস (Payment Gateway Settings `course_payment_settings`)
// =============================================================================
@Composable
private fun PaymentGatewaySettingsSubTab(
    settings: CoursePaymentSettings,
    isSaving: Boolean,
    onSave: (UpsertCoursePaymentSettingsPayload) -> Unit
) {
    var bkash by remember(settings) { mutableStateOf(settings.bkashNumber.orEmpty()) }
    var nagad by remember(settings) { mutableStateOf(settings.nagadNumber.orEmpty()) }
    var rocket by remember(settings) { mutableStateOf(settings.rocketNumber.orEmpty()) }
    var defaultPriceText by remember(settings) {
        mutableStateOf((settings.defaultCoursePrice ?: 1000).toString())
    }
    var instructionsBn by remember(settings) {
        mutableStateOf(
            settings.effectiveInstructionsBn.takeIf { it.isNotBlank() }
                ?: "উপরের যেকোনো নম্বরে Send Money করে নিচের ফর্মে আপনার প্রেরক নম্বর ও Transaction ID জমা দিন।"
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonCyan.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "বিকাশ / নগদ / রকেট পেমেন্ট সেটিংস",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = "স্টুডেন্ট অ্যাপের পেমেন্ট স্ক্রিনে এই নম্বরগুলো ও নির্দেশনা সরাসরি দেখাবে",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                            )
                        }
                    }

                    HorizontalDivider(color = Slate700)

                    // bKash Number
                    OutlinedTextField(
                        value = bkash,
                        onValueChange = { bkash = it },
                        label = { Text("বিকাশ নম্বর (bKash Personal / Send Money)") },
                        placeholder = { Text("যেমন: 017XXXXXXXX") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_bkash_number")
                    )

                    // Nagad Number
                    OutlinedTextField(
                        value = nagad,
                        onValueChange = { nagad = it },
                        label = { Text("নগদ নম্বর (Nagad Personal / Send Money)") },
                        placeholder = { Text("যেমন: 018XXXXXXXX") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_nagad_number")
                    )

                    // Rocket Number
                    OutlinedTextField(
                        value = rocket,
                        onValueChange = { rocket = it },
                        label = { Text("রকেট নম্বর (Rocket Personal / Send Money)") },
                        placeholder = { Text("যেমন: 019XXXXXXXX") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_rocket_number")
                    )

                    // Default Course Price
                    OutlinedTextField(
                        value = defaultPriceText,
                        onValueChange = { defaultPriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("ডিফল্ট কোর্স ফি (Default Course Price ৳)") },
                        placeholder = { Text("যেমন: 1000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_default_course_price")
                    )

                    // Payment Instructions (Bengali)
                    OutlinedTextField(
                        value = instructionsBn,
                        onValueChange = { instructionsBn = it },
                        label = { Text("পেমেন্টের নিয়মাবলী ও নির্দেশনা (বাংলায়)") },
                        minLines = 3,
                        maxLines = 6,
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_payment_instructions_bn")
                    )

                    Button(
                        onClick = {
                            val defaultPrice = defaultPriceText.toIntOrNull()?.coerceAtLeast(0) ?: 1000
                            onSave(
                                UpsertCoursePaymentSettingsPayload(
                                    id = 1,
                                    bkashNumber = bkash.trim(),
                                    nagadNumber = nagad.trim(),
                                    rocketNumber = rocket.trim(),
                                    paymentInstructionsBn = instructionsBn.trim(),
                                    defaultCoursePrice = defaultPrice
                                )
                            )
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_payment_settings")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "পেমেন্ট সেটিংস সেভ করুন",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Dialog 1: Approve Course Access Request + Custom Price & Installment Builder
// =============================================================================
@Composable
private fun ApproveCourseAccessPricingDialog(
    enrollment: CourseAccessEnrollment,
    defaultCoursePrice: Int,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (ApproveCourseAccessRequestRpcPayload) -> Unit
) {
    val initialPrice = if (enrollment.totalPrice > 0) enrollment.totalPrice else defaultCoursePrice
    var totalPriceText by remember(enrollment) { mutableStateOf(initialPrice.toString()) }
    var installmentEnabled by remember(enrollment) { mutableStateOf(enrollment.effectiveInstallmentsEnabled) }
    var adminNote by remember(enrollment) {
        mutableStateOf(
            enrollment.adminNote?.takeIf { it.isNotBlank() }
                ?: "বিকাশ বা নগদে সেন্ড মানি করে ট্রানজেকশন আইডি জমা দাও।"
        )
    }

    val existingPlans = remember(enrollment) { enrollment.parsedInstallmentPlans() }
    val plansList = remember(enrollment) {
        mutableStateListOf<InstallmentPlanItem>().apply {
            if (existingPlans.isNotEmpty()) {
                addAll(existingPlans)
            } else {
                addAll(InstallmentPlanItem.buildDefaultPlansForPrice(initialPrice))
            }
        }
    }

    // Custom Plan Creator Inputs
    var showAddCustomPlanForm by remember { mutableStateOf(false) }
    var customCountText by remember { mutableStateOf("3") }
    var customPerInstallmentText by remember { mutableStateOf("") }
    var customIntervalDaysText by remember { mutableStateOf("30") }
    var customTitleBn by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "কোর্সের মূল্য ও কিস্তি নির্ধারণ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = "${enrollment.studentName ?: "শিক্ষার্থী"} • ${enrollment.programTitle ?: ""}",
                            style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan),
                            maxLines = 2
                        )
                    }
                    IconButton(onClick = { if (!isProcessing) onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                HorizontalDivider(color = Slate700)

                // Step 1: Total Price Input + Auto-Recalculate Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = totalPriceText,
                        onValueChange = { newVal ->
                            totalPriceText = newVal.filter { it.isDigit() }
                        },
                        label = { Text("কোর্সের মোট দাম (Total Price ৳)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_input_total_price")
                    )

                    Button(
                        onClick = {
                            val price = totalPriceText.toIntOrNull()?.coerceAtLeast(0) ?: 1000
                            plansList.clear()
                            plansList.addAll(InstallmentPlanItem.buildDefaultPlansForPrice(price))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "অটো প্ল্যান\nহিসাব করুন",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick Price Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(500, 1000, 1200, 1500, 2000, 2500, 3000).forEach { presetPrice ->
                        SmallActionChip(
                            label = "৳$presetPrice",
                            color = if (totalPriceText == presetPrice.toString()) NeonCyan else Slate400,
                            onClick = {
                                totalPriceText = presetPrice.toString()
                                plansList.clear()
                                plansList.addAll(InstallmentPlanItem.buildDefaultPlansForPrice(presetPrice))
                            }
                        )
                    }
                }

                // Step 2: Enable Installments Toggle
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "কিস্তি (Installment) সুবিধা চালু রাখুন",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "বন্ধ করলে শিক্ষার্থীকে পুরো টাকা একবারে পরিশোধ করতে হবে",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                            )
                        }
                        Switch(
                            checked = installmentEnabled,
                            onCheckedChange = { installmentEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldSuccess
                            )
                        )
                    }
                }

                // Step 3: Installment Plans List & Custom Plan Builder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কিস্তি প্ল্যানসমূহ (${plansList.size}টি)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )

                    TextButton(
                        onClick = { showAddCustomPlanForm = !showAddCustomPlanForm }
                    ) {
                        Icon(
                            imageVector = if (showAddCustomPlanForm) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showAddCustomPlanForm) "বন্ধ করুন" else "কাস্টম কিস্তি যোগ করুন",
                            color = EmeraldSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Expandable Custom Plan Builder Form
                AnimatedVisibility(visible = showAddCustomPlanForm) {
                    Surface(
                        color = Slate950,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "নতুন কাস্টম কিস্তি প্ল্যান তৈরি করুন",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customCountText,
                                    onValueChange = { customCountText = it.filter { c -> c.isDigit() } },
                                    label = { Text("কিস্তি সংখ্যা") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = adminTextFieldColors(),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = customPerInstallmentText,
                                    onValueChange = { customPerInstallmentText = it.filter { c -> c.isDigit() } },
                                    label = { Text("প্রতি কিস্তি ৳ (ঐচ্ছিক)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = adminTextFieldColors(),
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = customIntervalDaysText,
                                    onValueChange = { customIntervalDaysText = it.filter { c -> c.isDigit() } },
                                    label = { Text("বিরতি (দিন)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = adminTextFieldColors(),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = customTitleBn,
                                onValueChange = { customTitleBn = it },
                                label = { Text("প্ল্যানের নাম (ফাঁকা রাখলে অটো তৈরি হবে)") },
                                singleLine = true,
                                colors = adminTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    val totalPrice = totalPriceText.toIntOrNull()?.coerceAtLeast(0) ?: 1000
                                    val count = customCountText.toIntOrNull()?.coerceIn(1, 24) ?: 3
                                    val customPer = customPerInstallmentText.toIntOrNull()?.takeIf { it > 0 }
                                    val intervalDays = if (count == 1) 0 else (customIntervalDaysText.toIntOrNull()?.coerceAtLeast(1) ?: 30)
                                    val schedule = InstallmentPlanItem.buildScheduleAmounts(totalPrice, count, customPer)
                                    val firstAmt = schedule.firstOrNull() ?: totalPrice
                                    val perAmt = schedule.getOrElse(1) { firstAmt }
                                    val planId = if (count == 1) "full" else "inst_${count}_${System.currentTimeMillis() % 1000}"
                                    val title = customTitleBn.trim().ifBlank {
                                        if (count == 1) {
                                            "একবারে সম্পূর্ণ পেমেন্ট (Full Payment)"
                                        } else {
                                            "$count কিস্তিতে পেমেন্ট (১ম কিস্তি ৳$firstAmt, প্রতি $intervalDays দিন পর)"
                                        }
                                    }

                                    plansList.add(
                                        InstallmentPlanItem.create(
                                            planId = planId,
                                            titleText = title,
                                            count = count,
                                            amountPerInst = perAmt,
                                            intervalDays = intervalDays,
                                            schedule = schedule
                                        )
                                    )
                                    customTitleBn = ""
                                    customPerInstallmentText = ""
                                    showAddCustomPlanForm = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "প্ল্যান তালিকায় যুক্ত করুন",
                                    color = Slate950,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Current Plans List Preview
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    plansList.forEachIndexed { idx, plan ->
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plan.effectiveTitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = buildString {
                                            append("কিস্তি: ${plan.effectiveInstallmentsCount}টি • ")
                                            append("ধাপ: ${plan.effectiveScheduleAmounts.joinToString(" + ") { "৳$it" }}")
                                            if (plan.intervalDays > 0) {
                                                append(" • বিরতি: ${plan.intervalDays} দিন")
                                            }
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonCyan,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                if (plansList.size > 1) {
                                    IconButton(
                                        onClick = { plansList.removeAt(idx) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove plan",
                                            tint = CoralDanger,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Admin Note Input
                OutlinedTextField(
                    value = adminNote,
                    onValueChange = { adminNote = it },
                    label = { Text("শিক্ষার্থীর জন্য অ্যাডমিন নোট") },
                    minLines = 2,
                    maxLines = 3,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = Slate700)

                // Action 1: Approve with Price & Installment Plans (`APPROVED_TO_BUY`)
                Button(
                    onClick = {
                        val totalPrice = totalPriceText.toIntOrNull()?.coerceAtLeast(0) ?: 1000
                        val finalPlans = if (!installmentEnabled) {
                            listOf(
                                InstallmentPlanItem.create(
                                    planId = "full_1",
                                    titleText = "একবারে সম্পূর্ণ পেমেন্ট (Full Payment)",
                                    count = 1,
                                    amountPerInst = totalPrice,
                                    intervalDays = 0,
                                    schedule = listOf(totalPrice)
                                )
                            )
                        } else if (plansList.isEmpty()) {
                            InstallmentPlanItem.buildDefaultPlansForPrice(totalPrice)
                        } else {
                            plansList.toList()
                        }

                        onSubmit(
                            ApproveCourseAccessRequestRpcPayload(
                                enrollmentId = enrollment.id,
                                totalPrice = totalPrice,
                                installmentEnabled = installmentEnabled,
                                installmentPlansJson = finalPlans,
                                adminNote = adminNote.trim(),
                                instantFreeUnlock = false
                            )
                        )
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_submit_approve_to_buy")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "দাম ও কিস্তি সেট করে অনুমোদন দিন",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }

                // Action 2: Instant Free Unlock (`ACTIVE` immediately without payment)
                OutlinedButton(
                    onClick = {
                        onSubmit(
                            ApproveCourseAccessRequestRpcPayload(
                                enrollmentId = enrollment.id,
                                totalPrice = 0,
                                installmentEnabled = false,
                                installmentPlansJson = emptyList(),
                                adminNote = adminNote.trim().ifBlank { "অ্যাডমিন কর্তৃক ফ্রি আনলক করা হয়েছে" },
                                instantFreeUnlock = true
                            )
                        )
                    },
                    enabled = !isProcessing,
                    border = BorderStroke(1.dp, EmeraldSuccess),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_submit_instant_free_unlock")
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ফ্রি আনলক করে দিন (Instant Free Unlock)",
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// =============================================================================
// Dialog 2: Approve Payment Confirmation Dialog
// =============================================================================
@Composable
private fun ApprovePaymentDialog(
    transaction: CoursePaymentTransaction,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirmApprove: (customNextDueDays: Int?) -> Unit
) {
    var customDaysText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = Slate900,
        title = {
            Text(
                text = "পেমেন্ট অনুমোদন ও কোর্স আনলক",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "শিক্ষার্থী: ${transaction.studentName ?: "অজ্ঞাত"}\n" +
                        "কোর্স: ${transaction.programTitle ?: ""}\n" +
                        "পরিমাণ: ৳${transaction.amount} (${formatPaymentMethodLabel(transaction.paymentMethod)})\n" +
                        "TrxID: ${transaction.transactionId ?: ""}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate200)
                )

                if (transaction.installmentNumber < transaction.totalInstallments) {
                    OutlinedTextField(
                        value = customDaysText,
                        onValueChange = { customDaysText = it.filter { c -> c.isDigit() } },
                        label = { Text("পরবর্তী কিস্তির সময় (দিন - ঐচ্ছিক)") },
                        placeholder = { Text("ফাঁকা রাখলে প্ল্যানের ডিফল্ট দিন সেট হবে") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = adminTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val customDays = customDaysText.trim().toIntOrNull()?.takeIf { it > 0 }
                    onConfirmApprove(customDays)
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                Text("হ্যাঁ, Approve & Unlock করুন", color = Slate950, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("বাতিল", color = Slate400)
            }
        }
    )
}

// =============================================================================
// Dialog 3: Reject Payment Reason Dialog
// =============================================================================
@Composable
private fun RejectPaymentDialog(
    transaction: CoursePaymentTransaction,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirmReject: (String) -> Unit
) {
    var reason by remember { mutableStateOf("ভুল Transaction ID বা টাকা এখনো রিসিভ হয়নি। সঠিক তথ্য দিয়ে আবার চেষ্টা করো।") }

    val quickReasons = listOf(
        "ভুল Transaction ID দেওয়া হয়েছে",
        "এই নম্বরে কোনো টাকা রিসিভ হয়নি",
        "নির্ধারিত কিস্তির চেয়ে কম টাকা পাঠানো হয়েছে",
        "প্রেরক মোবাইল নম্বর ও TrxID মিলছে না"
    )

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = Slate900,
        title = {
            Text(
                text = "পেমেন্ট বাতিলের কারণ লিখুন",
                color = CoralDanger,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "এই কারণটি শিক্ষার্থীর অ্যাপে লাল রঙের ওয়ার্নিং বক্সে দেখানো হবে যাতে সে পুনরায় সঠিক পেমেন্ট জমা দিতে পারে:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                )

                // Quick preset chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickReasons.forEach { preset ->
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reason = preset }
                        ) {
                            Text(
                                text = "• $preset",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate200)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("বাতিলের কারণ (Rejection Reason)") },
                    minLines = 2,
                    maxLines = 4,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = reason.trim().ifBlank { "অ্যাডমিন কর্তৃক পেমেন্ট বাতিল করা হয়েছে" }
                    onConfirmReject(finalReason)
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = CoralDanger)
            ) {
                Text("পেমেন্ট Reject করুন", color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("ফিরে যান", color = Slate400)
            }
        }
    )
}

// =============================================================================
// Dialog 4: Custom Due Date Extension Dialog
// =============================================================================
@Composable
private fun ExtendDueDateCustomDialog(
    enrollment: CourseAccessEnrollment,
    onDismiss: () -> Unit,
    onConfirmDays: (Int) -> Unit
) {
    var daysText by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(
                text = "কিস্তির সময় বাড়িয়ে দিন",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${enrollment.studentName ?: "শিক্ষার্থী"}-এর পরবর্তী কিস্তির মেয়াদ আজ থেকে বা বর্তমান মেয়াদ থেকে কত দিন বাড়াতে চান?",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate200)
                )
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it.filter { c -> c.isDigit() } },
                    label = { Text("বর্ধিত দিন সংখ্যা (যেমন: 7, 15, 30)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysText.toIntOrNull()?.coerceIn(1, 365) ?: 7
                    onConfirmDays(days)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("মেয়াদ বাড়ান ও আনলক করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = Slate400)
            }
        }
    )
}

// =============================================================================
// Reusable UI Helpers
// =============================================================================
@Composable
private fun MiniSummaryPill(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) accentColor.copy(alpha = 0.2f) else Slate900,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else Slate800
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) Color.White else Slate400,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                color = accentColor.copy(alpha = 0.25f),
                shape = CircleShape
            ) {
                Text(
                    text = "$count",
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun SubTabChip(
    title: String,
    badgeCount: Int,
    icon: ImageVector,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = if (selected) accentColor.copy(alpha = 0.22f) else Slate900,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) accentColor else Slate700
        ),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accentColor else Slate400,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (selected) Color.White else Slate200,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                )
            )
            if (badgeCount > 0) {
                Surface(
                    color = accentColor,
                    shape = CircleShape
                ) {
                    Text(
                        text = "$badgeCount",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate950,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun CopyableMetaChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accentColor: Color = Slate200,
    onCopy: () -> Unit
) {
    Surface(
        color = Slate950,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = modifier.clickable { onCopy() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate400,
                        fontSize = 9.sp
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = ElectricBlueLight,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun FinancialStatColumn(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Slate400,
                fontSize = 10.sp
            )
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.titleSmall.copy(
                color = color,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

@Composable
private fun SmallActionChip(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun EmptyStateBox(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Slate600,
                modifier = Modifier.size(42.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
            )
        }
    }
}

@Composable
private fun adminTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricBlue,
    unfocusedBorderColor = Slate700,
    focusedContainerColor = Slate950,
    unfocusedContainerColor = Slate950,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Slate400
)

private fun statusBadgeColor(status: String?): Color {
    return when (status?.uppercase()) {
        "REQUESTED" -> ElectricBlueLight
        "APPROVED_TO_BUY" -> AmberWarning
        "PAYMENT_PENDING" -> NeonCyan
        "ACTIVE" -> EmeraldSuccess
        "PAYMENT_REJECTED" -> CoralDanger
        "OVERDUE" -> CoralDanger
        "REVOKED" -> CoralDanger
        else -> Slate400
    }
}

private fun statusBadgeLabelBn(status: String?): String {
    return when (status?.uppercase()) {
        "REQUESTED" -> "নতুন রিকোয়েস্ট"
        "APPROVED_TO_BUY" -> "কেনার অনুমতি প্রাপ্ত"
        "PAYMENT_PENDING" -> "পেমেন্ট যাচাই চলছে"
        "ACTIVE" -> "আনলকড (সক্রিয়)"
        "PAYMENT_REJECTED" -> "পেমেন্ট বাতিল"
        "OVERDUE" -> "কিস্তি মেয়াদোত্তীর্ণ (লকড)"
        "REVOKED" -> "লকড (Revoked)"
        else -> status ?: "অজ্ঞাত"
    }
}

private fun paymentMethodColor(method: String?): Color {
    return when (method?.lowercase()?.trim()) {
        "bkash" -> Color(0xFFE2136E)
        "nagad" -> Color(0xFFF7941D)
        "rocket" -> Color(0xFF8C3494)
        else -> NeonCyan
    }
}

private fun formatPaymentMethodLabel(method: String?): String {
    return when (method?.lowercase()?.trim()) {
        "bkash" -> "বিকাশ (bKash)"
        "nagad" -> "নগদ (Nagad)"
        "rocket" -> "রকেট (Rocket)"
        else -> method?.uppercase() ?: "পেমেন্ট"
    }
}

private fun formatClassBn(classCode: String?): String {
    return when (classCode?.uppercase()?.trim()) {
        "C5" -> "৫ম শ্রেণি"
        "C6" -> "৬ষ্ঠ শ্রেণি"
        "C7" -> "৭ম শ্রেণি"
        "C8" -> "৮ম শ্রেণি"
        "C9" -> "৯ম শ্রেণি"
        "C10" -> "১০ম শ্রেণি"
        "C11" -> "একাদশ শ্রেণি"
        "C12" -> "দ্বাদশ শ্রেণি"
        else -> classCode.orEmpty()
    }
}

private fun formatGroupBn(groupCode: String?): String {
    return when (groupCode?.lowercase()?.trim()) {
        "science", "sci" -> "বিজ্ঞান"
        "humanities", "hum" -> "মানবিক"
        "businessstudies", "business", "bus" -> "ব্যবসায় শিক্ষা"
        else -> groupCode.orEmpty()
    }
}

private fun formatIsoTimestampBn(rawIso: String?): String {
    if (rawIso.isNullOrBlank()) return "নির্ধারিত নেই"
    return try {
        val clean = rawIso.take(19).replace("T", " ")
        clean
    } catch (_: Exception) {
        rawIso
    }
}
