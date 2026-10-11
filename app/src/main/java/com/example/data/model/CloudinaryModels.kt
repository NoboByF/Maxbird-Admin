package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Cloudinary API Response model
 * Matches response from POST https://api.cloudinary.com/v1_1/{cloud_name}/image/upload
 */
@JsonClass(generateAdapter = true)
data class CloudinaryUploadResponse(
    @Json(name = "public_id") val publicId: String? = null,
    @Json(name = "version") val version: Long? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null,
    @Json(name = "format") val format: String? = null,
    @Json(name = "resource_type") val resourceType: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "bytes") val bytes: Long? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "secure_url") val secureUrl: String? = null,
    @Json(name = "original_filename") val originalFilename: String? = null
)

/**
 * Local uploaded media item for history display
 */
@JsonClass(generateAdapter = true)
data class CloudinaryHistoryItem(
    @Json(name = "id") val id: String = java.util.UUID.randomUUID().toString(),
    @Json(name = "file_name") val fileName: String,
    @Json(name = "file_size_formatted") val fileSizeFormatted: String,
    @Json(name = "mime_type") val mimeType: String,
    @Json(name = "resource_type") val resourceType: String, // "image", "video", "raw" (pdf)
    @Json(name = "secure_url") val secureUrl: String,
    @Json(name = "uploaded_at_formatted") val uploadedAtFormatted: String
)
