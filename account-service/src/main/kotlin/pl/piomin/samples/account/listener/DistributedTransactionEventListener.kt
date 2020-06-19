package pl.piomin.samples.account.listener

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import pl.piomin.samples.account.client.DistributedTransaction
import pl.piomin.samples.account.service.EventBus

@Component
class DistributedTransactionEventListener(val eventBus: EventBus) {

    @RabbitListener(queues = ["trx-events"])
    fun onMessage(transaction: DistributedTransaction) {
        eventBus.sendTransaction(transaction)
    }

}