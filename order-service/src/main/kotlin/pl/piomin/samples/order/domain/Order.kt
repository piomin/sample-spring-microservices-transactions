package pl.piomin.samples.order.domain

import org.springframework.data.annotation.Id
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType

@Entity
data class Order(@Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int? = null,
                 val productId: Int = 0,
                 val count: Int = 0,
                 val customerId: Int = 0)