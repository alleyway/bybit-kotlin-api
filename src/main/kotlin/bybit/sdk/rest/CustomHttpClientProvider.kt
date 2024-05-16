package bybit.sdk.rest

import bybit.sdk.DefaultOkHttpClientProvider
import bybit.sdk.HttpClientProvider
import bybit.sdk.Logging
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody

val okHttpClientProvider: HttpClientProvider
    get() = DefaultOkHttpClientProvider(
        applicationInterceptors = listOf(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val logger = Logging.getLogger("bybit.sdk.okHttpClientProvider")
                logger.debug("Intercepting application level")
                logger.debug("request: ${chain.request().url}")
                var response = chain.proceed(chain.request())

                val content: String? = response.body?.string()

                if (content !== null) {
                    val wrappedBody: ResponseBody = content.toResponseBody(response.body?.contentType())
                    response = response.newBuilder().body(wrappedBody).build()
                    logger.debug(content)
                }

                return response
            }
        }),
        networkInterceptors = listOf(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                Logging.getLogger("bybit.sdk.okHttpClientProvider").debug("Intercepting network level")
                return chain.proceed(chain.request())
            }
        })
    )
