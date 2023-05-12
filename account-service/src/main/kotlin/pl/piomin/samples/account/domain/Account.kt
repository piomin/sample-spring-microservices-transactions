package pl.piomin.samples.account.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Account(@Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int? = null,
                   val customerId: Int = 0,
                   var balance: Int = 0)