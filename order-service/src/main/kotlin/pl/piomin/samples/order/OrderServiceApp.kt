package pl.piomin.samples.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class OrderServiceApp {

    @Bean
    fun restTemplate(): RestTemplate = RestTemplateBuilder().build()

}