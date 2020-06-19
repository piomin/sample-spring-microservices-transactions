package pl.piomin.samples.account.client

data class DistributedTransaction(var id: String? = null,
                                  val status: DistributedTransactionStatus,
                                  val participants: MutableList<DistributedTransactionParticipant> = mutableListOf())