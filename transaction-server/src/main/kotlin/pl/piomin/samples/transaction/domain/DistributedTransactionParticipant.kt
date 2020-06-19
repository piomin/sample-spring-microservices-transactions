package pl.piomin.samples.transaction.domain

class DistributedTransactionParticipant(val serviceId: String, var status: DistributedTransactionStatus)