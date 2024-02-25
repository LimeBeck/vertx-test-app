package dev.limebeck.kodein

import io.vertx.core.Promise
import io.vertx.core.Verticle
import io.vertx.core.spi.VerticleFactory
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.type.JVMTypeToken
import org.kodein.type.typeToken
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable

class KodeinVerticleFactory(
    val di: DI
) : VerticleFactory {
    companion object {
        const val PREFIX = "kodein"
        private val logger = LoggerFactory.getLogger(KodeinVerticleFactory::class.java)
    }

    override fun prefix() = PREFIX

    override fun createVerticle(
        verticleName: String,
        classLoader: ClassLoader,
        promise: Promise<Callable<Verticle>>
    ) {
        logger.info("<381f8022> Load $verticleName")
        val className = VerticleFactory.removePrefix(verticleName)
        val clazz = classLoader.loadClass(className) ?: Class.forName(className)
        val type = typeToken(clazz) as JVMTypeToken<Verticle>

        promise.complete {
            di.direct.Instance(type, null)
        }
    }
}

fun <T : Verticle> Class<T>.asKodeinVerticleName() = "${KodeinVerticleFactory.PREFIX}:$canonicalName"
