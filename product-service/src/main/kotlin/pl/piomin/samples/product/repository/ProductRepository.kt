package pl.piomin.samples.product.repository

import org.springframework.data.repository.CrudRepository
import pl.piomin.samples.product.domain.Product

interface ProductRepository: CrudRepository<Product, Int> {
}