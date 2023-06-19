package bybit.sdk.rest.order

sealed class OrderType(val value: String) {
    object Limit : OrderType("Limit")
    object Market : OrderType("Market")
}


sealed class Side(val value: String) {
    object Buy : Side("Buy")
    object Sell : Side("Sell")
}
