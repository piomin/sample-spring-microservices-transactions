package pl.piomin.samples.product.domain

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Product(@Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Int? = null,
                   val name: String = "",
                   var count: Int = 0,
                   val price: Int = 0)