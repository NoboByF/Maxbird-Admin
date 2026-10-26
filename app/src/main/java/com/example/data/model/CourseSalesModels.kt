package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.math.ceil

/**
 * Row model for `public.course_payment_settings` (id = 1).
 */
@JsonClass(generateAdapter = true)
data class CoursePaymentSettings(
    @Json(name = "id")
    val id: Int = 1,

    @Json(name = "bkash_number")
    val bkashNumber: String? = "",

    @Json(name = "nagad_number")
    val nagadNumber: String? = "",

    @Json(name = "rocket_number")
    val rocketNumber: String? = "",

    @Json(name = "payment_instructions_bn")
    val paymentInstructionsBn: String? = "",

    @Json(name = "default_course_price")
    val defaultCoursePrice: Int? = 1000,

    @Json(name = "updated_at")
    val updatedAt: String? = null
)

/**
 * Payload for upserting/updating `public.course_payment_settings` (id = 1).
 */
@JsonClass(generateAdapter = true)
data class UpsertCoursePaymentSettingsPayload(
    @Json(name = "id")
    val id: Int = 1,

    @Json(name = "bkash_number")
    val bkashNumber: String,

    @Json(name = "nagad_number")
    val nagadNumber: String,

    @Json(name = "rocket_number")
    val rocketNumber: String,

    @Json(name = "payment_instructions_bn")
    val paymentInstructionsBn: String,

    @Json(name = "default_course_price")
    val defaultCoursePrice: Int
)

/**
 * Individual installment plan option stored inside `installment_plans_json` (JSONB).
 */
@JsonClass(generateAdapter = true)
data class InstallmentPlanItem(
    @Json(name = "plan_id")
    val planId: String = "full",

    @Json(name = "title_bn")
    val titleBn: String = "একবারে সম্পূর্ণ পেমেন্ট (Full Payment)",

    @Json(name = "installment_count")
    val installmentCount: Int = 1,

    @Json(name = "first_payment")
    val firstPayment: Int = 1000,

    @Json(name = "per_installment_amount")
    val perInstallmentAmount: Int = 1000,

    @Json(name = "interval_days")
    val intervalDays: Int = 0,

    @Json(name = "schedule_amounts")
    val scheduleAmounts: List<Int> = listOf(1000)
) {
    companion object {
        /**
         * Builds default standard plans (Full, 2-installment, 3-installment, 5-installment)
         * dynamically calculated from a given total price.
         */
        fun buildDefaultPlansForPrice(totalPrice: Int): List<InstallmentPlanItem> {
            val safePrice = totalPrice.coerceAtLeast(0)
            if (safePrice == 0) {
                return listOf(
                    InstallmentPlanItem(
                        planId = "full",
                        titleBn = "একবারে সম্পূর্ণ পেমেন্ট (Full Payment)",
                        installmentCount = 1,
                        firstPayment = 0,
                        perInstallmentAmount = 0,
                        intervalDays = 0,
                        scheduleAmounts = listOf(0)
                    )
                )
            }

            val twoSchedules = buildScheduleAmounts(safePrice, 2)
            val threeSchedules = buildScheduleAmounts(safePrice, 3)
            val fiveSchedules = buildScheduleAmounts(safePrice, 5)

            return listOf(
                InstallmentPlanItem(
                    planId = "full",
                    titleBn = "একবারে সম্পূর্ণ পেমেন্ট (Full Payment)",
                    installmentCount = 1,
                    firstPayment = safePrice,
                    perInstallmentAmount = safePrice,
                    intervalDays = 0,
                    scheduleAmounts = listOf(safePrice)
                ),
                InstallmentPlanItem(
                    planId = "inst_2",
                    titleBn = "২ কিস্তিতে পেমেন্ট ( প্রতি কিস্তি ৳${twoSchedules.firstOrNull() ?: safePrice} )",
                    installmentCount = 2,
                    firstPayment = twoSchedules.firstOrNull() ?: safePrice,
                    perInstallmentAmount = twoSchedules.firstOrNull() ?: safePrice,
                    intervalDays = 30,
                    scheduleAmounts = twoSchedules
                ),
                InstallmentPlanItem(
                    planId = "inst_3",
                    titleBn = "৩ কিস্তিতে পেমেন্ট (১ম কিস্তি ৳${threeSchedules.firstOrNull() ?: safePrice})",
                    installmentCount = 3,
                    firstPayment = threeSchedules.firstOrNull() ?: safePrice,
                    perInstallmentAmount = threeSchedules.getOrElse(1) { threeSchedules.firstOrNull() ?: safePrice },
                    intervalDays = 30,
                    scheduleAmounts = threeSchedules
                ),
                InstallmentPlanItem(
                    planId = "inst_5",
                    titleBn = "৫ কিস্তিতে পেমেন্ট ( প্রতি কিস্তি ৳${fiveSchedules.firstOrNull() ?: safePrice} )",
                    installmentCount = 5,
                    firstPayment = fiveSchedules.firstOrNull() ?: safePrice,
                    perInstallmentAmount = fiveSchedules.firstOrNull() ?: safePrice,
                    intervalDays = 30,
                    scheduleAmounts = fiveSchedules
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

    @Json(name = "status")
    val status: String? = "REQUESTED",

    @Json(name = "total_price")
    val totalPrice: Int = 0,

    @Json(name = "paid_amount")
    val paidAmount: Int = 0,

    @Json(name = "due_amount")
    val dueAmount: Int = 0,

    @Json(name = "installment_enabled")
    val installmentEnabled: Boolean = true,

    @Json(name = "selected_plan_id")
    val selectedPlanId: String? = null,

    @Json(name = "selected_plan_title")
    val selectedPlanTitle: String? = null,

    @Json(name = "total_installments")
    val totalInstallments: Int = 1,

    @Json(name = "completed_installments")
    val completedInstallments: Int = 0,

    @Json(name = "next_installment_amount")
    val nextInstallmentAmount: Int = 0,

    @Json(name = "installment_interval_days")
    val installmentIntervalDays: Int = 30,

    @Json(name = "next_due_date")
    val nextDueDate: String? = null,

    @Json(name = "installment_plans_json")
    val installmentPlansJson: List<InstallmentPlanItem>? = emptyList(),

    @Json(name = "rejection_reason")
    val rejectionReason: String? = null,

    @Json(name = "admin_note")
    val adminNote: String? = null,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "updated_at")
    val updatedAt: String? = null
) {
    fun parsedInstallmentPlans(): List<InstallmentPlanItem> = installmentPlansJson.orEmpty()
}

/**
 * Row model for `public.course_payment_transactions`.
 */
@JsonClass(generateAdapter = true)
data class CoursePaymentTransaction(
    @Json(name = "id")
    val id: Long = 0L,

    @Json(name = "enrollment_id")
    val enrollmentId: Long = 0L,

    @Json(name = "device_id")
    val deviceId: String? = null,

    @Json(name = "shikho_account_key")
    val shikhoAccountKey: String? = null,

    @Json(name = "student_name")
    val studentName: String? = null,

    @Json(name = "program_id")
    val programId: String? = null,

    @Json(name = "program_title")
    val programTitle: String? = null,

    @Json(name = "plan_id")
    val planId: String? = null,

    @Json(name = "plan_title")
    val planTitle: String? = null,

    @Json(name = "installment_number")
    val installmentNumber: Int = 1,

    @Json(name = "total_installments")
    val totalInstallments: Int = 1,

    @Json(name = "payment_method")
    val paymentMethod: String? = null,

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

    @Json(name = "reviewed_at")
    val reviewedAt: String? = null
)

/**
 * RPC Payload for `POST /rest/v1/rpc/admin_approve_course_access_request`
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

    @Json(name = "p_instant_free_unlock")
    val instantFreeUnlock: Boolean
)

/**
 * RPC Payload for `POST /rest/v1/rpc/admin_review_course_payment`
 */
@JsonClass(generateAdapter = true)
data class ReviewCoursePaymentRpcPayload(
    @Json(name = "p_transaction_id")
    val transactionId: Long,

    @Json(name = "p_approve")
    val approve: Boolean,

    @Json(name = "p_rejection_reason")
    val rejectionReason: String = "",

    @Json(name = "p_custom_next_due_days")
    val customNextDueDays: Int? = null
)
