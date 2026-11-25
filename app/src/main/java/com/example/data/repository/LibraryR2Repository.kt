package com.example.data.repository

import android.net.Uri
import com.example.data.api.CloudflareR2Client
import com.example.data.model.LibraryIndex
import com.example.data.model.LibraryIndexJsonCodec
import com.example.data.model.LibraryUtils

/**
 * Repository managing Digital HTML Book Library directly on Cloudflare R2 (`maxbird` bucket).
 * No Supabase table is required — all `.html` files and `library/library_index.json` live in R2.
 */
class LibraryR2Repository(
    private val r2Client: CloudflareR2Client
) {
    companion object {
        const val INDEX_FILE_KEY = "library/library_index.json"
        const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
        const val CONTENT_TYPE_HTML = "text/html; charset=utf-8"
        const val CACHE_CONTROL_NO_CACHE = "no-cache, max-age=0"

        val INDEX_PUBLIC_URL: String
            get() = CloudflareR2Client.buildPublicUrl(INDEX_FILE_KEY)
    }

    data class FetchIndexResult(
        val index: LibraryIndex,
        val existedOnR2: Boolean
    )

    /**
     * Loads `library/library_index.json` directly from R2 (bypassing CDN cache).
     * If HTTP 404 is returned (file not created yet), returns an empty `LibraryIndex` without error.
     */
    suspend fun fetchLibraryIndex(): Result<FetchIndexResult> {
        val rawResult = r2Client.downloadTextObjectFromR2(INDEX_FILE_KEY)
        return rawResult.fold(
            onSuccess = { rawJson ->
                if (rawJson == null) {
                    Result.success(
                        FetchIndexResult(
                            index = LibraryIndex(
                                updatedAt = LibraryUtils.nowUtcIso8601(),
                                r2BaseUrl = CloudflareR2Client.PUBLIC_BASE_URL,
                                classes = emptyList()
                            ),
                            existedOnR2 = false
                        )
                    )
                } else {
                    try {
                        val parsed = LibraryIndexJsonCodec.parse(rawJson)
                        Result.success(
                            FetchIndexResult(
                                index = parsed,
                                existedOnR2 = true
                            )
                        )
                    } catch (e: Exception) {
                        Result.failure(Exception("library_index.json পার্স ত্রুটি: ${e.message}"))
                    }
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Serializes and uploads `library/library_index.json` to R2 with
     * `Content-Type: application/json; charset=utf-8` and `Cache-Control: no-cache, max-age=0`.
     */
    suspend fun saveLibraryIndex(index: LibraryIndex): Result<LibraryIndex> {
        val updated = index.copy(
            updatedAt = LibraryUtils.nowUtcIso8601(),
            r2BaseUrl = CloudflareR2Client.PUBLIC_BASE_URL
        )
        val jsonBytes = LibraryIndexJsonCodec.serialize(updated).toByteArray(Charsets.UTF_8)
        val uploadResult = r2Client.uploadBytesToR2(
            bytes = jsonBytes,
            targetFileKey = INDEX_FILE_KEY,
            contentType = CONTENT_TYPE_JSON,
            cacheControl = CACHE_CONTROL_NO_CACHE
        )
        return uploadResult.fold(
            onSuccess = { Result.success(updated) },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Uploads an `.html` book/lesson file to `library/{classId}/{subjectId}/{chapterId}/{fileId}.html`
     * with `Content-Type: text/html; charset=utf-8`.
     */
    suspend fun uploadHtmlFile(
        uri: Uri,
        classId: String,
        subjectId: String,
        chapterId: String,
        fileId: String,
        onProgress: (percent: Int, uploadedBytes: Long, totalBytes: Long) -> Unit = { _, _, _ -> }
    ): Result<CloudflareR2Client.R2UploadResult> {
        val fileKey = LibraryUtils.buildHtmlFileKey(classId, subjectId, chapterId, fileId)
        return uploadHtmlFileToKey(uri = uri, fileKey = fileKey, onProgress = onProgress)
    }

    /**
     * Uploads or replaces an `.html` file at an exact R2 object key.
     */
    suspend fun uploadHtmlFileToKey(
        uri: Uri,
        fileKey: String,
        onProgress: (percent: Int, uploadedBytes: Long, totalBytes: Long) -> Unit = { _, _, _ -> }
    ): Result<CloudflareR2Client.R2UploadResult> {
        return r2Client.uploadUriToR2(
            uri = uri,
            targetFileKey = fileKey.trim().trimStart('/'),
            contentTypeOverride = CONTENT_TYPE_HTML,
            cacheControl = CACHE_CONTROL_NO_CACHE,
            onProgress = onProgress
        )
    }

    /**
     * Optionally uploads a subject cover image to `library/covers/...` on R2.
     */
    suspend fun uploadSubjectCover(
        uri: Uri,
        classId: String,
        subjectId: String
    ): Result<CloudflareR2Client.R2UploadResult> {
        val meta = r2Client.inspectUriMetadata(uri)
        val ext = meta.fileName.substringAfterLast('.', "jpg")
            .lowercase()
            .takeIf { it in setOf("jpg", "jpeg", "png", "webp") } ?: "jpg"
        val key = "library/covers/${classId}_${subjectId}_${System.currentTimeMillis()}.$ext"
        return r2Client.uploadUriToR2(
            uri = uri,
            targetFileKey = key
        )
    }

    /**
     * Deletes all specified R2 object keys (`.html` files or covers) safely.
     * Returns the count of objects deleted without error.
     */
    suspend fun deleteR2Objects(fileKeys: List<String>): Int {
        var deletedCount = 0
        fileKeys.distinct().forEach { key ->
            if (key.isNotBlank()) {
                val res = r2Client.deleteObjectFromR2(key)
                if (res.isSuccess) {
                    deletedCount++
                }
            }
        }
        return deletedCount
    }

    fun inspectUri(uri: Uri): CloudflareR2Client.UriMetadata {
        return r2Client.inspectUriMetadata(uri)
    }
}
