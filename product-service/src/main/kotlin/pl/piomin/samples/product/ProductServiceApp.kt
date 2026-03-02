package pl.piomin.samples.product

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import pl.piomin.samples.product.service.EventBus

@SpringBootApplication
@EnableAsync
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