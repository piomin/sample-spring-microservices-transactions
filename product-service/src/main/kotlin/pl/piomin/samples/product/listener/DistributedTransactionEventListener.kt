package pl.piomin.samples.product.listener

import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import pl.piomin.samples.product.client.DistributedTransaction
import pl.piomin.samples.product.service.EventBus

@Component
class DistributedTransactionEventListener(val eventBus: EventBus) {

    @RabbitListener(bindings = [
        QueueBinding(exchange = Exchange(type = ExchangeTypes.TOPIC, name = "trx-events"),
                value = Queue("trx-events-product"))
    ])
    fun onMessage(transaction: DistributedTransaction) {
        eventBus.sendTransaction(transaction)
    }

}