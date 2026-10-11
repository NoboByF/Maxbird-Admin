package com.example.data.api

import com.example.data.model.CloudinaryUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface CloudinaryApi {

    /**
     * Upload image to Shikho Cloudinary.
     * Endpoint: POST https://api.cloudinary.com/v1_1/{cloud_name}/image/upload
     */
    @Multipart
    @POST("v1_1/{cloudName}/image/upload")
    suspend fun uploadMedia(
        @Path("cloudName") cloudName: String = "cross-border-education-technologies-pte-ltd",
        @Part("upload_preset") uploadPreset: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<CloudinaryUploadResponse>
}
