package dev.limebeck.activemqApp

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

fun KClass<*>.logger(): Logger = LoggerFactory.getLogger(this.java)
