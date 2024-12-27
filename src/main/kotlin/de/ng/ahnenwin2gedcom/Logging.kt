package de.ng.ahnenwin2gedcom

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.logging.LogManager

internal fun initLoggerWithLoggingPropertiesFile() {
    val classLoader = Thread.currentThread().contextClassLoader
    val props = classLoader.getResourceAsStream("logging.properties")
    LogManager.getLogManager().readConfiguration(props)
}

fun logger(moduleName: String): Logger {
    return LoggerFactory.getLogger(moduleName)
}

fun <T : Any> T.logger(): Logger {
    return LoggerFactory.getLogger(this::class.simpleName)
}

