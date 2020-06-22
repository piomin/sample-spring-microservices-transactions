# Distributed transactions in microservices Demo Project [![Twitter](https://img.shields.io/twitter/follow/piotr_minkowski.svg?style=social&logo=twitter&label=Follow%20Me)](https://twitter.com/piotr_minkowski)

In this project I'm demonstrating a simple architecture of microservices that perform distributed transactions. The example application applications are simple Spring Boot app that expose some HTTP endpoints for CRUD operations and connects to Postgres using Spring Data JPA.

## Getting Started 
All the examples are described in a separated articles on my blog. Here's a full list of available examples:
1. Rollback or confirmation of distributed transaction across order-service, account-service and product-service. A detailed guide may be find in the following article: [Distributed Transaction in Microservices with Spring Boot](https://piotrminkowski.com/2020/06/19/distributed-transactions-in-microservices-with-spring-boot/)

## Usage
1. Start discovery-server. It is available on port 8761.
2. Start RabbitMQ on Docker with command `docker run -d --name rabbit -h rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management`
3. Start transaction-server
4. Start Postgres on Docker with command `docker run -d --name postgres -p 5432:5432 -e POSTGRES_USER=trx -e POSTGRES_PASSWORD=trx123 -e POSTGRES_DB=trx postgres`
4. Start microservices: account-service, product-service and order-service. The app order-service is listening on port 8080.
5. Add some test data to product-service and account-service
5. Send some tests requests. For example: `$ curl http://localhost:8080/orders -H "Content-Type: application/json" -d "{\"productId\":1, \"count\":10, \"customerId\":1}"`