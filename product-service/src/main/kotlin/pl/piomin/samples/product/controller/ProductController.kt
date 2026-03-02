package pl.piomin.samples.product.controller

import org.springframework.web.bind.annotation.*
import pl.piomin.samples.product.domain.Product
import pl.piomin.samples.product.repository.ProductRepository
import pl.piomin.samples.product.service.EventBus
import pl.piomin.samples.product.service.ProductService

@RestController
@RequestMapping("/products")
class ProductController(val repository: ProductRepository,
                        val service: ProductService,
                        val eventBus: EventBus) {

    @PostMapping
    fun add(@RequestBody product: Product): Product = repository.save(product)

    @PutMapping("/{id}/count/{count}")
    fun updateCount(@PathVariable id: Int, @PathVariable count: Int,
                    @RequestHeader("X-Transaction-ID") transactionId: String): Product {
        service.updateCount(id, count, transactionId)
        return eventBus.receiveEvent(transactionId)!!.product
    }

}