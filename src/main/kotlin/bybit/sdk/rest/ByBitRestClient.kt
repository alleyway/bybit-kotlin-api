package bybit.sdk.rest

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.*
import bybit.sdk.DefaultJvmHttpClientProvider
import bybit.sdk.HttpClientProvider
import bybit.sdk.Version
import bybit.sdk.rest.contract.ByBitContractClient
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
	private val httpClientProvider: HttpClientProvider = DefaultJvmHttpClientProvider(),
	private val bybitApiDomain: String = "api-testnet.bybit.com"
) {


	val contractClient by lazy { ByBitContractClient(this) }


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
        return withHttpClient { httpClient ->
            httpClient.get(url) {
                options.forEach { this.it() }

                // Set after options are applied to be sure it doesn't get over-written.
                headers["User-Agent"] = Version.userAgent
            }
        }.body()
    }

    /**
     * Helper function for creating request iterators
     */
    internal inline fun <reified T> requestIteratorFetch(vararg opts: ByBitRestOption): (String) -> T =
        { url -> runBlocking { fetchResult({ takeFrom(url) }, *opts) } }

}
