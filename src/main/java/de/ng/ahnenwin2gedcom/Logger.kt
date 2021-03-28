package de.ng.ahnenwin2gedcom

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.logging.LogManager

fun <T : Any> T.logger(): Logger {
    return LoggerFactory.getLogger(this::class.simpleName)
}

fun <T : Any> T.log(e: Exception) {
    logger().error(e.message)
    logger().debug(e.stackTrace?.toString())
}

object LoggingInitializer {
    init {
        initLoggerWithLoggingPropertiesFile()
    }

    fun initLoggerWithLoggingPropertiesFile() {
        try {
            val classLoader = Thread.currentThread().contextClassLoader
            val props = classLoader.getResourceAsStream("logging.properties")
            LogManager.getLogManager().readConfiguration(props)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
