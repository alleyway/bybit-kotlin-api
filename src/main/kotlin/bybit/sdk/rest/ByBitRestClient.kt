package bybit.sdk.rest

import bybit.sdk.DefaultCIOHttpClientProvider
import bybit.sdk.HttpClientProvider
import bybit.sdk.Logging
import bybit.sdk.RateLimitReachedException
import bybit.sdk.Version
import bybit.sdk.rest.account.ByBitAccountClient
import bybit.sdk.rest.asset.ByBitAssetClient
import bybit.sdk.rest.market.ByBitMarketClient
import bybit.sdk.rest.order.ByBitOrderClient
import bybit.sdk.rest.position.ByBitPositionClient
import bybit.sdk.rest.user.ByBitUserClient
import bybit.sdk.shared.sha256_HMAC
import io.github.resilience4j.kotlin.ratelimiter.executeSuspendFunction
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.Instant


/**
 * A client for the ByBit API
 *
 * @param apiKey the API key to use with all API requests
 * @param secret the secret to use with all API requests
 * @param testnet whether to use testnet or not
 * @param httpClientProvider (Optional) A provider for the ktor [HttpClient] to use
 * @param rateLimits map of PATH to INT for calls per second, check api rate limit docs
 */
class ByBitRestClient
@JvmOverloads
constructor(
    val apiKey: String?,
    val secret: String?,
    val testnet: Boolean = false,
    val httpClientProvider: HttpClientProvider = DefaultCIOHttpClientProvider(),
    val rateLimits: Map<String, Int>? = defaultRateLimits
) {

    val logger = Logging.getLogger(ByBitRestClient::class)

    val assetClient by lazy { ByBitAssetClient(this) }

    val accountClient by lazy { ByBitAccountClient(this) }

    val marketClient by lazy { ByBitMarketClient(this) }

    val orderClient by lazy { ByBitOrderClient(this) }

    val userClient by lazy { ByBitUserClient(this) }

    val positionClient by lazy { ByBitPositionClient(this) }

    private var bybitApiDomain: String

    private var rateLimitMap: Map<String, RateLimiter>? = null

    companion object {

        val DEFAULT = "default"

        val defaultRateLimits: Map<String, Int> = mapOf(
            DEFAULT to 10,
            "/v5/order/create" to 10,
            "/v5/order/amend" to 10,
            "/v5/order/cancel" to 10,
            "/v5/order/cancel-all" to 10,
            "/v5/order/realtime" to 10,
            "/v5/order/history" to 10,
            "/v5/execution/list" to 10,
            "/v5/position/list" to 10,
            "/v5/position/closed-pnl" to 10,
            "/v5/account/wallet-balance" to 10,
            "/v5/account/fee-rate" to 10,
        )
    }


    private fun createRateLimitConfig(limit: Int): RateLimiterConfig {
        return RateLimiterConfig.custom()
            .limitForPeriod(limit) // Allow 10 calls within a time window
            .limitRefreshPeriod(Duration.ofMillis(1_000)) // Time window of 1 second
            .timeoutDuration(Duration.ofMillis(1_500)) // Timeout for acquiring a permit
            .drainPermissionsOnResult {
                if (it.isRight && it.get() is Boolean && it.get() as Boolean) {
                    logger.warn("Reached rate limit!")
                    return@drainPermissionsOnResult true
                }
                else if (it.isLeft && it.left is RateLimitReachedException) {
                    return@drainPermissionsOnResult true
                }
                false
            }
            .build()
    }

    init {

        if (apiKey == null || secret == null) {
            throw Exception("You must specify ByBit apikey/secret!")
        }

        bybitApiDomain = if (testnet) {
            "api-testnet.bybit.com"
        } else {
            "api.bybit.com"
        }

        rateLimits?.let {
            val map = mutableMapOf<String, RateLimiter>()
            rateLimits.entries.forEach {
                val rateLimitConfig = createRateLimitConfig(it.value)
                map[it.key] = RateLimiter.of(it.key, rateLimitConfig)
            }

            if (rateLimits[DEFAULT] == null) {
                val rateLimitConfig = createRateLimitConfig(10)
                map[DEFAULT] = RateLimiter.of(DEFAULT, rateLimitConfig)
            }
            rateLimitMap = map.toMap()
        }
    }

    private val baseUrlBuilder: URLBuilder
        get() = httpClientProvider.getDefaultRestURLBuilder().apply {
            host = bybitApiDomain
            if (!apiKey.isNullOrEmpty()) parameters["apiKey"] = apiKey
        }

    private inline fun <R> withHttpClient(codeBlock: (client: HttpClient) -> R) =
        httpClientProvider.buildClient().use(codeBlock)


    private fun signIfNeeded(headers: HeadersBuilder, isPublicAPI: Boolean, queryOrBody: String) {

        if (!isPublicAPI) {

            val timestamp = Instant.now().toEpochMilli()
            val recvWindow = 4000
            val key = if (!apiKey.isNullOrBlank()) {
                apiKey
            } else {
                throw Exception("found a null apiKey")
            }

            // timestamp+api_key+recv_window+queryString

            val toEncode = "${timestamp}${key}${recvWindow}${queryOrBody}"

            val signature = sha256_HMAC(toEncode, secret)

            headers["X-BAPI-SIGN"] = signature
            headers["X-BAPI-API-KEY"] = key
            headers["X-BAPI-TIMESTAMP"] = timestamp.toString()
            headers["X-BAPI-RECV-WINDOW"] = recvWindow.toString()
        }
    }

    internal suspend inline fun <reified T> call(
        crossinline urlBuilderBlock: URLBuilder.() -> Unit,
        method: HttpMethod = HttpMethod.Get,
        isPublicAPI: Boolean = true
    ): T {

        if (rateLimitMap !== null) {
            val url = baseUrlBuilder.apply(urlBuilderBlock).build()

            val rateLimiter = rateLimitMap!![url.encodedPath]
                ?: rateLimitMap!![DEFAULT]!!

            if (rateLimiter.name == DEFAULT) {
                logger.warn("Using 'default' ratelimiter for path '${url.encodedPath}'")
            }

            return rateLimiter.executeSuspendFunction {
                callRaw(
                    urlBuilderBlock,
                    method,
                    isPublicAPI
                )
            }
        } else {
            return callRaw(
                urlBuilderBlock,
                method,
                isPublicAPI
            )
        }
    }


    @OptIn(InternalAPI::class)
    internal suspend inline fun <reified T> callRaw(
        urlBuilderBlock: URLBuilder.() -> Unit,
        method: HttpMethod = HttpMethod.Get,
        isPublicAPI: Boolean = true
    ): T {
        val url = baseUrlBuilder.apply(urlBuilderBlock).build()


        val body = withHttpClient { httpClient ->


            if (method === HttpMethod.Get) {

                httpClient.get(url) {
                    signIfNeeded(headers, isPublicAPI, url.encodedQuery)
                    // Set after options are applied to be sure it doesn't get over-written.
                    headers["User-Agent"] = Version.userAgent
                }


            } else {
                httpClient.post(url) {
                    contentType(ContentType.Application.Json)

                    val map: MutableMap<String, String> = HashMap()
                    url.parameters.entries().forEach {
                        map[it.key] = it.value[0]
                    }

                    val dataString = Json.encodeToString(map)
                    signIfNeeded(headers, isPublicAPI, dataString)
                    body = dataString
                    // Set after options are applied to be sure it doesn't get over-written.
                    headers["User-Agent"] = Version.userAgent
                }
            }

        }.body<T>()

        if (body is Paginatable<*> && body.result?.nextPageCursor?.isNotBlank() == true) {
            val nextUrl = baseUrlBuilder.apply(urlBuilderBlock).apply {
                parameters["cursor"] = body.result!!.nextPageCursor.toString()
            }.buildString()
            body.nextUrl = nextUrl
        }

        return body
    }

    /**
     * Helper function for creating request iterators
     */
    internal inline fun <reified T> requestIteratorCall(
        method: HttpMethod = HttpMethod.Get,
        isPublicAPI: Boolean = true
    ): (String) -> T =
        { url ->
            runBlocking { call({ takeFrom(url) }, method, isPublicAPI) }
        }

}
