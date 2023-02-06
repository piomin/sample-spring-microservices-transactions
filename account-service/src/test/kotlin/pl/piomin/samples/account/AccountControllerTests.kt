package pl.piomin.samples.account

import org.instancio.Instancio
import org.instancio.Select
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import pl.piomin.samples.account.domain.Account


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AccountControllerTests {

    @Autowired
    lateinit var template: TestRestTemplate

    companion object {
        @Container
        val container = PostgreSQLContainer<Nothing>("postgres:14")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl);
            registry.add("spring.datasource.password", container::getPassword);
            registry.add("spring.datasource.username", container::getUsername);
        }
    }

    @Test
    @Order(1)
    fun shouldAddAccount() {
        val accounts = Instancio.ofList(Account::class.java).size(2)
            .ignore(Select.field("id"))
            .set(Select.field(Account::customerId), 1)
            .create()

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

}