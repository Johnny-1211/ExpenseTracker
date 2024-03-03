package com.example.expensetracker.add

import android.app.DatePickerDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.R
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.ActivityAddBinding
import com.example.expensetracker.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private lateinit var expenseRepository : ExpenseRepository

    private val categoryList = listOf("House","Food","Social","Clothes", "Transportation", "Other")
    private val transactionTypeList = listOf("Income", "Expense")
    private lateinit var currentTransaction : Expense

    var amount = 0.0
    var category = ""
    var transactionType = ""
    var description = ""
    var transactionDate = LocalDate.now()
    var currentTransactionID = 0
    private var clicked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clicked = intent.getBooleanExtra("editClick", false)

        if(clicked){
            binding.btnSaveTransaction.text = "Update"
        }

        expenseRepository = ExpenseRepository(application)

        val categoryAdapter = ArrayAdapter(this,
            R.layout.dropdown_item_layout,
            categoryList
        )

        val transactionTypeAdapter = ArrayAdapter(this,
            R.layout.dropdown_item_layout,
            transactionTypeList)

        binding.categoryFilledExposedDropdown.setAdapter(categoryAdapter)
        binding.transactionTypeFilledExposedDropdown.setAdapter(transactionTypeAdapter)

        binding.categoryFilledExposedDropdown.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.categoryInputLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.transactionTypeFilledExposedDropdown.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.transactionTypeInputLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.calendarEditText.setOnClickListener {
            val datePicker = DatePickerFragment()
            datePicker.show(supportFragmentManager, "transaction_date_picker")
        }

        binding.btnSaveTransaction.setOnClickListener {
            val clicked = intent.getBooleanExtra("editClick", false)
            if(clicked){
                updateExpense()
            }else{
                saveExpense()
            }

        }
    }

    override fun onStart() {
        super.onStart()
        expenseRepository.allTransactionsByDate?.observe(this) {receivedTransaction ->
            Log.d("currentTransactionID", intent.getIntExtra("transaction_id", 0).toString())
            currentTransactionID = intent.getIntExtra("transaction_id", -1)
            if (currentTransactionID != -1){
                if (receivedTransaction.isNotEmpty()) {
                    receivedTransaction?.forEach { transaction ->
                        if (currentTransactionID == transaction.id) {
                            currentTransaction = transaction
                        }
                    }
                }
            }
        }
    }

    class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener{

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            return DatePickerDialog(this.requireActivity(), this, year, month, day)
        }

        override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            val myDate = LocalDate.of(year, month+1, dayOfMonth)
            (this.requireActivity() as AddActivity).binding.calendarEditText.setText(myDate.toString())
            (this.requireActivity() as AddActivity).transactionDate = myDate
        }
    }

    private fun saveExpense() {
        val selectedCategory = binding.categoryFilledExposedDropdown.text.toString()
        val selectedTransactionType = binding.transactionTypeFilledExposedDropdown.text.toString()

        if(selectedCategory.isEmpty()){
            binding.categoryInputLayout.error = "Category must not be empty"
            return
        }else{
            category = binding.categoryInputLayout.editText?.text.toString()
        }

        if (selectedTransactionType.isEmpty()){
            binding.transactionTypeInputLayout.error = "Transaction Type must not be empty"
            return
        }else{
            transactionType = binding.transactionTypeInputLayout.editText?.text.toString()
        }

        if(binding.descriptionEditText.text.toString().isEmpty()){
            binding.descriptionEditText.error = "Description must not be empty"
            return
        }else {
            description = binding.descriptionEditText.text.toString()
        }

        if(binding.calendarEditText.text.toString().isEmpty()){
            binding.calendarEditText.error = "Date can not be empty"
            return
        }

        if(binding.amountEditText.text.toString().isEmpty()){
            binding.amountEditText.error = "Amount must not be empty"
            return
        } else if(transactionType == "Income") {
            amount = +binding.amountEditText.text.toString().toDouble()
        } else{
            amount = -binding.amountEditText.text.toString().toDouble()
        }

        val newTransaction = Expense(amount, category, transactionType, description, transactionDate)

        lifecycleScope.launch {
            expenseRepository.insertTransaction(newTransaction)
        }

        Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show()
        binding.amountEditText.text?.clear()
        binding.categoryFilledExposedDropdown.text.clear()
        binding.transactionTypeFilledExposedDropdown.text.clear()
        binding.descriptionEditText.text?.clear()
        binding.calendarEditText.text?.clear()
    }

    private fun updateExpense(){
        val selectedCategory = binding.categoryFilledExposedDropdown.text.toString()
        val selectedTransactionType = binding.transactionTypeFilledExposedDropdown.text.toString()
        Log.d("currentTransaction_Clicked", "$currentTransaction" )

        category = if(selectedCategory.isEmpty()){
            currentTransaction.category
        }else{
            binding.categoryInputLayout.editText?.text.toString()
        }

        transactionType = if (selectedTransactionType.isEmpty()){
            currentTransaction.transactionType
        }else{
            binding.transactionTypeInputLayout.editText?.text.toString()
        }

        description = if(binding.descriptionEditText.text.toString().isEmpty()){
            currentTransaction.transactionType
        }else {
            binding.descriptionEditText.text.toString()
        }

        if(binding.calendarEditText.text.toString().isEmpty()){
            transactionDate = currentTransaction.selectedDate
        }

        amount = if(binding.amountEditText.text.toString().isEmpty()){
            currentTransaction.amount.toDouble()
        } else if(transactionType == "Income") {
            +binding.amountEditText.text.toString().toDouble()
        } else{
            -binding.amountEditText.text.toString().toDouble()
        }

        lifecycleScope.launch {
            expenseRepository.updateTransaction(currentTransactionID.toInt(),amount.toFloat(), category, transactionType, description, transactionDate.toString())
        }

        Toast.makeText(this, "Update Successfully", Toast.LENGTH_SHORT).show()
        binding.amountEditText.text?.clear()
        binding.categoryFilledExposedDropdown.text.clear()
        binding.transactionTypeFilledExposedDropdown.text.clear()
        binding.descriptionEditText.text?.clear()
        binding.calendarEditText.text?.clear()

    }
}

