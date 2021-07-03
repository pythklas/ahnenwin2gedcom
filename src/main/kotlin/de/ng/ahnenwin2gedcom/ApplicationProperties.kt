package de.ng.ahnenwin2gedcom

import java.io.IOException
import java.util.*

object ApplicationProperties {

    @JvmStatic
    lateinit var version: String
        private set

    init {
        try {
            load()
        } catch (e: IOException) {
            log(e)
        }
    }

    @Throws(IOException::class)
    private fun load() {
        val properties = Properties()
        val applicationPropertiesInputStream = ApplicationProperties::class.java
            .classLoader
            .getResourceAsStream("application.properties")
        properties.load(applicationPropertiesInputStream)
        version = properties.getProperty("version")
    }
}
