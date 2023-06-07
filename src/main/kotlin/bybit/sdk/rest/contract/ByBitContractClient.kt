package bybit.sdk.rest.contract

import bybit.sdk.ext.coroutineToRestCallback
import bybit.sdk.rest.ByBitRestApiCallback
import bybit.sdk.rest.ByBitRestClient
import bybit.sdk.rest.ByBitRestOption
import bybit.sdk.rest.*
import kotlinx.coroutines.runBlocking

class ByBitContractClient
internal constructor(internal val bybitClient: ByBitRestClient) {

    @SafeVarargs
    fun getServerTimeBlocking(
        vararg opts: ByBitRestOption
    ): ServerTimeDTO =
        runBlocking { getServerTime(*opts) }

    /** See [getServerTimeBlocking] */
    @SafeVarargs
	fun getServerTime(
		callback: ByBitRestApiCallback<ServerTimeDTO>,
		vararg opts: ByBitRestOption
	) = coroutineToRestCallback(callback, { getServerTime( *opts) })

}
