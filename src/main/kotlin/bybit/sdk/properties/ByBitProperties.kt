package bybit.sdk.properties


/**
 * [ByBitProperties] defines properties for [ByBitRestClient].
 */
object ByBitProperties {
    private const val BYBIT_PROPERTIES_FILE = "bybit.properties"
    private const val BYBIT_DEFAULT_PROPERTIES_FILE = "bybit.default.properties"

    private const val APIKEY_KEY = "apikey"

    val APIKEY: String? = PropertyUtil.getProperty(
        BYBIT_PROPERTIES_FILE, BYBIT_DEFAULT_PROPERTIES_FILE,
        APIKEY_KEY, "BLAH"
    )
    private const val SECRET_KEY = "secret"

    val SECRET: String? = PropertyUtil.getProperty(
        BYBIT_PROPERTIES_FILE, BYBIT_DEFAULT_PROPERTIES_FILE,
        SECRET_KEY
    )


    private const val TESTNET_KEY = "testnet"

    val TESTNET: Boolean = (PropertyUtil.getProperty(
        BYBIT_PROPERTIES_FILE, BYBIT_DEFAULT_PROPERTIES_FILE,
        TESTNET_KEY
    ) == "true")


    private const val USER_AGENT_KEY = "user_agent"

    val USER_AGENT: String? = PropertyUtil.getProperty(
        BYBIT_PROPERTIES_FILE, BYBIT_DEFAULT_PROPERTIES_FILE,
        USER_AGENT_KEY
    )
}
