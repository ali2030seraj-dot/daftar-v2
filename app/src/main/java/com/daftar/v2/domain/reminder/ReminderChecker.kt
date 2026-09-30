package com.daftar.v2.domain.reminder

import com.daftar.v2.data.ReminderPreferences
import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.PersonType
import com.daftar.v2.data.entity.TransactionType
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⭐ الفحص المشترك للمدينين المتأخرين.
 *
 * يُستخدم من:
 * - ReminderWorker (يومياً الساعة 9 صباحاً)
 * - ReminderSettingsViewModel (زر "فحص المدينين الآن")
 */
@Singleton
class ReminderChecker @Inject constructor(
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val reminderPreferences: ReminderPreferences
) {

    /**
     * يفحص كل المدينين ويُعيد قائمة المتأخرين.
     *
     * المنطق:
     * - المدين: شخص نوعه DEBTOR (ما عليك)
     * - الرصيد > 0 (لم يسدد كاملاً)
     * - آخر سداد أقدم من thresholdDays
     * - إذا لم يسدد أبداً → استخدم تاريخ أول دين
     *
     * @return قائمة المتأخرين مرتبة حسب المبلغ تنازلياً
     */
    suspend fun findOverdueDebtors(): List<OverduePerson> {
        val thresholdDays = reminderPreferences.thresholdDays.first()
        val thresholdMillis = thresholdDays * 24L * 60L * 60L * 1000L
        val now = System.currentTimeMillis()
        val cutoff = now - thresholdMillis

        val debtors = personDao.getAll()
            .filter { it.type == PersonType.DEBTOR && !it.isArchived }

        val overdue = mutableListOf<OverduePerson>()

        for (person in debtors) {
            val transactions = transactionDao
                .observeByPerson(person.id)
                .first()

            if (transactions.isEmpty()) continue

            // ─── حساب الرصيد ───
            val balance = transactions.sumOf { tx ->
                if (tx.type == TransactionType.DEBT) tx.amountCents
                else -tx.amountCents
            }

            if (balance <= 0) continue

            // ─── آخر سداد ───
            val lastPayment = transactions
                .filter { it.type == TransactionType.PAYMENT }
                .maxByOrNull { it.timestamp }

            // ─── إذا لا يوجد سداد → استخدم أول دين ───
            val referenceDate = lastPayment?.timestamp
                ?: transactions
                    .filter { it.type == TransactionType.DEBT }
                    .minByOrNull { it.timestamp }
                    ?.timestamp
                ?: continue

            // ─── هل تجاوز المدة؟ ───
            if (referenceDate < cutoff) {
                val daysOverdue = (now - referenceDate) / (24L * 60L * 60L * 1000L)
                overdue.add(
                    OverduePerson(
                        id = person.id,
                        name = person.name,
                        balanceCents = balance,
                        currency = person.currency,
                        daysOverdue = daysOverdue
                    )
                )
            }
        }

        return overdue.sortedByDescending { it.balanceCents }
    }
}