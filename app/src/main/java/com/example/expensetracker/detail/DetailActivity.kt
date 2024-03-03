package com.example.expensetracker.detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.expensetracker.add.AddActivity
import com.example.expensetracker.databinding.ActivityDetailBinding
import com.example.expensetracker.repository.ExpenseRepository

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val TAG = this.javaClass.canonicalName
    private lateinit var expenseRepository: ExpenseRepository
    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var currentTransaction = intent.getStringExtra("transaction_id").toString()
        expenseRepository = ExpenseRepository(application)

        setSupportActionBar(binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true);

        binding.menuToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.editBtn.setOnClickListener {
            clicked = true
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("transaction_id", currentTransaction)
            intent.putExtra("editClick", clicked)
            startActivity(intent)
        }

    }


    override fun onStart() {
        super.onStart()
        var transactionID = intent.getStringExtra("transaction_id").toString().toInt()
        expenseRepository.allTransactionsByDate?.observe(this) {receivedTransaction ->
            receivedTransaction.forEach { transaction ->
                if(transactionID == transaction.id){
                    binding.amount.text = transaction.amount.toString()
                    binding.category.text = transaction.category
                    binding.transactionType.text = transaction.transactionType
                    binding.description.text = transaction.description
                    binding.date.text = transaction.selectedDate.toString()
                }
            }
        }
    }

}


