package pl.piomin.samples.account

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
import pl.piomin.samples.account.client.DistributedTransaction
import pl.piomin.samples.account.client.DistributedTransactionStatus
import pl.piomin.samples.account.domain.Account
import pl.piomin.samples.account.repository.AccountRepository
import pl.piomin.samples.account.service.EventBus


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.cloud.discovery.enabled=false"])
@AutoConfigureTestRestTemplate
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
        @ServiceConnection
        val container = PostgreSQLContainer<Nothing>("postgres:15")

        @Container
        @ServiceConnection
        val broker = RabbitMQContainer("rabbitmq:latest")
    }

    @Test
    @Order(1)
    fun shouldAddAccount() {
        val accounts = listOf(
            Account(customerId = 1, balance = 1000),
            Account(customerId = 2, balance = 3000),
            Account(customerId = 2, balance = 100))

        accounts.forEach { a ->
            val personAdd = template.postForObject("/accounts", a, Account::class.java)!!
            Assertions.assertNotNull(personAdd)
            Assertions.assertNotNull(personAdd.id)
            println(personAdd)
        }
    }

    @Test
    @Order(2)
    fun shouldFindAccountsByCustomerId() {
        val persons = template.getForObject("/accounts/customer/{customerId}", List::class.java, 1)!!
        Assertions.assertFalse(persons.isEmpty())
    }

    @Test
    @Order(3)
    fun payment() {
        eventBus.sendTransaction(DistributedTransaction(id = "1", status = DistributedTransactionStatus.CONFIRMED))
        val headers = HttpHeaders()
        headers.set("X-Transaction-ID", "1")
        val entity: HttpEntity<Nothing> = HttpEntity(null, headers)
        val resp = template.exchange("/accounts/{id}/payment/{amount}", HttpMethod.PUT, entity, Account::class.java, 1, 1000)
        assertTrue(resp.statusCode.is2xxSuccessful)
        Assertions.assertEquals(2000, resp.body!!.balance)
    }

    @Test
    @Order(4)
    fun withdrawal() {
        eventBus.sendTransaction(DistributedTransaction(id = "2", status = DistributedTransactionStatus.CONFIRMED))
        val headers = HttpHeaders()
        headers.set("X-Transaction-ID", "2")
        val entity: HttpEntity<Nothing> = HttpEntity(null, headers)
        val resp = template.exchange("/accounts/{id}/withdrawal/{amount}", HttpMethod.PUT, entity, Account::class.java, 1, 1000)
        assertTrue(resp.statusCode.is2xxSuccessful)
        Assertions.assertEquals(1000, resp.body!!.balance)
    }
}
