package pl.piomin.samples.order.client

data class Account(val id: Int,
                   val customerId: Int,
                   var balance: Int)