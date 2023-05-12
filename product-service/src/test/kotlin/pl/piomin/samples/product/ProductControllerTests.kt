package pl.piomin.samples.product

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import pl.piomin.samples.product.domain.Product
import pl.piomin.samples.product.repository.ProductRepository


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.cloud.discovery.enabled=false"])
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ProductControllerTests {

    @Autowired
    lateinit var template: TestRestTemplate
    @Autowired
    lateinit var repository: ProductRepository

    companion object {
        @Container
        val container = PostgreSQLContainer<Nothing>("postgres:14")

        @Container
        val broker = RabbitMQContainer("rabbitmq:3.10.22")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl);
            registry.add("spring.datasource.password", container::getPassword);
            registry.add("spring.datasource.username", container::getUsername);
            registry.add("spring.rabbitmq.port", broker::getAmqpPort)
        }
    }

    @Test
    @Order(1)
    fun shouldAddProduct() {

        val products = listOf(
            Product(name = "Test1", count = 100, price = 100),
            Product(name = "Test2", count = 10, price = 1000),
            Product(name = "Test3", count = 1000, price = 10))

        products.forEach { p ->
            val product = template.postForObject("/products", p, Product::class.java)
            assertNotNull(product)
            assertNotNull(product.id)
            println(product)
        }
    }

    @Test
    @Order(2)
    fun shouldUpdateProduct() {
        template.put("/products/{id}/count/{count}", null, 1, 10)
        val product = repository.findById(1)
        assertTrue(!product.isEmpty)
        assertEquals(90, product.get().count)
    }


}