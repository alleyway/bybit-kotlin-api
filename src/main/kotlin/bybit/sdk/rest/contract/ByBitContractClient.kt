package bybit.sdk.rest.contract

import bybit.sdk.ext.coroutineToRestCallback
import bybit.sdk.rest.ByBitRestApiCallback
import bybit.sdk.rest.ByBitRestClient
import kotlinx.coroutines.runBlocking

class ByBitContractClient
internal constructor(internal val byBitRestClient: ByBitRestClient) {

    @SafeVarargs
    fun getServerTimeBlocking(): ServerTimeDTO =
        runBlocking { getServerTime() }

    /** See [getServerTimeBlocking] */
    @SafeVarargs
	fun getServerTime(
		callback: ByBitRestApiCallback<ServerTimeDTO>
	) = coroutineToRestCallback(callback, { getServerTime() })

}
