package pl.piomin.samples.transaction.controller

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.web.bind.annotation.*
import pl.piomin.samples.transaction.domain.DistributedTransaction
import pl.piomin.samples.transaction.domain.DistributedTransactionParticipant
import pl.piomin.samples.transaction.domain.DistributedTransactionStatus
import pl.piomin.samples.transaction.repository.TransactionRepository

@RestController
@RequestMapping("/transactions")
class TransactionController(val repository: TransactionRepository,
                            val template: RabbitTemplate) {

    @PostMapping
    fun add(@RequestBody transaction: DistributedTransaction): DistributedTransaction =
            repository.save(transaction)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): DistributedTransaction? = repository.findById(id)

    @GetMapping
    fun findAll(): List<DistributedTransaction> = repository.findAll()

    @PutMapping("/{id}/finish/{status}")
    fun finish(@PathVariable id: String, @PathVariable status: DistributedTransactionStatus) {
        val transaction: DistributedTransaction? = repository.findById(id)
        if (transaction != null) {
            transaction.status = status
            repository.update(transaction)
            template.convertAndSend("trx-events", DistributedTransaction(id, status))
        }
    }

    @PutMapping("/{id}/participants")
    fun addParticipant(@PathVariable id: String,
                       @RequestBody participant: DistributedTransactionParticipant) =
            repository.findById(id)?.participants?.add(participant)

    @PutMapping("/{id}/participants/{serviceId}/status/{status}")
    fun updateParticipant(@PathVariable id: String,
                          @PathVariable serviceId: String,
                          @PathVariable status: DistributedTransactionStatus) {
        val transaction: DistributedTransaction? = repository.findById(id)
        if (transaction != null) {
            val index = transaction.participants.indexOfFirst { it.serviceId == serviceId }
            if (index != -1) {
                transaction.participants[index].status = status
                template.convertAndSend("trx-events", DistributedTransaction(id, status))
            }
        }
    }

}