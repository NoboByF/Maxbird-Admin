package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.math.ceil

/**
 * Individual payment method item stored inside `public.course_payment_settings.payment_methods_json` (JSONB array).
 */
@JsonClass(generateAdapter = true)
data class DynamicPaymentMethodItem(
    @Json(name = "id")
    val id: String,

    @Json(name = "code")
    val code: String,

    @Json(name = "name_bn")
    val nameBn: String,

    @Json(name = "account_number")
    val accountNumber: String,

    @Json(name = "account_type")
    val accountType: String = "Send Money করুন",

    @Json(name = "instruction_bn")
    val instructionBn: String = "",

    @Json(name = "color_hex")
    val colorHex: String = "#E2136E",

    @Json(name = "is_enabled")
    val isEnabled: Boolean = true
)

/**
 * Row model for `public.course_payment_settings` (id = 1).
 * Matches live Supabase SQL schema 100%, including `payment_methods_json`, legacy number/enabled columns,
 * and both `payment_instructions_bn` and `payment_instruction_bn`.
 * Never uses dummy numbers like 01700000000 or 01800000000.
 */
@JsonClass(generateAdapter = true)
data class CoursePaymentSettings(
    @Json(name = "id")
    val id: Int = 1,

    @Json(name = "payment_methods_json")
    val paymentMethodsJson: List<DynamicPaymentMethodItem>? = null,

    @Json(name = "bkash_number")
    val bkashNumber: String? = "",

    @Json(name = "bkash_enabled")
    val bkashEnabled: Boolean? = false,

    @Json(name = "nagad_number")
    val nagadNumber: String? = "",

    @Json(name = "nagad_enabled")
    val nagadEnabled: Boolean? = false,

    @Json(name = "rocket_number")
    val rocketNumber: String? = "",

    @Json(name = "rocket_enabled")
    val rocketEnabled: Boolean? = false,

    @Json(name = "upay_number")
    val upayNumber: String? = "",

    @Json(name = "upay_enabled")
    val upayEnabled: Boolean? = false,

    @Json(name = "payment_instructions_bn")
    val paymentInstructionsBn: String? = null,

    @Json(name = "payment_instruction_bn")
    val paymentInstructionBn: String? = null,

    @Json(name = "default_course_price")
    val defaultCoursePrice: Int? = 1000,

    @Json(name = "updated_at")
    val updatedAt: String? = null
) {
    val effectiveInstructionsBn: String
        get() = paymentInstructionsBn?.takeIf { it.isNotBlank() }
            ?: paymentInstructionBn?.takeIf { it.isNotBlank() }
            ?: "প্রদত্ত পেমেন্ট নম্বরে টাকা পাঠিয়ে নিচের বক্সে আপনার প্রেরক মোবাইল নম্বর এবং Transaction ID দিয়ে সাবমিট করুন।"

    /**
     * Returns the dynamic payment methods list, filtering out any dummy placeholder numbers
     * (`01700000000`, `01800000000`, etc.). If `payment_methods_json` was not yet populated
     * in the database, migrates any real (non-dummy) legacy numbers automatically.
     */
    fun effectivePaymentMethods(): List<DynamicPaymentMethodItem> {
        val fromJson = paymentMethodsJson.orEmpty().filter { item ->
            val num = item.accountNumber.trim()
            num.isNotBlank() && !isDummyPhoneNumber(num)
        }
        if (fromJson.isNotEmpty() || paymentMethodsJson != null) {
            return fromJson
        }

        val migrated = mutableListOf<DynamicPaymentMethodItem>()
        val cleanBkash = bkashNumber?.trim().orEmpty()
        if (cleanBkash.isNotBlank() && !isDummyPhoneNumber(cleanBkash)) {
            migrated.add(
                DynamicPaymentMethodItem(
                    id = "bkash",
                    code = "bKash",
                    nameBn = "বিকাশ",
                    accountNumber = cleanBkash,
                    accountType = "Send Money করুন",
                    instructionBn = "বিকাশ পার্সোনাল নম্বরে Send Money করে TrxID দিন।",
                    colorHex = "#E2136E",
                    isEnabled = bkashEnabled ?: true
                )
            )
        }

        val cleanNagad = nagadNumber?.trim().orEmpty()
        if (cleanNagad.isNotBlank() && !isDummyPhoneNumber(cleanNagad)) {
            migrated.add(
                DynamicPaymentMethodItem(
                    id = "nagad",
                    code = "Nagad",
                    nameBn = "নগদ",
                    accountNumber = cleanNagad,
                    accountType = "Send Money করুন",
                    instructionBn = "নগদ পার্সোনাল নম্বরে Send Money করে TrxID দিন।",
                    colorHex = "#F7941D",
                    isEnabled = nagadEnabled ?: true
                )
            )
        }

        val cleanRocket = rocketNumber?.trim().orEmpty()
        if (cleanRocket.isNotBlank() && !isDummyPhoneNumber(cleanRocket)) {
            migrated.add(
                DynamicPaymentMethodItem(
                    id = "rocket",
                    code = "Rocket",
                    nameBn = "রকেট",
                    accountNumber = cleanRocket,
                    accountType = "Send Money করুন",
                    instructionBn = "রকেট নম্বরে Send Money করে TrxID দিন।",
                    colorHex = "#8C3494",
                    isEnabled = rocketEnabled ?: true
                )
            )
        }

        val cleanUpay = upayNumber?.trim().orEmpty()
        if (cleanUpay.isNotBlank() && !isDummyPhoneNumber(cleanUpay)) {
            migrated.add(
                DynamicPaymentMethodItem(
                    id = "upay",
                    code = "Upay",
                    nameBn = "উপায়",
                    accountNumber = cleanUpay,
                    accountType = "Send Money করুন",
                    instructionBn = "উপায় নম্বরে Send Money করে TrxID দিন।",
                    colorHex = "#0066B3",
                    isEnabled = upayEnabled ?: true
                )
            )
        }

        return migrated
    }

    companion object {
        private val DUMMY_NUMBERS = setOf(
            "01700000000",
            "01800000000",
            "01900000000",
            "01600000000",
            "01500000000",
            "01300000000",
            "01400000000",
            "017XXXXXXXX",
            "018XXXXXXXX",
            "019XXXXXXXX"
        )

        fun isDummyPhoneNumber(number: String?): Boolean {
            val clean = number?.trim().orEmpty()
            if (clean.isEmpty()) return false
            if (DUMMY_NUMBERS.any { it.equals(clean, ignoreCase = true) }) return true
            if (clean.endsWith("00000000") || clean.contains("XXXX", ignoreCase = true)) return true
            return false
        }
    }
}

/**
 * Payload for upserting/updating `public.course_payment_settings` (id = 1).
 * Saves `payment_methods_json` AND automatically syncs `bkash_number`, `bkash_enabled`,
 * `nagad_number`, `nagad_enabled`, `rocket_number`, `rocket_enabled`, `upay_number`, `upay_enabled`,
 * `payment_instructions_bn`, and `payment_instruction_bn`.
 */
@JsonClass(generateAdapter = true)
data class UpsertCoursePaymentSettingsPayload(
    @Json(name = "id")
    val id: Int = 1,

    @Json(name = "payment_methods_json")
    val paymentMethodsJson: List<DynamicPaymentMethodItem> = emptyList(),

    @Json(name = "bkash_number")
    val bkashNumber: String = "",

    @Json(name = "bkash_enabled")
    val bkashEnabled: Boolean = false,

    @Json(name = "nagad_number")
    val nagadNumber: String = "",

    @Json(name = "nagad_enabled")
    val nagadEnabled: Boolean = false,

    @Json(name = "rocket_number")
    val rocketNumber: String = "",

    @Json(name = "rocket_enabled")
    val rocketEnabled: Boolean = false,

    @Json(name = "upay_number")
    val upayNumber: String = "",

    @Json(name = "upay_enabled")
    val upayEnabled: Boolean = false,

    @Json(name = "payment_instructions_bn")
    val paymentInstructionsBn: String = "",

    @Json(name = "payment_instruction_bn")
    val paymentInstructionBn: String = paymentInstructionsBn,

    @Json(name = "default_course_price")
    val defaultCoursePrice: Int = 1000
) {
    companion object {
        /**
         * Builds a 100% synced payload from the list of dynamic payment methods, general instructions, and default price.
         * Never populates dummy numbers.
         */
        fun fromMethods(
            methods: List<DynamicPaymentMethodItem>,
            instructionsBn: String,
            defaultPrice: Int
        ): UpsertCoursePaymentSettingsPayload {
            val sanitizedMethods = methods.mapNotNull { item ->
                val cleanNum = item.accountNumber.trim()
                if (cleanNum.isBlank() || CoursePaymentSettings.isDummyPhoneNumber(cleanNum)) {
                    null
                } else {
                    item.copy(
                        id = item.id.trim().ifBlank { item.code.trim().lowercase() },
                        code = item.code.trim(),
                        nameBn = item.nameBn.trim(),
                        accountNumber = cleanNum,
                        accountType = item.accountType.trim().ifBlank { "Send Money করুন" },
                        instructionBn = item.instructionBn.trim(),
                        colorHex = item.colorHex.trim().ifBlank { "#E2136E" }
                    )
                }
            }

            fun findMethod(key: String): DynamicPaymentMethodItem? {
                return sanitizedMethods.firstOrNull {
                    it.id.equals(key, ignoreCase = true) ||
                        it.code.equals(key, ignoreCase = true) ||
                        it.id.startsWith("${key}_", ignoreCase = true)
                }
            }

            // Prefer an enabled method if multiple exist for the same provider, otherwise first matching
            fun findBestMethod(key: String): DynamicPaymentMethodItem? {
                val matches = sanitizedMethods.filter {
                    it.id.equals(key, ignoreCase = true) ||
                        it.code.equals(key, ignoreCase = true) ||
                        it.id.startsWith("${key}_", ignoreCase = true)
                }
                return matches.firstOrNull { it.isEnabled } ?: matches.firstOrNull()
            }

            val bkash = findBestMethod("bkash")
            val nagad = findBestMethod("nagad")
            val rocket = findBestMethod("rocket")
            val upay = findBestMethod("upay")

            // Note: Only write legacy number if enabled OR if the method exists;
            // to ensure legacy Student Apps only see enabled numbers if they read bkash_number directly without checking bkash_enabled,
            // we set bkash_number = bkash.accountNumber when bkash != null, and bkash_enabled = bkash.isEnabled.
            val bkashNum = if (bkash != null && bkash.isEnabled) bkash.accountNumber else (bkash?.accountNumber ?: "")
            val nagadNum = if (nagad != null && nagad.isEnabled) nagad.accountNumber else (nagad?.accountNumber ?: "")
            val rocketNum = if (rocket != null && rocket.isEnabled) rocket.accountNumber else (rocket?.accountNumber ?: "")
            val upayNum = if (upay != null && upay.isEnabled) upay.accountNumber else (upay?.accountNumber ?: "")

            val cleanInstructions = instructionsBn.trim()

            return UpsertCoursePaymentSettingsPayload(
                id = 1,
                paymentMethodsJson = sanitizedMethods,
                bkashNumber = bkashNum,
                bkashEnabled = bkash?.isEnabled == true && bkash.accountNumber.isNotBlank(),
                nagadNumber = nagadNum,
                nagadEnabled = nagad?.isEnabled == true && nagad.accountNumber.isNotBlank(),
                rocketNumber = rocketNum,
                rocketEnabled = rocket?.isEnabled == true && rocket.accountNumber.isNotBlank(),
                upayNumber = upayNum,
                upayEnabled = upay?.isEnabled == true && upay.accountNumber.isNotBlank(),
                paymentInstructionsBn = cleanInstructions,
                paymentInstructionBn = cleanInstructions,
                defaultCoursePrice = defaultPrice.coerceAtLeast(0)
            )
        }
    }
}

/**
 * Individual installment plan option stored inside `installment_plans_json` (JSONB).
 * Includes both the Student App keys (`plan_id`, `title`, `installments_count`, `amount_per_installment`, `interval_days`)
 * and extended keys (`title_bn`, `installment_count`, `per_installment_amount`, `first_payment`, `schedule_amounts`)
 * for 100% cross-compatibility.
 */
@JsonClass(generateAdapter = true)
data class InstallmentPlanItem(
    @Json(name = "plan_id")
    val planId: String = "full",

    @Json(name = "title")
    val title: String? = null,

    @Json(name = "title_bn")
    val titleBn: String? = null,

    @Json(name = "installments_count")
    val installmentsCount: Int? = null,

    @Json(name = "installment_count")
    val installmentCount: Int? = null,

    @Json(name = "amount_per_installment")
    val amountPerInstallment: Int? = null,

    @Json(name = "per_installment_amount")
    val perInstallmentAmount: Int? = null,

    @Json(name = "first_payment")
    val firstPayment: Int? = null,

    @Json(name = "interval_days")
    val intervalDays: Int = 30,

    @Json(name = "schedule_amounts")
    val scheduleAmounts: List<Int>? = null
) {
    val effectiveTitle: String
        get() = title?.takeIf { it.isNotBlank() }
            ?: titleBn?.takeIf { it.isNotBlank() }
            ?: "একবারে সম্পূর্ণ পেমেন্ট"

    val effectiveInstallmentsCount: Int
        get() = (installmentsCount ?: installmentCount ?: 1).coerceAtLeast(1)

    val effectiveAmountPerInstallment: Int
        get() = amountPerInstallment ?: perInstallmentAmount ?: firstPayment ?: 0

    val effectiveScheduleAmounts: List<Int>
        get() {
            val existing = scheduleAmounts
            if (!existing.isNullOrEmpty()) return existing
            val count = effectiveInstallmentsCount
            val perAmt = effectiveAmountPerInstallment
            return List(count) { perAmt }
        }

    companion object {
        fun create(
            planId: String,
            titleText: String,
            count: Int,
            amountPerInst: Int,
            intervalDays: Int,
            schedule: List<Int>? = null
        ): InstallmentPlanItem {
            val safeCount = count.coerceAtLeast(1)
            val finalSchedule = schedule ?: List(safeCount) { amountPerInst }
            val firstAmt = finalSchedule.firstOrNull() ?: amountPerInst
            return InstallmentPlanItem(
                planId = planId,
                title = titleText,
                titleBn = titleText,
                installmentsCount = safeCount,
                installmentCount = safeCount,
                amountPerInstallment = amountPerInst,
                perInstallmentAmount = amountPerInst,
                firstPayment = firstAmt,
                intervalDays = intervalDays,
                scheduleAmounts = finalSchedule
            )
        }

        /**
         * Builds default standard plans matching the Student App specification:
         * - full (1 installment, 30 days)
         * - inst_2 (2 installments, 15 days interval)
         * - inst_3 (3 installments, 10 days interval)
         * - inst_5 (5 installments, 7 days interval)
         */
        fun buildDefaultPlansForPrice(totalPrice: Int): List<InstallmentPlanItem> {
            val safePrice = totalPrice.coerceAtLeast(0)
            if (safePrice == 0) {
                return listOf(
                    create(
                        planId = "full",
                        titleText = "একবারে সম্পূর্ণ পেমেন্ট (০ টাকা)",
                        count = 1,
                        amountPerInst = 0,
                        intervalDays = 30,
                        schedule = listOf(0)
                    )
                )
            }

            val twoSchedules = buildScheduleAmounts(safePrice, 2)
            val twoPer = twoSchedules.firstOrNull() ?: safePrice

            val threeSchedules = buildScheduleAmounts(safePrice, 3)
            val threePer = threeSchedules.firstOrNull() ?: safePrice

            val fiveSchedules = buildScheduleAmounts(safePrice, 5)
            val fivePer = fiveSchedules.firstOrNull() ?: safePrice

            return listOf(
                create(
                    planId = "full",
                    titleText = "একবারে সম্পূর্ণ পেমেন্ট ($safePrice টাকা)",
                    count = 1,
                    amountPerInst = safePrice,
                    intervalDays = 30,
                    schedule = listOf(safePrice)
                ),
                create(
                    planId = "inst_2",
                    titleText = "২ কিস্তিতে পেমেন্ট ($twoPer টাকা করে)",
                    count = 2,
                    amountPerInst = twoPer,
                    intervalDays = 15,
                    schedule = twoSchedules
                ),
                create(
                    planId = "inst_3",
                    titleText = "৩ কিস্তিতে পেমেন্ট ($threePer টাকা করে)",
                    count = 3,
                    amountPerInst = threePer,
                    intervalDays = 10,
                    schedule = threeSchedules
                ),
                create(
                    planId = "inst_5",
                    titleText = "৫ কিস্তিতে পেমেন্ট ($fivePer টাকা করে প্রতি সপ্তাহে)",
                    count = 5,
                    amountPerInst = fivePer,
                    intervalDays = 7,
                    schedule = fiveSchedules
                )
            )
        }

        /**
         * Splits `totalPrice` across `count` installments cleanly so the sum equals `totalPrice`.
         */
        fun buildScheduleAmounts(totalPrice: Int, count: Int, customPerInstallment: Int? = null): List<Int> {
            val safeCount = count.coerceAtLeast(1)
            val safeTotal = totalPrice.coerceAtLeast(0)
            if (safeCount == 1) return listOf(safeTotal)

            val baseAmount = customPerInstallment?.takeIf { it > 0 }
                ?: ceil(safeTotal.toDouble() / safeCount.toDouble()).toInt()

            val result = mutableListOf<Int>()
            var remaining = safeTotal
            for (i in 1 until safeCount) {
                val installment = baseAmount.coerceAtMost(remaining.coerceAtLeast(0))
                result.add(installment)
                remaining = (remaining - installment).coerceAtLeast(0)
            }
            result.add(remaining)
            return result
        }
    }
}

/**
 * Row model for `public.course_access_enrollments`.
 * Matches live Supabase SQL schema 100%.
 */
@JsonClass(generateAdapter = true)
data class CourseAccessEnrollment(
    @Json(name = "id")
    val id: Long = 0L,

    @Json(name = "device_id")
    val deviceId: String? = null,

    @Json(name = "shikho_account_key")
    val shikhoAccountKey: String? = null,

    @Json(name = "student_name")
    val studentName: String? = null,

    @Json(name = "student_phone")
    val studentPhone: String? = null,

    @Json(name = "student_class")
    val studentClass: String? = null,

    @Json(name = "student_group")
    val studentGroup: String? = null,

    @Json(name = "program_id")
    val programId: String? = null,

    @Json(name = "program_title")
    val programTitle: String? = null,

    @Json(name = "class_code")
    val classCode: String? = null,

    @Json(name = "group_code")
    val groupCode: String? = null,

    @Json(name = "batch_id")
    val batchId: String? = null,

    @Json(name = "banner_url")
    val bannerUrl: String? = null,

    @Json(name = "status")
    val status: String? = "REQUESTED",

    @Json(name = "total_price")
    val totalPrice: Int = 1000,

    @Json(name = "paid_amount")
    val paidAmount: Int = 0,

    @Json(name = "due_amount")
    val dueAmount: Int = 1000,

    @Json(name = "installment_enabled")
    val installmentEnabled: Boolean = true,

    @Json(name = "allow_installments")
    val allowInstallments: Boolean? = true,

    @Json(name = "selected_plan_id")
    val selectedPlanId: String? = null,

    @Json(name = "selected_plan_title")
    val selectedPlanTitle: String? = null,

    @Json(name = "active_plan_id")
    val activePlanId: String? = null,

    @Json(name = "active_plan_title")
    val activePlanTitle: String? = null,

    @Json(name = "total_installments")
    val totalInstallments: Int = 1,

    @Json(name = "completed_installments")
    val completedInstallments: Int = 0,

    @Json(name = "next_installment_amount")
    val nextInstallmentAmount: Int = 0,

    @Json(name = "installment_interval_days")
    val installmentIntervalDays: Int = 7,

    @Json(name = "next_due_date")
    val nextDueDate: String? = null,

    @Json(name = "installment_plans_json")
    val installmentPlansJson: List<InstallmentPlanItem>? = emptyList(),

    @Json(name = "last_transaction_id")
    val lastTransactionId: String? = null,

    @Json(name = "last_payment_amount")
    val lastPaymentAmount: Int? = 0,

    @Json(name = "last_sender_mobile")
    val lastSenderMobile: String? = null,

    @Json(name = "last_payment_method")
    val lastPaymentMethod: String? = null,

    @Json(name = "pending_installment_tx_id")
    val pendingInstallmentTxId: String? = null,

    @Json(name = "pending_installment_amount")
    val pendingInstallmentAmount: Int? = 0,

    @Json(name = "rejection_reason")
    val rejectionReason: String? = null,

    @Json(name = "admin_note")
    val adminNote: String? = null,

    @Json(name = "requested_at")
    val requestedAt: String? = null,

    @Json(name = "approved_at")
    val approvedAt: String? = null,

    @Json(name = "unlocked_at")
    val unlockedAt: String? = null,

    @Json(name = "updated_at")
    val updatedAt: String? = null,

    @Json(name = "device_hash")
    val deviceHash: String? = null,

    @Json(name = "display_device_id")
    val displayDeviceId: String? = null,

    @Json(name = "shikho_user_id")
    val shikhoUserId: String? = null,

    @Json(name = "user_phone")
    val userPhone: String? = null
) {
    fun parsedInstallmentPlans(): List<InstallmentPlanItem> = installmentPlansJson.orEmpty()

    val effectivePhone: String
        get() = studentPhone?.takeIf { it.isNotBlank() }
            ?: userPhone?.takeIf { it.isNotBlank() }
            ?: shikhoAccountKey?.takeIf { it.isNotBlank() }
            ?: deviceId?.takeIf { isValidBdPhone(it) }
            ?: ""

    val effectiveClass: String
        get() = studentClass?.takeIf { it.isNotBlank() }
            ?: classCode?.takeIf { it.isNotBlank() }
            ?: ""

    val effectiveGroup: String
        get() = studentGroup?.takeIf { it.isNotBlank() }
            ?: groupCode?.takeIf { it.isNotBlank() }
            ?: ""

    val effectiveDeviceId: String
        get() = deviceId?.takeIf { it.isNotBlank() }
            ?: displayDeviceId?.takeIf { it.isNotBlank() }
            ?: deviceHash?.takeIf { it.isNotBlank() }
            ?: ""

    val effectiveAccountKey: String
        get() = shikhoAccountKey?.takeIf { it.isNotBlank() }
            ?: studentPhone?.takeIf { it.isNotBlank() }
            ?: userPhone?.takeIf { it.isNotBlank() }
            ?: shikhoUserId?.takeIf { it.isNotBlank() }
            ?: ""

    val effectiveTimestamp: String?
        get() = requestedAt?.takeIf { it.isNotBlank() }
            ?: updatedAt?.takeIf { it.isNotBlank() }

    val effectiveInstallmentsEnabled: Boolean
        get() = installmentEnabled && (allowInstallments ?: true)

    companion object {
        fun normalizeBdPhone(raw: String?): String {
            val digits = raw?.filter { it.isDigit() }.orEmpty()
            return when {
                digits.length == 13 && digits.startsWith("8801") -> digits.substring(2)
                digits.length == 11 && digits.startsWith("01") -> digits
                else -> raw?.trim().orEmpty()
            }
        }

        fun isValidBdPhone(raw: String?): Boolean {
            val normalized = normalizeBdPhone(raw)
            return normalized.matches(Regex("^01[3-9]\\d{8}$"))
        }
    }
}

/**
 * Row model for `public.course_payment_transactions`.
 * Matches live Supabase SQL schema 100%.
 */
@JsonClass(generateAdapter = true)
data class CoursePaymentTransaction(
    @Json(name = "id")
    val id: Long = 0L,

    @Json(name = "enrollment_id")
    val enrollmentId: Long? = 0L,

    @Json(name = "device_id")
    val deviceId: String? = null,

    @Json(name = "shikho_account_key")
    val shikhoAccountKey: String? = null,

    @Json(name = "student_phone")
    val studentPhone: String? = null,

    @Json(name = "student_name")
    val studentName: String? = null,

    @Json(name = "program_id")
    val programId: String? = null,

    @Json(name = "program_title")
    val programTitle: String? = null,

    @Json(name = "payment_type")
    val paymentType: String? = "FULL",

    @Json(name = "plan_id")
    val planId: String? = null,

    @Json(name = "plan_title")
    val planTitle: String? = null,

    @Json(name = "selected_plan_id")
    val selectedPlanId: String? = null,

    @Json(name = "selected_plan_title")
    val selectedPlanTitle: String? = null,

    @Json(name = "installment_number")
    val installmentNumber: Int = 1,

    @Json(name = "total_installments")
    val totalInstallments: Int = 1,

    @Json(name = "interval_days")
    val intervalDays: Int? = 7,

    @Json(name = "payment_method")
    val paymentMethod: String? = "bKash",

    @Json(name = "sender_mobile")
    val senderMobile: String? = null,

    @Json(name = "amount")
    val amount: Int = 0,

    @Json(name = "transaction_id")
    val transactionId: String? = null,

    @Json(name = "status")
    val status: String? = "PENDING",

    @Json(name = "rejection_reason")
    val rejectionReason: String? = null,

    @Json(name = "submitted_at")
    val submittedAt: String? = null,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "reviewed_at")
    val reviewedAt: String? = null,

    @Json(name = "device_hash")
    val deviceHash: String? = null,

    @Json(name = "display_device_id")
    val displayDeviceId: String? = null,

    @Json(name = "shikho_user_id")
    val shikhoUserId: String? = null,

    @Json(name = "user_phone")
    val userPhone: String? = null
) {
    val effectiveStudentPhone: String
        get() = studentPhone?.takeIf { it.isNotBlank() }
            ?: userPhone?.takeIf { it.isNotBlank() }
            ?: shikhoAccountKey?.takeIf { it.isNotBlank() }
            ?: senderMobile?.takeIf { it.isNotBlank() }
            ?: ""

    val effectiveAccountKey: String
        get() = shikhoAccountKey?.takeIf { it.isNotBlank() }
            ?: studentPhone?.takeIf { it.isNotBlank() }
            ?: userPhone?.takeIf { it.isNotBlank() }
            ?: shikhoUserId?.takeIf { it.isNotBlank() }
            ?: ""

    val effectivePlanTitle: String
        get() = planTitle?.takeIf { it.isNotBlank() }
            ?: selectedPlanTitle?.takeIf { it.isNotBlank() }
            ?: ""

    val effectiveSubmittedAt: String?
        get() = submittedAt?.takeIf { it.isNotBlank() }
            ?: createdAt?.takeIf { it.isNotBlank() }
}

/**
 * RPC Payload for `POST /rest/v1/rpc/admin_approve_course_access_request`
 * Matches `admin_approve_course_access_request(p_enrollment_id, p_total_price, p_installment_enabled, p_installment_plans_json, p_admin_note)`
 */
@JsonClass(generateAdapter = true)
data class ApproveCourseAccessRequestRpcPayload(
    @Json(name = "p_enrollment_id")
    val enrollmentId: Long,

    @Json(name = "p_total_price")
    val totalPrice: Int,

    @Json(name = "p_installment_enabled")
    val installmentEnabled: Boolean,

    @Json(name = "p_installment_plans_json")
    val installmentPlansJson: List<InstallmentPlanItem>,

    @Json(name = "p_admin_note")
    val adminNote: String,

    @Transient
    val instantFreeUnlock: Boolean = false,

    @Transient
    val studentPhone: String? = null,

    @Transient
    val shikhoAccountKey: String? = null,

    @Transient
    val programId: String? = null
)

/**
 * Payload for manually granting course access or configuring an installment plan
 * for a student using their 11-digit mobile number (`01XXXXXXXXX`).
 * Saves the mobile number into `student_phone`, `shikho_account_key`, and `device_id`.
 */
data class ManualCourseAccessPayload(
    val studentPhone: String,
    val studentName: String,
    val programId: String,
    val programTitle: String,
    val classCode: String = "",
    val groupCode: String = "",
    val status: String = "ACTIVE", // "ACTIVE" or "APPROVED_TO_BUY"
    val totalPrice: Int = 0,
    val paidAmount: Int = 0,
    val installmentEnabled: Boolean = true,
    val installmentPlansJson: List<InstallmentPlanItem> = emptyList(),
    val nextDueDays: Int? = null,
    val adminNote: String = ""
)

/**
 * RPC Payload for `POST /rest/v1/rpc/admin_review_course_payment`
 * Matches live signature: `rpc('admin_review_course_payment', { p_transaction_row_id: tx.id, p_approve: true, p_rejection_reason: '' })`
 */
@JsonClass(generateAdapter = true)
data class ReviewCoursePaymentRpcPayload(
    @Json(name = "p_transaction_row_id")
    val transactionId: Long,

    @Json(name = "p_approve")
    val approve: Boolean,

    @Json(name = "p_rejection_reason")
    val rejectionReason: String = "",

    @Transient
    val customNextDueDays: Int? = null,

    @Transient
    val enrollmentId: Long? = null,

    @Transient
    val studentPhone: String? = null,

    @Transient
    val shikhoAccountKey: String? = null,

    @Transient
    val programId: String? = null
)

