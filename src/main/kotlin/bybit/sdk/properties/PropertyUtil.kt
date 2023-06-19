package bybit.sdk.properties

import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*


/**
 * [PropertyUtil] is a util class for all things [Properties].
 */
object PropertyUtil {
    private val LOGGER = LoggerFactory.getLogger(PropertyUtil::class.java)
    val CACHED_PROPERTIES = Collections.synchronizedMap(HashMap<String, Properties?>())

    /**
     * Gets a string property from a property file. Will try to IO load the properties in the property file if not
     * cached already.
     *
     * @param propertyFile the property file
     * @param key          the key
     *
     * @return the string
     */
    fun getProperty(propertyFile: String, key: String?): String? {
        return getProperty(propertyFile, null, key, null)
    }

    /**
     * Gets a string property from a property file. Will try to IO load the properties in the property file if not
     * cached already. If the desired property is not found in the `propertyFile`, then the
     * `defaultPropertyFile` is searched.
     *
     * @param propertyFile        the property file
     * @param defaultPropertyFile the default property file
     * @param key                 the key
     *
     * @return the string
     */
    fun getProperty(propertyFile: String, defaultPropertyFile: String?, key: String?): String? {
        return getProperty(propertyFile, defaultPropertyFile, key, null)
    }

    /**
     * Gets a string property from a property file. Will try to IO load the properties in the property file if not
     * cached already. If the desired property is not found in the `propertyFile`, then the
     * `defaultPropertyFile` is searched, and if it's not there, then `defaultValue` is returned.
     *
     * @param propertyFile        the property file
     * @param defaultPropertyFile the default property file
     * @param key                 the key
     * @param defaultValue        the default value (if the desired property wasn't found, then this is returned)
     *
     * @return the string
     */
    fun getProperty(propertyFile: String, defaultPropertyFile: String?, key: String?, defaultValue: String?): String? {
        val properties: Properties?
        if (!CACHED_PROPERTIES.containsKey(propertyFile)) {
            properties = loadPropertyFile(propertyFile, defaultPropertyFile)
            CACHED_PROPERTIES[propertyFile] = properties
        } else {
            properties = CACHED_PROPERTIES[propertyFile]
        }
        return if (properties == null) defaultValue else properties.getProperty(key, defaultValue)
    }

    /**
     * Loads property file [Properties].
     *
     * @param propertyFile        the property file name
     * @param defaultPropertyFile the default property file name
     *
     * @return the properties
     */
    @Synchronized
    fun loadPropertyFile(propertyFile: String?, defaultPropertyFile: String?): Properties? {
        var classLoader = PropertyUtil::class.java.classLoader
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader()
        }

        // Load the default property file if exists
        var defaultProperties: Properties? = null
        val defaultPropertyStream = classLoader!!.getResourceAsStream(defaultPropertyFile)
        if (defaultPropertyStream != null) {
            defaultProperties = Properties()

            // Load the properties
            try {
                defaultProperties.load(defaultPropertyStream)
                LOGGER.debug("Loaded default properties file: {}", defaultPropertyFile)
            } catch (exception: IOException) {
                LOGGER.error("Could not load default property file: {}\n{}", defaultPropertyFile, exception)
            }

            // Close the InputStream
            try {
                defaultPropertyStream.close()
            } catch (exception: IOException) {
                LOGGER.error("Could not close default property file stream: {}\n{}", defaultPropertyFile, exception)
            }
        } else {
            LOGGER.warn("No default property file found for: {}", propertyFile)
        }

        // Load the property file
        var properties: Properties? = null
        val propertyStream = classLoader.getResourceAsStream(propertyFile)
        if (propertyStream != null) {
            // Add default properties if they were found
            properties = if (defaultProperties == null) Properties() else Properties(defaultProperties)

            // Load the properties
            try {
                properties.load(propertyStream)
                LOGGER.info("Loaded properties file: {}", propertyFile)
            } catch (exception: IOException) {
                LOGGER.error("Could not load property file: {}\n{}", propertyFile, exception)
            }

            // Close the InputStream
            try {
                propertyStream.close()
            } catch (exception: IOException) {
                LOGGER.error("Could not close property file stream: {}\n{}", propertyFile, exception)
            }
        } else {
            LOGGER.debug("Could not find property file: {}", propertyFile)
            if (defaultProperties != null) {
                LOGGER.info("Using default properties for: {}", propertyFile)
                properties = defaultProperties
            }
        }
        return properties
    }
}
