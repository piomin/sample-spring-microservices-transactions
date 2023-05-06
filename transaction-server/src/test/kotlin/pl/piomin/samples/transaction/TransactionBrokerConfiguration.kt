package pl.piomin.samples.transaction

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TransactionBrokerConfiguration {

    @Bean
    fun queue(): Queue = Queue("trx-events")

    @Bean
    fun exchange(): TopicExchange = TopicExchange("trx-events")

    @Bean
    fun binding(queue: Queue, exchange: TopicExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).with("trx-events")

    @Bean
    fun messageConverter(): MessageConverter = Jackson2JsonMessageConverter()
}