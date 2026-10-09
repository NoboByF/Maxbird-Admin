package com.example.data.api

import com.example.data.preferences.AdminPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class SupabaseClient(private val adminPreferences: AdminPreferences) {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun createOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val apiKey = adminPreferences.getSupabaseKey()

            val requestBuilder = original.newBuilder()
                .header("apikey", apiKey)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")

            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun getApi(): SupabaseApi {
        val urlToUse = adminPreferences.getNormalizedSupabaseUrl()

        return try {
            Retrofit.Builder()
                .baseUrl(urlToUse)
                .client(createOkHttpClient())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SupabaseApi::class.java)
        } catch (_: Exception) {
            Retrofit.Builder()
                .baseUrl(AdminPreferences.DEFAULT_FALLBACK_URL)
                .client(createOkHttpClient())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SupabaseApi::class.java)
        }
    }
}
