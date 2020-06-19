package pl.piomin.samples.account.repository

import org.springframework.data.repository.CrudRepository
import pl.piomin.samples.account.domain.Account

interface AccountRepository: CrudRepository<Account, Int> {

    fun findByCustomerId(id: Int): List<Account>

}