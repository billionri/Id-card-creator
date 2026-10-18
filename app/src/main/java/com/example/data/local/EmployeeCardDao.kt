package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.EmployeeIdCard
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeCardDao {
    @Query("SELECT * FROM employee_cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<EmployeeIdCard>>

    @Query("SELECT * FROM employee_cards WHERE id = :cardId LIMIT 1")
    suspend fun getCardById(cardId: Long): EmployeeIdCard?

    @Query("SELECT * FROM employee_cards WHERE id = :cardId LIMIT 1")
    fun getCardByIdFlow(cardId: Long): Flow<EmployeeIdCard?>

    @Query("SELECT * FROM employee_cards WHERE fullName LIKE '%' || :query || '%' OR employeeCode LIKE '%' || :query || '%' OR branchName LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchCards(query: String): Flow<List<EmployeeIdCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: EmployeeIdCard): Long

    @androidx.room.Update
    suspend fun updateCard(card: EmployeeIdCard)

    @Delete
    suspend fun deleteCard(card: EmployeeIdCard)

    @Query("DELETE FROM employee_cards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: Long)
}
