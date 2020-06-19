package pl.piomin.samples.transaction

import org.springframework.amqp.core.TopicExchange
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class TransactionServerApp {

    @Bean
    fun topic(): TopicExchange = TopicExchange("trx-events")
}

fun main(args: Array<String>) {
    runApplication<TransactionServerApp>(*args)
}