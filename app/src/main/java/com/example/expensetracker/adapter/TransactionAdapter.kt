package com.example.expensetracker.adapter

import android.R.string
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.ItemTransactionBinding
import java.util.Locale
import android.widget.Filter
import android.widget.Filterable


class TransactionAdapter(
    private val context:Context,
    var transactionList:MutableList<Expense>,
    private val rowClicked: (Int) -> Unit
) :
    RecyclerView.Adapter<TransactionAdapter.TransactionHolder>(), Filterable{

    var filteredList: List<Expense> = transactionList


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionHolder {
        return TransactionHolder(ItemTransactionBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(
        holder: TransactionHolder,
        position: Int
    ) {
        val currentTransaction: Expense = filteredList[position]
        holder.bind(currentTransaction)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    inner class TransactionHolder(b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root){
        var binding: ItemTransactionBinding = b

        @SuppressLint("SetTextI18n")
        fun bind(currentTransaction: Expense?) {
            if (currentTransaction != null) {

                if(currentTransaction.transactionType == "Income"){
                    binding.rvAmount.text = "+${currentTransaction.amount}"
                    binding.rvAmount.setTextColor(Color.parseColor("#4CAF50"))
                }else{
                    binding.rvAmount.text = "${currentTransaction.amount}"
                    binding.rvAmount.setTextColor(Color.parseColor("#FF0000"))
                }

                binding.rvCategory.text = currentTransaction.category
                binding.rvDescription.text = currentTransaction.description
                binding.rvDate.text = currentTransaction.selectedDate.toString()
            }
            itemView.setOnClickListener {
                rowClicked(adapterPosition)
                Log.d("rowClick" , "adapterPosition : $adapterPosition")
                Log.d("rowClick" , "currentTransaction: $currentTransaction")
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrEmpty() || constraint == "All") {
                    transactionList
                } else {
                    transactionList.filter { it.category == constraint.toString() }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<Expense> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    fun filter(category: String) {
        val filter = filter
        filter.filter(category)
    }
}