package dev.limebeck.activemqApp

import dev.limebeck.koinVerticleFactory.KoinVerticleFactory
import dev.limebeck.koinVerticleFactory.asKoinVerticleName
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main(args: Array<String>) {
    val vertx = Vertx.vertx(
        vertxOptionsOf(
            eventLoopPoolSize = 20,
            preferNativeTransport = true,
            haEnabled = true
        )
    )

    val eb = vertx.eventBus()

    val ebConsumerModule = module {
        factory {
            EventBusConsumerVerticle("test")
        }
    }

    val koinApplication = startKoin {
        modules(ebConsumerModule)
    }

    vertx.registerVerticleFactory(KoinVerticleFactory(koinApplication.koin))
//    vertx.deployVerticle(MainHttpVerticle())
    vertx.deployVerticle(
        EventBusConsumerVerticle::class.java.asKoinVerticleName(),
        deploymentOptionsOf(
            instances = 10
        )
    )

    repeat(300) {
        val counter = vertx.sharedData().getCounter("Timer$it")
        vertx.setPeriodic(2000) { timerId ->
            GlobalScope.launch(vertx.dispatcher()) {
                val messNumber = counter.coAwait().getAndIncrement().coAwait()
                println("<2315ed35> Publish #$messNumber from $timerId")
                eb.publish("test.test", "Test message #$messNumber from $timerId")
            }
        }
    }
}
