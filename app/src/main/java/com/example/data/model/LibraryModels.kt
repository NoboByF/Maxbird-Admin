package com.example.data.model

import com.example.data.api.CloudflareR2Client
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Root JSON schema stored at `library/library_index.json` inside Cloudflare R2 (`maxbird`).
 * Matches the Student App's Digital HTML Book Library contract 100%.
 */
data class LibraryIndex(
    val updatedAt: String = LibraryUtils.nowUtcIso8601(),
    val r2BaseUrl: String = CloudflareR2Client.PUBLIC_BASE_URL,
    val classes: List<LibraryClass> = emptyList()
) {
    val totalSubjects: Int
        get() = classes.sumOf { it.subjects.size }

    val totalChapters: Int
        get() = classes.sumOf { cls -> cls.subjects.sumOf { it.chapters.size } }

    val totalFiles: Int
        get() = classes.sumOf { cls ->
            cls.subjects.sumOf { sub -> sub.chapters.sumOf { it.files.size } }
        }
}

data class LibraryClass(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val badge: String = "",
    val colorHex: String = "#10B981",
    val order: Int = 1,
    val isActive: Boolean = true,
    val subjects: List<LibrarySubject> = emptyList()
) {
    val totalChapters: Int
        get() = subjects.sumOf { it.chapters.size }

    val totalFiles: Int
        get() = subjects.sumOf { sub -> sub.chapters.sumOf { it.files.size } }

    fun collectAllR2Keys(): List<String> {
        val keys = mutableListOf<String>()
        subjects.forEach { subject ->
            keys.addAll(subject.collectAllR2Keys())
        }
        return keys
    }
}

data class LibrarySubject(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val coverUrl: String = "",
    val colorHex: String = "#0284C7",
    val order: Int = 1,
    val isActive: Boolean = true,
    val chapters: List<LibraryChapter> = emptyList()
) {
    val totalFiles: Int
        get() = chapters.sumOf { it.files.size }

    fun collectAllR2Keys(): List<String> {
        val keys = mutableListOf<String>()
        CloudflareR2Client.extractFileKeyFromPublicUrl(coverUrl)?.let { keys.add(it) }
        chapters.forEach { chapter ->
            keys.addAll(chapter.collectAllR2Keys())
        }
        return keys
    }
}

data class LibraryChapter(
    val id: String,
    val chapterNumber: Int = 1,
    val title: String,
    val subtitle: String = "",
    val order: Int = 1,
    val isActive: Boolean = true,
    val files: List<LibraryHtmlFile> = emptyList()
) {
    fun collectAllR2Keys(): List<String> {
        return files.mapNotNull { it.fileUrl.trim().takeIf { url -> url.isNotEmpty() } }
    }
}

data class LibraryHtmlFile(
    val id: String,
    val title: String,
    val description: String = "",
    val fileUrl: String,
    val fileSizeBytes: Long = 0L,
    val version: Int = 1,
    val order: Int = 1,
    val isActive: Boolean = true
)

object LibraryIndexJsonCodec {

    fun parse(rawJson: String): LibraryIndex {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) {
            return LibraryIndex()
        }
        val root = JSONObject(trimmed)
        val updatedAt = root.optString("updated_at", "").ifBlank { LibraryUtils.nowUtcIso8601() }
        val r2BaseUrl = root.optString("r2_base_url", "").ifBlank { CloudflareR2Client.PUBLIC_BASE_URL }
        val classesArray = root.optJSONArray("classes") ?: JSONArray()

        val classes = mutableListOf<LibraryClass>()
        for (i in 0 until classesArray.length()) {
            val obj = classesArray.optJSONObject(i) ?: continue
            classes.add(parseClass(obj, fallbackOrder = i + 1))
        }

        return LibraryIndex(
            updatedAt = updatedAt,
            r2BaseUrl = r2BaseUrl,
            classes = classes.sortedBy { it.order }
        )
    }

    private fun parseClass(obj: JSONObject, fallbackOrder: Int): LibraryClass {
        val subjectsArray = obj.optJSONArray("subjects") ?: JSONArray()
        val subjects = mutableListOf<LibrarySubject>()
        for (i in 0 until subjectsArray.length()) {
            val sObj = subjectsArray.optJSONObject(i) ?: continue
            subjects.add(parseSubject(sObj, fallbackOrder = i + 1))
        }

        return LibraryClass(
            id = obj.optString("id", "").ifBlank { "class-$fallbackOrder" },
            title = obj.optString("title", ""),
            subtitle = obj.optString("subtitle", ""),
            badge = obj.optString("badge", ""),
            colorHex = obj.optString("color_hex", "#10B981").ifBlank { "#10B981" },
            order = obj.optInt("order", fallbackOrder),
            isActive = obj.optBoolean("is_active", true),
            subjects = subjects.sortedBy { it.order }
        )
    }

    private fun parseSubject(obj: JSONObject, fallbackOrder: Int): LibrarySubject {
        val chaptersArray = obj.optJSONArray("chapters") ?: JSONArray()
        val chapters = mutableListOf<LibraryChapter>()
        for (i in 0 until chaptersArray.length()) {
            val cObj = chaptersArray.optJSONObject(i) ?: continue
            chapters.add(parseChapter(cObj, fallbackOrder = i + 1))
        }

        return LibrarySubject(
            id = obj.optString("id", "").ifBlank { "subject-$fallbackOrder" },
            title = obj.optString("title", ""),
            subtitle = obj.optString("subtitle", ""),
            coverUrl = obj.optString("cover_url", ""),
            colorHex = obj.optString("color_hex", "#0284C7").ifBlank { "#0284C7" },
            order = obj.optInt("order", fallbackOrder),
            isActive = obj.optBoolean("is_active", true),
            chapters = chapters.sortedBy { it.order }
        )
    }

    private fun parseChapter(obj: JSONObject, fallbackOrder: Int): LibraryChapter {
        val filesArray = obj.optJSONArray("files") ?: JSONArray()
        val files = mutableListOf<LibraryHtmlFile>()
        for (i in 0 until filesArray.length()) {
            val fObj = filesArray.optJSONObject(i) ?: continue
            files.add(parseFile(fObj, fallbackOrder = i + 1))
        }

        return LibraryChapter(
            id = obj.optString("id", "").ifBlank { "ch-$fallbackOrder" },
            chapterNumber = obj.optInt("chapter_number", fallbackOrder),
            title = obj.optString("title", ""),
            subtitle = obj.optString("subtitle", ""),
            order = obj.optInt("order", fallbackOrder),
            isActive = obj.optBoolean("is_active", true),
            files = files.sortedBy { it.order }
        )
    }

    private fun parseFile(obj: JSONObject, fallbackOrder: Int): LibraryHtmlFile {
        return LibraryHtmlFile(
            id = obj.optString("id", "").ifBlank { "file-$fallbackOrder" },
            title = obj.optString("title", ""),
            description = obj.optString("description", ""),
            fileUrl = obj.optString("file_url", ""),
            fileSizeBytes = obj.optLong("file_size_bytes", 0L),
            version = obj.optInt("version", 1).coerceAtLeast(1),
            order = obj.optInt("order", fallbackOrder),
            isActive = obj.optBoolean("is_active", true)
        )
    }

    fun serialize(index: LibraryIndex): String {
        val root = JSONObject()
        root.put("updated_at", index.updatedAt)
        root.put("r2_base_url", index.r2BaseUrl.ifBlank { CloudflareR2Client.PUBLIC_BASE_URL })

        val classesArr = JSONArray()
        index.classes.sortedBy { it.order }.forEach { cls ->
            val clsObj = JSONObject()
            clsObj.put("id", cls.id)
            clsObj.put("title", cls.title)
            clsObj.put("subtitle", cls.subtitle)
            clsObj.put("badge", cls.badge)
            clsObj.put("color_hex", cls.colorHex)
            clsObj.put("order", cls.order)
            clsObj.put("is_active", cls.isActive)

            val subjectsArr = JSONArray()
            cls.subjects.sortedBy { it.order }.forEach { sub ->
                val subObj = JSONObject()
                subObj.put("id", sub.id)
                subObj.put("title", sub.title)
                subObj.put("subtitle", sub.subtitle)
                subObj.put("cover_url", sub.coverUrl)
                subObj.put("color_hex", sub.colorHex)
                subObj.put("order", sub.order)
                subObj.put("is_active", sub.isActive)

                val chaptersArr = JSONArray()
                sub.chapters.sortedBy { it.order }.forEach { ch ->
                    val chObj = JSONObject()
                    chObj.put("id", ch.id)
                    chObj.put("chapter_number", ch.chapterNumber)
                    chObj.put("title", ch.title)
                    chObj.put("subtitle", ch.subtitle)
                    chObj.put("order", ch.order)
                    chObj.put("is_active", ch.isActive)

                    val filesArr = JSONArray()
                    ch.files.sortedBy { it.order }.forEach { file ->
                        val fObj = JSONObject()
                        fObj.put("id", file.id)
                        fObj.put("title", file.title)
                        fObj.put("description", file.description)
                        fObj.put("file_url", file.fileUrl)
                        fObj.put("file_size_bytes", file.fileSizeBytes)
                        fObj.put("version", file.version)
                        fObj.put("order", file.order)
                        fObj.put("is_active", file.isActive)
                        filesArr.put(fObj)
                    }

                    chObj.put("files", filesArr)
                    chaptersArr.put(chObj)
                }

                subObj.put("chapters", chaptersArr)
                subjectsArr.put(subObj)
            }

            clsObj.put("subjects", subjectsArr)
            classesArr.put(clsObj)
        }

        root.put("classes", classesArr)
        return root.toString(2)
    }
}

object LibraryUtils {

    val PRESET_COLORS = listOf(
        "#10B981", // Emerald
        "#0284C7", // Sky Blue
        "#6366F1", // Indigo
        "#8B5CF6", // Violet
        "#EC4899", // Pink
        "#F59E0B", // Amber
        "#EF4444", // Coral Red
        "#14B8A6"  // Teal
    )

    fun nowUtcIso8601(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun slugify(raw: String, fallbackPrefix: String): String {
        val cleaned = raw
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(40)
        return if (cleaned.isNotBlank()) {
            cleaned
        } else {
            val suffix = (System.currentTimeMillis() % 100000L).toString()
            "$fallbackPrefix-$suffix"
        }
    }

    fun ensureUniqueId(desiredId: String, existingIds: Set<String>, fallbackPrefix: String): String {
        val base = slugify(desiredId, fallbackPrefix)
        if (base !in existingIds) return base
        var counter = 2
        while ("$base-$counter" in existingIds) {
            counter++
        }
        return "$base-$counter"
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024L * 1024L -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            bytes > 0L -> "$bytes B"
            else -> "0 B"
        }
    }

    fun buildHtmlFileKey(classId: String, subjectId: String, chapterId: String, fileId: String): String {
        return "library/$classId/$subjectId/$chapterId/$fileId.html"
    }

    fun normalizeHexColor(input: String, fallback: String): String {
        val trimmed = input.trim().uppercase(Locale.US)
        val withHash = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
        return if (Regex("^#[0-9A-F]{6}$").matches(withHash)) withHash else fallback
    }

    fun cleanTitleFromFileName(fileName: String): String {
        val withoutExt = fileName.substringBeforeLast('.')
        return withoutExt
            .replace('_', ' ')
            .replace('-', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { "নতুন HTML পাঠ" }
    }
}
