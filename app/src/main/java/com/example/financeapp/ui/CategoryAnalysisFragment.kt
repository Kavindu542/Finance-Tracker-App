package com.example.financeapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.viewmodel.SettingsViewModel
import com.example.financeapp.viewmodel.TransactionViewModel

class CategoryAnalysisFragment : Fragment() {
    private val transactionViewModel: TransactionViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var expenseBtn: Button
    private lateinit var incomeBtn: Button
    private lateinit var categoryBreakdownRecycler: RecyclerView
    private lateinit var categoryBreakdownAdapter: CategoryBreakdownAdapter
    private var isExpenseMode: Boolean = true

    // Use lazy delegate to initialize categoryColorMap when context is available
    private val categoryColorMap: Map<String, Int> by lazy {
        mapOf(
            "Food" to ContextCompat.getColor(requireContext(), R.color.category_food),
            "Transport" to ContextCompat.getColor(requireContext(), R.color.category_transport),
            "Bills" to ContextCompat.getColor(requireContext(), R.color.category_bills),
            "Entertainment" to ContextCompat.getColor(requireContext(), R.color.category_entertainment),
            "Shopping" to ContextCompat.getColor(requireContext(), R.color.category_shopping),
            "Subscription" to ContextCompat.getColor(requireContext(), R.color.category_subscription),
            "Health" to ContextCompat.getColor(requireContext(), R.color.category_health),
            "Others" to ContextCompat.getColor(requireContext(), R.color.category_others)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category_analysis, container, false)

        circularProgressBar = view.findViewById(R.id.circular_progress_bar)
        expenseBtn = view.findViewById(R.id.expense_btn)
        incomeBtn = view.findViewById(R.id.income_btn)
        categoryBreakdownRecycler = view.findViewById(R.id.category_breakdown_recycler)
        val noDataText: TextView = view.findViewById(R.id.no_data_text)

        // Setup RecyclerView for category breakdown
        categoryBreakdownRecycler.layoutManager = LinearLayoutManager(context)
        categoryBreakdownAdapter = CategoryBreakdownAdapter()
        categoryBreakdownRecycler.adapter = categoryBreakdownAdapter

        // Set initial button states
        updateButtonStates()

        // Button click listeners
        expenseBtn.setOnClickListener {
            isExpenseMode = true
            updateButtonStates()
            updateChartAndBreakdown()
        }

        incomeBtn.setOnClickListener {
            isExpenseMode = false
            updateButtonStates()
            updateChartAndBreakdown()
        }

        transactionViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                circularProgressBar.visibility = View.GONE
                categoryBreakdownRecycler.visibility = View.GONE
                noDataText.visibility = View.VISIBLE
            } else {
                circularProgressBar.visibility = View.VISIBLE
                categoryBreakdownRecycler.visibility = View.VISIBLE
                noDataText.visibility = View.GONE
                updateChartAndBreakdown()
            }
        }

        return view
    }

    private fun updateButtonStates() {
        if (isExpenseMode) {
            expenseBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.light_purple)
            expenseBtn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            incomeBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.white)
            incomeBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark))
        } else {
            expenseBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.white)
            expenseBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark))
            incomeBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.light_purple)
            incomeBtn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
    }

    private fun updateChartAndBreakdown() {
        val transactions = transactionViewModel.transactions.value ?: emptyList()
        val totalExpenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }.toFloat()
        val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }.toFloat()
        val totalAmount = if (isExpenseMode) totalExpenses else totalIncome

        // Calculate category breakdown
        val breakdownData = if (isExpenseMode) {
            transactions.filter { it.type == "Expense" }
        } else {
            transactions.filter { it.type == "Income" }
        }.groupBy { it.category }
            .map { (category, categoryTransactions) ->
                val categoryAmount = categoryTransactions.sumOf { it.amount }.toFloat()
                val percentage = if (totalAmount > 0) (categoryAmount / totalAmount) * 100f else 0f
                CategoryBreakdownAdapter.CategoryBreakdownItem(
                    category = category,
                    amount = categoryAmount,
                    percentage = percentage,
                    currency = settingsViewModel.currency,
                    color = categoryColorMap[category] ?: ContextCompat.getColor(requireContext(), R.color.category_others)
                )
            }.sortedByDescending { it.amount }

        // Convert breakdown data to CircularProgressBar format
        val circularBreakdownData = breakdownData.map { item ->
            CircularProgressBar.CategoryBreakdownItem(
                category = item.category,
                amount = item.amount,
                percentage = item.percentage,
                color = item.color // Pass the custom color
            )
        }

        // Update the circular progress bar with category-wise data
        circularProgressBar.setData(
            totalAmount = totalAmount,
            categoryData = circularBreakdownData,
            currency = settingsViewModel.currency
        )

        // Update the RecyclerView with the same breakdown data
        categoryBreakdownAdapter.submitList(breakdownData)
    }
}