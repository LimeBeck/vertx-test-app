package dev.limebeck.activemqApp

import com.fasterxml.jackson.module.kotlin.readValue
import dev.limebeck.kodein.KodeinVerticleFactory
import dev.limebeck.kodein.asKodeinVerticleName
import dev.limebeck.koinVerticleFactory.KoinVerticleFactory
import dev.limebeck.koinVerticleFactory.asKoinVerticleName
import io.vertx.core.ThreadingModel
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.bindFactory
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.time.Duration
import java.time.Instant

class MainClass

val logger = MainClass::class.logger()

fun main(args: Array<String>) {
    val vertx = Vertx.vertx(
        vertxOptionsOf(
            preferNativeTransport = true,
            haEnabled = true
        )
    )

    val eb = vertx.eventBus()

    val di = DI {
        val counter = vertx.sharedData().getCounter("KodeinModule")
        bindFactory<Unit, EventBusConsumerVerticle> {
            val verticleNumber = runBlocking { counter.coAwait().getAndIncrement().coAwait() }
            EventBusConsumerVerticle("Kodein:$verticleNumber")
        }
    }

    val ebConsumerModule = module {
        val counter = vertx.sharedData().getCounter("KoinModule")
        factory {
            val verticleNumber = runBlocking { counter.coAwait().getAndIncrement().coAwait() }
            EventBusConsumerVerticle("Koin:$verticleNumber")
        }
    }

    val koinApplication = startKoin {
        modules(ebConsumerModule)
    }

    vertx.registerVerticleFactory(KoinVerticleFactory(koinApplication.koin))
    vertx.deployVerticle(
        EventBusConsumerVerticle::class.java.asKoinVerticleName(),
        deploymentOptionsOf(
            instances = 1000,
            threadingModel = ThreadingModel.VIRTUAL_THREAD
        )
    ).onFailure {
        it.printStackTrace()
    }

    vertx.registerVerticleFactory(KodeinVerticleFactory(di))
    vertx.deployVerticle(
        EventBusConsumerVerticle::class.java.asKodeinVerticleName(),
        deploymentOptionsOf(
            instances = 1000,
            threadingModel = ThreadingModel.VIRTUAL_THREAD
        )
    ).onFailure {
        it.printStackTrace()
    }

//    vertx.deployVerticle(MainHttpVerticle())
    repeat(3000) {
        val counter = vertx.sharedData().getCounter("Timer$it")
        vertx.setPeriodic(2) { timerId ->
            GlobalScope.launch(vertx.dispatcher()) {
                val messNumber = counter.coAwait().getAndIncrement().coAwait()
//                logger.info("<2315ed35> Publish #$messNumber from $timerId")
                val result = eb.request<String>(
                    /* address = */ "test.test",
                    mapper.writeValueAsString(Instant.now())
                ).coAwait()
                val body = mapper.readValue<Response>(result.body())
                logger.info("<de3b0656> $timerId:$messNumber response time: ${Duration.between(body.requestTime, body.responseTime).toNanos()} from ${body.fromWorker}")
            }
        }
    }
}

data class Response(
    val requestTime: Instant,
    val responseTime: Instant,
    val fromWorker: String
)
