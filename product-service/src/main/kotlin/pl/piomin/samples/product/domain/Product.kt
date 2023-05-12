package pl.piomin.samples.product.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Product(@Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Int? = null,
                   val name: String = "",
                   var count: Int = 0,
                   val price: Int = 0)