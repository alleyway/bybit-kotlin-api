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


fun fromEndPoint(endpoint: ByBitEndpoint): Category {
    return when (endpoint) {
        ByBitEndpoint.Spot -> Category.spot
        ByBitEndpoint.Linear -> Category.linear
        ByBitEndpoint.Inverse -> Category.inverse
        ByBitEndpoint.Option -> Category.option
        else -> {
            throw Exception("Cannot convert $endpoint to category")
        }
    }
}
