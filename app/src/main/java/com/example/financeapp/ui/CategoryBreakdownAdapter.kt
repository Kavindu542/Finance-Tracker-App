package com.example.financeapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R

class CategoryBreakdownAdapter : RecyclerView.Adapter<CategoryBreakdownAdapter.ViewHolder>() {

    private var categoryData: List<CategoryBreakdownItem> = emptyList()

    data class CategoryBreakdownItem(
        val category: String,
        val amount: Float,
        val percentage: Float,
        val currency: String,
        val color: Int // Added color field
    )

    fun submitList(data: List<CategoryBreakdownItem>) {
        categoryData = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_breakdown, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categoryData[position]
        holder.category.text = item.category
        holder.amount.text = "${item.currency}${String.format("%.2f", item.amount)}"
        holder.percentage.text = "${String.format("%.1f", item.percentage)}%"
        holder.colorIndicator.setBackgroundColor(item.color)
    }

    override fun getItemCount(): Int = categoryData.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = itemView.findViewById(R.id.category_name)
        val amount: TextView = itemView.findViewById(R.id.category_amount)
        val percentage: TextView = itemView.findViewById(R.id.category_percentage)
        val colorIndicator: View = itemView.findViewById(R.id.color_indicator)
    }
}