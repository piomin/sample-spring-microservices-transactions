package pl.piomin.samples.order.repository

import org.springframework.data.repository.CrudRepository
import pl.piomin.samples.order.domain.Order

interface OrderRepository: CrudRepository<Order, Int> {
}