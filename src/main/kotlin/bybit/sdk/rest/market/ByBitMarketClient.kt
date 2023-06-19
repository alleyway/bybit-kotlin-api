package bybit.sdk.rest.market

import bybit.sdk.ext.coroutineToRestCallback
import bybit.sdk.rest.ByBitRestApiCallback
import bybit.sdk.rest.ByBitRestClient
import bybit.sdk.rest.RequestIterator
import kotlinx.coroutines.runBlocking

class ByBitMarketClient
internal constructor(internal val byBitRestClient: ByBitRestClient) {

    @SafeVarargs
    fun getInstrumentsInfoBlocking(params: GetInstrumentsInfoParamsV5):
            TickersDTO = runBlocking { getInstrumentsInfo(params) }

    /** See [getInstrumentsInfoBlocking] */
    @SafeVarargs
    fun getInstrumentsInfo(
        params: GetInstrumentsInfoParamsV5,
        callback: ByBitRestApiCallback<TickersDTO>
    ) = coroutineToRestCallback(callback, { getInstrumentsInfo(params) })


    /**
     * Get an iterator to iterate through all pages of results for the given parameters.
     *
     * See [getInstrumentsInfo] if you instead need to get exactly one page of results.
     * See section "Pagination" in the README for more details on iterators.
     */

    @SafeVarargs
    fun listSupportedInstruments(
        params: GetInstrumentsInfoParamsV5,
    ): RequestIterator<TickerDTO> =
        RequestIterator(
            { getInstrumentsInfoBlocking(params) },
            byBitRestClient.requestIteratorCall<TickersDTO>()
        )
}
