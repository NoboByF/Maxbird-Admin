package com.example.data.api

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.CloudinaryUploadResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.InputStream
import java.util.concurrent.TimeUnit

class CloudinaryClient(private val context: Context) {

    companion object {
        const val CLOUD_NAME = "cross-border-education-technologies-pte-ltd"
        const val UPLOAD_PRESET = "profile"
        const val BASE_URL = "https://api.cloudinary.com/"
    }

    private val api: CloudinaryApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CloudinaryApi::class.java)
    }

    data class FileMeta(
        val name: String,
        val size: Long,
        val mimeType: String
    )

    fun getFileMeta(uri: Uri): FileMeta {
        var name = "uploaded_file"
        var size = 0L
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex) ?: name
                }
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
        return FileMeta(name, size, mimeType)
    }

    suspend fun uploadFile(uri: Uri): Result<CloudinaryUploadResponse> = withContext(Dispatchers.IO) {
        try {
            val meta = getFileMeta(uri)
            val stream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("ফাইলটি পড়া সম্ভব হয়নি। (Cannot open file)"))

            val bytes = stream.use { it.readBytes() }
            if (bytes.isEmpty()) {
                return@withContext Result.failure(Exception("নির্বাচিত ফাইলটি শূন্য (0 byte) বা ফাঁকা।"))
            }

            val requestFile = bytes.toRequestBody(meta.mimeType.toMediaTypeOrNull(), 0, bytes.size)
            val body = MultipartBody.Part.createFormData("file", meta.name, requestFile)
            val presetPart = UPLOAD_PRESET.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadMedia(
                cloudName = CLOUD_NAME,
                uploadPreset = presetPart,
                file = body
            )

            if (response.isSuccessful && response.body() != null) {
                val uploadResult = response.body()!!
                if (!uploadResult.secureUrl.isNullOrBlank()) {
                    Result.success(uploadResult)
                } else {
                    Result.failure(Exception("Cloudinary secure_url পাওয়া যায়নি।"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("আপলোড ব্যর্থ হয়েছে (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
