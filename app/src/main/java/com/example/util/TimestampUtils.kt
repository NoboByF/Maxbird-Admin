package com.example.util

import com.example.data.model.CourseAccessEnrollment
import com.example.data.model.CoursePaymentTransaction
import com.example.data.model.UserDevice
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/**
 * Accurate timestamp parser & formatter for Postgres `timestamptz` / ISO-8601 strings.
 * Fixes the Java `SimpleDateFormat(".SSSSSS")` microsecond bug (where 6-digit microseconds
 * were treated as hundreds of thousands of milliseconds, shifting timestamps by up to +16 minutes).
 */
object TimestampUtils {

    data class DeviceOnlineInfo(
        val lastActiveMillis: Long?,
        val isOnlineNow: Boolean,
        val isRecentlyActive: Boolean,
        val statusBadgeTextBn: String,
        val lastActiveRelativeBn: String,
        val lastActiveExactBn: String,
        val requestedAtFormattedBn: String,
        val approvedAtFormattedBn: String?,
        val isActiveWithinHour: Boolean = isRecentlyActive
    )

    fun formatWithRelativeBn(
        rawIso: String?,
        nowMillis: Long = System.currentTimeMillis()
    ): String = formatIsoWithRelativeBn(rawIso, nowMillis)

    /**
     * Overload accepting raw timestamp strings (`lastSeen`, `updatedAt`, `approvedAt`, `requestedAt`)
     * directly from UI screens.
     */
    fun resolveDeviceOnlineInfo(
        lastSeen: String?,
        updatedAt: String?,
        approvedAt: String?,
        requestedAt: String?,
        extraActivityTimestamp: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): DeviceOnlineInfo {
        val lastSeenMs = parseIsoToMillis(lastSeen)
        val extraSeenMs = parseIsoToMillis(extraActivityTimestamp)
        val requestedMs = parseIsoToMillis(requestedAt)
        val approvedMs = parseIsoToMillis(approvedAt)
        val updatedMs = parseIsoToMillis(updatedAt)

        val updatedByDeviceMs = if (updatedMs != null && approvedMs != null && abs(updatedMs - approvedMs) <= 5_000L) {
            null
        } else {
            updatedMs
        }

        val candidateOnlineTimestamps = listOfNotNull(
            lastSeenMs,
            extraSeenMs,
            updatedByDeviceMs,
            requestedMs,
            updatedMs
        )

        val effectiveOnlineMs = candidateOnlineTimestamps.maxOrNull()
        val diffMs = if (effectiveOnlineMs != null) (nowMillis - effectiveOnlineMs).coerceAtLeast(0L) else Long.MAX_VALUE

        val isOnlineNow = effectiveOnlineMs != null && diffMs <= 180_000L // Within 3 minutes
        val isRecentlyActive = effectiveOnlineMs != null && diffMs <= 3_600_000L // Within 60 minutes

        val exactOnline = if (effectiveOnlineMs != null) {
            formatExactDateTimeBn(effectiveOnlineMs, nowMillis)
        } else {
            "রেকর্ড নেই"
        }

        val relativeOnline = if (effectiveOnlineMs != null) {
            formatRelativeTimeFromMillisBn(effectiveOnlineMs, nowMillis)
        } else {
            "অজানা"
        }

        val statusBadge = when {
            isOnlineNow -> "🟢 এখন অনলাইন (Online Now)"
            isRecentlyActive -> "🟢 সক্রিয় ছিল: $relativeOnline"
            effectiveOnlineMs != null -> "🕒 অনলাইন ছিল: $relativeOnline"
            else -> "⚪ অনলাইন তথ্য নেই"
        }

        val requestedText = if (requestedMs != null) {
            "${formatExactDateTimeBn(requestedMs, nowMillis)} (${formatRelativeTimeFromMillisBn(requestedMs, nowMillis)})"
        } else {
            "উল্লেখ নেই"
        }

        val approvedText = if (approvedMs != null) {
            "${formatExactDateTimeBn(approvedMs, nowMillis)} (${formatRelativeTimeFromMillisBn(approvedMs, nowMillis)})"
        } else {
            "পেন্ডিং"
        }

        return DeviceOnlineInfo(
            lastActiveMillis = effectiveOnlineMs,
            isOnlineNow = isOnlineNow,
            isRecentlyActive = isRecentlyActive,
            statusBadgeTextBn = statusBadge,
            lastActiveRelativeBn = relativeOnline,
            lastActiveExactBn = exactOnline,
            requestedAtFormattedBn = requestedText,
            approvedAtFormattedBn = approvedText,
            isActiveWithinHour = isRecentlyActive
        )
    }

    /**
     * Parses any ISO-8601 / Postgres `timestamptz` string into accurate epoch milliseconds.
     * Supports:
     * - `2026-10-31T13:29:45.842109+00:00`
     * - `2026-10-31 13:29:45.842109+00`
     * - `2026-10-31T13:29:45.84Z`
     * - `2026-10-31T13:29:45+06:00`
     * - `2026-10-31T13:29:45`
     */
    fun parseIsoToMillis(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        // If raw is numeric epoch milliseconds or seconds
        trimmed.toLongOrNull()?.let { num ->
            return if (num > 1_000_000_000_000L) num else num * 1000L
        }

        return try {
            // 1. Replace space between date and time with 'T'
            var normalized = trimmed.replace(' ', 'T')

            // 2. Normalize timezone suffix at the end:
            // Postgres often returns "+00" or "+00:00" or "-05"
            if (normalized.endsWith("+00:00") || normalized.endsWith("-00:00")) {
                normalized = normalized.substring(0, normalized.length - 6) + "Z"
            } else if (normalized.endsWith("+0000") || normalized.endsWith("-0000")) {
                normalized = normalized.substring(0, normalized.length - 5) + "Z"
            } else if (normalized.endsWith("+00") || normalized.endsWith("-00")) {
                normalized = normalized.substring(0, normalized.length - 3) + "Z"
            } else if (Regex(".*[+-]\\d{2}$").matches(normalized)) {
                normalized += ":00"
            } else if (
                !normalized.endsWith("Z", ignoreCase = true) &&
                !Regex(".*[+-]\\d{2}:\\d{2}$").matches(normalized) &&
                !Regex(".*[+-]\\d{4}$").matches(normalized)
            ) {
                normalized += "Z"
            }

            // 3. Normalize fractional seconds to exactly 3 digits (milliseconds)
            // so SimpleDateFormat(".SSS") never misinterprets microseconds as minutes!
            val tIndex = normalized.indexOf('T')
            val dotIndex = if (tIndex != -1) normalized.indexOf('.', tIndex) else normalized.indexOf('.')
            if (dotIndex != -1) {
                val beforeDot = normalized.substring(0, dotIndex)
                val afterDot = normalized.substring(dotIndex + 1)
                val tzStart = afterDot.indexOfFirst { it == 'Z' || it == 'z' || it == '+' || it == '-' }
                val fractionPart = if (tzStart != -1) afterDot.substring(0, tzStart) else afterDot
                val tzPart = if (tzStart != -1) afterDot.substring(tzStart) else "Z"

                val digitsOnly = fractionPart.takeWhile { it.isDigit() }
                val millisThreeDigits = when {
                    digitsOnly.isEmpty() -> "000"
                    digitsOnly.length == 1 -> "${digitsOnly}00"
                    digitsOnly.length == 2 -> "${digitsOnly}0"
                    else -> digitsOnly.substring(0, 3)
                }
                normalized = "$beforeDot.$millisThreeDigits$tzPart"
            } else {
                // Insert .000 before timezone
                val timePartStart = if (tIndex != -1) tIndex else 0
                val tzOffsetIdx = normalized.indexOfFirst {
                    it == 'Z' || it == 'z'
                }.takeIf { it != -1 } ?: run {
                    val sub = normalized.substring(timePartStart)
                    val rel = sub.indexOfFirst { it == '+' || it == '-' }
                    if (rel != -1) timePartStart + rel else -1
                }
                if (tzOffsetIdx != -1) {
                    normalized = normalized.substring(0, tzOffsetIdx) + ".000" + normalized.substring(tzOffsetIdx)
                }
            }

            if (normalized.endsWith("Z", ignoreCase = true)) {
                val cleanUtc = normalized.substring(0, normalized.length - 1)
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                sdf.parse(cleanUtc)?.time
            } else {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
                sdf.parse(normalized)?.time
            }
        } catch (_: Exception) {
            try {
                val fallback = trimmed.replace(' ', 'T').take(19)
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                sdf.parse(fallback)?.time
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Formats epoch milliseconds into an exact date/time string in local timezone.
     * Examples:
     * - "আজ 08:31 AM"
     * - "গতকাল 01:29 PM"
     * - "31 Oct 2026, 01:29 PM"
     */
    fun formatExactDateTimeBn(
        millis: Long?,
        nowMillis: Long = System.currentTimeMillis()
    ): String {
        if (millis == null || millis <= 0L) return "সময় উল্লেখ নেই"
        val date = Date(millis)
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val targetCal = Calendar.getInstance().apply { timeInMillis = millis }

        val timeFormatted = SimpleDateFormat("hh:mm a", Locale.US).format(date)

        val sameYear = nowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR)
        val nowDay = nowCal.get(Calendar.DAY_OF_YEAR)
        val targetDay = targetCal.get(Calendar.DAY_OF_YEAR)

        return when {
            sameYear && nowDay == targetDay -> "আজ $timeFormatted"
            sameYear && (nowDay - targetDay == 1) -> "গতকাল $timeFormatted"
            sameYear -> SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(date)
            else -> SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(date)
        }
    }

    /**
     * Formats an ISO timestamp string into an exact date/time string in local timezone.
     */
    fun formatIsoExactDateTimeBn(
        rawIso: String?,
        nowMillis: Long = System.currentTimeMillis()
    ): String {
        val millis = parseIsoToMillis(rawIso) ?: return rawIso?.take(16)?.replace('T', ' ').orEmpty().ifBlank { "অজানা" }
        return formatExactDateTimeBn(millis, nowMillis)
    }

    /**
     * Formats epoch milliseconds into an accurate Bengali relative duration string.
     */
    fun formatRelativeTimeFromMillisBn(
        millis: Long?,
        nowMillis: Long = System.currentTimeMillis()
    ): String {
        if (millis == null || millis <= 0L) return "অজানা"
        val diffMs = (nowMillis - millis).coerceAtLeast(0L)
        val diffSec = diffMs / 1000L
        val diffMin = diffSec / 60L
        val diffHours = diffMin / 60L
        val remMin = diffMin % 60L
        val diffDays = diffHours / 24L
        val remHours = diffHours % 24L

        return when {
            diffSec < 60L -> "এইমাত্র (${diffSec.coerceAtLeast(1)} সেকেন্ড আগে)"
            diffMin < 60L -> "$diffMin মিনিট আগে"
            diffHours < 6L && remMin > 0L -> "$diffHours ঘণ্টা $remMin মিনিট আগে"
            diffHours < 24L -> "$diffHours ঘণ্টা আগে"
            diffDays == 1L -> if (remHours > 0L) "১ দিন $remHours ঘণ্টা আগে" else "গতকাল"
            diffDays < 30L -> "$diffDays দিন আগে"
            else -> SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(millis))
        }
    }

    /**
     * Formats an ISO timestamp with both exact local time and relative duration:
     * e.g., "আজ 08:30 AM • ২ মিনিট আগে" or "31 Oct, 01:29 PM • ১৯ ঘণ্টা আগে"
     */
    fun formatIsoWithRelativeBn(
        rawIso: String?,
        nowMillis: Long = System.currentTimeMillis()
    ): String {
        if (rawIso.isNullOrBlank()) return "অজানা"
        val millis = parseIsoToMillis(rawIso)
            ?: return rawIso.take(16).replace("T", " ")
        val exact = formatExactDateTimeBn(millis, nowMillis)
        val rel = formatRelativeTimeFromMillisBn(millis, nowMillis)
        return "$exact ($rel)"
    }

    /**
     * Computes real-time online & activity info for a `UserDevice`.
     * Considers:
     * 1. `device.lastSeen` / `device.lastActiveAt` (primary online heartbeat)
     * 2. Student-initiated activity timestamps from linked course requests / payments
     * 3. `device.requestedAt` / `device.updatedAt` / `device.createdAt`
     */
    fun resolveDeviceOnlineInfo(
        device: UserDevice,
        linkedEnrollments: List<CourseAccessEnrollment> = emptyList(),
        linkedTransactions: List<CoursePaymentTransaction> = emptyList(),
        extraLastSeenIso: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): DeviceOnlineInfo {
        val lastSeenMs = parseIsoToMillis(device.lastSeen)
        val lastActiveAtMs = parseIsoToMillis(device.lastActiveAt)
        val extraSeenMs = parseIsoToMillis(extraLastSeenIso)
        val requestedMs = parseIsoToMillis(device.requestedAt) ?: parseIsoToMillis(device.createdAt)
        val approvedMs = parseIsoToMillis(device.approvedAt)
        val updatedMs = parseIsoToMillis(device.updatedAt)

        // Check if student also interacted via Course Request or Payment submission recently
        val latestEnrollmentReqMs = linkedEnrollments.mapNotNull {
            parseIsoToMillis(it.requestedAt)
        }.maxOrNull()

        val latestPaymentSubMs = linkedTransactions.mapNotNull {
            parseIsoToMillis(it.effectiveSubmittedAt)
        }.maxOrNull()

        // If `updated_at` is nearly identical to `approved_at` (within 5 seconds), `updated_at` was caused by
        // the admin clicking Approve, whereas if `updated_at` is significantly newer than `approved_at`,
        // it reflects a newer device heartbeat/session update.
        val updatedByDeviceMs = if (updatedMs != null && approvedMs != null && abs(updatedMs - approvedMs) <= 5_000L) {
            null
        } else {
            updatedMs
        }

        val candidateOnlineTimestamps = listOfNotNull(
            lastSeenMs,
            lastActiveAtMs,
            extraSeenMs,
            latestPaymentSubMs,
            latestEnrollmentReqMs,
            updatedByDeviceMs,
            requestedMs,
            updatedMs
        )

        val effectiveOnlineMs = candidateOnlineTimestamps.maxOrNull()
        val diffMs = if (effectiveOnlineMs != null) (nowMillis - effectiveOnlineMs).coerceAtLeast(0L) else Long.MAX_VALUE

        val isOnlineNow = effectiveOnlineMs != null && diffMs <= 180_000L // Within 3 minutes
        val isRecentlyActive = effectiveOnlineMs != null && diffMs <= 1_800_000L // Within 30 minutes

        val exactOnline = if (effectiveOnlineMs != null) {
            formatExactDateTimeBn(effectiveOnlineMs, nowMillis)
        } else {
            "রেকর্ড নেই"
        }

        val relativeOnline = if (effectiveOnlineMs != null) {
            formatRelativeTimeFromMillisBn(effectiveOnlineMs, nowMillis)
        } else {
            "অজানা"
        }

        val statusBadge = when {
            isOnlineNow -> "🟢 এখন অনলাইন (Online Now)"
            isRecentlyActive -> "🟢 সক্রিয় ছিল: $relativeOnline"
            effectiveOnlineMs != null -> "🕒 অনলাইন ছিল: $relativeOnline"
            else -> "⚪ অনলাইন তথ্য নেই"
        }

        val requestedText = if (requestedMs != null) {
            "${formatExactDateTimeBn(requestedMs, nowMillis)} (${formatRelativeTimeFromMillisBn(requestedMs, nowMillis)})"
        } else {
            "উল্লেখ নেই"
        }

        val approvedText = if (approvedMs != null) {
            "${formatExactDateTimeBn(approvedMs, nowMillis)} (${formatRelativeTimeFromMillisBn(approvedMs, nowMillis)})"
        } else {
            null
        }

        return DeviceOnlineInfo(
            lastActiveMillis = effectiveOnlineMs,
            isOnlineNow = isOnlineNow,
            isRecentlyActive = isRecentlyActive,
            statusBadgeTextBn = statusBadge,
            lastActiveRelativeBn = relativeOnline,
            lastActiveExactBn = exactOnline,
            requestedAtFormattedBn = requestedText,
            approvedAtFormattedBn = approvedText
        )
    }
}
