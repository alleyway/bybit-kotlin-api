package bybit.sdk.rest.market

import bybit.sdk.ext.coroutineToRestCallback
import bybit.sdk.rest.ByBitRestApiCallback
import bybit.sdk.rest.ByBitRestClient
import bybit.sdk.rest.RequestIterator
import kotlinx.coroutines.runBlocking

class ByBitMarketClient
internal constructor(internal val byBitRestClient: ByBitRestClient) {

    fun getInstrumentsInfoBlocking(params: InstrumentsInfoParams):
            InstrumentsInfoResponse<InstrumentsInfoResultItem> = runBlocking { getInstrumentsInfo(params) }

    /** See [getInstrumentsInfoBlocking] */

    fun getInstrumentsInfo(
        params: InstrumentsInfoParams,
        callback: ByBitRestApiCallback<InstrumentsInfoResponse<InstrumentsInfoResultItem>>
    ) = coroutineToRestCallback(callback, { getInstrumentsInfo(params) })


    fun getPublicTradingHistoryBlocking(params: PublicTradingHistoryParams):
            PublicTradingHistoryResponse = runBlocking { getPublicTradingHistory(params) }

    /** See [getPublicTradingHistoryBlocking] */

    fun getPublicTradingHistory(
        params: PublicTradingHistoryParams,
        callback: ByBitRestApiCallback<PublicTradingHistoryResponse>
    ) = coroutineToRestCallback(callback, { getPublicTradingHistory(params) })


    fun listSupportedInstruments(
        params: InstrumentsInfoParams,
    ): RequestIterator<InstrumentsInfoResultItem> =
        RequestIterator(
            { getInstrumentsInfoBlocking(params) },
            byBitRestClient.requestIteratorCall<InstrumentsInfoResponse<InstrumentsInfoResultItem>>()
        )
}
