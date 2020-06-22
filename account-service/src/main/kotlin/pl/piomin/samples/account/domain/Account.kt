package pl.piomin.samples.account.domain

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Account(@Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int? = null,
                   val customerId: Int = 0,
                   var balance: Int = 0)