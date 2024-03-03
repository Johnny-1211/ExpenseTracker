package com.example.expensetracker.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.expensetracker.data.AppDB
import com.example.expensetracker.data.Expense

class ExpenseRepository(application: Application) {

    //obtain the instance of the database and notesDAO
    private var db : AppDB? = null
    private var expenseDAO = AppDB.getDB(application)?.expenseDAO()

    var allTransactionsByDate : LiveData<List<Expense>>? = expenseDAO?.getAllTransactionByDate()
    var totalAmount : LiveData<Double>? = expenseDAO?.getTotalAmount()
    var incomeTotalAmount : LiveData<Double>? = expenseDAO?.getTotalAmountByType("Income")
    var expenseTotalAmount : LiveData<Double>? = expenseDAO?.getTotalAmountByType("Expense")
    var incomeTransactionListByType : LiveData<List<Expense>>? = expenseDAO?.getAllTransactionByType("Income")
    var expenseTransactionListByType : LiveData<List<Expense>>? = expenseDAO?.getAllTransactionByType("Expense")


    init {
        this.db = AppDB.getDB(application)
    }

    fun insertTransaction(transactionToInsert : Expense){
        AppDB.databaseQueryExecutor.execute {
            this.expenseDAO?.insertTransaction(transactionToInsert)
        }
    }

    fun updateTransaction(id:Int, amount:Float, category: String, transactionType: String, desc:String, date:String){
        AppDB.databaseQueryExecutor.execute {
            this.expenseDAO?.updateTransaction(id,amount,category,transactionType,desc,date)
        }
    }

    fun deleteTransaction(transactionToDelete: Expense){
        AppDB.databaseQueryExecutor.execute {
            this.expenseDAO?.deleteTransaction(transactionToDelete)
        }
    }


}