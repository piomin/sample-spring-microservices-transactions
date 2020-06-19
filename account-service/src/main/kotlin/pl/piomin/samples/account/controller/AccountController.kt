package pl.piomin.samples.account.controller

import org.springframework.web.bind.annotation.*
import pl.piomin.samples.account.domain.Account
import pl.piomin.samples.account.repository.AccountRepository
import pl.piomin.samples.account.service.AccountService
import pl.piomin.samples.account.service.EventBus

@RestController
@RequestMapping("/accounts")
class AccountController(val repository: AccountRepository,
                        val service: AccountService,
                        val eventBus: EventBus) {

    @PostMapping
    fun add(@RequestBody account: Account): Account = repository.save(account)

    @GetMapping("/customer/{customerId}")
    fun findByCustomerId(@PathVariable customerId: Int): List<Account> =
            repository.findByCustomerId(customerId)

    @PutMapping("/{id}/payment/{amount}")
    fun payment(@PathVariable id: Int, @PathVariable amount: Int,
                @RequestHeader("X-Transaction-ID") transactionId: String): Account {
        service.payment(id, amount, transactionId)
        return eventBus.receiveEvent(transactionId)!!.account
    }

    @PutMapping("/{id}/withdrawal/{amount}")
    fun withdrawal(@PathVariable id: Int, @PathVariable amount: Int,
                   @RequestHeader("X-Transaction-ID") transactionId: String): Account {
        service.withdrawal(id, amount, transactionId)
        return eventBus.receiveEvent(transactionId)!!.account
    }

}