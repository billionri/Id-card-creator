package com.example.data.repository

import com.example.data.local.EmployeeCardDao
import com.example.data.model.EmployeeIdCard
import kotlinx.coroutines.flow.Flow

class CardRepository(private val dao: EmployeeCardDao) {
    val allCards: Flow<List<EmployeeIdCard>> = dao.getAllCards()

    fun searchCards(query: String): Flow<List<EmployeeIdCard>> {
        return if (query.isBlank()) {
            dao.getAllCards()
        } else {
            dao.searchCards(query.trim())
        }
    }

    fun getCardById(id: Long): Flow<EmployeeIdCard?> = dao.getCardById(id)

    suspend fun saveCard(card: EmployeeIdCard): Long = dao.insertCard(card)

    suspend fun updateCard(card: EmployeeIdCard) = dao.updateCard(card)

    suspend fun deleteCard(card: EmployeeIdCard) = dao.deleteCard(card)

    suspend fun deleteById(id: Long) = dao.deleteCardById(id)
}
