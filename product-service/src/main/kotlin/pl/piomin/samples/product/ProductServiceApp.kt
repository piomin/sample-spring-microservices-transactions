package pl.piomin.samples.product

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import pl.piomin.samples.product.service.EventBus

@SpringBootApplication
class ProductServiceApp {

    @LoadBalanced
    @Bean
    fun restTemplate(): RestTemplate = RestTemplateBuilder().build()

    @Bean
    fun eventBus(): EventBus = EventBus()

}

fun main(args: Array<String>) {
    runApplication<ProductServiceApp>(*args)
}