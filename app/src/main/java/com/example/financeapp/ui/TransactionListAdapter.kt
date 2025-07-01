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
import java.text.SimpleDateFormat
import java.util.*

class TransactionListAdapter(
    private val currency: String,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var groupedTransactions: List<TransactionListItem> = emptyList()

    // Define view types
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    // Sealed class to represent either a header or a transaction item
    sealed class TransactionListItem {
        data class Header(val date: String) : TransactionListItem()
        data class Item(val transaction: Transaction) : TransactionListItem()
    }

    fun submitList(transactions: List<Transaction>) {
        // Group transactions by date
        val today = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val yesterday = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.time)

        val grouped = transactions.groupBy { transaction ->
            when (transaction.date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> transaction.date
            }
        }.flatMap { (date, transactionsForDate) ->
            listOf(TransactionListItem.Header(date)) + transactionsForDate.map { TransactionListItem.Item(it) }
        }

        groupedTransactions = grouped
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (groupedTransactions[position]) {
            is TransactionListItem.Header -> TYPE_HEADER
            is TransactionListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction, parent, false)
                TransactionViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = groupedTransactions[position]) {
            is TransactionListItem.Header -> {
                (holder as HeaderViewHolder).bind(item.date)
            }
            is TransactionListItem.Item -> {
                (holder as TransactionViewHolder).bind(item.transaction)
            }
        }
    }

    override fun getItemCount(): Int = groupedTransactions.size

    // ViewHolder for section headers
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView = itemView.findViewById(R.id.header_text)

        fun bind(date: String) {
            headerText.text = date
        }
    }

    // ViewHolder for transaction items
    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.category_icon)
        private val category: TextView = itemView.findViewById(R.id.transaction_category)
        private val title: TextView = itemView.findViewById(R.id.transaction_title)
        private val amount: TextView = itemView.findViewById(R.id.transaction_amount)
        private val time: TextView = itemView.findViewById(R.id.transaction_time)

        fun bind(transaction: Transaction) {
            category.text = transaction.category
            title.text = transaction.title
            time.text = transaction.time

            // Set the amount with a sign based on type
            val amountText = if (transaction.type == "Expense") {
                "-$currency ${String.format("%.2f", transaction.amount)}"
            } else {
                "+$currency ${String.format("%.2f", transaction.amount)}"
            }
            amount.text = amountText

            // Set the amount color based on type
            val color = if (transaction.type == "Income") R.color.income else R.color.expense
            amount.setTextColor(ContextCompat.getColor(itemView.context, color))

            // Set the category icon
            val iconResId = when (transaction.category) {
                "Shopping" -> R.drawable.shopping
                "Subscription" -> R.drawable.subscription
                "Food" -> R.drawable.food
                "Transport" -> R.drawable.transport
                "Bills" -> R.drawable.bills
                "Entertainment" -> R.drawable.entertainment
                "Health" -> R.drawable.entertainment
                else -> R.drawable.others
            }
            categoryIcon.setImageResource(iconResId)

            itemView.setOnClickListener { onEdit(transaction) }
            itemView.setOnLongClickListener {
                // Show confirmation dialog before deletion
                AlertDialog.Builder(itemView.context)
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
    }
}