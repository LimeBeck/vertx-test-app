package dev.limebeck.koinVerticleFactory

import io.vertx.core.Promise
import io.vertx.core.Verticle
import io.vertx.core.spi.VerticleFactory
import org.koin.core.Koin
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import kotlin.jvm.internal.Reflection

class KoinVerticleFactory(
    val koin: Koin
) : VerticleFactory {
    companion object {
        const val PREFIX = "koin"
        val logger = LoggerFactory.getLogger(KoinVerticleFactory::class.java)
    }

    override fun prefix(): String = PREFIX

    override fun createVerticle(
        verticleName: String,
        classLoader: ClassLoader,
        promise: Promise<Callable<Verticle>>
    ) {
        logger.info("<6f8620a9> Load $verticleName")
        val className = VerticleFactory.removePrefix(verticleName)
        val clazz = classLoader.loadClass(className) ?: Class.forName(className)
        val kotlinClass = Reflection.createKotlinClass(clazz)

        runCatching {
            koin.get<Verticle>(kotlinClass, null)
        }.onSuccess {
            promise.complete { it }
        }.onFailure {
            logger.error("<57233541> Can`t load $verticleName", it)
            promise.fail(it)
        }
    }
}

fun <T : Verticle> Class<T>.asKoinVerticleName() = "${KoinVerticleFactory.PREFIX}:$canonicalName"
