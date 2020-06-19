package pl.piomin.samples.transaction.domain

enum class DistributedTransactionStatus {
    NEW, CONFIRMED, ROLLBACK, TO_ROLLBACK
}