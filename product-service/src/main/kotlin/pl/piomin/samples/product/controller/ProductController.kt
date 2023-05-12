package pl.piomin.samples.product.controller

import org.springframework.web.bind.annotation.*
import pl.piomin.samples.product.domain.Product
import pl.piomin.samples.product.repository.ProductRepository

@RestController
@RequestMapping("/products")
class ProductController(val repository: ProductRepository) {

    @PostMapping
    fun add(@RequestBody product: Product): Product = repository.save(product)

    @PutMapping("/{id}/count/{count}")
    fun updateCount(@PathVariable id: Int, @PathVariable count: Int): Product {
        val product: Product = repository.findById(id).get()
        product.count -= count
        repository.save(product)
        return product
    }

}