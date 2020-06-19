package pl.piomin.samples.product.listener

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.client.RestTemplate
import pl.piomin.samples.product.client.DistributedTransaction
import pl.piomin.samples.product.client.DistributedTransactionStatus
import pl.piomin.samples.product.exception.ProductProcessingException
import pl.piomin.samples.product.service.EventBus

@Component
class ProductTransactionListener(val restTemplate: RestTemplate,
                                 val eventBus: EventBus) {

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Throws(ProductProcessingException::class)
    fun handleEvent(event: ProductTransactionEvent) {
        eventBus.sendEvent(event)
        var transaction: DistributedTransaction? = null
        for (x in 0..100) {
            transaction = eventBus.receiveTransaction(event.transactionId)
            if (transaction == null)
                Thread.sleep(100)
            else break
        }
        if (transaction == null || transaction.status != DistributedTransactionStatus.CONFIRMED)
            throw ProductProcessingException()
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    fun handleAfterRollback(event: ProductTransactionEvent) {
        restTemplate.put("http://transaction-server/transactions/transactionId/participants/{serviceId}/status/{status}",
                null, "account-service", "TO_ROLLBACK")
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    fun handleAfterCompletion(event: ProductTransactionEvent) {
        restTemplate.put("http://transaction-server/transactions/transactionId/participants/{serviceId}/status/{status}",
                null, "account-service", "CONFIRM")
    }

}