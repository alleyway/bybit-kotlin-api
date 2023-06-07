package bybit.sdk.rest

import bybit.sdk.DefaultOkHttpClientProvider
import bybit.sdk.HttpClientProvider
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody

public val okHttpClientProvider: HttpClientProvider
	get() = DefaultOkHttpClientProvider(
		applicationInterceptors = listOf(object : Interceptor {
			override fun intercept(chain: Interceptor.Chain): Response {
				println("Intercepting application level")
				println("request: ${chain.request().url}")
				var response = chain.proceed(chain.request())

				val content: String? = response.body?.string()

				if (content !== null) {
					val wrappedBody: ResponseBody = ResponseBody.create(response.body?.contentType(), content)
					response = response.newBuilder().body(wrappedBody).build()
					println(content)
				}



				return response
			}
		}),
		networkInterceptors = listOf(object : Interceptor {
			override fun intercept(chain: Interceptor.Chain): Response {
				println("Intercepting network level")
				return chain.proceed(chain.request())
			}
		})
	)
