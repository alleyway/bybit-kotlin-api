package bybit.sdk.rest.market

import bybit.sdk.ext.coroutineToRestCallback
import bybit.sdk.rest.ByBitRestApiCallback
import bybit.sdk.rest.ByBitRestClient
import bybit.sdk.rest.RequestIterator
import kotlinx.coroutines.runBlocking

class ByBitMarketClient
internal constructor(internal val byBitRestClient: ByBitRestClient) {

    fun getInstrumentsInfoBlocking(params: InstrumentsInfoParams):
            InstrumentsInfoResponse = runBlocking { getInstrumentsInfo(params) }

    /** See [getInstrumentsInfoBlocking] */

    fun getInstrumentsInfo(
        params: InstrumentsInfoParams,
        callback: ByBitRestApiCallback<InstrumentsInfoResponse>
    ) = coroutineToRestCallback(callback, { getInstrumentsInfo(params) })


    /**
     * Get an iterator to iterate through all pages of results for the given parameters.
     *
     * See [getInstrumentsInfo] if you instead need to get exactly one page of results.
     * See section "Pagination" in the README for more details on iterators.
     */

    fun listSupportedInstruments(
        params: InstrumentsInfoParams,
    ): RequestIterator<InstrumentsInfoResultItem> =
        RequestIterator(
            { getInstrumentsInfoBlocking(params) },
            byBitRestClient.requestIteratorCall<InstrumentsInfoResponse>()
        )
}
