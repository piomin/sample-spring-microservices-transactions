package pl.piomin.samples.order.client

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

data class Account(val id: Int,
                   val customerId: Int,
                   var balance: Int)