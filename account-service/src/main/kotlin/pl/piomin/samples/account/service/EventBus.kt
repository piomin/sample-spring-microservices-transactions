package pl.piomin.samples.account.service

import pl.piomin.samples.account.client.DistributedTransaction
import pl.piomin.samples.account.listener.AccountTransactionEvent

class EventBus(private val transactions: MutableSet<DistributedTransaction> = mutableSetOf(),
               private val events: MutableSet<AccountTransactionEvent> = mutableSetOf()) {

    fun sendTransaction(event: DistributedTransaction) = transactions.add(event)

    fun receiveTransaction(eventId: String): DistributedTransaction? {
        var transaction: DistributedTransaction? = null
        while (transaction == null) {
            transaction = transactions.find { it.id == eventId }
            transactions.remove(transaction)
            if (transaction != null)
                return transaction
            else
                Thread.sleep(10)
        }
        return null
    }

    fun sendEvent(event: AccountTransactionEvent) = events.add(event)

    fun receiveEvent(eventId: String): AccountTransactionEvent? {
        var event: AccountTransactionEvent? = null
        while (event == null) {
            event = events.find { it.transactionId == eventId }
            events.remove(event)
            if (event != null)
                return event
            else
                Thread.sleep(10)
        }
        return null
    }

}