package pl.piomin.samples.account.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.piomin.samples.account.domain.Account
import pl.piomin.samples.account.listener.AccountTransactionEvent
import pl.piomin.samples.account.repository.AccountRepository
import java.util.*

@Service
@Transactional
@Async
class AccountService(val repository: AccountRepository,
                     var applicationEventPublisher: ApplicationEventPublisher) {

    fun payment(id: Int, amount: Int, transactionId: String) =
            transfer(id, amount, transactionId)

    fun withdrawal(id: Int, amount: Int, transactionId: String) =
            transfer(id, (-1) * amount, transactionId)

    private fun transfer(id: Int, amount: Int, transactionId: String) {
        val account: Account = repository.findById(id).orElseThrow()
        account.balance += amount
        applicationEventPublisher.publishEvent(AccountTransactionEvent(transactionId, account))
        repository.save(account)
    }

}