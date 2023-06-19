package bybit.sdk.rest

import bybit.sdk.DefaultJvmHttpClientProvider
import bybit.sdk.HttpClientProvider
import bybit.sdk.Version
import bybit.sdk.rest.contract.ByBitContractClient
import bybit.sdk.rest.market.ByBitMarketClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

/**
 * A client for the ByBit API
 *
 * @param apiKey the API key to use with all API requests
 * @param secret the secret to use with all API requests
 * @param httpClientProvider (Optional) A provider for the ktor [HttpClient] to use; defaults to [DefaultJvmHttpClientProvider]
 * @param bybitApiDomain (Optional) The domain to hit for all API requests; defaults to ByBit's API domain "api-testnet.bybit.com". Useful for overriding in a testing environment
 */
class ByBitRestClient
@JvmOverloads
constructor(
	private val apiKey: String,
	private val secret: String,
    testnet: Boolean,
    private val httpClientProvider: HttpClientProvider = DefaultJvmHttpClientProvider()
) {

	val contractClient by lazy { ByBitContractClient(this) }

    val marketClient by lazy { ByBitMarketClient(this) }

    private var bybitApiDomain: String

    init {
        bybitApiDomain = if (testnet) {
            "api-testnet.bybit.com"
        } else {
            "api.bybit.com"
        }
    }

    private val baseUrlBuilder: URLBuilder
        get() = httpClientProvider.getDefaultRestURLBuilder().apply {
            host = bybitApiDomain
            parameters["apiKey"] = apiKey
        }

    private inline fun <R> withHttpClient(codeBlock: (client: HttpClient) -> R) =
        httpClientProvider.buildClient().use(codeBlock)

    internal suspend inline fun <reified T> fetchResult(
        urlBuilderBlock: URLBuilder.() -> Unit,
        vararg options: ByBitRestOption
    ): T {
        val url = baseUrlBuilder.apply(urlBuilderBlock).build()
        val body = withHttpClient { httpClient ->
            httpClient.get(url) {
                options.forEach { this.it() }

                // Set after options are applied to be sure it doesn't get over-written.
                headers["User-Agent"] = Version.userAgent
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
    internal inline fun <reified T> requestIteratorFetch(vararg opts: ByBitRestOption): (String) -> T =
        { url -> runBlocking { fetchResult({ takeFrom(url) }, *opts) } }

}
