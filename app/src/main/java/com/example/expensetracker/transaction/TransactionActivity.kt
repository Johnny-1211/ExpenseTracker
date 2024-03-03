package com.example.expensetracker.transaction

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.adapter.TransactionAdapter
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.ActivityTransactionBinding
import com.example.expensetracker.detail.DetailActivity
import com.example.expensetracker.repository.ExpenseRepository
import kotlinx.coroutines.launch


class TransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionBinding
    private val TAG = this.javaClass.canonicalName
    private lateinit var transactionList : MutableList<Expense>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var expenseRepository: ExpenseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseRepository = ExpenseRepository(application)

        setSupportActionBar(binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true);

        binding.menuToolbar.setNavigationOnClickListener {
            finish()
        }

        transactionList = mutableListOf()
        transactionAdapter = TransactionAdapter(
            applicationContext,
            transactionList)
        { pos -> rowClicked(pos) }
        binding.rvTransaction.adapter = transactionAdapter
        binding.rvTransaction.layoutManager = LinearLayoutManager(this)
        binding.rvTransaction.setHasFixedSize(true)

        //filter spinner
        val filterOptions = listOf("All", "House","Food","Social","Clothes", "Transportation", "Other")
        val filterAdapter = ArrayAdapter(this,
            R.layout.dropdown_item_layout,
            filterOptions)

        binding.categoryFilterFilledExposedDropdown.setAdapter(filterAdapter)

        binding.categoryFilterFilledExposedDropdown.addTextChangedListener ( object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.categoryFilterLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        (binding.categoryFilterLayout.editText as AutoCompleteTextView).onItemClickListener =
            OnItemClickListener { adapterView, view, position, id ->
                val selectedValue: String = filterOptions[position]
                transactionAdapter.filter(selectedValue)
            }

        //swipe to delete
        val simpleCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if (direction == ItemTouchHelper.LEFT) {
                        SwipeToDeleteTransaction(viewHolder.adapterPosition)
                    }
                }

                override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                    return 1f
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    setDeleteIcon(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive)
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

        val helper = ItemTouchHelper(simpleCallback)
        helper.attachToRecyclerView(binding.rvTransaction)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStart() {
        super.onStart()
        expenseRepository.allTransactionsByDate?.observe(this) { receivedTransactions ->
            if (receivedTransactions.isNotEmpty()) {
                transactionList.clear()
                lifecycleScope.launch {
                    receivedTransactions.forEach { transaction ->
                        if (transactionList.contains(transaction)) {
                            Log.d(TAG, "onStart: object already present the list")
                        } else {
                            transactionList.add(transaction)
                        }
                    }
                    transactionAdapter.notifyDataSetChanged()
                }
            } else {
                Log.d(TAG, "onStart: No data received from observer")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.dashboard -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun rowClicked(position: Int){
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("transaction_id", "${transactionList[position].id}")
        startActivity(intent)
    }

    private fun SwipeToDeleteTransaction(position: Int) {
        val confirmDialog = AlertDialog.Builder(this)
        confirmDialog.setTitle("Delete")
        confirmDialog.setMessage("Are you sure you want to delete this transaction?")
        confirmDialog.setNegativeButton("Cancel") { dialogInterface, i ->
            transactionAdapter.notifyDataSetChanged()
            dialogInterface.dismiss()
        }
        confirmDialog.setPositiveButton("Yes") { dialogInterface, i ->
            lifecycleScope.launch {
                expenseRepository.deleteTransaction(transactionList[position])
            }
        }
        confirmDialog.show()

    }

    //delete icon
    private fun setDeleteIcon(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean){
        var mClearPaint = Paint()
        mClearPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        var mBackground = GradientDrawable()

        mBackground.cornerRadius = 40f
        var deleteDrawable = ContextCompat.getDrawable(this, R.drawable.ic_baseline_delete)
        var intrinsicWidth = 120
        var intrinsicHeight = 120
        var itemView = viewHolder.itemView
        var itemHeight = itemView.height

        var isCancelled = dX.toInt() == 0 && !isCurrentlyActive
        if(isCancelled){
            c.drawRect(itemView.right + dX, itemView.top.toFloat(),
            itemView.right.toFloat(), itemView.bottom.toFloat(), mClearPaint)
            return
        }

        val colorStateList = ColorStateList.valueOf(Color.RED)
        mBackground.color = colorStateList
        mBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        mBackground.draw(c)

        var deleteIconTop = itemView.top + (itemHeight - intrinsicHeight!!) / 2
        var deleteIconMargin = (itemHeight - intrinsicHeight) /2
        var deletIconLeft = itemView.right - deleteIconMargin - intrinsicWidth!!
        var deletIconRight = itemView.right - deleteIconMargin
        var deletIconBottom = deleteIconTop + intrinsicHeight

        deleteDrawable?.setBounds(deletIconLeft,deleteIconTop,deletIconRight,deletIconBottom)
        deleteDrawable?.draw(c)

    }
}