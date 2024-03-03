package com.example.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

//Entity
@Entity(tableName = "table_expense")
class Expense(
    val amount: Double,
    val category: String,
    val transactionType: String,
    val description: String,
    val selectedDate: LocalDate = LocalDate.now()
) {
    companion object{
        var houseTotalAmount = 0.0
        var foodTotalAmount = 0.0
        var socialTotalAmount = 0.0
        var clothesTotalAmount = 0.0
        var transTotalAmount = 0.0
        var otherTotalAmount = 0.0
    }


    @PrimaryKey(autoGenerate = true)
    var id = 0
    override fun toString(): String {
        return "Expense(amount=$amount, " +
                "category='$category'," +
                "transactionType='$transactionType'," +
                " description='$description', " +
                "selectedDate='$selectedDate'," +
                "id=$id)"
    }
}
