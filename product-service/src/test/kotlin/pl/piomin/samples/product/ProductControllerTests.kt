package pl.piomin.samples.product

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.returnResult
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
@AutoConfigureRestTestClient
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ProductControllerTests {

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
    fun shouldAddProduct(@Autowired client: RestTestClient) {
        val products = listOf(
            Product(name = "Test1", count = 100, price = 100),
            Product(name = "Test2", count = 10, price = 1000),
            Product(name = "Test3", count = 1000, price = 10))

        products.forEach { p ->
            val product = client.post().uri("/products").body(p)
                .exchange()
                .expectStatus().isOk
                .returnResult<Product>().responseBody!!
            Assertions.assertNotNull(product)
            Assertions.assertNotNull(product.id)
            println(product)
        }
    }

    @Test
    @Order(2)
    fun shouldUpdateProduct(@Autowired client: RestTestClient) {
        eventBus.sendTransaction(DistributedTransaction(id = "0", status = DistributedTransactionStatus.CONFIRMED))
        client.put().uri("/products/{id}/count/{count}", 1, 10)
            .header("X-Transaction-ID", "0")
            .exchange()
            .expectStatus().isOk
        val product = repository.findById(1)
        Assertions.assertTrue(!product.isEmpty)
        Assertions.assertEquals(90, product.get().count)
    }

    @Test
    @Order(3)
    fun shouldUpdateProductWithTransaction(@Autowired client: RestTestClient) {
        val product = repository.findById(1).orElseThrow()
        eventBus.sendTransaction(DistributedTransaction(id = "1", status = DistributedTransactionStatus.CONFIRMED))
        eventBus.sendEvent(ProductTransactionEvent("1", product))
        client.put().uri("/products/{id}/count/{count}", product.id!!, 5)
            .header("X-Transaction-ID", "1")
            .exchange()
            .expectStatus().is2xxSuccessful
    }

    @Test
    @Order(4)
    fun shouldRollbackProductUpdate(@Autowired client: RestTestClient) {
        val product = repository.findById(1).orElseThrow()
        eventBus.sendTransaction(DistributedTransaction(id = "2", status = DistributedTransactionStatus.ROLLBACK))
        eventBus.sendEvent(ProductTransactionEvent("2", product))
        client.put().uri("/products/{id}/count/{count}", product.id!!, 5)
            .header("X-Transaction-ID", "2")
            .exchange()
            .expectStatus().isOk
        Assertions.assertEquals(product.count, repository.findById(product.id!!).get().count)
    }

}
