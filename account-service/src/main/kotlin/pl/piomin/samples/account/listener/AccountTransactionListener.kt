package pl.piomin.samples.account.listener

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.client.RestTemplate
import pl.piomin.samples.account.client.DistributedTransaction
import pl.piomin.samples.account.client.DistributedTransactionStatus
import pl.piomin.samples.account.exception.AccountProcessingException
import pl.piomin.samples.account.service.EventBus

@Component
class AccountTransactionListener(val restTemplate: RestTemplate,
                                 val eventBus: EventBus) {

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Throws(AccountProcessingException::class)
    fun handleEvent(event: AccountTransactionEvent) {
        eventBus.sendEvent(event)
        var transaction: DistributedTransaction? = null
        for (x in 0..100) {
            transaction = eventBus.receiveTransaction(event.transactionId)
            if (transaction == null)
                Thread.sleep(100)
            else break
        }
        if (transaction == null || transaction.status != DistributedTransactionStatus.CONFIRMED)
            throw AccountProcessingException()
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    fun handleAfterRollback(event: AccountTransactionEvent) {
        restTemplate.put("http://transaction-server/transactions/transactionId/participants/{serviceId}/status/{status}",
                null, "account-service", "TO_ROLLBACK")
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    fun handleAfterCompletion(event: AccountTransactionEvent) {
        restTemplate.put("http://transaction-server/transactions/transactionId/participants/{serviceId}/status/{status}",
                null, "account-service", "CONFIRM")
    }

}