package pl.piomin.samples.order.client

import org.springframework.data.annotation.Id
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType

class Product(var id: Int? = null,
              val name: String,
              var count: Int,
              val price: Int)