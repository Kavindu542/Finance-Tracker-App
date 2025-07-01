package com.example.financeapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.data.Transaction

class TransactionAdapter(
    private val currency: String,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private var transactions: List<Transaction> = emptyList()

    fun submitList(newList: List<Transaction>) {
        transactions = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.category.text = transaction.category
        holder.title.text = transaction.title
        holder.time.text = transaction.time

        // Set the amount with a negative sign for expenses, using the passed currency
        val amountText = if (transaction.type == "Expense") {
            "-$currency ${String.format("%.2f", transaction.amount)}"
        } else {
            "+$currency ${String.format("%.2f", transaction.amount)}"
        }
        holder.amount.text = amountText

        // Set the amount color based on type
        val color = if (transaction.type == "Income") R.color.income else R.color.expense
        holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, color))

        // Set the category icon
        val iconResId = when (transaction.category) {
            "Shopping" -> R.drawable.shopping
            "Subscription" -> R.drawable.subscription
            "Food" -> R.drawable.food
            "Transport" -> R.drawable.transport
            "Bills" -> R.drawable.bills
            "Entertainment" -> R.drawable.entertainment
            else -> R.drawable.others
        }
        holder.categoryIcon.setImageResource(iconResId)

        holder.itemView.setOnClickListener { onEdit(transaction) }
        holder.itemView.setOnLongClickListener {
            // Show confirmation dialog before deletion
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Yes") { _, _ ->
                    onDelete(transaction)
                }
                .setNegativeButton("No", null)
                .setCancelable(true)
                .show()
            true
        }
    }

    override fun getItemCount(): Int = transactions.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryIcon: ImageView = itemView.findViewById(R.id.category_icon)
        val category: TextView = itemView.findViewById(R.id.transaction_category)
        val title: TextView = itemView.findViewById(R.id.transaction_title)
        val amount: TextView = itemView.findViewById(R.id.transaction_amount)
        val time: TextView = itemView.findViewById(R.id.transaction_time)
    }
}