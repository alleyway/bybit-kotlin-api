package bybit.sdk.websocket

interface ByBitWebSocketListener {
    fun onAuthenticated(client: ByBitWebSocketClient)
    fun onReceive(client: ByBitWebSocketClient, message: ByBitWebSocketMessage)
    fun onDisconnect(client: ByBitWebSocketClient)
    fun onError(client: ByBitWebSocketClient, error: Throwable)
}

/**
 * A default implementation of [ByBitWebSocketListener] with stubbed implementations for each method.
 *
 * Extend this class to implement only the callbacks you care about
 */
open class DefaultByBitWebSocketListener : ByBitWebSocketListener {
    override fun onAuthenticated(client: ByBitWebSocketClient) { }
    override fun onReceive(client: ByBitWebSocketClient, message: ByBitWebSocketMessage) { }
    override fun onDisconnect(client: ByBitWebSocketClient) { }
    override fun onError(client: ByBitWebSocketClient, error: Throwable) { }
}
