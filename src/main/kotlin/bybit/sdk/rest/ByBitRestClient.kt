package bybit.sdk.rest

import bybit.sdk.DefaultCIOHttpClientProvider
import bybit.sdk.DefaultJvmHttpClientProvider
import bybit.sdk.HttpClientProvider
import bybit.sdk.Version
import bybit.sdk.rest.account.ByBitAccountClient
import bybit.sdk.rest.market.ByBitMarketClient
import bybit.sdk.rest.order.ByBitOrderClient
import bybit.sdk.rest.position.ByBitPositionClient
import bybit.sdk.rest.user.ByBitUserClient
import bybit.sdk.shared.sha256_HMAC
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant


/**
 * A client for the ByBit API
 *
 * @param apiKey the API key to use with all API requests
 * @param secret the secret to use with all API requests
 * @param testnet whether to use testnet or not
 * @param httpClientProvider (Optional) A provider for the ktor [HttpClient] to use; defaults to [DefaultJvmHttpClientProvider]
 */
class ByBitRestClient
@JvmOverloads
constructor(
    val apiKey: String?,
    val secret: String?,
    val testnet: Boolean = false,
    val httpClientProvider: HttpClientProvider = DefaultCIOHttpClientProvider()
) {

    val accountClient by lazy { ByBitAccountClient(this) }

    val marketClient by lazy { ByBitMarketClient(this) }

    val orderClient by lazy { ByBitOrderClient(this) }

    val userClient by lazy { ByBitUserClient(this) }

    val positionClient by lazy { ByBitPositionClient(this) }

    private var bybitApiDomain: String

    init {

        if (apiKey == null || secret == null) {
            throw Exception("You must specify ByBit apikey/secret!")
        }

        bybitApiDomain = if (testnet) {
            "api-testnet.bybit.com"
        } else {
            "api.bybit.com"
        }
    }

    private val baseUrlBuilder: URLBuilder
        get() = httpClientProvider.getDefaultRestURLBuilder().apply {
            host = bybitApiDomain
            if (!apiKey.isNullOrEmpty()) parameters["apiKey"] = apiKey
        }

    private inline fun <R> withHttpClient(codeBlock: (client: HttpClient) -> R) =
        httpClientProvider.buildClient().use(codeBlock)




//    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
//    private fun genGetSign(params: Map<String, Any>): String? {
//        val sb: java.lang.StringBuilder = genQueryStr(params)
//        val queryStr: String = TIMESTAMP + API_KEY + RECV_WINDOW + sb
//        val sha256_HMAC = Mac.getInstance("HmacSHA256")
//        val secret_key = SecretKeySpec(API_SECRET.getBytes(), "HmacSHA256")
//        sha256_HMAC.init(secret_key)
//        return bytesToHex(sha256_HMAC.doFinal(queryStr.toByteArray()))
//    }
//
//    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
//    private fun genPostSign(params: Map<String, Any>): String? {
//        val sha256_HMAC = Mac.getInstance("HmacSHA256")
//        val secret_key = SecretKeySpec(secret?.toByteArray(), "HmacSHA256")
//        sha256_HMAC.init(secret_key)
//        val paramJson: String = JSON.toJSONString(params)
//        val sb: String = (TIMESTAMP + API_KEY + RECV_WINDOW).toString() + paramJson
//        return bytesToHex(sha256_HMAC.doFinal(sb.toByteArray()))
//    }


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

            val signature =  sha256_HMAC(toEncode, secret)

            headers["X-BAPI-SIGN"] = signature
            headers["X-BAPI-API-KEY"] = key
            headers["X-BAPI-TIMESTAMP"] = timestamp.toString()
            headers["X-BAPI-RECV-WINDOW"] = recvWindow.toString()
        }
    }

    @OptIn(InternalAPI::class)
    internal suspend inline fun <reified T> call(
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

//    internal suspend inline fun <reified T> fetchResult(
//        urlBuilderBlock: URLBuilder.() -> Unit,
//        vararg options: ByBitRestOption
//    ): T {
//        val url = baseUrlBuilder.apply(urlBuilderBlock).build()
//        val body = withHttpClient { httpClient ->
//            httpClient.get(url) {
//                options.forEach { this.it() }
//
//                // Set after options are applied to be sure it doesn't get over-written.
//                headers["User-Agent"] = Version.userAgent
//            }
//        }.body<T>()
//
//        if (body is Paginatable<*> && body.result?.nextPageCursor?.isNotBlank() == true) {
//            val nextUrl = baseUrlBuilder.apply(urlBuilderBlock).apply {
//                parameters["cursor"] = body.result!!.nextPageCursor.toString()
//            }.buildString()
//            body.nextUrl = nextUrl
//        }
//
//        return body
//    }

//
//    /**
//     * Helper function for creating request iterators
//     */
//    internal inline fun <reified T> requestIteratorFetch(vararg opts: ByBitRestOption): (String) -> T =
//        { url -> runBlocking { fetchResult({ takeFrom(url) }, *opts) } }
//

    /**
     * Helper function for creating request iterators
     */
    internal inline fun <reified T> requestIteratorCall(
        method: HttpMethod = HttpMethod.Get,
        isPublicAPI: Boolean = true
    ): (String) -> T =
        {
            url -> runBlocking { call({ takeFrom(url) }, method, isPublicAPI) }
        }

}
