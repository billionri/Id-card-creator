package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employee_id_cards")
data class EmployeeIdCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val employeeCode: String,
    val designation: String,
    val department: String,
    val branchName: String = "Hariom Enterprises",
    val branchCode: String = "DTDC-HE-4102",
    val phoneNumber: String,
    val emergencyContact: String,
    val bloodGroup: String,
    val dateOfJoining: String,
    val validTill: String,
    val aadhaarMasked: String = "XXXX-XXXX-8921",
    val branchAddress: String = "Shop No 2, 317-A, Shinde Niwas, Kasturba Cross Road No 6, Opposite Platform No 10, Borivali East, Mumbai — 400066, Maharashtra",
    val photoPath: String? = null,
    val aiVerified: Boolean = false,
    val aiNote: String = "AI ID Assessment: Photo verified for official badge",
    val createdAt: Long = System.currentTimeMillis()
)
