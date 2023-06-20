package bybit.sdk.shared

import kotlinx.serialization.Serializable

@Serializable
enum class Category {
    spot,
    linear,
    inverse,
    option
}
