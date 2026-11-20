package com.example.data.api

import com.example.data.preferences.AdminPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class SupabaseClient(private val adminPreferences: AdminPreferences) {

    val authManager: SupabaseAuthManager = SupabaseAuthManager(adminPreferences)

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

    /**
     * Zero-Trust Authenticated OkHttp Client:
     * - Always sends `apikey: <SUPABASE_ANON_KEY>` (never service_role).
     * - Sends `Authorization: Bearer <user_access_token>` from the logged-in admin session.
     * - Proactively refreshes the token if expired or expiring within 5 minutes.
     * - On `401 Unauthorized`, refreshes once and retries; if refresh fails, clears the session
     *   so the UI immediately navigates back to the Admin Login screen.
     */
    private fun createOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val anonKey = adminPreferences.getSupabaseAnonKey()
            val accessToken = authManager.ensureValidAccessTokenBlocking()

            // Strict Zero-Trust fail-closed guard: never execute REST/RPC calls without an
            // authenticated admin JWT (and never allow Authorization: Bearer <SUPABASE_ANON_KEY>).
            if (accessToken.isNullOrBlank() || accessToken == anonKey || AdminPreferences.isServiceRoleJwt(accessToken)) {
                adminPreferences.clearAuthSession("অ্যাডমিন সেশন পাওয়া যায়নি। অনুগ্রহ করে পুনরায় লগইন করুন।")
                return@Interceptor Response.Builder()
                    .request(original)
                    .protocol(Protocol.HTTP_1_1)
                    .code(401)
                    .message("Unauthorized - Admin session required")
                    .body("{\"error\":\"admin_session_required\"}".toResponseBody("application/json".toMediaType()))
                    .build()
            }

            fun buildSignedRequest(token: String) = original.newBuilder()
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()

            val firstResponse = chain.proceed(buildSignedRequest(accessToken))

            if (firstResponse.code == 401) {
                // Attempt a single synchronized token refresh & replay
                val refreshed = authManager.refreshSessionBlocking(failedAccessToken = accessToken)
                if (refreshed != null && refreshed.accessToken.isNotBlank() && refreshed.accessToken != anonKey) {
                    firstResponse.close()
                    val retryResponse = chain.proceed(buildSignedRequest(refreshed.accessToken))
                    if (retryResponse.code == 401) {
                        adminPreferences.clearAuthSession("অ্যাডমিন সেশনের মেয়াদ শেষ হয়েছে। অনুগ্রহ করে পুনরায় লগইন করুন।")
                    }
                    return@Interceptor retryResponse
                } else {
                    adminPreferences.clearAuthSession("অ্যাডমিন সেশনের মেয়াদ শেষ হয়েছে। অনুগ্রহ করে পুনরায় লগইন করুন।")
                }
            }

            firstResponse
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
            redactHeader("apikey")
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
