package pl.piomin.samples.product.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.piomin.samples.product.listener.ProductTransactionEvent
import pl.piomin.samples.product.repository.ProductRepository

@Service
@Transactional
@Async
class ProductService(val repository: ProductRepository,
                     var applicationEventPublisher: ApplicationEventPublisher) {

    fun updateCount(id: Int, count: Int, transactionId: String) {
        val product = repository.findById(id).orElseThrow()
        product.count -= count
        applicationEventPublisher.publishEvent(ProductTransactionEvent(transactionId, product))
        repository.save(product)
    }

}
