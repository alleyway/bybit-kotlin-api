package bybit.sdk.rest

import io.ktor.client.plugins.*
import io.ktor.client.request.*

typealias ByBitRestOption = HttpRequestBuilder.() -> Unit

/**
 * ByBitRestOptions encapsulates a few helper functions for common request options.
 *
 * Option functionality is not limited to these helper functions.
 * You can always create your own [ByBitRestOption] to manipulate anything within an [HttpRequestBuilder].
 */
object ByBitRestOptions {

    /**
     * Sets an arbitrary header on an out-going request.
     *
     * Note that setting `User-Agent` isn't supported, and will be disregarded at request-time.
     */
    @JvmStatic
    fun withHeader(header: String, value: String): ByBitRestOption = {
        headers[header] = value
    }

    /**
     * Sets an arbitrary query parameter on an out-going request.
     *
     * This may be useful if a new query parameter is introduced to an API
     * but the SDK hasn't yet been updated to include it,
     * or if something is preventing you from updating to the latest version of the SDK.
     */
    @JvmStatic
    fun withQueryParam(name: String, value: String): ByBitRestOption = {
        url.parameters[name] = value
    }

    /**
     * Overrides the default timeout for an out-going request.
     */
    @JvmStatic
    fun withTimeout(timeoutMillis: Long): ByBitRestOption = {
        timeout { requestTimeoutMillis = timeoutMillis }
    }
}
