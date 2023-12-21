package bybit.sdk

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor

@Serializable
data class Error(val retCode: Int, val retMsg: String)

class CustomResponseException(
    response: HttpResponse, cachedResponseText: String,
    val retCode: Int? = null,
    val retMsg: String? = null
) :
    ResponseException(response, cachedResponseText) {
    override val message: String = "Custom server error: ${response.call.request.url}. " +
            "HTTP Status: ${response.status} Text: \"$cachedResponseText\""
}


interface HttpClientProvider {
    fun buildClient(): HttpClient

    fun getDefaultRestURLBuilder(): URLBuilder
}

/**
 * A default [HttpClientProvider] which provides an [HttpClient] powered by the [OkHttp] engine.
 *
 * For more details on the interceptors and the difference between an application interceptor and a network interceptor,
 * see OkHttp's documentation: https://square.github.io/okhttp/interceptors/
 */
open class DefaultOkHttpClientProvider
@JvmOverloads
constructor(
    private val applicationInterceptors: List<Interceptor> = emptyList(),
    private val networkInterceptors: List<Interceptor> = emptyList()
) : DefaultJvmHttpClientProvider() {

    override fun buildEngine() =
        OkHttp.create {
            applicationInterceptors.forEach(::addInterceptor)
            networkInterceptors.forEach(::addNetworkInterceptor)
        }
}

/**
 * A default [HttpClientProvider] which provides an [HttpClient] powered by a configurable [HttpClientEngine] engine.
 * For a list of possible engines, see https://ktor.io/clients/http-client/engines.html
 *
 * Engine defaults to [OkHttp]
 *
 * @see DefaultOkHttpClientProvider
 */
open class DefaultJvmHttpClientProvider
@JvmOverloads
constructor(
    private val engine: HttpClientEngine = OkHttp.create()
) : HttpClientProvider {

    private val logger = Logging.getLogger(DefaultJvmHttpClientProvider::class)

    open fun buildEngine(): HttpClientEngine = engine

    override fun buildClient() =
        HttpClient(buildEngine()) {
            install(WebSockets) {
                pingInterval = 5_000
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 5000
                socketTimeoutMillis = 5000

            }
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            HttpResponseValidator {
                validateResponse { response ->

                    if (response.request.url.protocol == URLProtocol.HTTPS && !response.headers.get("ret_code")
                            .equals("0")
                    ) {
                        if (response.status.value != 200) {
                            logger.warn("HTTP error: ${response.status.toString()} ${response.status.description}")

                            logger.warn(response.bodyAsText())

                        } else {
                            val error: Error = response.body()
                            if (error.retCode != 0) {
                                throw CustomResponseException(
                                    response,
                                    "Code: ${error.retCode}, message: ${error.retMsg}"
                                )
                            }
                        }

                    }

                }
            }
        }

    override fun getDefaultRestURLBuilder() =
        URLBuilder(
            protocol = URLProtocol.HTTPS,
            port = DEFAULT_PORT
        )
}


open class DefaultCIOHttpClientProvider
constructor() : HttpClientProvider {

    private val logger = Logging.getLogger(DefaultCIOHttpClientProvider::class)

    override fun buildClient() =
        HttpClient(CIO) {
            engine {
//                config {
//                    followRedirects(true)
//                }
                // options for CIO engine
                    maxConnectionsCount = 1000
                    endpoint {
                        maxConnectionsPerRoute = 100
                        pipelineMaxSize = 20
                        keepAliveTime = 5000
                        connectTimeout = 5000
                        connectAttempts = 5
                    }
            }
            // could not get compression to work, fails silently
//            install(ContentEncoding) {
//                deflate(1.0F)
//                gzip(0.9F)
//            }
            install(HttpTimeout) {
                connectTimeoutMillis = 5000
                socketTimeoutMillis = 5000

            }
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            HttpResponseValidator {
                validateResponse { response ->

                    if (response.request.url.protocol == URLProtocol.HTTPS && !response.headers.get("ret_code")
                            .equals("0")
                    ) {
                        if (response.status.value != 200) {
                            logger.warn("HTTP error: ${response.status.toString()} ${response.status.description}")

                            logger.warn(response.bodyAsText())

                        } else {
                            val error: Error = response.body()
                            if (error.retCode != 0) {
                                throw CustomResponseException(
                                    response,
                                    "Code: ${error.retCode}, message: ${error.retMsg}",
                                    retCode = error.retCode,
                                    retMsg = error.retMsg
                                )
                            }
                        }

                    }

                }
            }
        }

    override fun getDefaultRestURLBuilder() =
        URLBuilder(
            protocol = URLProtocol.HTTPS,
            port = DEFAULT_PORT
        )
}


open class DefaultCIOWebSocketClientProvider
constructor() : HttpClientProvider {

    private val logger = Logging.getLogger(DefaultCIOWebSocketClientProvider::class)

    override fun buildClient() =
        HttpClient(OkHttp) {
            engine {
                config {
                    followRedirects(true)
                }
            }            // options for CIO:
//            engine {
//                maxConnectionsCount = 1000
//                endpoint {
//                    maxConnectionsPerRoute = 100
//                    pipelineMaxSize = 20
//                    keepAliveTime = 5000
//                    connectTimeout = 5000
//                    connectAttempts = 5
//                }
//            }
            // could not get compression to work, fails silently
//            install(ContentEncoding) {
//                deflate(1.0F)
//                gzip(0.9F)
//            }
            install(HttpTimeout) {
                connectTimeoutMillis = 1000
                socketTimeoutMillis = 1000

            }
            install(WebSockets) {
                pingInterval = -1L
//                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            HttpResponseValidator {
                validateResponse { response ->

                    if (response.request.url.protocol == URLProtocol.HTTPS && !response.headers.get("ret_code")
                            .equals("0")
                    ) {
                        if (response.status.value != 200) {
                            logger.warn("HTTP error: ${response.status.toString()} ${response.status.description}")

                            logger.warn(response.bodyAsText())

                        } else {
                            val error: Error = response.body()
                            if (error.retCode != 0) {
                                throw CustomResponseException(
                                    response,
                                    "Code: ${error.retCode}, message: ${error.retMsg}"
                                )
                            }
                        }

                    }

                }
            }
        }

    override fun getDefaultRestURLBuilder() =
        URLBuilder(
            protocol = URLProtocol.HTTPS,
            port = DEFAULT_PORT
        )
}
