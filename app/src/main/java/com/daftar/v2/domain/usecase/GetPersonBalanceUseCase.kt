package com.daftar.v2.domain.usecase

import com.daftar.v2.data.repository.TransactionRepository
import javax.inject.Inject

data class PersonBalance(
    val personId: Long,
    val totalDebtCents: Long,
    val totalPaymentCents: Long,
    val balanceCents: Long
)

class GetPersonBalanceUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {

    suspend operator fun invoke(personId: Long): PersonBalance {
        require(personId > 0) { "معرف الشخص مطلوب" }

        val totalDebt = transactionRepository.totalDebtCents(personId)
        val totalPaid = transactionRepository.totalPaymentCents(personId)

        return PersonBalance(
            personId = personId,
            totalDebtCents = totalDebt,
            totalPaymentCents = totalPaid,
            balanceCents = totalDebt - totalPaid
        )
    }
}