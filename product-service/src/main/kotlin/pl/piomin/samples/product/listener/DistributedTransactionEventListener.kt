package pl.piomin.samples.product.listener

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import pl.piomin.samples.product.client.DistributedTransaction
import pl.piomin.samples.product.service.EventBus

@Component
class DistributedTransactionEventListener(val eventBus: EventBus) {

    @RabbitListener(queues = ["trx-events"])
    fun onMessage(transaction: DistributedTransaction) {
        eventBus.sendTransaction(transaction)
    }

}