package dev.limebeck.activemqApp

import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import java.util.UUID

class EventBusConsumerVerticle(
    val testData: String
) : AbstractVerticle() {
    val instanceId = UUID.randomUUID().toString()

//    init {
//        println("Loaded EventBusConsumerVerticle $instanceId")
//    }

    override fun init(vertx: Vertx?, context: Context?) {
        super.init(vertx, context)
        println("Deployed EventBusConsumerVerticle $instanceId")
    }

    override fun start(startPromise: Promise<Void>?) {
        val eb = vertx.eventBus()
        eb.consumer<String>("test.test") {
            val body = it.body()
            println("<e874b400> Consume Body $body")
        }
    }
}
