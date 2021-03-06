package de.ng.ahnenwin2gedcom

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.logging.LogManager

internal fun initLoggerWithLoggingPropertiesFile() {
    val classLoader = Thread.currentThread().contextClassLoader
    val props = classLoader.getResourceAsStream("logging.properties")
    LogManager.getLogManager().readConfiguration(props)
}

fun <T : Any> T.logger(): Logger {
    return LoggerFactory.getLogger(this::class.simpleName)
}

fun <T : Any> T.log(e: Exception) {
    logger().error(e.message)
    logger().debug(e.stackTrace?.toString())
}
