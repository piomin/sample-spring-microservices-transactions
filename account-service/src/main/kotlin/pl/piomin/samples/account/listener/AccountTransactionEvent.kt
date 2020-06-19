package pl.piomin.samples.account.listener

import pl.piomin.samples.account.domain.Account

class AccountTransactionEvent(val transactionId: String, val account: Account)