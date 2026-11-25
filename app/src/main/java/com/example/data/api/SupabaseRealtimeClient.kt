package com.example.data.api

import com.example.data.preferences.AdminPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Zero-Trust Authenticated Supabase Realtime (Phoenix v1 WebSocket) Client.
 *
 * - Connects to `wss://<project>.supabase.co/realtime/v1/websocket?apikey=<anon>&vsn=1.0.0`.
 * - Strictly requires a valid authenticated Admin JWT (`access_token`) from [SupabaseAuthManager].
 *   Never connects using anon key or service_role key as Bearer/access_token.
 * - Subscribes to `postgres_changes` (`event: "*"`, `schema: "public"`) on all 9 live
 *   Realtime-enabled tables in the Supabase database.
 * - Maintains Phoenix heartbeats every 25 seconds, automatically pushes refreshed JWTs
 *   on token rotation, and reconnects with exponential backoff while an Admin session is active.
 */
class SupabaseRealtimeClient(
    private val adminPreferences: AdminPreferences,
    private val authManager: SupabaseAuthManager
) {

    companion object {
        val LIVE_REALTIME_TABLES: List<String> = listOf(
            "activation_support_links",
            "app_notices",
            "app_updates",
            "course_access_enrollments",
            "course_payment_settings",
            "course_payment_transactions",
            "device_shikho_accounts",
            "devices",
            "student_notifications"
        )

        private const val CHANNEL_TOPIC = "realtime:public"
        private const val HEARTBEAT_INTERVAL_MS = 25_000L
        private const val INITIAL_RECONNECT_DELAY_MS = 3_000L
        private const val MAX_RECONNECT_DELAY_MS = 30_000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refCounter = AtomicInteger(1)

    private val wsClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS) // Required for persistent WebSockets
            .pingInterval(20, TimeUnit.SECONDS)
            .build()
    }

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    @Volatile
    private var activeSocket: WebSocket? = null

    @Volatile
    private var shouldStayConnected: Boolean = false

    @Volatile
    private var lastSentToken: String = ""

    @Volatile
    private var tableChangeListener: ((table: String, eventType: String) -> Unit)? = null

    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private var currentBackoffMs: Long = INITIAL_RECONNECT_DELAY_MS

    @Synchronized
    fun start(onTableChanged: (table: String, eventType: String) -> Unit) {
        tableChangeListener = onTableChanged
        if (shouldStayConnected && (activeSocket != null || reconnectJob?.isActive == true)) {
            return
        }
        shouldStayConnected = true
        currentBackoffMs = INITIAL_RECONNECT_DELAY_MS
        connectAsync()
    }

    @Synchronized
    fun stop() {
        shouldStayConnected = false
        reconnectJob?.cancel()
        reconnectJob = null
        heartbeatJob?.cancel()
        heartbeatJob = null
        try {
            activeSocket?.close(1000, "Admin session stopped")
        } catch (_: Throwable) {
        }
        activeSocket = null
        lastSentToken = ""
        _isConnected.value = false
    }

    private fun connectAsync() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            if (!shouldStayConnected) return@launch
            if (!adminPreferences.hasValidAdminSession()) {
                _isConnected.value = false
                return@launch
            }

            val anonKey = adminPreferences.getSupabaseAnonKey().trim()
            val accessToken = authManager.ensureValidAccessTokenBlocking()

            // Strict Zero-Trust guard: never open Realtime without a valid authenticated Admin JWT
            if (anonKey.isBlank() || AdminPreferences.isServiceRoleJwt(anonKey)) {
                _isConnected.value = false
                return@launch
            }
            if (accessToken.isNullOrBlank() ||
                accessToken == anonKey ||
                AdminPreferences.isServiceRoleJwt(accessToken)
            ) {
                _isConnected.value = false
                if (shouldStayConnected && adminPreferences.hasValidAdminSession()) {
                    scheduleReconnect(force = true)
                }
                return@launch
            }

            val baseHttp = adminPreferences.getNormalizedSupabaseUrl().trimEnd('/')
            val wsBase = when {
                baseHttp.startsWith("https://", ignoreCase = true) ->
                    "wss://" + baseHttp.removePrefix("https://").removePrefix("HTTPS://")
                baseHttp.startsWith("http://", ignoreCase = true) ->
                    "ws://" + baseHttp.removePrefix("http://").removePrefix("HTTP://")
                else -> "wss://$baseHttp"
            }

            val encodedKey = try {
                URLEncoder.encode(anonKey, "UTF-8")
            } catch (_: Throwable) {
                anonKey
            }
            val wsUrl = "$wsBase/realtime/v1/websocket?apikey=$encodedKey&vsn=1.0.0"

            try {
                activeSocket?.close(1000, "Reconnecting")
            } catch (_: Throwable) {
            }
            activeSocket = null

            val request = Request.Builder()
                .url(wsUrl)
                .build()

            lastSentToken = accessToken
            activeSocket = wsClient.newWebSocket(request, createSocketListener(accessToken))
        }
    }

    private fun createSocketListener(initialAccessToken: String): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                if (webSocket !== activeSocket || !shouldStayConnected) {
                    webSocket.close(1000, "Stale socket")
                    return
                }
                sendJoinChannel(webSocket, initialAccessToken)
                startHeartbeatLoop(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (webSocket !== activeSocket) return
                handleIncomingMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                if (webSocket === activeSocket) {
                    _isConnected.value = false
                }
                try {
                    webSocket.close(1000, null)
                } catch (_: Throwable) {
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (webSocket === activeSocket) {
                    activeSocket = null
                    heartbeatJob?.cancel()
                    _isConnected.value = false
                    if (shouldStayConnected) {
                        scheduleReconnect()
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (webSocket === activeSocket) {
                    activeSocket = null
                    heartbeatJob?.cancel()
                    _isConnected.value = false
                    if (shouldStayConnected) {
                        scheduleReconnect()
                    }
                }
            }
        }
    }

    private fun sendJoinChannel(webSocket: WebSocket, accessToken: String) {
        val pgChanges = JSONArray().apply {
            LIVE_REALTIME_TABLES.forEach { tableName ->
                put(
                    JSONObject().apply {
                        put("event", "*")
                        put("schema", "public")
                        put("table", tableName)
                    }
                )
            }
        }

        val configObj = JSONObject().apply {
            put("broadcast", JSONObject().apply { put("self", false) })
            put("presence", JSONObject().apply { put("key", "") })
            put("postgres_changes", pgChanges)
        }

        val joinPayload = JSONObject().apply {
            put("config", configObj)
            put("access_token", accessToken)
        }

        val joinFrame = JSONObject().apply {
            put("topic", CHANNEL_TOPIC)
            put("event", "phx_join")
            put("payload", joinPayload)
            put("ref", nextRef())
        }
        webSocket.send(joinFrame.toString())
    }

    private fun sendAccessTokenUpdate(webSocket: WebSocket, newToken: String) {
        val tokenFrame = JSONObject().apply {
            put("topic", CHANNEL_TOPIC)
            put("event", "access_token")
            put("payload", JSONObject().apply { put("access_token", newToken) })
            put("ref", nextRef())
        }
        webSocket.send(tokenFrame.toString())
    }

    private fun startHeartbeatLoop(webSocket: WebSocket) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive && shouldStayConnected && activeSocket === webSocket) {
                delay(HEARTBEAT_INTERVAL_MS)
                if (!shouldStayConnected || activeSocket !== webSocket) break
                if (!adminPreferences.hasValidAdminSession()) {
                    stop()
                    break
                }

                try {
                    val hbFrame = JSONObject().apply {
                        put("topic", "phoenix")
                        put("event", "heartbeat")
                        put("payload", JSONObject())
                        put("ref", nextRef())
                    }
                    webSocket.send(hbFrame.toString())

                    // Keep Realtime channel JWT fresh before expiry
                    val currentToken = authManager.ensureValidAccessTokenBlocking()
                    if (!currentToken.isNullOrBlank() &&
                        currentToken != lastSentToken &&
                        !AdminPreferences.isServiceRoleJwt(currentToken)
                    ) {
                        lastSentToken = currentToken
                        sendAccessTokenUpdate(webSocket, currentToken)
                    }
                } catch (_: Throwable) {
                }
            }
        }
    }

    private fun handleIncomingMessage(rawText: String) {
        try {
            val json = JSONObject(rawText)
            val topic = json.optString("topic", "")
            val event = json.optString("event", "")
            val payload = json.optJSONObject("payload")

            when (event) {
                "phx_reply" -> {
                    if (topic == CHANNEL_TOPIC) {
                        val status = payload?.optString("status", "").orEmpty()
                        if (status.equals("ok", ignoreCase = true)) {
                            _isConnected.value = true
                            currentBackoffMs = INITIAL_RECONNECT_DELAY_MS
                        } else if (status.equals("error", ignoreCase = true)) {
                            _isConnected.value = false
                            try {
                                activeSocket?.close(1000, "Channel join error")
                            } catch (_: Throwable) {
                            }
                            activeSocket = null
                            heartbeatJob?.cancel()
                            if (shouldStayConnected) {
                                scheduleReconnect()
                            }
                        }
                    }
                }

                "postgres_changes" -> {
                    val dataObj = payload?.optJSONObject("data") ?: payload
                    val tableName = dataObj?.optString("table", "")?.trim().orEmpty()
                    val eventType = dataObj?.optString("type", "")?.trim().orEmpty()
                        .ifBlank { dataObj?.optString("eventType", "")?.trim().orEmpty() }
                    if (tableName.isNotBlank()) {
                        tableChangeListener?.invoke(tableName, eventType)
                    }
                }

                "phx_error", "phx_close" -> {
                    if (topic == CHANNEL_TOPIC) {
                        _isConnected.value = false
                        if (shouldStayConnected) {
                            scheduleReconnect()
                        }
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun scheduleReconnect(force: Boolean = false) {
        if (!shouldStayConnected || !adminPreferences.hasValidAdminSession()) return
        if (!force && reconnectJob?.isActive == true) return

        val delayMs = currentBackoffMs
        currentBackoffMs = (currentBackoffMs * 2).coerceAtMost(MAX_RECONNECT_DELAY_MS)

        reconnectJob = scope.launch {
            delay(delayMs)
            if (shouldStayConnected && adminPreferences.hasValidAdminSession()) {
                connectAsync()
            }
        }
    }

    private fun nextRef(): String = refCounter.getAndIncrement().toString()
}
