package pl.piomin.samples.transaction;

import org.junit.jupiter.api.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import org.springframework.test.web.servlet.client.returnResult
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.rabbitmq.RabbitMQContainer
import pl.piomin.samples.transaction.domain.DistributedTransaction
import pl.piomin.samples.transaction.domain.DistributedTransactionParticipant
import pl.piomin.samples.transaction.domain.DistributedTransactionStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["spring.cloud.discovery.enabled=false"])
@Import(TransactionBrokerConfiguration::class)
@AutoConfigureRestTestClient
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TransactionControllerTests {

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    companion object {

        var id: String? = null

        @Container
        @ServiceConnection
        val broker = RabbitMQContainer("rabbitmq:latest")
   }

   @Test
   @Order(1)
   fun shouldAddDistributedTransaction(@Autowired client: RestTestClient) {

       val transactions = listOf(
           DistributedTransaction(status = DistributedTransactionStatus.NEW,
                   participants = mutableListOf(DistributedTransactionParticipant(status = DistributedTransactionStatus.NEW, serviceId = "test-1"))),
           DistributedTransaction(status = DistributedTransactionStatus.CONFIRMED,
                   participants = mutableListOf(DistributedTransactionParticipant(status = DistributedTransactionStatus.CONFIRMED, serviceId = "test-2")))
       )

       transactions.forEach { t ->
           val trxAdd = client.post().uri("/transactions").body(t)
               .exchange()
               .expectStatus().isOk
               .returnResult<DistributedTransaction>().responseBody!!
           Assertions.assertNotNull(trxAdd)
           Assertions.assertNotNull(trxAdd.id)
           if (trxAdd.status == DistributedTransactionStatus.NEW)
               id = trxAdd.id
           println(trxAdd)
       }
   }

    @Test
    @Order(2)
    fun shouldFindById(@Autowired client: RestTestClient) {
        client.get().uri("/transactions/{id}", id!!)
            .exchange()
            .expectStatus().isOk
            .expectBody<DistributedTransaction>()
            .value {
                Assertions.assertNotNull(it)
                Assertions.assertNotNull(it!!.id)
            }
    }

    @Test
    @Order(3)
    fun shouldFindAll(@Autowired client: RestTestClient) {
        val result = client.get().uri("/transactions")
            .exchange()
            .expectStatus().isOk
            .expectBody(object : ParameterizedTypeReference<List<DistributedTransaction>>() {})
            .returnResult().responseBody
        Assertions.assertNotNull(result)
        Assertions.assertTrue(result!!.size >= 2)
    }

    @Test
    @Order(4)
    fun shouldFinish(@Autowired client: RestTestClient) {
        client.put().uri("/transactions/{id}/participants/{serviceId}/status/{status}", id!!, "test-1", "CONFIRMED")
            .exchange()
            .expectStatus().isOk
        val message: DistributedTransaction = rabbitTemplate.receiveAndConvert("trx-events") as DistributedTransaction
        Assertions.assertNotNull(message)
        println(message)
        Assertions.assertNotNull(message.id)
        Assertions.assertEquals(DistributedTransactionStatus.CONFIRMED, message.status)
    }

}
