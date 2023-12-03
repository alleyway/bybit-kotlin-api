package bybit.sdk.shared

import kotlinx.serialization.Serializable

@Serializable
enum class PositionStatus {
    Normal,
    Liq,
    Adl
}
