package bybit.sdk.shared

import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    Created,
    New,
    Rejected,
    PartiallyFilled,
    PartiallyFilledCanceled,
    Filled,
    Cancelled,
    Untriggered,
    Triggered,
    Deactivated,
    Active
}
