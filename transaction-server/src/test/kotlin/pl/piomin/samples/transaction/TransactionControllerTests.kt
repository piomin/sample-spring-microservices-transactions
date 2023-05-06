package pl.piomin.samples.transaction;

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.piomin.samples.transaction.domain.DistributedTransaction
import pl.piomin.samples.transaction.domain.DistributedTransactionParticipant
import pl.piomin.samples.transaction.domain.DistributedTransactionStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["spring.cloud.discovery.enabled=false"])
@Import(TransactionBrokerConfiguration::class)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
public class TransactionControllerTests {

    @Autowired
    lateinit var template: TestRestTemplate
    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    companion object {

        var id: String? = null;

        @Container
        val broker = RabbitMQContainer("rabbitmq:3.10.22")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.rabbitmq.port", broker::getAmqpPort)
        }
   }

   @Test
   @Order(1)
   fun shouldAddDistributedTransaction() {

       val transactions = listOf(
           DistributedTransaction(status = DistributedTransactionStatus.NEW,
                   participants = mutableListOf(DistributedTransactionParticipant(status = DistributedTransactionStatus.NEW, serviceId = "test-1"))),
           DistributedTransaction(status = DistributedTransactionStatus.CONFIRMED,
                   participants = mutableListOf(DistributedTransactionParticipant(status = DistributedTransactionStatus.CONFIRMED, serviceId = "test-2")))
       )

       transactions.forEach { t ->
           val trxAdd = template.postForObject("/transactions", t, DistributedTransaction::class.java)
           assertNotNull(trxAdd)
           assertNotNull(trxAdd.id)
           if (trxAdd.status == DistributedTransactionStatus.NEW)
               id = trxAdd.id
           println(trxAdd)
       }
   }

    @Test
    @Order(2)
    fun shouldFindById() {
        val transaction = template.getForObject("/transactions/{id}", DistributedTransaction::class.java, id)
        assertNotNull(transaction)
    }

   @Test
   @Order(2)
   fun shouldFindAll() {
       val transactions = template.getForObject("/transactions", List::class.java)
       assertFalse(transactions.isEmpty())
   }

   @Test
   @Order(3)
   fun shouldFinish() {
       template.put("/transactions/{id}/finish/{status}", null, id, "CONFIRMED")
       val message: DistributedTransaction = rabbitTemplate.receiveAndConvert("trx-events") as DistributedTransaction
       assertNotNull(message)
       println(message)
       assertNotNull(message.id)
       assertEquals(DistributedTransactionStatus.CONFIRMED, message.status)
   }

}
