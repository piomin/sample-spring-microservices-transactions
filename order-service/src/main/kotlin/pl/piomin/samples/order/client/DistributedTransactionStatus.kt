package pl.piomin.samples.order.client

enum class DistributedTransactionStatus {
    NEW, CONFIRMED, ROLLBACK, TO_ROLLBACK
}