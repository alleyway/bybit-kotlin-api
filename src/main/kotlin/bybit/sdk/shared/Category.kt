package bybit.sdk.shared

import bybit.sdk.websocket.ByBitEndpoint
import kotlinx.serialization.Serializable

@Serializable
enum class Category {
    spot,
    linear,
    inverse,
    option
}


fun ByBitEndpoint.toCategory(): Category {
    return when (this) {
        ByBitEndpoint.Spot -> Category.spot
        ByBitEndpoint.Linear -> Category.linear
        ByBitEndpoint.Inverse -> Category.inverse
        ByBitEndpoint.Option -> Category.option
        else -> {
            throw Exception("Cannot convert $this to category")
        }
    }
}

fun Category.toEndPoint(): ByBitEndpoint {
    return when (this) {
        Category.spot -> ByBitEndpoint.Spot
        Category.linear -> ByBitEndpoint.Linear
        Category.inverse -> ByBitEndpoint.Inverse
        Category.option -> ByBitEndpoint.Option
    }
}
