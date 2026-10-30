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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.DynamicPaymentMethodItem
import com.example.data.model.InstallmentPlanItem
import com.example.data.model.ManageCourseTrialPayload
import com.example.data.model.ManualCourseAccessPayload
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
import kotlinx.coroutines.delay
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
    onGrantManualCourseAccess: (ManualCourseAccessPayload, onSuccess: () -> Unit) -> Unit = { _, _ -> },
    onManageCourseTrial: (ManageCourseTrialPayload, onSuccess: () -> Unit) -> Unit = { _, _ -> },
    onReviewPayment: (ReviewCoursePaymentRpcPayload, onSuccess: () -> Unit) -> Unit,
    onEnforceOverdueInstallments: () -> Unit,
    onExtendDueDate: (CourseAccessEnrollment, Int) -> Unit,
    onUpdateEnrollmentStatus: (CourseAccessEnrollment, String) -> Unit,
    onSavePaymentSettings: (UpsertCoursePaymentSettingsPayload) -> Unit,
    onShowToast: (String) -> Unit,
    initialSubTab: Int = 0,
    onSubTabChanged: (Int) -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current

    // Live ticking clock for Free Trial countdowns
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    // 4 Primary Sub-tabs:
    // 0 -> এক্সেস রিকোয়েস্ট (REQUESTED / ACCESS_REQUESTED / TRIAL_ACTIVE)
    // 1 -> পেমেন্ট ভেরিফিকেশন (PENDING Transactions)
    // 2 -> চলমান কিস্তি ও বকেয়া (ACTIVE, TRIAL_ACTIVE, OVERDUE)
    // 3 -> পেমেন্ট মেথড ও নম্বর সেটিংস (course_payment_settings)
    var selectedTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab.coerceIn(0, 3)) }
    var searchQuery by remember { mutableStateOf("") }

    fun selectTab(tabIndex: Int) {
        val clamped = tabIndex.coerceIn(0, 3)
        selectedTab = clamped
        onSubTabChanged(clamped)
    }

    // Sub-filters per tab
    var requestTabFilter by remember { mutableStateOf("REQUESTED") } // REQUESTED, TRIAL_ACTIVE, TRIAL_EXPIRED, APPROVED_TO_BUY, ALL
    var paymentTabFilter by remember { mutableStateOf("PENDING") } // PENDING, APPROVED, REJECTED, ALL
    var installmentTabFilter by remember { mutableStateOf("DUE_AND_OVERDUE") } // DUE_AND_OVERDUE, TRIAL_ACTIVE, TRIAL_EXPIRED, OVERDUE, ACTIVE_DUE, ALL_ACTIVE, REVOKED

    // Dialog states
    var enrollmentForPricingDialog by remember { mutableStateOf<CourseAccessEnrollment?>(null) }
    var isManualCourseAccessDialogOpen by remember { mutableStateOf(false) }
    var transactionForApproveDialog by remember { mutableStateOf<CoursePaymentTransaction?>(null) }
    var transactionForRejectDialog by remember { mutableStateOf<CoursePaymentTransaction?>(null) }
    var enrollmentForCustomDaysDialog by remember { mutableStateOf<CourseAccessEnrollment?>(null) }
    var trialActionDialogTarget by remember { mutableStateOf<Pair<CourseAccessEnrollment, String>?>(null) }

    // Counts for badges
    val requestedCount = remember(enrollments) {
        enrollments.count { it.isAccessRequestedStatus }
    }
    val pendingPaymentsCount = remember(transactions) {
        transactions.count { it.status.equals("PENDING", ignoreCase = true) }
    }
    val overdueCount = remember(enrollments) {
        enrollments.count { it.isOverdueStatus }
    }
    val activeDueCount = remember(enrollments) {
        enrollments.count {
            (it.status.equals("ACTIVE", ignoreCase = true) && it.dueAmount > 0) ||
                it.isOverdueStatus ||
                it.isTrialActiveStatus
        }
    }
    val effectivePaymentMethods = remember(paymentSettings) {
        paymentSettings.effectivePaymentMethods()
    }
    val enabledPaymentMethodsCount = remember(effectivePaymentMethods) {
        effectivePaymentMethods.count { it.isEnabled }
    }

    // Trial Duration Management Dialog (ADD_HOURS, REDUCE_HOURS, SET_DURATION)
    trialActionDialogTarget?.let { (targetEnrollment, actionType) ->
        TrialDurationManageDialog(
            enrollment = targetEnrollment,
            action = actionType,
            isProcessing = isProcessingAction,
            onDismiss = { trialActionDialogTarget = null },
            onConfirm = { days, hours ->
                onManageCourseTrial(
                    ManageCourseTrialPayload(
                        enrollmentId = targetEnrollment.id,
                        studentPhone = targetEnrollment.effectivePhone,
                        shikhoAccountKey = targetEnrollment.effectiveAccountKey.ifBlank { targetEnrollment.effectivePhone },
                        programId = targetEnrollment.programId,
                        action = actionType,
                        days = days,
                        hours = hours,
                        currentTrialStartedAt = targetEnrollment.trialStartedAt,
                        currentTrialEndsAt = targetEnrollment.trialEndsAt,
                        currentTrialDays = targetEnrollment.trialDays,
                        currentTrialHours = targetEnrollment.trialHours
                    )
                ) {
                    trialActionDialogTarget = null
                }
            }
        )
    }

    // Manual Course Access / Installment Assignment Dialog (by 11-digit Student Phone)
    if (isManualCourseAccessDialogOpen) {
        ManualCourseAccessDialog(
            existingEnrollments = enrollments,
            defaultCoursePrice = paymentSettings.defaultCoursePrice ?: 1000,
            isProcessing = isProcessingAction,
            onDismiss = { isManualCourseAccessDialogOpen = false },
            onSubmit = { payload ->
                onGrantManualCourseAccess(payload) {
                    isManualCourseAccessDialogOpen = false
                }
            }
        )
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
                        customNextDueDays = customDays,
                        enrollmentId = targetTx.enrollmentId,
                        studentPhone = targetTx.effectiveStudentPhone,
                        shikhoAccountKey = targetTx.effectiveAccountKey,
                        programId = targetTx.programId
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
                        customNextDueDays = null,
                        enrollmentId = targetTx.enrollmentId,
                        studentPhone = targetTx.effectiveStudentPhone,
                        shikhoAccountKey = targetTx.effectiveAccountKey,
                        programId = targetTx.programId
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Prominent Quick-Access Bar for Payment Methods Manager
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTab == 3) NeonCyan.copy(alpha = 0.18f) else Slate900,
                    border = BorderStroke(
                        1.dp,
                        if (selectedTab == 3) NeonCyan else EmeraldSuccess.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectTab(3) }
                        .testTag("banner_open_payment_methods_manager")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "পেমেন্ট মেথড ম্যানেজার (বিকাশ / নগদ / রকেট / উপায়)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Text(
                                    text = "মোট ${effectivePaymentMethods.size}টি মেথড • সক্রিয় (ON): $enabledPaymentMethodsCount টি",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (enabledPaymentMethodsCount > 0) EmeraldSuccess else AmberWarning,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Surface(
                            color = EmeraldSuccess,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { selectTab(3) }
                                .testTag("btn_quick_open_payment_methods")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Slate950,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (selectedTab == 3) "মেথড লিস্ট" else "+ মেথড যোগ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate950,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Quick Live Counters Row (4 Equal Pills including Payment Methods)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniSummaryPill(
                        modifier = Modifier.weight(1f),
                        label = "রিকোয়েস্ট",
                        count = requestedCount,
                        accentColor = ElectricBlue,
                        isSelected = selectedTab == 0,
                        onClick = { selectTab(0) }
                    )
                    MiniSummaryPill(
                        modifier = Modifier.weight(1f),
                        label = "পেমেন্ট যাচাই",
                        count = pendingPaymentsCount,
                        accentColor = AmberWarning,
                        isSelected = selectedTab == 1,
                        onClick = { selectTab(1) }
                    )
                    MiniSummaryPill(
                        modifier = Modifier.weight(1f),
                        label = "চলমান কিস্তি",
                        count = activeDueCount,
                        accentColor = if (overdueCount > 0) CoralDanger else EmeraldSuccess,
                        isSelected = selectedTab == 2,
                        onClick = { selectTab(2) }
                    )
                    MiniSummaryPill(
                        modifier = Modifier.weight(1f),
                        label = "পেমেন্ট মেথড",
                        count = enabledPaymentMethodsCount,
                        accentColor = NeonCyan,
                        isSelected = selectedTab == 3,
                        onClick = { selectTab(3) }
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
                        title = "পেমেন্ট মেথড সেটিংস",
                        badgeCount = enabledPaymentMethodsCount,
                        icon = Icons.Default.AccountBalanceWallet,
                        selected = selectedTab == 3,
                        accentColor = NeonCyan,
                        onClick = { selectTab(3) },
                        testTag = "subtab_settings"
                    )
                    SubTabChip(
                        title = "এক্সেস রিকোয়েস্ট",
                        badgeCount = requestedCount,
                        icon = Icons.Default.NotificationsActive,
                        selected = selectedTab == 0,
                        accentColor = ElectricBlue,
                        onClick = { selectTab(0) },
                        testTag = "subtab_requests"
                    )
                    SubTabChip(
                        title = "পেমেন্ট ভেরিফিকেশন",
                        badgeCount = pendingPaymentsCount,
                        icon = Icons.Default.PriceCheck,
                        selected = selectedTab == 1,
                        accentColor = AmberWarning,
                        onClick = { selectTab(1) },
                        testTag = "subtab_payments"
                    )
                    SubTabChip(
                        title = "চলমান কিস্তি ও বকেয়া",
                        badgeCount = overdueCount,
                        icon = Icons.Default.EventRepeat,
                        selected = selectedTab == 2,
                        accentColor = if (overdueCount > 0) CoralDanger else EmeraldSuccess,
                        onClick = { selectTab(2) },
                        testTag = "subtab_installments"
                    )
                }

                // Search bar + Manual Course Access button for Tabs 0, 1, 2
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
                                        0 -> "মোবাইল নম্বর (student_phone), নাম বা কোর্স খুঁজুন..."
                                        1 -> "মোবাইল নম্বর, TrxID, প্রেরক নম্বর বা নাম খুঁজুন..."
                                        else -> "মোবাইল নম্বর (017XXXXXXXX), নাম বা কোর্স খুঁজুন..."
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

                        Button(
                            onClick = { isManualCourseAccessDialogOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("btn_open_manual_course_access")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "ম্যানুয়াল কোর্স এক্সেস",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "নম্বর দিয়ে\nকোর্স দিন",
                                color = Color.White,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

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
                    nowMillis = nowMillis,
                    isProcessing = isProcessingAction,
                    onFilterChange = { requestTabFilter = it },
                    onOpenApproveDialog = { enrollmentForPricingDialog = it },
                    onOpenManualAccessDialog = { isManualCourseAccessDialogOpen = true },
                    onOpenTrialActionDialog = { enrollment, action ->
                        trialActionDialogTarget = enrollment to action
                    },
                    onQuickTrialAction = { payload ->
                        onManageCourseTrial(payload) {}
                    },
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
                    nowMillis = nowMillis,
                    isProcessing = isProcessingAction,
                    onFilterChange = { installmentTabFilter = it },
                    onEnforceOverdue = onEnforceOverdueInstallments,
                    onExtendDueDays = { enrollment, days -> onExtendDueDate(enrollment, days) },
                    onOpenCustomDaysDialog = { enrollmentForCustomDaysDialog = it },
                    onSetStatus = { enrollment, targetStatus ->
                        onUpdateEnrollmentStatus(enrollment, targetStatus)
                    },
                    onOpenEditPlans = { enrollmentForPricingDialog = it },
                    onOpenManualAccessDialog = { isManualCourseAccessDialogOpen = true },
                    onOpenTrialActionDialog = { enrollment, action ->
                        trialActionDialogTarget = enrollment to action
                    },
                    onQuickTrialAction = { payload ->
                        onManageCourseTrial(payload) {}
                    },
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
                    onSave = onSavePaymentSettings,
                    onCopyText = { label, text ->
                        clipboardManager.setText(AnnotatedString(text))
                        onShowToast("$label কপি করা হয়েছে!")
                    },
                    onShowToast = onShowToast
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
    nowMillis: Long,
    isProcessing: Boolean,
    onFilterChange: (String) -> Unit,
    onOpenApproveDialog: (CourseAccessEnrollment) -> Unit,
    onOpenManualAccessDialog: () -> Unit,
    onOpenTrialActionDialog: (CourseAccessEnrollment, String) -> Unit,
    onQuickTrialAction: (ManageCourseTrialPayload) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val filteredList = remember(enrollments, searchQuery, selectedFilter) {
        enrollments.filter { item ->
            val statusMatch = when (selectedFilter) {
                "REQUESTED" -> item.isAccessRequestedStatus
                "APPROVED_TO_BUY" -> item.status.equals("APPROVED_TO_BUY", ignoreCase = true) ||
                    item.status.equals("PAYMENT_REJECTED", ignoreCase = true)
                "TRIAL_ACTIVE" -> item.isTrialActiveStatus
                "TRIAL_EXPIRED" -> item.isTrialExpiredStatus
                else -> true
            }
            val query = searchQuery.trim().lowercase()
            val queryMatch = query.isEmpty() ||
                (item.studentPhone?.lowercase()?.contains(query) == true) ||
                (item.effectivePhone.lowercase().contains(query)) ||
                (item.shikhoAccountKey?.lowercase()?.contains(query) == true) ||
                (item.effectiveAccountKey.lowercase().contains(query)) ||
                (item.studentName?.lowercase()?.contains(query) == true) ||
                (item.programTitle?.lowercase()?.contains(query) == true) ||
                (item.programId?.lowercase()?.contains(query) == true) ||
                (item.effectiveClass.lowercase().contains(query))
            statusMatch && queryMatch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Manual Course Access Banner Card (11-digit student_phone based)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricBlue.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
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
                            text = "মোবাইল নম্বর দিয়ে সরাসরি কোর্স, ফ্রি ট্রায়াল বা কিস্তি দিন",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = "স্টুডেন্টের ১১ ডিজিটের নম্বর (01XXXXXXXXX) দিয়ে কোর্স অ্যাসাইন করলে যেকোনো ফোনে লগইন করলেই কোর্স পাবে।",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenManualAccessDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("btn_manual_assign_from_requests")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Slate950,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ম্যানুয়ালি দিন",
                            color = Slate950,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

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
                            "নতুন রিকোয়েস্ট (${enrollments.count { it.isAccessRequestedStatus }})",
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
                    selected = selectedFilter == "TRIAL_ACTIVE",
                    onClick = { onFilterChange("TRIAL_ACTIVE") },
                    label = {
                        Text(
                            "🎁 চলমান ট্রায়াল (${enrollments.count { it.isTrialActiveStatus }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                        selectedLabelColor = NeonCyan,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
                FilterChip(
                    selected = selectedFilter == "TRIAL_EXPIRED",
                    onClick = { onFilterChange("TRIAL_EXPIRED") },
                    label = {
                        Text(
                            "⏰ ট্রায়াল শেষ (${enrollments.count { it.isTrialExpiredStatus }})",
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
                    nowMillis = nowMillis,
                    isProcessing = isProcessing,
                    onConfigureClick = { onOpenApproveDialog(enrollment) },
                    onOpenTrialActionDialog = { action -> onOpenTrialActionDialog(enrollment, action) },
                    onQuickTrialAction = onQuickTrialAction,
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
    nowMillis: Long,
    isProcessing: Boolean,
    onConfigureClick: () -> Unit,
    onOpenTrialActionDialog: (String) -> Unit,
    onQuickTrialAction: (ManageCourseTrialPayload) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val statusColor = statusBadgeColor(enrollment.status)
    val statusLabel = statusBadgeLabelBn(enrollment.status)
    val plans = remember(enrollment.installmentPlansJson) { enrollment.parsedInstallmentPlans() }
    val primaryPhone = enrollment.effectivePhone.takeIf { it.isNotBlank() }
        ?: enrollment.effectiveAccountKey.takeIf { it.isNotBlank() }
        ?: "মোবাইল নম্বর নেই"

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
            // Header: Student Phone + Student Name + Status Badge
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
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = primaryPhone,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = buildString {
                                append(enrollment.studentName?.takeIf { it.isNotBlank() } ?: "শিক্ষার্থী")
                                if (enrollment.effectiveClass.isNotBlank()) {
                                    append(" • ${formatClassBn(enrollment.effectiveClass)}")
                                }
                                if (enrollment.effectiveGroup.isNotBlank()) {
                                    append(" (${formatGroupBn(enrollment.effectiveGroup)})")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
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

            // Student Mobile (`student_phone`) & Shikho Account Key (`shikho_account_key`) chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val studentPhoneVal = enrollment.effectivePhone.ifBlank { primaryPhone }
                val accKey = enrollment.effectiveAccountKey.ifBlank { studentPhoneVal }
                CopyableMetaChip(
                    modifier = Modifier.weight(1f),
                    label = "স্টুডেন্ট মোবাইল (student_phone)",
                    value = studentPhoneVal,
                    accentColor = NeonCyan,
                    onCopy = { onCopyText("স্টুডেন্ট মোবাইল নম্বর", studentPhoneVal) }
                )
                CopyableMetaChip(
                    modifier = Modifier.weight(1f),
                    label = "অ্যাকাউন্ট কী (shikho_account_key)",
                    value = accKey,
                    accentColor = EmeraldSuccess,
                    onCopy = { onCopyText("অ্যাকাউন্ট কী", accKey) }
                )
            }

            // Show already configured price & plans if approved to buy
            if (enrollment.totalPrice > 0 || plans.isNotEmpty() || enrollment.trialEnabled) {
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

            // Free Trial Duration Control Panel (Always visible on student Course Enrollment Card)
            EnrollmentTrialControlPanel(
                enrollment = enrollment,
                nowMillis = nowMillis,
                isProcessing = isProcessing,
                onOpenTrialActionDialog = onOpenTrialActionDialog,
                onQuickTrialAction = onQuickTrialAction
            )

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
                        containerColor = if (enrollment.isAccessRequestedStatus) ElectricBlue else Slate700
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
                        text = if (enrollment.isAccessRequestedStatus) {
                            "দাম, কিস্তি ও ট্রায়াল সেট করে অনুমোদন দিন"
                        } else {
                            "দাম, কিস্তি ও ট্রায়াল পরিবর্তন করুন"
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
                (tx.studentPhone?.lowercase()?.contains(query) == true) ||
                (tx.effectiveStudentPhone.lowercase().contains(query)) ||
                (tx.shikhoAccountKey?.lowercase()?.contains(query) == true) ||
                (tx.transactionId?.lowercase()?.contains(query) == true) ||
                (tx.senderMobile?.lowercase()?.contains(query) == true) ||
                (tx.studentName?.lowercase()?.contains(query) == true) ||
                (tx.programTitle?.lowercase()?.contains(query) == true) ||
                (tx.programId?.lowercase()?.contains(query) == true) ||
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
            // Header: Student Phone & Name + Payment Method + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val txStudentPhone = transaction.effectiveStudentPhone.takeIf { it.isNotBlank() }
                    if (txStudentPhone != null) {
                        Text(
                            text = "📱 $txStudentPhone • ${transaction.studentName?.takeIf { it.isNotBlank() } ?: "শিক্ষার্থী"}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                    } else {
                        Text(
                            text = transaction.studentName?.takeIf { it.isNotBlank() } ?: "অজ্ঞাত শিক্ষার্থী",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                    }
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
    nowMillis: Long,
    isProcessing: Boolean,
    onFilterChange: (String) -> Unit,
    onEnforceOverdue: () -> Unit,
    onExtendDueDays: (CourseAccessEnrollment, Int) -> Unit,
    onOpenCustomDaysDialog: (CourseAccessEnrollment) -> Unit,
    onSetStatus: (CourseAccessEnrollment, String) -> Unit,
    onOpenEditPlans: (CourseAccessEnrollment) -> Unit,
    onOpenManualAccessDialog: () -> Unit,
    onOpenTrialActionDialog: (CourseAccessEnrollment, String) -> Unit,
    onQuickTrialAction: (ManageCourseTrialPayload) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val filteredList = remember(enrollments, searchQuery, selectedFilter) {
        enrollments.filter { item ->
            val statusMatch = when (selectedFilter) {
                "DUE_AND_OVERDUE" ->
                    item.isOverdueStatus ||
                        (item.status.equals("ACTIVE", ignoreCase = true) && item.dueAmount > 0) ||
                        item.status.equals("PAYMENT_PENDING", ignoreCase = true) ||
                        item.isTrialActiveStatus ||
                        item.isTrialExpiredStatus
                "OVERDUE" -> item.isOverdueStatus
                "TRIAL_ACTIVE" -> item.isTrialActiveStatus
                "TRIAL_EXPIRED" -> item.isTrialExpiredStatus
                "ACTIVE_DUE" -> item.status.equals("ACTIVE", ignoreCase = true) && item.dueAmount > 0
                "ALL_ACTIVE" -> item.status.equals("ACTIVE", ignoreCase = true) || item.isTrialActiveStatus
                "REVOKED" -> item.status.equals("REVOKED", ignoreCase = true)
                else -> !item.isAccessRequestedStatus
            }
            val query = searchQuery.trim().lowercase()
            val queryMatch = query.isEmpty() ||
                (item.studentPhone?.lowercase()?.contains(query) == true) ||
                (item.effectivePhone.lowercase().contains(query)) ||
                (item.shikhoAccountKey?.lowercase()?.contains(query) == true) ||
                (item.effectiveAccountKey.lowercase().contains(query)) ||
                (item.studentName?.lowercase()?.contains(query) == true) ||
                (item.programTitle?.lowercase()?.contains(query) == true) ||
                (item.programId?.lowercase()?.contains(query) == true)
            statusMatch && queryMatch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Overdue Enforcement + Manual Course Assignment Banner Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CoralDanger.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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

                    HorizontalDivider(color = Slate700)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "১১ ডিজিট মোবাইল নম্বর দিয়ে সরাসরি কোর্স বা কিস্তি যোগ করুন:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onOpenManualAccessDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_manual_assign_from_installments")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Slate950,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ কোর্স / কিস্তি দিন",
                                color = Slate950,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
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
                    label = { Text("বকেয়া, ট্রায়াল ও ওভারডিউ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberWarning.copy(alpha = 0.25f),
                        selectedLabelColor = AmberWarning,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
                FilterChip(
                    selected = selectedFilter == "TRIAL_ACTIVE",
                    onClick = { onFilterChange("TRIAL_ACTIVE") },
                    label = {
                        Text(
                            "🎁 চলমান ট্রায়াল (${enrollments.count { it.isTrialActiveStatus }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                        selectedLabelColor = NeonCyan,
                        containerColor = Slate800,
                        labelColor = Slate400
                    )
                )
                FilterChip(
                    selected = selectedFilter == "TRIAL_EXPIRED",
                    onClick = { onFilterChange("TRIAL_EXPIRED") },
                    label = {
                        Text(
                            "⏰ ট্রায়াল শেষ (${enrollments.count { it.isTrialExpiredStatus }})",
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
                    selected = selectedFilter == "OVERDUE",
                    onClick = { onFilterChange("OVERDUE") },
                    label = {
                        Text(
                            "মেয়াদোত্তীর্ণ (${enrollments.count { it.isOverdueStatus }})",
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
                            "সকল সক্রিয় কোর্স (${enrollments.count { it.status.equals("ACTIVE", true) || it.isTrialActiveStatus }})",
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
                    subtitle = "চলমান কিস্তি, ফ্রি ট্রায়াল, বকেয়া টাকা এবং মেয়াদোত্তীর্ণ (Overdue) শিক্ষার্থীদের তালিকা এখানে দেখা যাবে।"
                )
            }
        } else {
            items(filteredList, key = { it.id }) { enrollment ->
                InstallmentLedgerCard(
                    enrollment = enrollment,
                    nowMillis = nowMillis,
                    isProcessing = isProcessing,
                    onExtend7Days = { onExtendDueDays(enrollment, 7) },
                    onExtend15Days = { onExtendDueDays(enrollment, 15) },
                    onOpenCustomDays = { onOpenCustomDaysDialog(enrollment) },
                    onSetStatus = { targetStatus -> onSetStatus(enrollment, targetStatus) },
                    onOpenEditPlans = { onOpenEditPlans(enrollment) },
                    onOpenTrialActionDialog = { action -> onOpenTrialActionDialog(enrollment, action) },
                    onQuickTrialAction = onQuickTrialAction,
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
    nowMillis: Long,
    isProcessing: Boolean,
    onExtend7Days: () -> Unit,
    onExtend15Days: () -> Unit,
    onOpenCustomDays: () -> Unit,
    onSetStatus: (String) -> Unit,
    onOpenEditPlans: () -> Unit,
    onOpenTrialActionDialog: (String) -> Unit,
    onQuickTrialAction: (ManageCourseTrialPayload) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val isOverdue = enrollment.isOverdueStatus
    val isActive = enrollment.status.equals("ACTIVE", ignoreCase = true)
    val statusColor = statusBadgeColor(enrollment.status)
    val statusLabel = statusBadgeLabelBn(enrollment.status)
    val primaryPhone = enrollment.effectivePhone.takeIf { it.isNotBlank() }
        ?: enrollment.effectiveAccountKey.takeIf { it.isNotBlank() }
        ?: "মোবাইল নম্বর নেই"

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
            // Top Row: Student Phone + Student Name + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📱 $primaryPhone",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = buildString {
                            append(enrollment.studentName?.takeIf { it.isNotBlank() } ?: "শিক্ষার্থী")
                            if (enrollment.effectiveClass.isNotBlank()) {
                                append(" • ${formatClassBn(enrollment.effectiveClass)}")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = enrollment.programTitle?.takeIf { it.isNotBlank() }
                            ?: enrollment.programId
                            ?: "কোর্স",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                StatusPill(label = statusLabel, color = statusColor)
            }

            // Copyable Student Phone & Account Key Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val studentPhoneVal = enrollment.effectivePhone.ifBlank { primaryPhone }
                val accKey = enrollment.effectiveAccountKey.ifBlank { studentPhoneVal }
                CopyableMetaChip(
                    modifier = Modifier.weight(1f),
                    label = "স্টুডেন্ট মোবাইল (student_phone)",
                    value = studentPhoneVal,
                    accentColor = NeonCyan,
                    onCopy = { onCopyText("স্টুডেন্ট মোবাইল নম্বর", studentPhoneVal) }
                )
                CopyableMetaChip(
                    modifier = Modifier.weight(1f),
                    label = "অ্যাকাউন্ট কী (shikho_account_key)",
                    value = accKey,
                    accentColor = EmeraldSuccess,
                    onCopy = { onCopyText("অ্যাকাউন্ট কী", accKey) }
                )
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

            // Free Trial Duration Control Panel (Always visible on student Course Enrollment Card)
            EnrollmentTrialControlPanel(
                enrollment = enrollment,
                nowMillis = nowMillis,
                isProcessing = isProcessing,
                onOpenTrialActionDialog = onOpenTrialActionDialog,
                onQuickTrialAction = onQuickTrialAction
            )

            // Quick Due-Date Extension Buttons (+7 days, +15 days, Custom)
            if (enrollment.dueAmount > 0 || isOverdue) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কিস্তির মেয়াদ বাড়ান:",
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

            // Quick Manual Status Control: ACTIVE / TRIAL_ACTIVE / OVERDUE / REVOKED
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
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
                    Text("প্ল্যান, দাম ও ট্রায়াল এডিট", color = Slate200, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
// Sub-Tab 4: পেমেন্ট নম্বর সেটিংস — Dynamic Payment Methods Manager (`course_payment_settings`)
// =============================================================================
@Composable
private fun PaymentGatewaySettingsSubTab(
    settings: CoursePaymentSettings,
    isSaving: Boolean,
    onSave: (UpsertCoursePaymentSettingsPayload) -> Unit,
    onCopyText: (String, String) -> Unit,
    onShowToast: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DynamicPaymentMethodsManagerSection(
                settings = settings,
                isSaving = isSaving,
                onSave = onSave,
                onCopyText = onCopyText,
                onShowToast = onShowToast
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun DynamicPaymentMethodsManagerSection(
    settings: CoursePaymentSettings,
    isSaving: Boolean,
    onSave: (UpsertCoursePaymentSettingsPayload) -> Unit,
    onCopyText: (String, String) -> Unit,
    onShowToast: (String) -> Unit
) {
    val initialMethods = remember(settings) { settings.effectivePaymentMethods() }
    val methodsList = remember(settings) {
        mutableStateListOf<DynamicPaymentMethodItem>().apply {
            addAll(initialMethods)
        }
    }

    var defaultPriceText by remember(settings) {
        mutableStateOf((settings.defaultCoursePrice ?: 1000).toString())
    }
    var instructionsBn by remember(settings) {
        mutableStateOf(
            settings.effectiveInstructionsBn.takeIf { it.isNotBlank() }
                ?: "প্রদত্ত পেমেন্ট নম্বরে টাকা পাঠিয়ে নিচের ফর্মে আপনার প্রেরক নম্বর ও Transaction ID জমা দিন।"
        )
    }

    var isAddOrEditDialogOpen by remember { mutableStateOf(false) }
    var editingMethodIndex by remember { mutableStateOf<Int?>(null) }
    var methodIndexToDelete by remember { mutableStateOf<Int?>(null) }

    fun pushSyncedSave(updatedMethods: List<DynamicPaymentMethodItem>) {
        val defaultPrice = defaultPriceText.toIntOrNull()?.coerceAtLeast(0) ?: 1000
        val payload = UpsertCoursePaymentSettingsPayload.fromMethods(
            methods = updatedMethods,
            instructionsBn = instructionsBn.trim(),
            defaultPrice = defaultPrice
        )
        onSave(payload)
    }

    if (isAddOrEditDialogOpen) {
        val existingItem = editingMethodIndex?.let { idx -> methodsList.getOrNull(idx) }
        AddOrEditPaymentMethodDialog(
            initialItem = existingItem,
            existingIds = methodsList.mapIndexedNotNull { idx, item ->
                if (idx == editingMethodIndex) null else item.id.lowercase()
            }.toSet(),
            onDismiss = {
                isAddOrEditDialogOpen = false
                editingMethodIndex = null
            },
            onConfirm = { savedItem ->
                val targetIdx = editingMethodIndex
                if (targetIdx != null && targetIdx in methodsList.indices) {
                    methodsList[targetIdx] = savedItem
                } else {
                    methodsList.add(savedItem)
                }
                isAddOrEditDialogOpen = false
                editingMethodIndex = null
                pushSyncedSave(methodsList.toList())
            },
            onShowToast = onShowToast
        )
    }

    methodIndexToDelete?.let { deleteIdx ->
        val targetMethod = methodsList.getOrNull(deleteIdx)
        if (targetMethod != null) {
            AlertDialog(
                onDismissRequest = { methodIndexToDelete = null },
                containerColor = Slate900,
                title = {
                    Text(
                        text = "পেমেন্ট মেথড মুছে ফেলবেন?",
                        color = CoralDanger,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = "আপনি কি নিশ্চিত যে '${targetMethod.nameBn} (${targetMethod.accountNumber})' পেমেন্ট মেথডটি তালিকা থেকে ডিলিট করতে চান? ডিলিট করলে স্টুডেন্ট অ্যাপে এটি আর দেখাবে না।",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate200)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (deleteIdx in methodsList.indices) {
                                methodsList.removeAt(deleteIdx)
                                pushSyncedSave(methodsList.toList())
                            }
                            methodIndexToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralDanger)
                    ) {
                        Text("হ্যাঁ, ডিলিট করুন", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { methodIndexToDelete = null }) {
                        Text("বাতিল", color = Slate400)
                    }
                }
            )
        }
    }

    val enabledCount = methodsList.count { it.isEnabled }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header + Add Payment Method Button Card
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
                                text = "ডায়নামিক পেমেন্ট মেথড ম্যানেজার",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = "মোট ${methodsList.size}টি মেথড • সক্রিয় (ON) আছে $enabledCount টি",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (enabledCount > 0) EmeraldSuccess else AmberWarning,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Text(
                    text = "যে পেমেন্ট মেথডগুলো এখানে যোগ করে ON রাখবেন, স্টুডেন্ট অ্যাপে শুধুমাত্র সেগুলোই রিয়েল-টাইমে দেখাবে। কোনো ডামি নম্বর কখনোই দেখানো হবে না।",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate400,
                        fontSize = 11.sp
                    )
                )

                Button(
                    onClick = {
                        editingMethodIndex = null
                        isAddOrEditDialogOpen = true
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_add_payment_method")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Slate950,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "নতুন পেমেন্ট মেথড যোগ করুন (+ Add Payment Method)",
                        color = Slate950,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Dynamic Payment Methods List
        if (methodsList.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Default.AccountBalanceWallet,
                title = "কোনো পেমেন্ট মেথড যোগ করা হয়নি",
                subtitle = "উপরের '+ নতুন পেমেন্ট মেথড যোগ করুন' বাটনে ক্লিক করে বিকাশ, নগদ, রকেট, উপায় বা ব্যাংক নম্বর যোগ করুন।"
            )
        } else {
            methodsList.forEachIndexed { idx, method ->
                DynamicPaymentMethodCard(
                    method = method,
                    isSaving = isSaving,
                    onToggleEnabled = { newEnabled ->
                        methodsList[idx] = method.copy(isEnabled = newEnabled)
                        pushSyncedSave(methodsList.toList())
                    },
                    onEditClick = {
                        editingMethodIndex = idx
                        isAddOrEditDialogOpen = true
                    },
                    onDeleteClick = {
                        methodIndexToDelete = idx
                    },
                    onCopyNumber = {
                        onCopyText("${method.nameBn} নম্বর", method.accountNumber)
                    }
                )
            }
        }

        // General Instructions & Default Course Fee Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate700, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "সাধারণ পেমেন্ট নির্দেশনা ও ডিফল্ট কোর্স ফি",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NeonCyan,
                        fontWeight = FontWeight.ExtraBold
                    )
                )

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

                OutlinedTextField(
                    value = instructionsBn,
                    onValueChange = { instructionsBn = it },
                    label = { Text("সাধারণ পেমেন্টের নিয়মাবলী ও নির্দেশনা (বাংলায়)") },
                    minLines = 3,
                    maxLines = 6,
                    colors = adminTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_payment_instructions_bn")
                )

                Button(
                    onClick = {
                        pushSyncedSave(methodsList.toList())
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

@Composable
private fun DynamicPaymentMethodCard(
    method: DynamicPaymentMethodItem,
    isSaving: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCopyNumber: () -> Unit
) {
    val brandColor = parseHexColorSafe(method.colorHex, paymentMethodColor(method.code))
    val borderColor = if (method.isEnabled) brandColor.copy(alpha = 0.65f) else Slate700

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (method.isEnabled) Slate800 else Slate900
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (method.isEnabled) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("payment_method_card_${method.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Color Badge + Name/Code + ON/OFF Switch
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
                            .clip(RoundedCornerShape(11.dp))
                            .background(brandColor.copy(alpha = 0.22f))
                            .border(1.dp, brandColor.copy(alpha = 0.6f), RoundedCornerShape(11.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = method.code.take(2).uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = brandColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = method.nameBn,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Surface(
                                color = brandColor.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, brandColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = method.code,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = brandColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Text(
                            text = method.accountType.ifBlank { "Send Money করুন" },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Direct 1-click ON/OFF Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (method.isEnabled) "ON" else "OFF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (method.isEnabled) EmeraldSuccess else Slate400,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Switch(
                        checked = method.isEnabled,
                        onCheckedChange = { onToggleEnabled(it) },
                        enabled = !isSaving,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldSuccess,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate950
                        ),
                        modifier = Modifier.testTag("switch_payment_method_${method.id}")
                    )
                }
            }

            // Account Number Display Box (Copyable)
            Surface(
                color = Slate950,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, brandColor.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCopyNumber() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "একাউন্ট / মোবাইল নম্বর",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate400,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = method.accountNumber,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (method.isEnabled) Color.White else Slate400,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = brandColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "কপি",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = brandColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Method-specific instruction if present
            if (method.instructionBn.isNotBlank()) {
                Text(
                    text = "নির্দেশনা: ${method.instructionBn}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate200,
                        fontSize = 11.sp
                    )
                )
            }

            // Bottom Row: Status Indicator + Edit & Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(
                    label = if (method.isEnabled) "স্টুডেন্ট অ্যাপে চালু আছে" else "স্টুডেন্ট অ্যাপে বন্ধ আছে",
                    color = if (method.isEnabled) EmeraldSuccess else Slate400
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onEditClick,
                        enabled = !isSaving,
                        border = BorderStroke(1.dp, ElectricBlueLight.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(9.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_edit_payment_method_${method.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = ElectricBlueLight,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Edit",
                            color = ElectricBlueLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onDeleteClick,
                        enabled = !isSaving,
                        border = BorderStroke(1.dp, CoralDanger.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(9.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_delete_payment_method_${method.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = CoralDanger,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Delete",
                            color = CoralDanger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private data class PaymentMethodPreset(
    val id: String,
    val code: String,
    val nameBn: String,
    val accountType: String,
    val defaultInstructionBn: String,
    val colorHex: String
)

@Composable
private fun AddOrEditPaymentMethodDialog(
    initialItem: DynamicPaymentMethodItem?,
    existingIds: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (DynamicPaymentMethodItem) -> Unit,
    onShowToast: (String) -> Unit
) {
    val isEdit = initialItem != null

    var nameBn by remember(initialItem) { mutableStateOf(initialItem?.nameBn ?: "বিকাশ") }
    var code by remember(initialItem) { mutableStateOf(initialItem?.code ?: "bKash") }
    var methodId by remember(initialItem) { mutableStateOf(initialItem?.id ?: "bkash") }
    var accountNumber by remember(initialItem) { mutableStateOf(initialItem?.accountNumber ?: "") }
    var accountType by remember(initialItem) { mutableStateOf(initialItem?.accountType ?: "Send Money করুন") }
    var instructionBn by remember(initialItem) {
        mutableStateOf(initialItem?.instructionBn ?: "বিকাশ পার্সোনাল নম্বরে Send Money করে TrxID দিন।")
    }
    var colorHex by remember(initialItem) { mutableStateOf(initialItem?.colorHex ?: "#E2136E") }
    var isEnabled by remember(initialItem) { mutableStateOf(initialItem?.isEnabled ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val presets = remember {
        listOf(
            PaymentMethodPreset(
                id = "bkash",
                code = "bKash",
                nameBn = "বিকাশ",
                accountType = "Send Money করুন",
                defaultInstructionBn = "বিকাশ পার্সোনাল নম্বরে Send Money করে TrxID দিন।",
                colorHex = "#E2136E"
            ),
            PaymentMethodPreset(
                id = "nagad",
                code = "Nagad",
                nameBn = "নগদ",
                accountType = "Send Money করুন",
                defaultInstructionBn = "নগদ পার্সোনাল নম্বরে Send Money করে TrxID দিন।",
                colorHex = "#F7941D"
            ),
            PaymentMethodPreset(
                id = "rocket",
                code = "Rocket",
                nameBn = "রকেট",
                accountType = "Send Money করুন",
                defaultInstructionBn = "রকেট পার্সোনাল নম্বরে Send Money করে TrxID দিন।",
                colorHex = "#8C3494"
            ),
            PaymentMethodPreset(
                id = "upay",
                code = "Upay",
                nameBn = "উপায়",
                accountType = "Send Money করুন",
                defaultInstructionBn = "উপায় নম্বরে Send Money করে TrxID দিন।",
                colorHex = "#0066B3"
            ),
            PaymentMethodPreset(
                id = "cellfin",
                code = "CellFin",
                nameBn = "সেলফিন",
                accountType = "Send Money করুন",
                defaultInstructionBn = "সেলফিন নম্বরে ফান্ড ট্রান্সফার করে TrxID দিন।",
                colorHex = "#009444"
            ),
            PaymentMethodPreset(
                id = "bkash_merchant",
                code = "bKash",
                nameBn = "বিকাশ মার্চেন্ট",
                accountType = "Make Payment (Merchant)",
                defaultInstructionBn = "বিকাশ অ্যাপ থেকে Make Payment অপশনে গিয়ে পেমেন্ট করে TrxID দিন।",
                colorHex = "#E2136E"
            ),
            PaymentMethodPreset(
                id = "bank",
                code = "Bank",
                nameBn = "ব্যাংক একাউন্ট",
                accountType = "Bank Transfer",
                defaultInstructionBn = "ব্যাংক একাউন্টে টাকা পাঠিয়ে রেফারেন্স বা TrxID দিন।",
                colorHex = "#2563EB"
            )
        )
    }

    val accountTypePresets = remember {
        listOf(
            "Send Money করুন",
            "Make Payment (Merchant)",
            "Cash Out",
            "Bank Transfer"
        )
    }

    val colorPresets = remember {
        listOf(
            "bKash" to "#E2136E",
            "Nagad" to "#F7941D",
            "Rocket" to "#8C3494",
            "Upay" to "#0066B3",
            "CellFin" to "#009444",
            "Bank" to "#2563EB"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
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
                            text = if (isEdit) "পেমেন্ট মেথড এডিট করুন" else "নতুন পেমেন্ট মেথড যোগ করুন",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = "যেকোনো মোবাইল ব্যাংকিং বা ব্যাংক একাউন্ট যুক্ত করুন",
                            style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                HorizontalDivider(color = Slate700)

                // Quick Presets Row
                Text(
                    text = "দ্রুত প্রিসেট সিলেক্ট করুন (অথবা নিচে কাস্টম লিখুন):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate200,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        val pColor = parseHexColorSafe(preset.colorHex, NeonCyan)
                        val isSelected = nameBn == preset.nameBn && code.equals(preset.code, ignoreCase = true)
                        Surface(
                            color = if (isSelected) pColor.copy(alpha = 0.25f) else Slate800,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) pColor else Slate700
                            ),
                            modifier = Modifier.clickable {
                                methodId = preset.id
                                code = preset.code
                                nameBn = preset.nameBn
                                accountType = preset.accountType
                                instructionBn = preset.defaultInstructionBn
                                colorHex = preset.colorHex
                                validationError = null
                            }
                        ) {
                            Text(
                                text = "${preset.nameBn} (${preset.code})",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.White else pColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }

                // Name (Bengali) & Code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nameBn,
                        onValueChange = {
                            nameBn = it
                            validationError = null
                        },
                        label = { Text("মেথডের নাম (বাংলায়)") },
                        placeholder = { Text("যেমন: বিকাশ") },
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_input_method_name_bn")
                    )

                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            code = it
                            if (!isEdit) {
                                methodId = it.trim().lowercase().replace(" ", "_")
                            }
                            validationError = null
                        },
                        label = { Text("মেথডের কোড (Code)") },
                        placeholder = { Text("যেমন: bKash") },
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_input_method_code")
                    )
                }

                // Account / Mobile Number (Mandatory & No Dummy Allowed)
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = {
                        accountNumber = it
                        validationError = null
                    },
                    label = { Text("একাউন্ট / মোবাইল নম্বর (খালি রাখা যাবে না)") },
                    placeholder = { Text("যেমন: 017XXXXXXXX") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = adminTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_input_method_account_number")
                )

                // Account Type Selector + Input
                Text(
                    text = "একাউন্টের ধরন:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate200,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accountTypePresets.forEach { typePreset ->
                        SmallActionChip(
                            label = typePreset,
                            color = if (accountType == typePreset) NeonCyan else Slate400,
                            onClick = { accountType = typePreset }
                        )
                    }
                }
                OutlinedTextField(
                    value = accountType,
                    onValueChange = { accountType = it },
                    label = { Text("একাউন্টের ধরন (Account Type)") },
                    placeholder = { Text("যেমন: Send Money করুন / Make Payment") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Method Specific Instruction (Bengali)
                OutlinedTextField(
                    value = instructionBn,
                    onValueChange = { instructionBn = it },
                    label = { Text("মেথডের নির্দেশনা (Instruction - ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: বিকাশ পার্সোনাল নম্বরে Send Money করে TrxID দিন।") },
                    minLines = 2,
                    maxLines = 3,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Color Picker Presets + Hex Input
                Text(
                    text = "কার্ডের রঙ (Brand Color):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate200,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorPresets.forEach { (label, hex) ->
                        val c = parseHexColorSafe(hex, ElectricBlue)
                        val selected = colorHex.equals(hex, ignoreCase = true)
                        Surface(
                            color = c.copy(alpha = if (selected) 0.3f else 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(if (selected) 2.dp else 1.dp, c),
                            modifier = Modifier.clickable { colorHex = hex }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                )
                                Text(
                                    text = "$label ($hex)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = colorHex,
                    onValueChange = { colorHex = it },
                    label = { Text("রঙের Hex কোড (Color Hex)") },
                    placeholder = { Text("#E2136E") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Active / Inactive Switch (is_enabled)
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
                                text = "পেমেন্ট মেথড সক্রিয় রাখুন (is_enabled)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (isEnabled) "চালু — স্টুডেন্ট অ্যাপে এই পেমেন্ট মেথডটি দেখাবে" else "বন্ধ — স্টুডেন্ট অ্যাপে এটি লুকানো থাকবে",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isEnabled) EmeraldSuccess else Slate400
                                )
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldSuccess
                            )
                        )
                    }
                }

                // Validation Error Banner
                validationError?.let { errMsg ->
                    Surface(
                        color = CoralDanger.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CoralDanger),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errMsg,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CoralDanger,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }

                // Save & Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Slate600),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("বাতিল", color = Slate200, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val cleanName = nameBn.trim()
                            val cleanCode = code.trim()
                            val cleanNum = accountNumber.trim()

                            if (cleanName.isBlank()) {
                                validationError = "পেমেন্ট মেথডের নাম লিখুন (যেমন: বিকাশ, নগদ)।"
                                return@Button
                            }
                            if (cleanCode.isBlank()) {
                                validationError = "পেমেন্ট মেথডের কোড লিখুন (যেমন: bKash, Nagad)।"
                                return@Button
                            }
                            if (cleanNum.isBlank()) {
                                validationError = "একাউন্ট বা মোবাইল নম্বর খালি রাখা যাবে না!"
                                return@Button
                            }
                            if (CoursePaymentSettings.isDummyPhoneNumber(cleanNum)) {
                                validationError = "ডামি নম্বর ($cleanNum) ব্যবহার করা যাবে না! আপনার আসল পেমেন্ট নম্বর দিন।"
                                onShowToast("আসল পেমেন্ট নম্বর দিন!")
                                return@Button
                            }

                            val baseId = methodId.trim()
                                .ifBlank { cleanCode.lowercase().replace(" ", "_") }
                                .lowercase()
                            val finalId = if (!isEdit && existingIds.contains(baseId)) {
                                "${baseId}_${System.currentTimeMillis() % 1000}"
                            } else {
                                baseId
                            }

                            val cleanColor = colorHex.trim().let {
                                if (it.startsWith("#")) it else "#$it"
                            }

                            onConfirm(
                                DynamicPaymentMethodItem(
                                    id = finalId,
                                    code = cleanCode,
                                    nameBn = cleanName,
                                    accountNumber = cleanNum,
                                    accountType = accountType.trim().ifBlank { "Send Money করুন" },
                                    instructionBn = instructionBn.trim(),
                                    colorHex = cleanColor,
                                    isEnabled = isEnabled
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("btn_confirm_payment_method")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Slate950,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEdit) "আপডেট ও সেভ করুন" else "মেথড যোগ ও সেভ করুন",
                            color = Slate950,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Dialog 1: Approve Course Access Request + Custom Price, Installments & Free Trial Builder
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

    // Free Trial State (`trial_enabled`, `trial_days`, `trial_hours`, `start_trial_immediately`)
    var trialEnabled by remember(enrollment) {
        mutableStateOf(enrollment.trialEnabled || enrollment.isTrialActiveStatus)
    }
    var trialDaysText by remember(enrollment) {
        mutableStateOf(
            if (enrollment.trialDays > 0 || enrollment.trialHours > 0) {
                enrollment.trialDays.toString()
            } else {
                "1"
            }
        )
    }
    var trialHoursText by remember(enrollment) {
        mutableStateOf(enrollment.trialHours.toString())
    }
    var startTrialImmediately by remember(enrollment) {
        mutableStateOf(enrollment.isTrialActiveStatus)
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
                            text = "কোর্সের মূল্য, কিস্তি ও ফ্রি ট্রায়াল নির্ধারণ",
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

                // =========================================================================
                // Step 4: 🎁 ফ্রি ট্রায়াল সুবিধা (Free Trial) Section
                // =========================================================================
                Surface(
                    color = Slate950,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = if (trialEnabled) 1.5.dp else 1.dp,
                        color = if (trialEnabled) NeonCyan else Slate700
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🎁 ফ্রি ট্রায়াল সুবিধা (Free Trial)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Text(
                                    text = "কেনার আগে ফ্রি ট্রায়াল দিন (trial_enabled)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Switch(
                                checked = trialEnabled,
                                onCheckedChange = { enabled ->
                                    trialEnabled = enabled
                                    if (enabled && (trialDaysText.toIntOrNull() ?: 0) == 0 && (trialHoursText.toIntOrNull() ?: 0) == 0) {
                                        trialDaysText = "1"
                                    }
                                    if (!enabled) {
                                        startTrialImmediately = false
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Slate950,
                                    checkedTrackColor = NeonCyan
                                ),
                                modifier = Modifier.testTag("switch_trial_enabled")
                            )
                        }

                        AnimatedVisibility(visible = trialEnabled) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Days (`trial_days`) & Hours (`trial_hours`) Input Boxes
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = trialDaysText,
                                        onValueChange = { trialDaysText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("দিন (trial_days)") },
                                        placeholder = { Text("যেমন: 1, 3") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = adminTextFieldColors(),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_trial_days")
                                    )
                                    OutlinedTextField(
                                        value = trialHoursText,
                                        onValueChange = { trialHoursText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("ঘণ্টা (trial_hours)") },
                                        placeholder = { Text("যেমন: 0, 12") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = adminTextFieldColors(),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_trial_hours")
                                    )
                                }

                                // Quick Preset Buttons: ১ দিন, ২ দিন, ৩ দিন, ৭ দিন, ১২ ঘণ্টা
                                Text(
                                    text = "কুইক প্রিসেট বাটন:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate400,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val presets = listOf(
                                         Triple("১ দিন", 1, 0),
                                        Triple("২ দিন", 2, 0),
                                        Triple("৩ দিন", 3, 0),
                                        Triple("৭ দিন", 7, 0),
                                        Triple("১২ ঘণ্টা", 0, 12)
                                    )
                                    presets.forEach { (label, pDays, pHours) ->
                                        val isCurrentPreset =
                                            (trialDaysText.toIntOrNull() ?: 0) == pDays &&
                                                (trialHoursText.toIntOrNull() ?: 0) == pHours
                                        SmallActionChip(
                                            label = label,
                                            color = if (isCurrentPreset) NeonCyan else Slate200,
                                            onClick = {
                                                trialDaysText = pDays.toString()
                                                trialHoursText = pHours.toString()
                                            }
                                        )
                                    }
                                }

                                // Optional Checkbox: "এখনই সরাসরি ট্রায়াল চালু করে দিন (Start Trial Immediately)"
                                Surface(
                                    color = if (startTrialImmediately) EmeraldSuccess.copy(alpha = 0.16f) else Slate900,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (startTrialImmediately) EmeraldSuccess else Slate700
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { startTrialImmediately = !startTrialImmediately }
                                        .testTag("checkbox_start_trial_immediately")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = startTrialImmediately,
                                            onCheckedChange = { startTrialImmediately = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = EmeraldSuccess,
                                                uncheckedColor = Slate400,
                                                checkmarkColor = Slate950
                                            )
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "এখনই সরাসরি ট্রায়াল চালু করে দিন (Start Trial Immediately)",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = if (startTrialImmediately) EmeraldSuccess else Color.White,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            )
                                            Text(
                                                text = if (startTrialImmediately) {
                                                    "এখনই status = 'TRIAL_ACTIVE' এবং trial_ends_at সেট হয়ে সাথে সাথে কোর্স আনলক হবে।"
                                                } else {
                                                    "আনচেক থাকলে status = 'APPROVED_TO_BUY' হবে এবং স্টুডেন্ট অ্যাপ থেকে নিজে 'ফ্রি ট্রায়াল শুরু করুন' বাটনে চাপতে পারবে।"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Slate400,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
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

                // Action 1: Approve with Price, Installment Plans & Free Trial (`APPROVED_TO_BUY` or `TRIAL_ACTIVE`)
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

                        val rawDays = if (trialEnabled) (trialDaysText.toIntOrNull()?.coerceAtLeast(0) ?: 0) else 0
                        val rawHours = if (trialEnabled) (trialHoursText.toIntOrNull()?.coerceAtLeast(0) ?: 0) else 0
                        val safeDays = if (trialEnabled && rawDays == 0 && rawHours == 0) 1 else rawDays

                        onSubmit(
                            ApproveCourseAccessRequestRpcPayload(
                                enrollmentId = enrollment.id,
                                totalPrice = totalPrice,
                                installmentEnabled = installmentEnabled,
                                installmentPlansJson = finalPlans,
                                adminNote = adminNote.trim(),
                                instantFreeUnlock = false,
                                studentPhone = enrollment.effectivePhone,
                                shikhoAccountKey = enrollment.effectiveAccountKey.ifBlank { enrollment.effectivePhone },
                                programId = enrollment.programId,
                                trialEnabled = trialEnabled,
                                trialDays = safeDays,
                                trialHours = rawHours,
                                startTrialImmediately = trialEnabled && startTrialImmediately
                            )
                        )
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (trialEnabled && startTrialImmediately) NeonCyan else ElectricBlue
                    ),
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
                            color = if (trialEnabled && startTrialImmediately) Slate950 else Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (trialEnabled && startTrialImmediately) Slate950 else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when {
                            trialEnabled && startTrialImmediately -> "এখনই ফ্রি ট্রায়াল চালু ও অনুমোদন দিন"
                            trialEnabled -> "দাম, কিস্তি ও ফ্রি ট্রায়ালসহ অনুমোদন দিন"
                            else -> "দাম ও কিস্তি সেট করে অনুমোদন দিন"
                        },
                        color = if (trialEnabled && startTrialImmediately) Slate950 else Color.White,
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
                                instantFreeUnlock = true,
                                studentPhone = enrollment.effectivePhone,
                                shikhoAccountKey = enrollment.effectiveAccountKey.ifBlank { enrollment.effectivePhone },
                                programId = enrollment.programId
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
// Dialog 1B: Manual Course Access / Installment Grant Dialog (By 11-digit Student Phone)
// =============================================================================
@Composable
private fun ManualCourseAccessDialog(
    existingEnrollments: List<CourseAccessEnrollment>,
    defaultCoursePrice: Int,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (ManualCourseAccessPayload) -> Unit
) {
    var studentPhoneText by remember { mutableStateOf("") }
    var studentNameText by remember { mutableStateOf("") }
    var programIdText by remember { mutableStateOf("") }
    var programTitleText by remember { mutableStateOf("") }
    var classCodeText by remember { mutableStateOf("") }
    var groupCodeText by remember { mutableStateOf("") }

    // "ACTIVE" (সরাসরি কোর্স আনলক / সক্রিয় কিস্তি) or "APPROVED_TO_BUY" (দাম ও কিস্তি প্ল্যান সেট করে অনুমোদন)
    var selectedStatus by remember { mutableStateOf("ACTIVE") }
    var totalPriceText by remember { mutableStateOf("0") }
    var paidAmountText by remember { mutableStateOf("0") }
    var nextDueDaysText by remember { mutableStateOf("30") }
    var installmentEnabled by remember { mutableStateOf(true) }
    var adminNoteText by remember { mutableStateOf("অ্যাডমিন কর্তৃক মোবাইল নম্বরে কোর্স অ্যাক্সেস প্রদান করা হয়েছে।") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val knownCourses = remember(existingEnrollments) {
        existingEnrollments
            .filter { !it.programId.isNullOrBlank() }
            .distinctBy { it.programId }
            .take(15)
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "মোবাইল নম্বর দিয়ে কোর্স / কিস্তি দিন",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = "স্টুডেন্ট যেকোনো ফোনে এই নম্বরে লগইন করলেই কোর্স ও কিস্তি পাবে",
                            style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan)
                        )
                    }
                    IconButton(onClick = { if (!isProcessing) onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                HorizontalDivider(color = Slate700)

                // Info Banner explaining student_phone, shikho_account_key, and device_id sync
                Surface(
                    color = ElectricBlue.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ℹ️ স্টুডেন্টের ১১ ডিজিটের মোবাইল নম্বরটি (01XXXXXXXXX) ডাটাবেজের student_phone, shikho_account_key এবং device_id কলামে অটোমেটিক সেভ হবে।",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate200,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // 1. 11-Digit Student Mobile Number
                OutlinedTextField(
                    value = studentPhoneText,
                    onValueChange = {
                        studentPhoneText = it.filter { ch -> ch.isDigit() || ch == '+' }
                        validationError = null
                    },
                    label = { Text("স্টুডেন্টের ১১ ডিজিটের মোবাইল নম্বর (01XXXXXXXXX) *") },
                    placeholder = { Text("যেমন: 017XXXXXXXX") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = adminTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_student_phone")
                )

                // 2. Student Name
                OutlinedTextField(
                    value = studentNameText,
                    onValueChange = { studentNameText = it },
                    label = { Text("শিক্ষার্থীর নাম (student_name)") },
                    placeholder = { Text("যেমন: তানভীর আহমেদ") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_student_name")
                )

                // 3. Quick Course Picker from existing enrollments (if any)
                if (knownCourses.isNotEmpty()) {
                    Text(
                        text = "বিদ্যমান কোর্স থেকে দ্রুত সিলেক্ট করুন (অথবা নিচে লিখুন):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        knownCourses.forEach { courseItem ->
                            val isSelected = programIdText == courseItem.programId
                            Surface(
                                color = if (isSelected) ElectricBlue.copy(alpha = 0.28f) else Slate800,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) NeonCyan else Slate700
                                ),
                                modifier = Modifier.clickable {
                                    programIdText = courseItem.programId.orEmpty()
                                    programTitleText = courseItem.programTitle.orEmpty()
                                    classCodeText = courseItem.effectiveClass
                                    groupCodeText = courseItem.effectiveGroup
                                    validationError = null
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = courseItem.programTitle?.takeIf { it.isNotBlank() }
                                            ?: courseItem.programId.orEmpty(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "ID: ${courseItem.programId}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate400,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Program ID & Program Title
                OutlinedTextField(
                    value = programTitleText,
                    onValueChange = {
                        programTitleText = it
                        validationError = null
                    },
                    label = { Text("কোর্সের নাম (program_title) *") },
                    placeholder = { Text("যেমন: HSC '26 একাডেমিক প্রোগ্রাম বিজ্ঞান") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_program_title")
                )

                OutlinedTextField(
                    value = programIdText,
                    onValueChange = {
                        programIdText = it
                        validationError = null
                    },
                    label = { Text("কোর্স / প্রোগ্রাম আইডি (program_id) *") },
                    placeholder = { Text("যেমন: 65a1b2c3d4e5f6...") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_program_id")
                )

                // Class & Group (Optional)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = classCodeText,
                        onValueChange = { classCodeText = it },
                        label = { Text("শ্রেণি কোড (যেমন: C10)") },
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = groupCodeText,
                        onValueChange = { groupCodeText = it },
                        label = { Text("বিভাগ (যেমন: science)") },
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                // 4. Grant Mode Selection: ACTIVE vs APPROVED_TO_BUY
                Text(
                    text = "অ্যাক্সেসের ধরন নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate200,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (selectedStatus == "ACTIVE") EmeraldSuccess.copy(alpha = 0.22f) else Slate800,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            width = if (selectedStatus == "ACTIVE") 1.5.dp else 1.dp,
                            color = if (selectedStatus == "ACTIVE") EmeraldSuccess else Slate700
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedStatus = "ACTIVE"
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "সরাসরি আনলক (ACTIVE)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (selectedStatus == "ACTIVE") EmeraldSuccess else Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = "ফ্রি আনলক অথবা ১ম কিস্তি পরিশোধিত হিসেবে সক্রিয়",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Surface(
                        color = if (selectedStatus == "APPROVED_TO_BUY") AmberWarning.copy(alpha = 0.22f) else Slate800,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            width = if (selectedStatus == "APPROVED_TO_BUY") 1.5.dp else 1.dp,
                            color = if (selectedStatus == "APPROVED_TO_BUY") AmberWarning else Slate700
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedStatus = "APPROVED_TO_BUY"
                                if ((totalPriceText.toIntOrNull() ?: 0) == 0) {
                                    totalPriceText = defaultCoursePrice.toString()
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "কেনার অনুমতি (APPROVED)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (selectedStatus == "APPROVED_TO_BUY") AmberWarning else Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = "দাম ও কিস্তি প্ল্যান সেট হবে, স্টুডেন্ট পেমেন্ট করবে",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // 5. Pricing & Installment Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalPriceText,
                        onValueChange = { totalPriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("মোট কোর্স ফি ৳ (০ = ফ্রি)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = adminTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )

                    if (selectedStatus == "ACTIVE") {
                        OutlinedTextField(
                            value = paidAmountText,
                            onValueChange = { paidAmountText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("জমা / পরিশোধিত ৳") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = adminTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val currentTotal = totalPriceText.toIntOrNull() ?: 0
                val currentPaid = paidAmountText.toIntOrNull() ?: 0
                if (selectedStatus == "ACTIVE" && currentTotal > currentPaid) {
                    OutlinedTextField(
                        value = nextDueDaysText,
                        onValueChange = { nextDueDaysText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("বকেয়া ৳${currentTotal - currentPaid}-এর পরবর্তী কিস্তির মেয়াদ (দিন)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = adminTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = adminNoteText,
                    onValueChange = { adminNoteText = it },
                    label = { Text("অ্যাডমিন নোট (ঐচ্ছিক)") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                validationError?.let { err ->
                    Surface(
                        color = CoralDanger.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CoralDanger),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CoralDanger,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }

                // Submit & Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (!isProcessing) onDismiss() },
                        border = BorderStroke(1.dp, Slate600),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("বাতিল", color = Slate200, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val cleanPhone = CourseAccessEnrollment.normalizeBdPhone(studentPhoneText)
                            if (!CourseAccessEnrollment.isValidBdPhone(cleanPhone)) {
                                validationError = "সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন (যেমন: 017XXXXXXXX)"
                                return@Button
                            }
                            val cleanProgTitle = programTitleText.trim()
                            val cleanProgId = programIdText.trim().ifBlank {
                                cleanProgTitle.lowercase().replace(" ", "_")
                            }
                            if (cleanProgId.isBlank()) {
                                validationError = "কোর্সের নাম ও প্রোগ্রাম আইডি (program_id) দিন।"
                                return@Button
                            }

                            val totalPrice = totalPriceText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                            val paidAmount = if (selectedStatus == "ACTIVE") {
                                (paidAmountText.toIntOrNull()?.coerceAtLeast(0) ?: 0).coerceAtMost(totalPrice)
                            } else {
                                0
                            }
                            val dueDays = nextDueDaysText.toIntOrNull()?.coerceAtLeast(1) ?: 30
                            val plans = InstallmentPlanItem.buildDefaultPlansForPrice(totalPrice)

                            onSubmit(
                                ManualCourseAccessPayload(
                                    studentPhone = cleanPhone,
                                    studentName = studentNameText.trim(),
                                    programId = cleanProgId,
                                    programTitle = cleanProgTitle.ifBlank { cleanProgId },
                                    classCode = classCodeText.trim(),
                                    groupCode = groupCodeText.trim(),
                                    status = selectedStatus,
                                    totalPrice = totalPrice,
                                    paidAmount = paidAmount,
                                    installmentEnabled = installmentEnabled && totalPrice > 0,
                                    installmentPlansJson = plans,
                                    nextDueDays = dueDays,
                                    adminNote = adminNoteText.trim()
                                )
                            )
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("btn_confirm_manual_course_access")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Slate950
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Slate950,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "সেভ ও অ্যাসাইন করুন",
                            color = Slate950,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
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

// =============================================================================
// Free Trial Control Panel on Each Student Course Enrollment Card
// =============================================================================
@Composable
private fun EnrollmentTrialControlPanel(
    enrollment: CourseAccessEnrollment,
    nowMillis: Long,
    isProcessing: Boolean,
    onOpenTrialActionDialog: (String) -> Unit,
    onQuickTrialAction: (ManageCourseTrialPayload) -> Unit
) {
    val isTrialRunning = enrollment.isTrialCurrentlyRunning(nowMillis)
    val isTrialExpired = enrollment.isTrialExpiredStatus ||
        (enrollment.isTrialActiveStatus && !isTrialRunning && enrollment.parseTrialEndsAtMillis() != null)
    val hasTrialConfig = enrollment.trialEnabled ||
        enrollment.isTrialUsed ||
        enrollment.trialDays > 0 ||
        enrollment.trialHours > 0 ||
        enrollment.isTrialActiveStatus ||
        enrollment.isTrialExpiredStatus

    val borderColor = when {
        isTrialRunning -> NeonCyan.copy(alpha = 0.65f)
        isTrialExpired -> CoralDanger.copy(alpha = 0.6f)
        enrollment.trialEnabled -> EmeraldSuccess.copy(alpha = 0.5f)
        else -> Slate700
    }

    Surface(
        color = Slate950,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trial_control_panel_${enrollment.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Title + Live Trial Status / Countdown Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🎁 ফ্রি ট্রায়াল কন্ট্রোল প্যানেল",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                val badgeText = when {
                    isTrialRunning -> "চলমান • ${enrollment.formatTrialCountdownBn(nowMillis)}"
                    isTrialExpired -> "ট্রায়ালের মেয়াদ শেষ"
                    enrollment.trialEnabled && !enrollment.isTrialUsed -> "অনুমোদিত (${enrollment.formatConfiguredTrialDurationBn()})"
                    enrollment.isTrialUsed -> "ট্রায়াল ব্যবহৃত"
                    else -> "ট্রায়াল বন্ধ"
                }
                val badgeColor = when {
                    isTrialRunning -> NeonCyan
                    isTrialExpired -> CoralDanger
                    enrollment.trialEnabled && !enrollment.isTrialUsed -> EmeraldSuccess
                    else -> Slate400
                }
                StatusPill(label = badgeText, color = badgeColor)
            }

            // Detailed Trial Timing Info (if configured or active)
            if (hasTrialConfig) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildString {
                            append("বরাদ্দ: ${enrollment.formatConfiguredTrialDurationBn()}")
                            append(" • ব্যবহৃত: ${if (enrollment.isTrialUsed) "হ্যাঁ" else "না"}")
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate200,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                    if (!enrollment.trialEndsAt.isNullOrBlank()) {
                        Text(
                            text = "শেষ: ${formatIsoTimestampBn(enrollment.trialEndsAt)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isTrialExpired) CoralDanger else NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // 5 Action Buttons Row (Scrollable chips for mobile ergonomics):
            // 1. ➕ মেয়াদ বাড়ান (ADD_HOURS)
            // 2. ➖ মেয়াদ কমান (REDUCE_HOURS)
            // 3. ⏱️ নতুন মেয়াদ সেট করুন (SET_DURATION)
            // 4. 🛑 ট্রায়াল এখনই শেষ করুন (END_TRIAL_NOW)
            // 5. 🔄 ট্রায়াল রিসেট করুন (RESET_TRIAL)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallActionChip(
                    label = "➕ মেয়াদ বাড়ান",
                    color = EmeraldSuccess,
                    onClick = { if (!isProcessing) onOpenTrialActionDialog("ADD_HOURS") }
                )

                SmallActionChip(
                    label = "➖ মেয়াদ কমান",
                    color = AmberWarning,
                    onClick = { if (!isProcessing) onOpenTrialActionDialog("REDUCE_HOURS") }
                )

                SmallActionChip(
                    label = "⏱️ নতুন মেয়াদ সেট করুন",
                    color = NeonCyan,
                    onClick = { if (!isProcessing) onOpenTrialActionDialog("SET_DURATION") }
                )

                SmallActionChip(
                    label = "🛑 ট্রায়াল এখনই শেষ করুন",
                    color = CoralDanger,
                    onClick = {
                        if (!isProcessing) {
                            onQuickTrialAction(
                                ManageCourseTrialPayload(
                                    enrollmentId = enrollment.id,
                                    studentPhone = enrollment.effectivePhone,
                                    shikhoAccountKey = enrollment.effectiveAccountKey.ifBlank { enrollment.effectivePhone },
                                    programId = enrollment.programId,
                                    action = "END_TRIAL_NOW",
                                    days = 0,
                                    hours = 0,
                                    currentTrialStartedAt = enrollment.trialStartedAt,
                                    currentTrialEndsAt = enrollment.trialEndsAt,
                                    currentTrialDays = enrollment.trialDays,
                                    currentTrialHours = enrollment.trialHours
                                )
                            )
                        }
                    }
                )

                SmallActionChip(
                    label = "🔄 ট্রায়াল রিসেট করুন",
                    color = ElectricBlueLight,
                    onClick = {
                        if (!isProcessing) {
                            onQuickTrialAction(
                                ManageCourseTrialPayload(
                                    enrollmentId = enrollment.id,
                                    studentPhone = enrollment.effectivePhone,
                                    shikhoAccountKey = enrollment.effectiveAccountKey.ifBlank { enrollment.effectivePhone },
                                    programId = enrollment.programId,
                                    action = "RESET_TRIAL",
                                    days = enrollment.trialDays.takeIf { it > 0 } ?: 1,
                                    hours = enrollment.trialHours,
                                    currentTrialStartedAt = enrollment.trialStartedAt,
                                    currentTrialEndsAt = enrollment.trialEndsAt,
                                    currentTrialDays = enrollment.trialDays,
                                    currentTrialHours = enrollment.trialHours
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

// =============================================================================
// Dialog 5: Trial Duration Management Dialog (ADD_HOURS, REDUCE_HOURS, SET_DURATION)
// =============================================================================
@Composable
private fun TrialDurationManageDialog(
    enrollment: CourseAccessEnrollment,
    action: String,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (days: Int, hours: Int) -> Unit
) {
    val defaultDays = when (action) {
        "SET_DURATION" -> if (enrollment.trialDays > 0 || enrollment.trialHours > 0) enrollment.trialDays else 1
        "ADD_HOURS" -> 1
        else -> 0
    }
    val defaultHours = when (action) {
        "SET_DURATION" -> enrollment.trialHours
        "REDUCE_HOURS" -> 6
        else -> 0
    }

    var daysText by remember(enrollment, action) { mutableStateOf(defaultDays.toString()) }
    var hoursText by remember(enrollment, action) { mutableStateOf(defaultHours.toString()) }

    val dialogTitle = when (action) {
        "ADD_HOURS" -> "➕ ফ্রি ট্রায়ালের মেয়াদ বাড়ান (Extend Trial)"
        "REDUCE_HOURS" -> "➖ ফ্রি ট্রায়ালের মেয়াদ কমান (Reduce Trial)"
        else -> "⏱️ ফ্রি ট্রায়ালের নতুন মেয়াদ সেট করুন"
    }

    val dialogDescription = when (action) {
        "ADD_HOURS" -> "নির্ধারিত দিন ও ঘণ্টা বর্তমান ট্রায়ালের সাথে যোগ হবে। ট্রায়াল শেষ হয়ে গেলেও নতুন করে status = 'TRIAL_ACTIVE' হয়ে কোর্স আনলক হয়ে যাবে।"
        "REDUCE_HOURS" -> "চলমান ট্রায়ালের শেষ সময় (trial_ends_at) থেকে নির্ধারিত দিন বা ঘণ্টা কমে যাবে।"
        else -> "এখন থেকে নির্ধারিত দিন ও ঘণ্টার জন্য নতুন করে ট্রায়াল চালু (status = 'TRIAL_ACTIVE') হবে।"
    }

    val accentColor = when (action) {
        "ADD_HOURS" -> EmeraldSuccess
        "REDUCE_HOURS" -> AmberWarning
        else -> NeonCyan
    }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = Slate900,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = dialogTitle,
                    color = accentColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${enrollment.studentName ?: "শিক্ষার্থী"} • ${enrollment.programTitle ?: enrollment.programId ?: ""}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = dialogDescription,
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate200)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = daysText,
                        onValueChange = { daysText = it.filter { c -> c.isDigit() } },
                        label = { Text("দিন (Days)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = adminTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it.filter { c -> c.isDigit() } },
                        label = { Text("ঘণ্টা (Hours)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = adminTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "কুইক প্রিসেট:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate400,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        Triple("৬ ঘণ্টা", 0, 6),
                        Triple("১২ ঘণ্টা", 0, 12),
                        Triple("১ দিন", 1, 0),
                        Triple("২ দিন", 2, 0),
                        Triple("৩ দিন", 3, 0),
                        Triple("৭ দিন", 7, 0)
                    )
                    presets.forEach { (label, pDays, pHours) ->
                        val selected = (daysText.toIntOrNull() ?: 0) == pDays &&
                            (hoursText.toIntOrNull() ?: 0) == pHours
                        SmallActionChip(
                            label = label,
                            color = if (selected) accentColor else Slate200,
                            onClick = {
                                daysText = pDays.toString()
                                hoursText = pHours.toString()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = daysText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    val h = hoursText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    if (d == 0 && h == 0) {
                        onConfirm(0, 1)
                    } else {
                        onConfirm(d, h)
                    }
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    text = when (action) {
                        "ADD_HOURS" -> "মেয়াদ বাড়ান"
                        "REDUCE_HOURS" -> "মেয়াদ কমান"
                        else -> "নতুন মেয়াদ সেট করুন"
                    },
                    color = Slate950,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("বাতিল", color = Slate400)
            }
        }
    )
}

private fun statusBadgeColor(status: String?): Color {
    return when (status?.uppercase()?.trim()) {
        "REQUESTED", "ACCESS_REQUESTED" -> ElectricBlueLight
        "APPROVED_TO_BUY" -> AmberWarning
        "TRIAL_ACTIVE" -> NeonCyan
        "TRIAL_EXPIRED" -> CoralDanger
        "PAYMENT_PENDING" -> NeonCyan
        "ACTIVE" -> EmeraldSuccess
        "PAYMENT_REJECTED" -> CoralDanger
        "OVERDUE", "INSTALLMENT_OVERDUE" -> CoralDanger
        "REVOKED" -> CoralDanger
        else -> Slate400
    }
}

private fun statusBadgeLabelBn(status: String?): String {
    return when (status?.uppercase()?.trim()) {
        "REQUESTED", "ACCESS_REQUESTED" -> "নতুন রিকোয়েস্ট"
        "APPROVED_TO_BUY" -> "কেনার অনুমতি প্রাপ্ত"
        "TRIAL_ACTIVE" -> "🎁 ফ্রি ট্রায়াল চলমান"
        "TRIAL_EXPIRED" -> "⏰ ট্রায়ালের মেয়াদ শেষ"
        "PAYMENT_PENDING" -> "পেমেন্ট যাচাই চলছে"
        "ACTIVE" -> "আনলকড (সক্রিয়)"
        "PAYMENT_REJECTED" -> "পেমেন্ট বাতিল"
        "OVERDUE", "INSTALLMENT_OVERDUE" -> "কিস্তি মেয়াদোত্তীর্ণ (লকড)"
        "REVOKED" -> "লকড (Revoked)"
        else -> status ?: "অজ্ঞাত"
    }
}

private fun parseHexColorSafe(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val clean = if (hex.trim().startsWith("#")) hex.trim() else "#${hex.trim()}"
        Color(android.graphics.Color.parseColor(clean))
    } catch (_: Exception) {
        fallback
    }
}

private fun paymentMethodColor(method: String?): Color {
    val m = method?.lowercase()?.trim().orEmpty()
    return when {
        m.contains("bkash") -> Color(0xFFE2136E)
        m.contains("nagad") -> Color(0xFFF7941D)
        m.contains("rocket") -> Color(0xFF8C3494)
        m.contains("upay") -> Color(0xFF0066B3)
        m.contains("cellfin") -> Color(0xFF009444)
        m.contains("bank") -> Color(0xFF2563EB)
        else -> NeonCyan
    }
}

private fun formatPaymentMethodLabel(method: String?): String {
    val m = method?.lowercase()?.trim().orEmpty()
    return when {
        m == "bkash" -> "বিকাশ (bKash)"
        m == "nagad" -> "নগদ (Nagad)"
        m == "rocket" -> "রকেট (Rocket)"
        m == "upay" -> "উপায় (Upay)"
        m == "cellfin" -> "সেলফিন (CellFin)"
        m == "bank" -> "ব্যাংক (Bank)"
        else -> method?.takeIf { it.isNotBlank() } ?: "পেমেন্ট"
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
