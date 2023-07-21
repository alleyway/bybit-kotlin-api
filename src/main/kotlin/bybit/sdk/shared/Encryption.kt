package bybit.sdk.shared

import bybit.sdk.Logging
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


fun bytesToHex(hash: ByteArray): String {
    val hexString = StringBuilder()
    for (b in hash) {
        val hex = Integer.toHexString(0xff and b.toInt())
        if (hex.length == 1) hexString.append('0')
        hexString.append(hex)
    }
    return hexString.toString()
}


fun sha256_HMAC(message: String, secret: String?): String {
    var hash = "invalid"
    try {
        val sha256_HMAC = Mac.getInstance("HmacSHA256")
        val secret_key = SecretKeySpec(secret?.toByteArray(), "HmacSHA256")
        sha256_HMAC.init(secret_key)
        val bytes = sha256_HMAC.doFinal(message.toByteArray())
        hash = bytesToHex(bytes)
    } catch (e: Exception) {
        Logging.getLogger("sha256_HMAC").error { e.message }
    }
    return hash
}
