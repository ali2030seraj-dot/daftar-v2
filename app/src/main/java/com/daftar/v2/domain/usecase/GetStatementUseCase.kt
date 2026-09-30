package com.daftar.v2.domain.usecase

import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.TransactionEntity
import com.daftar.v2.data.entity.TransactionType
import com.daftar.v2.data.repository.PersonRepository
import com.daftar.v2.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class StatementRow(
    val transaction: TransactionEntity,
    val runningBalanceCents: Long
)

data class Statement(
    val person: PersonEntity?,                    // ✅ nullable لعرض "كل العمليات"
    val rows: List<StatementRow>,
    val totalDebtCents: Long,
    val totalPaymentCents: Long,
    val finalBalanceCents: Long,
    val isAllMode: Boolean = false,               // ✅ جديد: هل هذا عرض كل العمليات؟
    val displayCurrency: String = person?.currency ?: "YER"  // ✅ جديد: عملة العرض
)

class GetStatementUseCase @Inject constructor(
    private val personRepository: PersonRepository,
    private val transactionRepository: TransactionRepository
) {

    fun observe(personId: Long): Flow<Statement?> {
        val personFlow = personRepository.observeById(personId)
        val transactionsFlow = transactionRepository.observeByPerson(personId)

        return combine(personFlow, transactionsFlow) { person, transactions ->
            if (person == null) null
            else buildStatement(person, transactions)
        }
    }

    suspend operator fun invoke(personId: Long): Statement? {
        val person = personRepository.getById(personId) ?: return null
        val transactions = transactionRepository.observeByPerson(personId).first()
        return buildStatement(person, transactions)
    }

    private fun buildStatement(
        person: PersonEntity,
        transactions: List<TransactionEntity>
    ): Statement {
        val sorted = transactions.sortedBy { it.timestamp }

        var running = 0L
        val rows = mutableListOf<StatementRow>()

        for (tx in sorted) {
            running += when (tx.type) {
                TransactionType.DEBT -> tx.amountCents
                TransactionType.PAYMENT -> -tx.amountCents
            }
            rows.add(StatementRow(tx, running))
        }

        val totalDebt = sorted
            .filter { it.type == TransactionType.DEBT }
            .sumOf { it.amountCents }

        val totalPayment = sorted
            .filter { it.type == TransactionType.PAYMENT }
            .sumOf { it.amountCents }

        return Statement(
            person = person,
            rows = rows,
            totalDebtCents = totalDebt,
            totalPaymentCents = totalPayment,
            finalBalanceCents = totalDebt - totalPayment,
            isAllMode = false,
            displayCurrency = person.currency
        )
    }
}