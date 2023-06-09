package bybit.sdk.rest.market

import bybit.sdk.ext.coroutineToRestCallback
import bybit.sdk.rest.ByBitRestApiCallback
import bybit.sdk.rest.ByBitRestClient
import bybit.sdk.rest.ByBitRestOption
import bybit.sdk.rest.RequestIterator
import kotlinx.coroutines.runBlocking

class ByBitMarketClient
internal constructor(internal val byBitRestClient: ByBitRestClient) {

    @SafeVarargs
    fun getInstrumentsInfoBlocking(
        params: GetInstrumentsInfoParamsV5,
        vararg opts: ByBitRestOption
    ): TickersDTO =
        runBlocking { getInstrumentsInfo(params, *opts) }

    /** See [getInstrumentsInfoBlocking] */
    @SafeVarargs
    fun getInstrumentsInfo(
        params: GetInstrumentsInfoParamsV5,
        callback: ByBitRestApiCallback<TickersDTO>,
        vararg opts: ByBitRestOption
    ) = coroutineToRestCallback(callback, { getInstrumentsInfo(params, *opts) })


    /**
     * Get an iterator to iterate through all pages of results for the given parameters.
     *
     * See [getInstrumentsInfo] if you instead need to get exactly one page of results.
     * See section "Pagination" in the README for more details on iterators.
     */

    @SafeVarargs
    fun listSupportedInstruments(
        params: GetInstrumentsInfoParamsV5,
        vararg opts: ByBitRestOption
    ): RequestIterator<TickerDTO> =
        RequestIterator(
            { getInstrumentsInfoBlocking(params, *opts) },
            byBitRestClient.requestIteratorFetch<TickersDTO>()
        )
}
