package pl.piomin.samples.product

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import pl.piomin.samples.product.client.DistributedTransaction
import pl.piomin.samples.product.client.DistributedTransactionStatus
import pl.piomin.samples.product.domain.Product
import pl.piomin.samples.product.listener.ProductTransactionEvent
import pl.piomin.samples.product.repository.ProductRepository
import pl.piomin.samples.product.service.EventBus


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.cloud.discovery.enabled=false"])
@AutoConfigureTestRestTemplate
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ProductControllerTests {

    @Autowired
    lateinit var template: TestRestTemplate
    @Autowired
    lateinit var eventBus: EventBus
    @Autowired
    lateinit var repository: ProductRepository

    companion object {
        @Container
        @ServiceConnection
        val container = PostgreSQLContainer<Nothing>("postgres:15")

        @Container
        @ServiceConnection
        val broker = RabbitMQContainer("rabbitmq:latest")
    }

    @Test
    @Order(1)
    fun shouldAddProduct() {
        val products = listOf(
            Product(name = "Test1", count = 100, price = 100),
            Product(name = "Test2", count = 10, price = 1000),
            Product(name = "Test3", count = 1000, price = 10))

        products.forEach { p ->
            val product = template.postForObject("/products", p, Product::class.java)!!
            Assertions.assertNotNull(product)
            Assertions.assertNotNull(product.id)
            println(product)
        }
    }

    @Test
    @Order(2)
    fun shouldUpdateProduct() {
        eventBus.sendTransaction(DistributedTransaction(id = "0", status = DistributedTransactionStatus.CONFIRMED))
        val headers = HttpHeaders()
        headers.set("X-Transaction-ID", "0")
        val entity: HttpEntity<Nothing> = HttpEntity(null, headers)
        template.exchange("/products/{id}/count/{count}", HttpMethod.PUT, entity, Product::class.java, 1, 10)
        val product = repository.findById(1)
        Assertions.assertTrue(!product.isEmpty)
        Assertions.assertEquals(90, product.get().count)
    }

    @Test
    @Order(3)
    fun shouldUpdateProductWithTransaction() {
        val product = repository.findById(1).orElseThrow()
        eventBus.sendTransaction(DistributedTransaction(id = "1", status = DistributedTransactionStatus.CONFIRMED))
        eventBus.sendEvent(ProductTransactionEvent("1", product))
        val headers = HttpHeaders()
        headers.set("X-Transaction-ID", "1")
        val entity: HttpEntity<Nothing> = HttpEntity(null, headers)
        val resp = template.exchange("/products/{id}/count/{count}", HttpMethod.PUT,
            entity, Product::class.java, product.id!!, 5)
        assertTrue(resp.statusCode.is2xxSuccessful)
    }

    @Test
    @Order(4)
    fun shouldRollbackProductUpdate() {
        val product = repository.findById(1).orElseThrow()
        eventBus.sendTransaction(DistributedTransaction(id = "2", status = DistributedTransactionStatus.ROLLBACK))
        eventBus.sendEvent(ProductTransactionEvent("2", product))
        val headers = HttpHeaders()
        headers.set("X-Transaction-ID", "2")
        val entity: HttpEntity<Nothing> = HttpEntity(null, headers)
        val resp = template.exchange("/products/{id}/count/{count}", HttpMethod.PUT,
            entity, Product::class.java, product.id!!, 5)
        Assertions.assertEquals(product.count, repository.findById(product.id!!).get().count)
    }

}
