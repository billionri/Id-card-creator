package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employee_cards")
data class EmployeeIdCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String = "Rashid Alam",
    val employeeCode: String = "DTDC/HE/2026/8941",
    val designation: String = "Delivery Associate",
    val department: String = "Express Logistics & Dispatch",
    val branchId: String = "BOM/HE/1057",
    val branchName: String = "Hariom Enterprises",
    val branchAddress: String = "Shop No 2, 317-A, Shinde Niwas, Kasturba Cross Road No 6, Opposite Platform No 10, Borivali East, Mumbai — 400066, Maharashtra",
    val phoneNumber: String = "+91 98765 43210",
    val emergencyContact: String = "+91 98111 22334",
    val bloodGroup: String = "O+",
    val validTill: String = "31-DEC-2027",
    val photoPath: String? = null,
    val aiVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
