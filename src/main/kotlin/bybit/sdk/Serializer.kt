package bybit.sdk

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer

@OptIn(InternalSerializationApi::class)
fun Any?.toJsonElement(): JsonElement =
    when (this) {
        null -> JsonNull
        is Map<*, *> -> toJsonElement()
        is Collection<*> -> toJsonElement()
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        is Enum<*> -> JsonPrimitive(this.toString())
        else -> this.javaClass.kotlin.serializer().let { Json.encodeToJsonElement(it, this) }
    }

private fun Collection<*>.toJsonElement(): JsonElement =
    JsonArray(this.map { it.toJsonElement() })

private fun Map<String, Any?>.toJsonElement(): JsonElement {
    return JsonObject(this.mapValues { it.value.toJsonElement() })
}
