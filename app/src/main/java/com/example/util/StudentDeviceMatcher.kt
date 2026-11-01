package com.example.util

import com.example.data.model.CourseAccessEnrollment
import com.example.data.model.CoursePaymentTransaction
import com.example.data.model.UserDevice
import kotlin.math.abs

/**
 * Bidirectional resolver between Student Course Access Requests (`public.course_access_enrollments`),
 * Payment Verification Requests (`public.course_payment_transactions`), and Student Hardware Devices (`public.devices`).
 */
object StudentDeviceMatcher {

    data class ResolvedStudentDeviceInfo(
        val device: UserDevice?,
        val isExactMatch: Boolean,
        val displayDeviceIdText: String,
        val rawHardwareIdOrHash: String,
        val phoneModelText: String,
        val deviceStatusLabelBn: String,
        val isBlocked: Boolean,
        val isPending: Boolean,
        val onlineInfo: TimestampUtils.DeviceOnlineInfo?,
        val navigationQuery: String
    ) {
        val matchedDevice: UserDevice?
            get() = device

        val displayDeviceId: String
            get() = device?.displayDeviceId?.takeIf { it.isNotBlank() }
                ?: rawHardwareIdOrHash

        val deviceHash: String
            get() = device?.deviceHash?.takeIf { it.isNotBlank() }
                ?: rawHardwareIdOrHash

        val isInferredMatch: Boolean
            get() = device != null && !isExactMatch

        val isBlockedDevice: Boolean
            get() = isBlocked

        val isPendingDevice: Boolean
            get() = isPending

        val fullPhoneModel: String
            get() = phoneModelText
    }

    data class DeviceStudentProfile(
        val studentPhone: String,
        val studentName: String,
        val studentClassGroup: String,
        val courseTitles: List<String>,
        val enrollments: List<CourseAccessEnrollment>,
        val transactions: List<CoursePaymentTransaction>,
        val isExactMatch: Boolean
    ) {
        val coursesSummary: String
            get() = courseTitles.joinToString(", ")
    }

    fun resolveDeviceForEnrollment(
        enrollment: CourseAccessEnrollment,
        devices: List<UserDevice>,
        transactions: List<CoursePaymentTransaction> = emptyList(),
        nowMillis: Long = System.currentTimeMillis()
    ): ResolvedStudentDeviceInfo = resolveForEnrollment(
        enrollment = enrollment,
        devices = devices,
        allEnrollments = listOf(enrollment),
        allTransactions = transactions,
        nowMillis = nowMillis
    )

    fun resolveDeviceForTransaction(
        tx: CoursePaymentTransaction,
        devices: List<UserDevice>,
        enrollments: List<CourseAccessEnrollment> = emptyList(),
        nowMillis: Long = System.currentTimeMillis()
    ): ResolvedStudentDeviceInfo = resolveForTransaction(
        transaction = tx,
        devices = devices,
        allEnrollments = enrollments,
        allTransactions = listOf(tx),
        nowMillis = nowMillis
    )

    private fun isGenericStudentName(name: String?): Boolean {
        val clean = name?.trim().orEmpty()
        if (clean.isEmpty()) return true
        val lower = clean.lowercase()
        return lower == "শিক্ষার্থী" ||
            lower == "নতুন শিক্ষার্থী" ||
            lower == "অজ্ঞাত শিক্ষার্থী" ||
            lower == "student" ||
            lower == "new student" ||
            lower == "unknown"
    }

    /**
     * Resolves the student's `UserDevice` for a `CourseAccessEnrollment`.
     */
    fun resolveForEnrollment(
        enrollment: CourseAccessEnrollment,
        devices: List<UserDevice>,
        allEnrollments: List<CourseAccessEnrollment> = emptyList(),
        allTransactions: List<CoursePaymentTransaction> = emptyList(),
        nowMillis: Long = System.currentTimeMillis()
    ): ResolvedStudentDeviceInfo {
        val normPhone = CourseAccessEnrollment.normalizeBdPhone(enrollment.effectivePhone)
        val rawDispId = enrollment.displayDeviceId?.trim().orEmpty()
        val rawHash = enrollment.deviceHash?.trim().orEmpty()
        val rawDevId = enrollment.deviceId?.trim().orEmpty()
        val nonPhoneDevId = rawDevId.takeIf {
            it.isNotEmpty() && !CourseAccessEnrollment.isValidBdPhone(it) && it != normPhone
        }.orEmpty()

        // 1. Exact ID / Hash match in `devices`
        val exactById = devices.firstOrNull { dev ->
            (rawDispId.isNotEmpty() && (dev.displayDeviceId.equals(rawDispId, ignoreCase = true) || dev.deviceHash.equals(rawDispId, ignoreCase = true))) ||
                (rawHash.isNotEmpty() && (dev.deviceHash.equals(rawHash, ignoreCase = true) || dev.displayDeviceId.equals(rawHash, ignoreCase = true))) ||
                (nonPhoneDevId.isNotEmpty() && (
                    dev.displayDeviceId.equals(nonPhoneDevId, ignoreCase = true) ||
                        dev.deviceHash.equals(nonPhoneDevId, ignoreCase = true) ||
                        dev.id.equals(nonPhoneDevId, ignoreCase = true)
                    ))
        }

        // 2. Match by normalized phone number inside `device` fields or `student_note`
        val exactByPhone = if (exactById == null && normPhone.isNotEmpty()) {
            devices.firstOrNull { dev ->
                dev.extractedPhoneFromDevice == normPhone ||
                    dev.studentNote?.contains(normPhone) == true ||
                    dev.appliedCode?.contains(normPhone) == true ||
                    dev.displayDeviceId.equals(normPhone, ignoreCase = true)
            }
        } else null

        // 3. Match by student name (if non-generic) inside `student_note`
        val stName = enrollment.studentName?.trim().orEmpty()
        val exactByName = if (exactById == null && exactByPhone == null && !isGenericStudentName(stName)) {
            devices.firstOrNull { dev ->
                val note = dev.studentNote?.trim().orEmpty()
                note.isNotEmpty() && (
                    note.equals(stName, ignoreCase = true) ||
                        note.contains(stName, ignoreCase = true) ||
                        stName.contains(note, ignoreCase = true)
                    )
            }
        } else null

        val exactDevice = exactById ?: exactByPhone ?: exactByName

        // 4. If not directly linked yet, find the closest unlinked device in `devices` (e.g. when the student
        // verified their device as "নতুন শিক্ষার্থী" and then sent a course request with their phone number)
        val candidateDevice = if (exactDevice == null && devices.isNotEmpty()) {
            val enrollTimeMs = TimestampUtils.parseIsoToMillis(enrollment.requestedAt ?: enrollment.updatedAt)
            val unlinkedDevices = devices.filter { dev ->
                val devPhone = dev.extractedPhoneFromDevice
                devPhone.isEmpty() || devPhone == normPhone
            }.ifEmpty { devices }

            if (enrollTimeMs != null) {
                unlinkedDevices.minByOrNull { dev ->
                    val devMs = TimestampUtils.parseIsoToMillis(
                        dev.lastSeen ?: dev.updatedAt ?: dev.approvedAt ?: dev.requestedAt
                    ) ?: 0L
                    abs(enrollTimeMs - devMs)
                }
            } else {
                unlinkedDevices.firstOrNull()
            }
        } else null

        val targetDevice = exactDevice ?: candidateDevice
        val isExact = exactDevice != null

        val studentEnrollments = if (normPhone.isNotEmpty()) {
            allEnrollments.filter { CourseAccessEnrollment.normalizeBdPhone(it.effectivePhone) == normPhone }
                .ifEmpty { listOf(enrollment) }
        } else {
            listOf(enrollment)
        }

        val studentTransactions = if (normPhone.isNotEmpty()) {
            allTransactions.filter { CourseAccessEnrollment.normalizeBdPhone(it.effectiveStudentPhone) == normPhone }
        } else {
            emptyList()
        }

        return buildResolvedResult(
            targetDevice = targetDevice,
            isExact = isExact,
            fallbackDisplayId = rawDispId.ifBlank { nonPhoneDevId.ifBlank { rawHash } },
            fallbackPhone = normPhone,
            studentEnrollments = studentEnrollments,
            studentTransactions = studentTransactions,
            nowMillis = nowMillis
        )
    }

    /**
     * Resolves the student's `UserDevice` for a `CoursePaymentTransaction`.
     */
    fun resolveForTransaction(
        transaction: CoursePaymentTransaction,
        devices: List<UserDevice>,
        allEnrollments: List<CourseAccessEnrollment> = emptyList(),
        allTransactions: List<CoursePaymentTransaction> = emptyList(),
        nowMillis: Long = System.currentTimeMillis()
    ): ResolvedStudentDeviceInfo {
        val normPhone = CourseAccessEnrollment.normalizeBdPhone(transaction.effectiveStudentPhone)
        val senderNorm = CourseAccessEnrollment.normalizeBdPhone(transaction.senderMobile)

        // Find parent enrollment if available
        val parentEnrollment = allEnrollments.firstOrNull { enr ->
            (transaction.enrollmentId != null && transaction.enrollmentId > 0L && enr.id == transaction.enrollmentId) ||
                (normPhone.isNotEmpty() &&
                    CourseAccessEnrollment.normalizeBdPhone(enr.effectivePhone) == normPhone &&
                    (transaction.programId.isNullOrBlank() || enr.programId == transaction.programId))
        }

        val rawDispId = transaction.displayDeviceId?.trim()?.takeIf { it.isNotEmpty() }
            ?: parentEnrollment?.displayDeviceId?.trim().orEmpty()
        val rawHash = transaction.deviceHash?.trim()?.takeIf { it.isNotEmpty() }
            ?: parentEnrollment?.deviceHash?.trim().orEmpty()
        val rawDevId = transaction.deviceId?.trim()?.takeIf { it.isNotEmpty() }
            ?: parentEnrollment?.deviceId?.trim().orEmpty()
        val nonPhoneDevId = rawDevId.takeIf {
            it.isNotEmpty() && !CourseAccessEnrollment.isValidBdPhone(it) && it != normPhone
        }.orEmpty()

        // 1. Exact ID / Hash match
        val exactById = devices.firstOrNull { dev ->
            (rawDispId.isNotEmpty() && (dev.displayDeviceId.equals(rawDispId, ignoreCase = true) || dev.deviceHash.equals(rawDispId, ignoreCase = true))) ||
                (rawHash.isNotEmpty() && (dev.deviceHash.equals(rawHash, ignoreCase = true) || dev.displayDeviceId.equals(rawHash, ignoreCase = true))) ||
                (nonPhoneDevId.isNotEmpty() && (
                    dev.displayDeviceId.equals(nonPhoneDevId, ignoreCase = true) ||
                        dev.deviceHash.equals(nonPhoneDevId, ignoreCase = true) ||
                        dev.id.equals(nonPhoneDevId, ignoreCase = true)
                    ))
        }

        // 2. Match by student phone or sender phone
        val exactByPhone = if (exactById == null) {
            devices.firstOrNull { dev ->
                val devPhone = dev.extractedPhoneFromDevice
                (normPhone.isNotEmpty() && (devPhone == normPhone || dev.studentNote?.contains(normPhone) == true)) ||
                    (senderNorm.isNotEmpty() && (devPhone == senderNorm || dev.studentNote?.contains(senderNorm) == true))
            }
        } else null

        // 3. Match by student name
        val stName = transaction.studentName?.trim()?.takeIf { it.isNotEmpty() }
            ?: parentEnrollment?.studentName?.trim().orEmpty()
        val exactByName = if (exactById == null && exactByPhone == null && !isGenericStudentName(stName)) {
            devices.firstOrNull { dev ->
                val note = dev.studentNote?.trim().orEmpty()
                note.isNotEmpty() && (
                    note.equals(stName, ignoreCase = true) ||
                        note.contains(stName, ignoreCase = true) ||
                        stName.contains(note, ignoreCase = true)
                    )
            }
        } else null

        val exactDevice = exactById ?: exactByPhone ?: exactByName

        val candidateDevice = if (exactDevice == null && devices.isNotEmpty()) {
            val txTimeMs = TimestampUtils.parseIsoToMillis(transaction.effectiveSubmittedAt)
            val unlinkedDevices = devices.filter { dev ->
                val devPhone = dev.extractedPhoneFromDevice
                devPhone.isEmpty() || devPhone == normPhone || devPhone == senderNorm
            }.ifEmpty { devices }

            if (txTimeMs != null) {
                unlinkedDevices.minByOrNull { dev ->
                    val devMs = TimestampUtils.parseIsoToMillis(
                        dev.lastSeen ?: dev.updatedAt ?: dev.approvedAt ?: dev.requestedAt
                    ) ?: 0L
                    abs(txTimeMs - devMs)
                }
            } else {
                unlinkedDevices.firstOrNull()
            }
        } else null

        val targetDevice = exactDevice ?: candidateDevice
        val isExact = exactDevice != null

        val studentEnrollments = if (normPhone.isNotEmpty()) {
            allEnrollments.filter { CourseAccessEnrollment.normalizeBdPhone(it.effectivePhone) == normPhone }
        } else {
            listOfNotNull(parentEnrollment)
        }

        val studentTransactions = if (normPhone.isNotEmpty()) {
            allTransactions.filter { CourseAccessEnrollment.normalizeBdPhone(it.effectiveStudentPhone) == normPhone }
                .ifEmpty { listOf(transaction) }
        } else {
            listOf(transaction)
        }

        return buildResolvedResult(
            targetDevice = targetDevice,
            isExact = isExact,
            fallbackDisplayId = rawDispId.ifBlank { nonPhoneDevId.ifBlank { rawHash } },
            fallbackPhone = normPhone.ifBlank { senderNorm },
            studentEnrollments = studentEnrollments,
            studentTransactions = studentTransactions,
            nowMillis = nowMillis
        )
    }

    /**
     * Resolves student profile & course enrollments linked to a `UserDevice` for display in `DeviceManagementScreen`.
     */
    fun resolveStudentProfileForDevice(
        device: UserDevice,
        enrollments: List<CourseAccessEnrollment>,
        transactions: List<CoursePaymentTransaction>
    ): DeviceStudentProfile? {
        val dispId = device.displayDeviceId.trim()
        val devHash = device.deviceHash.trim()
        val devId = device.id.trim()
        val devPhone = device.extractedPhoneFromDevice
        val devNote = device.studentNote?.trim().orEmpty()

        val matchedEnrollments = enrollments.filter { enr ->
            val enrPhone = CourseAccessEnrollment.normalizeBdPhone(enr.effectivePhone)
            val enrDisp = enr.displayDeviceId?.trim().orEmpty()
            val enrHash = enr.deviceHash?.trim().orEmpty()
            val enrDevId = enr.deviceId?.trim().orEmpty()
            val enrName = enr.studentName?.trim().orEmpty()

            (dispId.isNotEmpty() && (enrDisp.equals(dispId, true) || enrDevId.equals(dispId, true))) ||
                (devHash.isNotEmpty() && (enrHash.equals(devHash, true) || enrDevId.equals(devHash, true))) ||
                (devId.isNotEmpty() && enrDevId.equals(devId, true)) ||
                (devPhone.isNotEmpty() && enrPhone == devPhone) ||
                (enrPhone.isNotEmpty() && devNote.contains(enrPhone)) ||
                (!isGenericStudentName(enrName) && devNote.isNotEmpty() && devNote.contains(enrName, ignoreCase = true))
        }

        val matchedTransactions = transactions.filter { tx ->
            val txPhone = CourseAccessEnrollment.normalizeBdPhone(tx.effectiveStudentPhone)
            val txSender = CourseAccessEnrollment.normalizeBdPhone(tx.senderMobile)
            val txDisp = tx.displayDeviceId?.trim().orEmpty()
            val txHash = tx.deviceHash?.trim().orEmpty()
            val txDevId = tx.deviceId?.trim().orEmpty()

            (dispId.isNotEmpty() && (txDisp.equals(dispId, true) || txDevId.equals(dispId, true))) ||
                (devHash.isNotEmpty() && (txHash.equals(devHash, true) || txDevId.equals(devHash, true))) ||
                (devPhone.isNotEmpty() && (txPhone == devPhone || txSender == devPhone)) ||
                (txPhone.isNotEmpty() && devNote.contains(txPhone))
        }

        if (matchedEnrollments.isEmpty() && matchedTransactions.isEmpty() && devPhone.isEmpty()) {
            return null
        }

        val primaryEnr = matchedEnrollments.firstOrNull()
        val primaryTx = matchedTransactions.firstOrNull()

        val resolvedPhone = devPhone
            .ifBlank { primaryEnr?.effectivePhone.orEmpty() }
            .ifBlank { primaryTx?.effectiveStudentPhone.orEmpty() }

        val resolvedName = primaryEnr?.studentName?.takeIf { !isGenericStudentName(it) }
            ?: primaryTx?.studentName?.takeIf { !isGenericStudentName(it) }
            ?: device.displayStudentName

        val classPart = primaryEnr?.effectiveClass.orEmpty()
        val groupPart = primaryEnr?.effectiveGroup.orEmpty()
        val classGroup = listOf(classPart, groupPart).filter { it.isNotBlank() }.joinToString(" • ")

        val courses = (
            matchedEnrollments.mapNotNull { it.programTitle?.takeIf { t -> t.isNotBlank() } ?: it.programId } +
                matchedTransactions.mapNotNull { it.programTitle?.takeIf { t -> t.isNotBlank() } ?: it.programId }
            ).distinct()

        return DeviceStudentProfile(
            studentPhone = resolvedPhone,
            studentName = resolvedName,
            studentClassGroup = classGroup,
            courseTitles = courses,
            enrollments = matchedEnrollments,
            transactions = matchedTransactions,
            isExactMatch = true
        )
    }

    private fun buildResolvedResult(
        targetDevice: UserDevice?,
        isExact: Boolean,
        fallbackDisplayId: String,
        fallbackPhone: String,
        studentEnrollments: List<CourseAccessEnrollment>,
        studentTransactions: List<CoursePaymentTransaction>,
        nowMillis: Long
    ): ResolvedStudentDeviceInfo {
        if (targetDevice != null) {
            val dispId = targetDevice.displayDeviceId.ifBlank {
                fallbackDisplayId.ifBlank { targetDevice.deviceHash.take(16) }
            }
            val onlineInfo = TimestampUtils.resolveDeviceOnlineInfo(
                device = targetDevice,
                linkedEnrollments = studentEnrollments,
                linkedTransactions = studentTransactions,
                nowMillis = nowMillis
            )
            val statusLabel = when {
                targetDevice.isBlockedDevice -> "ব্লকড (Blocked)"
                targetDevice.isPendingDevice -> "অপেক্ষমান (Pending)"
                else -> "ভেরিফাইড (Active)"
            }
            return ResolvedStudentDeviceInfo(
                device = targetDevice,
                isExactMatch = isExact,
                displayDeviceIdText = dispId,
                rawHardwareIdOrHash = targetDevice.deviceHash.ifBlank { targetDevice.id },
                phoneModelText = targetDevice.fullPhoneModel,
                deviceStatusLabelBn = statusLabel,
                isBlocked = targetDevice.isBlockedDevice,
                isPending = targetDevice.isPendingDevice,
                onlineInfo = onlineInfo,
                navigationQuery = targetDevice.displayDeviceId.ifBlank {
                    targetDevice.deviceHash.ifBlank { fallbackPhone }
                }
            )
        }

        val fallbackIdLabel = fallbackDisplayId.ifBlank {
            if (fallbackPhone.isNotBlank()) "ডিভাইস লিংক নেই ($fallbackPhone)" else "ডিভাইস আইডি নেই"
        }

        // Compute fallback online time from the student's latest enrollment/payment activity
        val latestActivityIso = (
            studentTransactions.mapNotNull { it.effectiveSubmittedAt } +
                studentEnrollments.mapNotNull { it.requestedAt ?: it.updatedAt }
            ).maxByOrNull { TimestampUtils.parseIsoToMillis(it) ?: 0L }

        val fallbackOnlineInfo = if (latestActivityIso != null) {
            val ms = TimestampUtils.parseIsoToMillis(latestActivityIso)
            val diff = if (ms != null) (nowMillis - ms).coerceAtLeast(0L) else Long.MAX_VALUE
            val isOnline = ms != null && diff <= 180_000L
            val rel = TimestampUtils.formatRelativeTimeFromMillisBn(ms, nowMillis)
            val exact = TimestampUtils.formatExactDateTimeBn(ms, nowMillis)
            TimestampUtils.DeviceOnlineInfo(
                lastActiveMillis = ms,
                isOnlineNow = isOnline,
                isRecentlyActive = ms != null && diff <= 1_800_000L,
                statusBadgeTextBn = if (isOnline) "🟢 এখন অনলাইন" else "🕒 এক্টিভ ছিল: $rel",
                lastActiveRelativeBn = rel,
                lastActiveExactBn = exact,
                requestedAtFormattedBn = "$exact ($rel)",
                approvedAtFormattedBn = null
            )
        } else null

        return ResolvedStudentDeviceInfo(
            device = null,
            isExactMatch = false,
            displayDeviceIdText = fallbackIdLabel,
            rawHardwareIdOrHash = fallbackDisplayId,
            phoneModelText = "ডিভাইস ট্যাবে চেক করুন",
            deviceStatusLabelBn = "শনাক্ত করুন",
            isBlocked = false,
            isPending = false,
            onlineInfo = fallbackOnlineInfo,
            navigationQuery = fallbackDisplayId.ifBlank { fallbackPhone }
        )
    }
}
