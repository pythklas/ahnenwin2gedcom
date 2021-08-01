package de.ng.ahnenwin2gedcom

import java.util.*

object ApplicationProperties {

    lateinit var version: String

    init {
        load()
    }

    private fun load() {
        val properties = Properties()
        val applicationPropertiesInputStream = javaClass
            .classLoader
            .getResourceAsStream("application.properties")
        properties.load(applicationPropertiesInputStream)
        version = properties.getProperty("version")
    }
}
