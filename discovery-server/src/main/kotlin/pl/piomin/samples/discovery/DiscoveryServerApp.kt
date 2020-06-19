package pl.piomin.samples.discovery

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@SpringBootApplication
@EnableEurekaServer
class DiscoveryServerApp

fun main(args: Array<String>) {
    runApplication<DiscoveryServerApp>(*args)
}