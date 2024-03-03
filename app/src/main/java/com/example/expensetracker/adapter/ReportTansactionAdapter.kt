package com.example.expensetracker.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.ItemHomeTransactionBinding
import com.example.expensetracker.databinding.ItemReportTransactionBinding

class ReportTansactionAdapter(
    private val context: Context,
    var transactionList:MutableList<Expense>
) :
    RecyclerView.Adapter<ReportTansactionAdapter.ReportTransactionHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReportTansactionAdapter.ReportTransactionHolder {
        return ReportTransactionHolder(ItemReportTransactionBinding.inflate(LayoutInflater.from(context), parent,false))
    }

    override fun onBindViewHolder(
        holder: ReportTansactionAdapter.ReportTransactionHolder,
        position: Int
    ) {
        val currentTransaction: Expense = transactionList[position]
        holder.bind(currentTransaction)
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    inner class ReportTransactionHolder(b: ItemReportTransactionBinding) : RecyclerView.ViewHolder(b.root){
        var binding: ItemReportTransactionBinding = b

        fun bind(currentTransaction : Expense?) {
            if (currentTransaction != null) {
                binding.tvCategoryTitle.text = currentTransaction.category
                binding.tvAmount.text = "$${currentTransaction.amount}"
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData:MutableList<Expense>){
        transactionList = newData
        notifyDataSetChanged()
    }
}