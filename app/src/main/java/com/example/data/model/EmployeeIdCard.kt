package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employee_cards")
data class EmployeeIdCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String = "Rashid Alam",
    val employeeCode: String = "DTDC/HE/2024/1609",
    val designation: String = "Delivery Associate",
    val department: String = "Hub Dispatch Operations",
    val branchId: String = "DTDC-HE-4102",
    val branchName: String = "Hariom Enterprises",
    val branchAddress: String = "Shop No 2, 317-A, Shinde Niwas, Kasturba Cross Road No 6, Opposite Platform No 10, Borivali East, Mumbai — 400066, Maharashtra",
    val phoneNumber: String = "8655707563",
    val emergencyContact: String = "8655707563",
    val bloodGroup: String = "B+",
    val validTill: String = "31 Dec 2027",
    val photoPath: String? = null,
    val aiVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
