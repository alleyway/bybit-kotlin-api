package bybit.sdk.rest.order

import bybit.sdk.ext.coroutineToRestCallback
import bybit.sdk.rest.ByBitRestApiCallback
import bybit.sdk.rest.ByBitRestClient
import bybit.sdk.rest.ByBitRestOption
import kotlinx.coroutines.runBlocking

class ByBitOrderClient
internal constructor(internal val byBitRestClient: ByBitRestClient) {

    @SafeVarargs
    fun placeOrderBlocking(
        params: PlaceOrderParams,
        vararg opts: ByBitRestOption
    ): PlaceOrderResponse =
        runBlocking { placeOrder(params, *opts) }

    /** See [placeOrderBlocking] */
    @SafeVarargs
    fun placeOrder(
        params: PlaceOrderParams,
        callback: ByBitRestApiCallback<PlaceOrderResponse>,
        vararg opts: ByBitRestOption
    ) = coroutineToRestCallback(callback, { placeOrder(params, *opts) })


}
