package com.example.financeapp.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.data.Transaction
import com.example.financeapp.viewmodel.SettingsViewModel
import com.example.financeapp.viewmodel.TransactionViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TransactionListFragment : Fragment() {
    private val transactionViewModel: TransactionViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private lateinit var adapter: TransactionListAdapter
    private var filterType: String? = null // "Income", "Expense", "Transfer", or null
    private var sortOrder: String? = null // "Highest", "Lowest", "Newest", "Oldest", or null
    private var selectedCategories: MutableSet<String> = mutableSetOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.transaction_recycler)
        val filterBtn = view.findViewById<TextView>(R.id.filter_btn)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TransactionListAdapter(
            currency = settingsViewModel.currency,
            onEdit = { transaction ->
                val action = TransactionListFragmentDirections.actionListToAddEdit(transactionId = transaction.id)
                findNavController().navigate(action)
            },
            onDelete = { transaction ->
                transactionViewModel.deleteTransaction(transaction.id)
            }
        )
        recyclerView.adapter = adapter

        // Observe transactions and apply filters
        transactionViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            applyFiltersAndSort(transactions)
        }

        // Filter button click listener
        filterBtn.setOnClickListener {
            showFilterDialog()
        }

        view.findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            val action = TransactionListFragmentDirections.actionListToAddEdit(type = "Expense")
            findNavController().navigate(action)
        }

        return view
    }

    private fun applyFiltersAndSort(transactions: List<Transaction>) {
        var filteredTransactions = transactions

        // Filter by type
        if (filterType != null) {
            filteredTransactions = filteredTransactions.filter { transaction ->
                when (filterType) {
                    "Income" -> transaction.type == "Income"
                    "Expense" -> transaction.type == "Expense"
                    "Transfer" -> false // Not implemented in this app
                    else -> true
                }
            }
        }

        // Filter by category
        if (selectedCategories.isNotEmpty()) {
            filteredTransactions = filteredTransactions.filter { it.category in selectedCategories }
        }

        // Sort transactions
        filteredTransactions = when (sortOrder) {
            "Highest" -> filteredTransactions.sortedByDescending { it.amount }
            "Lowest" -> filteredTransactions.sortedBy { it.amount }
            "Newest" -> filteredTransactions.sortedByDescending { it.timestamp }
            "Oldest" -> filteredTransactions.sortedBy { it.timestamp }
            else -> filteredTransactions
        }

        adapter.submitList(filteredTransactions)
    }

    private fun showFilterDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_filter_transactions)
        dialog.window?.attributes?.windowAnimations = R.style.DialogSlideAnimation

        // Filter by type
        val typeRadioGroup: RadioGroup = dialog.findViewById(R.id.type_radio_group)
        val incomeRadio: RadioButton = dialog.findViewById(R.id.income_radio)
        val expenseRadio: RadioButton = dialog.findViewById(R.id.expense_radio)
        val transferRadio: RadioButton = dialog.findViewById(R.id.transfer_radio)

        when (filterType) {
            "Income" -> incomeRadio.isChecked = true
            "Expense" -> expenseRadio.isChecked = true
            "Transfer" -> transferRadio.isChecked = true
        }

        // Sort by
        val sortRadioGroup: RadioGroup = dialog.findViewById(R.id.sort_radio_group)
        val highestRadio: RadioButton = dialog.findViewById(R.id.highest_radio)
        val lowestRadio: RadioButton = dialog.findViewById(R.id.lowest_radio)
        val newestRadio: RadioButton = dialog.findViewById(R.id.newest_radio)
        val oldestRadio: RadioButton = dialog.findViewById(R.id.oldest_radio)

        when (sortOrder) {
            "Highest" -> highestRadio.isChecked = true
            "Lowest" -> lowestRadio.isChecked = true
            "Newest" -> newestRadio.isChecked = true
            "Oldest" -> oldestRadio.isChecked = true
        }

        // Category selection
        val categorySpinner: Spinner = dialog.findViewById(R.id.category_spinner)
        val categories = arrayOf("All Categories", "Food", "Transport", "Bills", "Entertainment", "Shopping", "Subscription", "Others")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        categorySpinner.adapter = adapter

        // Reset button
        val resetBtn: TextView = dialog.findViewById(R.id.reset_btn)
        resetBtn.setOnClickListener {
            filterType = null
            sortOrder = null
            selectedCategories.clear()
            typeRadioGroup.clearCheck()
            sortRadioGroup.clearCheck()
            categorySpinner.setSelection(0)
            applyFiltersAndSort(transactionViewModel.transactions.value ?: emptyList())
            dialog.dismiss()
        }

        // Apply button
        val applyBtn: Button = dialog.findViewById(R.id.apply_btn)
        applyBtn.setOnClickListener {
            // Get selected type
            filterType = when (typeRadioGroup.checkedRadioButtonId) {
                R.id.income_radio -> "Income"
                R.id.expense_radio -> "Expense"
                R.id.transfer_radio -> "Transfer"
                else -> null
            }

            // Get selected sort order
            sortOrder = when (sortRadioGroup.checkedRadioButtonId) {
                R.id.highest_radio -> "Highest"
                R.id.lowest_radio -> "Lowest"
                R.id.newest_radio -> "Newest"
                R.id.oldest_radio -> "Oldest"
                else -> null
            }

            // Get selected categories
            val selectedCategory = categorySpinner.selectedItem.toString()
            selectedCategories.clear()
            if (selectedCategory != "All Categories") {
                selectedCategories.add(selectedCategory)
            }

            applyFiltersAndSort(transactionViewModel.transactions.value ?: emptyList())
            dialog.dismiss()
        }

        dialog.show()
    }
}