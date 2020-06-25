package pl.piomin.samples.account

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import pl.piomin.samples.account.service.EventBus


@SpringBootApplication
class AccountServiceApp {

    @LoadBalanced
    @Bean
    fun restTemplate(): RestTemplate = RestTemplateBuilder().build()

    @Bean
    fun eventBus(): EventBus = EventBus()

}

fun main(args: Array<String>) {
    runApplication<AccountServiceApp>(*args)
}