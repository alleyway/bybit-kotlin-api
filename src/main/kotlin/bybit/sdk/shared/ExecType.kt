package bybit.sdk.shared

import kotlinx.serialization.Serializable

@Serializable
enum class ExecType {
    Trade,
    AdlTrade, // auto-deleveraging
    Funding, // funding fee
    BustTrade, // Liquidation
    Delivery, // USDC futures delivery
    BlockTrade
}
