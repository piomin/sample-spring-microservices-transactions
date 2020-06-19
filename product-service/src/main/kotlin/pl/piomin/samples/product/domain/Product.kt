package pl.piomin.samples.product.domain

import org.springframework.data.annotation.Id
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType

@Entity
data class Product(@Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Int? = null,
              val name: String,
              var count: Int,
              val price: Int)