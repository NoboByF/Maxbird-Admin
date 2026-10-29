package com.example.data.api

import com.example.data.preferences.AdminPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class SupabaseClient(private val adminPreferences: AdminPreferences) {

    /**
     * Lenient adapter factory that safely handles PostgreSQL `numeric` (e.g. `500.00`)
     * into Kotlin `Int`/`Long`, and prevents `JsonDataException` when nullable SQL columns
     * return `null` for primitive or `String` fields with defaults.
     */
    private val lenientPrimitiveFactory = object : JsonAdapter.Factory {
        override fun create(
            type: Type,
            annotations: MutableSet<out Annotation>,
            moshi: Moshi
        ): JsonAdapter<*>? {
            if (annotations.isNotEmpty()) return null

            return when (type) {
                Int::class.javaPrimitiveType, Int::class.javaObjectType -> object : JsonAdapter<Int>() {
                    override fun fromJson(reader: JsonReader): Int? {
                        return when (reader.peek()) {
                            JsonReader.Token.NULL -> {
                                reader.nextNull<Any?>()
                                if (type == Int::class.javaPrimitiveType) 0 else null
                            }
                            JsonReader.Token.NUMBER -> reader.nextDouble().toInt()
                            JsonReader.Token.STRING -> reader.nextString().trim().toDoubleOrNull()?.toInt() ?: 0
                            JsonReader.Token.BOOLEAN -> if (reader.nextBoolean()) 1 else 0
                            else -> {
                                reader.skipValue()
                                0
                            }
                        }
                    }

                    override fun toJson(writer: JsonWriter, value: Int?) {
                        if (value == null) writer.nullValue() else writer.value(value)
                    }
                }

                Long::class.javaPrimitiveType, Long::class.javaObjectType -> object : JsonAdapter<Long>() {
                    override fun fromJson(reader: JsonReader): Long? {
                        return when (reader.peek()) {
                            JsonReader.Token.NULL -> {
                                reader.nextNull<Any?>()
                                if (type == Long::class.javaPrimitiveType) 0L else null
                            }
                            JsonReader.Token.NUMBER -> reader.nextDouble().toLong()
                            JsonReader.Token.STRING -> reader.nextString().trim().toDoubleOrNull()?.toLong() ?: 0L
                            else -> {
                                reader.skipValue()
                                0L
                            }
                        }
                    }

                    override fun toJson(writer: JsonWriter, value: Long?) {
                        if (value == null) writer.nullValue() else writer.value(value)
                    }
                }

                Double::class.javaPrimitiveType, Double::class.javaObjectType -> object : JsonAdapter<Double>() {
                    override fun fromJson(reader: JsonReader): Double? {
                        return when (reader.peek()) {
                            JsonReader.Token.NULL -> {
                                reader.nextNull<Any?>()
                                if (type == Double::class.javaPrimitiveType) 0.0 else null
                            }
                            JsonReader.Token.NUMBER -> reader.nextDouble()
                            JsonReader.Token.STRING -> reader.nextString().trim().toDoubleOrNull() ?: 0.0
                            else -> {
                                reader.skipValue()
                                0.0
                            }
                        }
                    }

                    override fun toJson(writer: JsonWriter, value: Double?) {
                        if (value == null) writer.nullValue() else writer.value(value)
                    }
                }

                Boolean::class.javaPrimitiveType, Boolean::class.javaObjectType -> object : JsonAdapter<Boolean>() {
                    override fun fromJson(reader: JsonReader): Boolean? {
                        return when (reader.peek()) {
                            JsonReader.Token.NULL -> {
                                reader.nextNull<Any?>()
                                if (type == Boolean::class.javaPrimitiveType) false else null
                            }
                            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
                            JsonReader.Token.STRING -> reader.nextString().trim().equals("true", ignoreCase = true)
                            JsonReader.Token.NUMBER -> reader.nextDouble() != 0.0
                            else -> {
                                reader.skipValue()
                                false
                            }
                        }
                    }

                    override fun toJson(writer: JsonWriter, value: Boolean?) {
                        if (value == null) writer.nullValue() else writer.value(value)
                    }
                }

                String::class.java -> object : JsonAdapter<String>() {
                    override fun fromJson(reader: JsonReader): String? {
                        return when (reader.peek()) {
                            JsonReader.Token.NULL -> {
                                reader.nextNull<Any?>()
                                ""
                            }
                            JsonReader.Token.STRING -> reader.nextString()
                            JsonReader.Token.NUMBER -> reader.nextString()
                            JsonReader.Token.BOOLEAN -> reader.nextBoolean().toString()
                            else -> {
                                reader.skipValue()
                                ""
                            }
                        }
                    }

                    override fun toJson(writer: JsonWriter, value: String?) {
                        if (value == null) writer.nullValue() else writer.value(value)
                    }
                }

                else -> null
            }
        }
    }

    val moshi: Moshi = Moshi.Builder()
        .add(lenientPrimitiveFactory)
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
