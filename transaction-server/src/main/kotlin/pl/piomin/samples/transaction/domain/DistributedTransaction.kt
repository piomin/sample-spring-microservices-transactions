package pl.piomin.samples.transaction.domain

data class DistributedTransaction(var id: String? = null,
                                  var status: DistributedTransactionStatus,
                                  val participants: MutableList<DistributedTransactionParticipant> = mutableListOf())