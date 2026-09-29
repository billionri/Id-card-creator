package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EmployeeIdCard
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeCardDao {
    @Query("SELECT * FROM employee_id_cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<EmployeeIdCard>>

    @Query("SELECT * FROM employee_id_cards WHERE id = :id LIMIT 1")
    fun getCardById(id: Long): Flow<EmployeeIdCard?>

    @Query("SELECT * FROM employee_id_cards WHERE fullName LIKE '%' || :query || '%' OR employeeCode LIKE '%' || :query || '%' OR designation LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchCards(query: String): Flow<List<EmployeeIdCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: EmployeeIdCard): Long

    @Update
    suspend fun updateCard(card: EmployeeIdCard)

    @Delete
    suspend fun deleteCard(card: EmployeeIdCard)

    @Query("DELETE FROM employee_id_cards WHERE id = :id")
    suspend fun deleteCardById(id: Long)
}
