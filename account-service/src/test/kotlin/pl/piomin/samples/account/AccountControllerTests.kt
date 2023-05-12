package pl.piomin.samples.account

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import pl.piomin.samples.account.domain.Account
import pl.piomin.samples.account.listener.AccountTransactionEvent
import pl.piomin.samples.account.repository.AccountRepository
import pl.piomin.samples.account.service.EventBus


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.cloud.discovery.enabled=false"])
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AccountControllerTests {

    @Autowired
    lateinit var template: TestRestTemplate
    @Autowired
    lateinit var eventBus: EventBus
    @Autowired
    lateinit var repository: AccountRepository

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
    fun shouldAddAccount() {

        val accounts = listOf(
            Account(customerId = 1, balance = 1000),
            Account(customerId = 2, balance = 3000),
            Account(customerId = 2, balance = 100))

        accounts.forEach { a ->
            val personAdd = template.postForObject("/accounts", a, Account::class.java)
            Assertions.assertNotNull(personAdd)
            Assertions.assertNotNull(personAdd.id)
            println(personAdd)
        }
    }

    @Test
    @Order(2)
    fun shouldFindAccountsByCustomerId() {
        val persons = template.getForObject("/accounts/customer/{customerId}", List::class.java, 1)
        Assertions.assertFalse(persons.isEmpty())
    }

//    @Test
//    @Order(2)
    fun payment() {
        val acc = repository.findById(1).orElseThrow()
        eventBus.sendEvent(AccountTransactionEvent("1", acc))
        val headers = HttpHeaders()
        headers.set("X-Transaction-ID", "1")
        val entity: HttpEntity<Nothing> = HttpEntity(null, headers)
        val resp = template.exchange("/accounts/{id}/payment/{amount}", HttpMethod.PUT, entity, Account::class.java, 1, 1000)
        assertTrue(resp.statusCode.is2xxSuccessful)
    }

//    @Test
//    @Order(3)
    fun withdrawal() {
        val headers = HttpHeaders()
        headers.set("X-Transaction-ID", "2")
        val entity: HttpEntity<Nothing> = HttpEntity(null, headers)
        val resp = template.exchange("/accounts/{id}/withdrawal/{amount}", HttpMethod.PUT, entity, Account::class.java, 1, 1000)
        assertTrue(resp.statusCode.is2xxSuccessful)
    }
}