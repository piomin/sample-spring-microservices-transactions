package pl.piomin.samples.account

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import org.springframework.test.web.servlet.client.returnResult
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
@AutoConfigureRestTestClient
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AccountControllerTests {

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
    fun shouldAddAccount(@Autowired client: RestTestClient) {
        val accounts = listOf(
            Account(customerId = 1, balance = 1000),
            Account(customerId = 2, balance = 3000),
            Account(customerId = 2, balance = 100))

        accounts.forEach { a ->
            val personAdd = client.post().uri("/accounts").body(a)
                .exchange()
                .expectStatus().isOk
                .returnResult<Account>().responseBody!!
            Assertions.assertNotNull(personAdd)
            Assertions.assertNotNull(personAdd.id)
            println(personAdd)
        }
    }

    @Test
    @Order(2)
    fun shouldFindAccountsByCustomerId(@Autowired client: RestTestClient) {
        val result = client.get().uri("/accounts/customer/{customerId}", 1)
            .exchange()
            .expectStatus().isOk
            .expectBody(object : ParameterizedTypeReference<List<Account>>() {})
            .returnResult().responseBody
        Assertions.assertNotNull(result)
        Assertions.assertFalse(result!!.isEmpty())
    }

    @Test
    @Order(3)
    fun payment(@Autowired client: RestTestClient) {
        eventBus.sendTransaction(DistributedTransaction(id = "1", status = DistributedTransactionStatus.CONFIRMED))
        val account = client.put().uri("/accounts/{id}/payment/{amount}", 1, 1000)
            .header("X-Transaction-ID", "1")
            .exchange()
            .expectStatus().isOk
            .returnResult<Account>().responseBody!!
        Assertions.assertNotNull(account)
        Assertions.assertEquals(2000, account.balance)
    }

    @Test
    @Order(4)
    fun withdrawal(@Autowired client: RestTestClient) {
        eventBus.sendTransaction(DistributedTransaction(id = "2", status = DistributedTransactionStatus.CONFIRMED))
        val account = client.put().uri("/accounts/{id}/withdrawal/{amount}", 1, 1000)
            .header("X-Transaction-ID", "2")
            .exchange()
            .expectStatus().isOk
            .returnResult<Account>().responseBody!!
        Assertions.assertNotNull(account)
        Assertions.assertEquals(1000, account.balance)
    }
}
