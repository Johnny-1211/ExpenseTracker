package com.example.expensetracker.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.add.AddActivity
import com.example.expensetracker.R
import com.example.expensetracker.report.ReportActivity
import com.example.expensetracker.transaction.TransactionActivity
import com.example.expensetracker.adapter.HomeTransactionAdapter
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.ActivityHomeBinding
import com.example.expensetracker.repository.ExpenseRepository
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val TAG = this.javaClass.canonicalName
    private lateinit var homeTransactionList : MutableList<Expense>
    private lateinit var transactionHomeAdapter: HomeTransactionAdapter
    private lateinit var expenseRepository: ExpenseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseRepository = ExpenseRepository(application)

        setSupportActionBar(binding.homeMenuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)



        binding.bottomNavigation.setOnItemSelectedListener {item ->
            when (item.itemId) {
                R.id.dashboard -> {
                    val intent = Intent(this@HomeActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.transaction -> {
                    val intent = Intent(this@HomeActivity, TransactionActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.report -> {
                    val intent = Intent(this@HomeActivity, ReportActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }

        binding.btnAddTransaction.setOnClickListener {
            val intent = Intent(this@HomeActivity, AddActivity::class.java)
            startActivity(intent)
        }

        binding.btnViewAll.setOnClickListener {
            val intent = Intent(this@HomeActivity, TransactionActivity::class.java)
            startActivity(intent)
        }


        homeTransactionList = mutableListOf()
        transactionHomeAdapter = HomeTransactionAdapter(applicationContext,homeTransactionList)
        binding.rvHomeTransaction .adapter = transactionHomeAdapter
        binding.rvHomeTransaction.layoutManager = LinearLayoutManager(this)
        binding.rvHomeTransaction.setHasFixedSize(true)


    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStart() {
        super.onStart()
        expenseRepository.allTransactionsByDate?.observe(this) { receivedTransactions ->
            if (receivedTransactions.isNotEmpty()) {
                homeTransactionList.clear()
                lifecycleScope.launch {
                    receivedTransactions.forEach { transaction ->
                        if (homeTransactionList.contains(transaction)) {
                            Log.d(TAG, "onStart: object already present the list")
                        } else {
                            homeTransactionList.add(transaction)
                        }
                    }
                    Log.d(TAG, "homeTransactionList: ${homeTransactionList}")
                    homeTransactionList.reverse()
                    transactionHomeAdapter.notifyDataSetChanged()
                }
            } else {
                Log.d(TAG, "onStart: No data received from observer")
            }
        }

        this.expenseRepository.totalAmount?.observe(this){receivedTotalAmount ->
            binding.balance.text = receivedTotalAmount.toString()
        }

        this.expenseRepository.incomeTotalAmount?.observe(this){ receivedTotalIncome ->
            binding.totalIncome.text = receivedTotalIncome.toString()
        }

        this.expenseRepository.expenseTotalAmount?.observe(this){ receivedTotalExpense->
            var totalExpense = receivedTotalExpense.toString()
            if (totalExpense.contains("-")){
                totalExpense = totalExpense.replace("-","")
            }
            binding.totalExpense.text = totalExpense
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }




}