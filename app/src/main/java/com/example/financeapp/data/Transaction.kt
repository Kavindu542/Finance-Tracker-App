package com.example.financeapp.data

import java.io.Serializable

data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val timestamp: String?, // Make timestamp nullable
    val type: String // "Income" or "Expense"
) : Serializable {
    // Helper properties to extract date and time
    val date: String
        get() = timestamp?.substringBefore(" ") ?: "Unknown Date"

    val time: String
        get() {
            if (timestamp == null) return "Unknown Time"
            val timePart = timestamp.substringAfter(" ").substringBeforeLast(":") // e.g., "10:00"
            val hour = timePart.substringBefore(":").toInt()
            val minute = timePart.substringAfter(":")
            val amPm = if (hour >= 12) "PM" else "AM"
            val adjustedHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            return "$adjustedHour:$minute $amPm" // e.g., "10:00 AM"
        }
}