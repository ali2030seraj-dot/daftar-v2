package com.daftar.v2.data.db

import androidx.room.TypeConverter
import com.daftar.v2.data.entity.PersonType
import com.daftar.v2.data.entity.TransactionType

class Converters {

    @TypeConverter
    fun fromPersonType(t: PersonType): String = t.name

    @TypeConverter
    fun toPersonType(s: String): PersonType = PersonType.valueOf(s)

    @TypeConverter
    fun fromTransactionType(t: TransactionType): String = t.name

    @TypeConverter
    fun toTransactionType(s: String): TransactionType = TransactionType.valueOf(s)
}