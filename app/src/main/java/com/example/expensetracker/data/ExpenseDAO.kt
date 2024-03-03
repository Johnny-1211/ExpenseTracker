package com.example.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpenseDAO {

    @Insert
    fun insertTransaction(newTransaction: Expense)

    @Query("UPDATE table_expense " +
            "SET amount = :amount, " +
            "category = :category, " +
            "transactionType = :transactionType, " +
            "description = :desc, " +
            "selectedDate = :date " +
            "WHERE id = :id")
    fun updateTransaction(id:Int, amount:Float, category: String, transactionType: String, desc:String, date:String)

    @Delete
    fun deleteTransaction(transaction : Expense)

    @Query("DELETE FROM table_expense")
    fun deleteAllTransaction()

    @Query("SELECT * FROM table_expense ORDER BY selectedDate")
    fun getAllTransactionByDate() : LiveData<List<Expense>>

    @Query("SELECT SUM(amount) FROM table_expense")
    fun getTotalAmount(): LiveData<Double>

    @Query("SELECT SUM(amount) FROM table_expense WHERE transactionType = :transactionType ")
    fun getTotalAmountByType(transactionType:String) : LiveData<Double>

    @Query("SELECT SUM(amount) FROM table_expense WHERE category = :category ")
    fun getTotalAmountByCategory(category:String) : LiveData<Double>

    @Query("SELECT * FROM table_expense WHERE transactionType = :transactionType ")
    fun getAllTransactionByType(transactionType:String) : LiveData<List<Expense>>




}