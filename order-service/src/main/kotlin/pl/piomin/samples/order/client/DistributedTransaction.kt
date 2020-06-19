package pl.piomin.samples.order.client

data class DistributedTransaction(var id: String? = null,
                                  var status: DistributedTransactionStatus = DistributedTransactionStatus.NEW,
                                  val participants: MutableList<DistributedTransactionParticipant> = mutableListOf())