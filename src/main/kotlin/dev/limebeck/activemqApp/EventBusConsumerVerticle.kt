package dev.limebeck.activemqApp

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import java.time.Instant

class EventBusConsumerVerticle(
    val instanceId: String
) : AbstractVerticle() {
    companion object {
        private val logger = EventBusConsumerVerticle::class.logger()
    }

    init {
        logger.info("Loaded EventBusConsumerVerticle $instanceId")
    }

    override fun init(vertx: Vertx?, context: Context?) {
        super.init(vertx, context)
        logger.info("Deployed EventBusConsumerVerticle $instanceId")
    }

    override fun start(startPromise: Promise<Void>?) {
        val eb = vertx.eventBus()
        eb.consumer<String>("test.test") {
            val body: Instant = mapper.readValue(it.body())
//            logger.info("<e874b400> Consume Body $body")
//            it.reply("Response to \"${body}\" at ${Instant.now()} from $instanceId")
            it.reply(
                mapper.writeValueAsString(
                    Response(
                        requestTime = body,
                        responseTime = Instant.now(),
                        fromWorker = instanceId
                    )
                )
            )
        }
    }
}

val mapper =
    jacksonObjectMapper().registerModules(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
