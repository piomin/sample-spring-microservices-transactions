package pl.piomin.samples.product.listener

import pl.piomin.samples.product.domain.Product

class ProductTransactionEvent(val transactionId: String, val product: Product)