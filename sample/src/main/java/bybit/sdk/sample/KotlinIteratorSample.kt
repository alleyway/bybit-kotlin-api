package bybit.sdk.sample

import bybit.sdk.rest.ByBitRestClient

//fun iteratorExample(bybitClient: ByBitRestClient) {
//    println("10 Supported Tickers:")
//    val params = SupportedTickersParameters(
//        sortBy = "ticker",
//        sortDescending = false,
//        market = "stocks",
//        limit = 3
//    )
//
//    // Take only the first 10 so this sample ends quickly.
//    // Note that since the limit = 3 in the params,
//    // this iterator will make 4 requests under the hood
//    bybitClient.referenceClient.listSupportedTickers(params).asSequence()
//        .take(10)
//        .forEachIndexed { index, tickerDTO -> println("${index}: ${tickerDTO.ticker}") }
//}
//
//fun tradesIteratorExample(bybitClient: ByBitRestClient) {
//    println("Running trade iterator:")
//    val params = TradesParameters(limit = 1)
//
//    bybitClient.listTrades("F", params).asSequence()
//        .take(2)
//        .forEachIndexed { index, tradeRes -> println("${index}: ${tradeRes.price}") }
//}
//
//fun quotesIteratorExample(bybitClient: ByBitRestClient) {
//    println("Running quote iterator:")
//    val params = QuotesParameters(limit = 1)
//
//    bybitClient.listQuotes("F", params).asSequence()
//        .take(2)
//        .forEachIndexed { index, quoteRes -> println("${index}: (${quoteRes.participantTimestamp}) | ${quoteRes.bidPrice} / ${quoteRes.askPrice}") }
//}
