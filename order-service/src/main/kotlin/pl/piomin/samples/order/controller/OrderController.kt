package pl.piomin.samples.order.controller

import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import pl.piomin.samples.order.client.Account
import pl.piomin.samples.order.client.DistributedTransaction
import pl.piomin.samples.order.client.Product
import pl.piomin.samples.order.domain.Order
import pl.piomin.samples.order.exception.OrderProcessingException
import pl.piomin.samples.order.listener.OrderTransactionEvent
import pl.piomin.samples.order.repository.OrderRepository
import kotlin.random.Random

@RestController
@RequestMapping("/orders")
class OrderController(val repository: OrderRepository,
                      val restTemplate: RestTemplate,
                      var applicationEventPublisher: ApplicationEventPublisher) {

    @PostMapping
    @Transactional
    @Throws(OrderProcessingException::class)
    fun addAndRollback(@RequestBody order: Order) {
        var transaction  = restTemplate.postForObject("http://transaction-server/transactions",
                DistributedTransaction(), DistributedTransaction::class.java)
        val orderSaved = repository.save(order)
        val product = updateProduct(transaction!!.id!!, order)
        val totalPrice = product.price * product.count
        val accounts = restTemplate.getForObject("http://account-service/accounts/customer/{customerId}",
                Array<Account>::class.java, order.customerId)
        val account  = accounts!!.first { it.balance >= totalPrice}
        updateAccount(transaction.id!!, account.id, totalPrice)
        val r = Random.nextInt(100)
        applicationEventPublisher.publishEvent(OrderTransactionEvent(transaction.id!!))
        if (r % 2 == 0)
            throw OrderProcessingException()
    }

    fun updateProduct(transactionId: String, order: Order): Product {
        val headers = HttpHeaders()
        headers.set("X-Transaction-ID", transactionId)
        val entity: HttpEntity<*> = HttpEntity<Any?>(headers)
        val product = restTemplate.exchange("http://product-service/products/{id}/count/{count}",
                HttpMethod.PUT, entity, Product::class.java, order.id, order.count)
        return product.body!!
    }

    fun updateAccount(transactionId: String, accountId: Int, totalPrice: Int): Account {
        val headers = HttpHeaders()
        headers.set("X-Transaction-ID", transactionId)
        val entity: HttpEntity<*> = HttpEntity<Any?>(headers)
        val account = restTemplate.exchange("http://account-service/accounts/{id}/withdrawal/{amount}",
                HttpMethod.PUT, entity, Account::class.java, accountId, totalPrice)
        return account.body!!
    }
}