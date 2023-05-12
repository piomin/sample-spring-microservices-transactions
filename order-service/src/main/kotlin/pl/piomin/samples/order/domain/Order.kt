package pl.piomin.samples.order.domain

import jakarta.persistence.*

@Entity
@Table(name = "orders")
data class Order(@Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int? = null,
                 val productId: Int = 0,
                 val count: Int = 0,
                 val customerId: Int = 0)