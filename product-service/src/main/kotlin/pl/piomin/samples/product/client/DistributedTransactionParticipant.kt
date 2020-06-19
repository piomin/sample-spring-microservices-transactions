package pl.piomin.samples.product.client

class DistributedTransactionParticipant(val serviceId: String,
                                        var status: DistributedTransactionStatus)