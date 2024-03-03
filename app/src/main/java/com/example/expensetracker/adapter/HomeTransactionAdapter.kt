package com.example.expensetracker.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.ItemHomeTransactionBinding

class HomeTransactionAdapter (
    private val context: Context,
    var transactionList:MutableList<Expense>
) :
    RecyclerView.Adapter<HomeTransactionAdapter.HomeTransactionHolder>() {

        private val limit = 4

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeTransactionHolder {
        return HomeTransactionHolder(ItemHomeTransactionBinding.inflate(LayoutInflater.from(context), parent,false))
    }

    override fun onBindViewHolder(
        holder: HomeTransactionHolder,
        position: Int
    ) {
        val currentTransaction: Expense = transactionList[position]
        holder.bind(currentTransaction)
    }

    override fun getItemCount(): Int {
        if(transactionList.size > limit){
            return limit
        }else {
            return transactionList.size
        }
    }

    inner class HomeTransactionHolder(b: ItemHomeTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        var binding: ItemHomeTransactionBinding = b

        fun bind(currentTransaction: Expense?) {
            if (currentTransaction != null) {
                if (currentTransaction.transactionType == "Income"){
                    binding.tvAmount.text = "+${currentTransaction.amount}"
                    binding.tvArrowImage.setImageResource(R.drawable.green_arrow)
                }else{
                    binding.tvAmount.text = "${currentTransaction.amount}"
                    binding.tvArrowImage.setImageResource(R.drawable.red_arrow)

                }

                binding.tvCategory.text = currentTransaction.category
            }

        }
    }
}