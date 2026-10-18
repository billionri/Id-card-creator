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
    val branchId: String = "DEL/HE/1029",
    val branchName: String = "Hariom Enterprises",
    val branchAddress: String = "Shop 4, Main Market, Sector 12, Dwarka, New Delhi 110075",
    val phoneNumber: String = "+91 98765 43210",
    val emergencyContact: String = "+91 98111 22334",
    val bloodGroup: String = "O+",
    val validTill: String = "31-DEC-2027",
    val photoPath: String? = null,
    val aiVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
