package com.example.financeapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.viewmodel.SettingsViewModel
import com.example.financeapp.viewmodel.TransactionViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private val transactionViewModel: TransactionViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    // Launcher to request notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            sendBudgetAlertNotification()
        }
    }

    private lateinit var spendFrequencyChart: LineChart
    private lateinit var tabToday: Button
    private lateinit var tabWeek: Button
    private lateinit var tabMonth: Button
    private lateinit var tabYear: Button
    private lateinit var recentTransactionsRecycler: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var seeAllBtn: TextView
    private var currentChartPeriod: String = "Week"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val balanceText: TextView = view.findViewById(R.id.balance_text)
        val budgetProgress: ProgressBar = view.findViewById(R.id.budget_progress)
        val budgetText: TextView = view.findViewById(R.id.budget_text)
        val incomeCard: View = view.findViewById(R.id.income_card_layout)
        val expenseCard: View = view.findViewById(R.id.expense_card_layout)
        val incomeAmountText: TextView = incomeCard.findViewById(R.id.income_amount)
        val expenseAmountText: TextView = expenseCard.findViewById(R.id.expense_amount)
        spendFrequencyChart = view.findViewById(R.id.spend_frequency_chart)
        tabToday = view.findViewById(R.id.tab_today)
        tabWeek = view.findViewById(R.id.tab_week)
        tabMonth = view.findViewById(R.id.tab_month)
        tabYear = view.findViewById(R.id.tab_year)
        recentTransactionsRecycler = view.findViewById(R.id.recent_transactions_list)
        seeAllBtn = view.findViewById(R.id.see_all_btn)

        // Setup the RecyclerView adapter immediately
        recentTransactionsRecycler.layoutManager = LinearLayoutManager(context)
        transactionAdapter = TransactionAdapter(
            currency = settingsViewModel.currency, // Pass the initial currency
            onEdit = { transaction ->
                val action = HomeFragmentDirections.actionHomeToAddEdit(transactionId = transaction.id)
                findNavController().navigate(action)
            },
            onDelete = { transaction ->
                transactionViewModel.deleteTransaction(transaction.id)
            }
        )
        recentTransactionsRecycler.adapter = transactionAdapter

        // Setup the chart
        setupChart()

        // Observe transactions and update UI
        transactionViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            // Update the RecyclerView with recent transactions
            transactionAdapter.submitList(transactions.take(5)) // Show only the 5 most recent transactions

            // Calculate total income and expenses
            val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
            val totalExpenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
            val balance = totalIncome - totalExpenses

            // Update balance text
            balanceText.text = "${settingsViewModel.currency} ${String.format("%.2f", balance)}"

            // Update income and expense texts
            incomeAmountText.text = "${settingsViewModel.currency} ${String.format("%.0f", totalIncome)}"
            expenseAmountText.text = "${settingsViewModel.currency} ${String.format("%.0f", totalExpenses)}"

            // Update budget progress
            val monthlyExpenses = transactionViewModel.getMonthlyExpenses()
            budgetText.text = "Spent: ${settingsViewModel.currency} ${String.format("%.2f", monthlyExpenses)} / ${settingsViewModel.budget}"
            if (settingsViewModel.budget > 0) {
                val progress = (monthlyExpenses / settingsViewModel.budget * 100).toInt()
                budgetProgress.progress = progress
                if (progress >= 90) {
                    budgetText.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense))
                    sendBudgetAlertNotification()
                } else {
                    budgetText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark))
                }
            }

            // Update chart with the current period
            updateChart(currentChartPeriod, transactions)
        }

        // Navigation to add income/expense
        incomeCard.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToAddEdit(type = "Income")
            findNavController().navigate(action)
        }

        expenseCard.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToAddEdit(type = "Expense")
            findNavController().navigate(action)
        }

        // Setup chart time period buttons
        tabToday.setOnClickListener {
            currentChartPeriod = "Today"
            updateChart("Today", transactionViewModel.transactions.value ?: emptyList())
            highlightButton(tabToday)
        }
        tabWeek.setOnClickListener {
            currentChartPeriod = "Week"
            updateChart("Week", transactionViewModel.transactions.value ?: emptyList())
            highlightButton(tabWeek)
        }
        tabMonth.setOnClickListener {
            currentChartPeriod = "Month"
            updateChart("Month", transactionViewModel.transactions.value ?: emptyList())
            highlightButton(tabMonth)
        }
        tabYear.setOnClickListener {
            currentChartPeriod = "Year"
            updateChart("Year", transactionViewModel.transactions.value ?: emptyList())
            highlightButton(tabYear)
        }

        // Navigate to TransactionListFragment when "See All" is clicked
        seeAllBtn.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_list)
        }

        // Highlight the default tab (Week)
        highlightButton(tabWeek)

        return view
    }

    private fun setupChart() {
        spendFrequencyChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setPinchZoom(false)
            setVisibleXRangeMinimum(6f)
            setVisibleXRangeMaximum(30f)
            data = LineData(LineDataSet(listOf(Entry(0f, 0f), Entry(6f, 0f)), "Spend Frequency").apply {
                color = ContextCompat.getColor(requireContext(), R.color.primary_purple)
                setDrawFilled(true)
                fillColor = ContextCompat.getColor(requireContext(), R.color.primary_purple)
                fillAlpha = 50
                setDrawCircles(false)
                lineWidth = 2f
                setDrawValues(false)
            })
            invalidate()
        }
    }

    private fun updateChart(period: String, transactions: List<com.example.financeapp.data.Transaction>) {
        val entries = when (period) {
            "Today" -> getTodayData(transactions)
            "Week" -> getWeekData(transactions)
            "Month" -> getMonthData(transactions)
            "Year" -> getYearData(transactions)
            else -> listOf(Entry(0f, 0f), Entry(6f, 0f))
        }

        val sortedEntries = entries.sortedBy { it.x }
        val dataSet = LineDataSet(sortedEntries, "Spend Frequency").apply {
            color = ContextCompat.getColor(requireContext(), R.color.primary_purple)
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.primary_purple)
            fillAlpha = 50
            setDrawCircles(false)
            lineWidth = 2f
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        spendFrequencyChart.data = lineData
        spendFrequencyChart.notifyDataSetChanged()
        spendFrequencyChart.invalidate()
    }

    private fun getTodayData(transactions: List<com.example.financeapp.data.Transaction>): List<Entry> {
        val today = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val expenses = transactions
            .filter { it.type == "Expense" && it.date == today }
            .groupBy { it.date }
            .mapValues { it.value.sumOf { t -> t.amount }.toFloat() }

        val amount = expenses[today] ?: 0f
        return listOf(
            Entry(0f, amount),
            Entry(6f, amount)
        )
    }

    private fun getWeekData(transactions: List<com.example.financeapp.data.Transaction>): List<Entry> {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = calendar.time

        val expenses = transactions
            .filter { it.type == "Expense" }
            .filter {
                val transactionDate = try {
                    SimpleDateFormat("yyyy-MM-dd").parse(it.date)
                } catch (e: Exception) {
                    null
                }
                transactionDate?.let { date ->
                    date.after(startDate) || date == startDate
                } ?: false
            }
            .groupBy {
                val date = SimpleDateFormat("yyyy-MM-dd").parse(it.date)
                val cal = Calendar.getInstance().apply { time = date }
                cal.get(Calendar.DAY_OF_YEAR).toString()
            }
            .mapValues { it.value.sumOf { t -> t.amount }.toFloat() }

        val entries = mutableListOf<Entry>()
        for (i in 0..6) {
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR).toString()
            val amount = expenses[dayOfYear] ?: 0f
            entries.add(Entry((6 - i).toFloat(), amount))
        }
        return entries
    }

    private fun getMonthData(transactions: List<com.example.financeapp.data.Transaction>): List<Entry> {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, -29)
        val startDate = calendar.time

        val expenses = transactions
            .filter { it.type == "Expense" }
            .filter {
                val transactionDate = try {
                    SimpleDateFormat("yyyy-MM-dd").parse(it.date)
                } catch (e: Exception) {
                    null
                }
                transactionDate?.let { date ->
                    date.after(startDate) || date == startDate
                } ?: false
            }
            .groupBy {
                val date = SimpleDateFormat("yyyy-MM-dd").parse(it.date)
                val cal = Calendar.getInstance().apply { time = date }
                cal.get(Calendar.DAY_OF_MONTH).toString()
            }
            .mapValues { it.value.sumOf { t -> t.amount }.toFloat() }

        val entries = mutableListOf<Entry>()
        for (i in 0..29) {
            calendar.time = today
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).toString()
            val amount = expenses[dayOfMonth] ?: 0f
            entries.add(Entry((29 - i).toFloat(), amount))
        }
        return entries
    }

    private fun getYearData(transactions: List<com.example.financeapp.data.Transaction>): List<Entry> {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.MONTH, -11)
        val startDate = calendar.time

        val expenses = transactions
            .filter { it.type == "Expense" }
            .filter {
                val transactionDate = try {
                    SimpleDateFormat("yyyy-MM-dd").parse(it.date)
                } catch (e: Exception) {
                    null
                }
                transactionDate?.let { date ->
                    date.after(startDate) || date == startDate
                } ?: false
            }
            .groupBy {
                val date = SimpleDateFormat("yyyy-MM-dd").parse(it.date)
                val cal = Calendar.getInstance().apply { time = date }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}"
            }
            .mapValues { it.value.sumOf { t -> t.amount }.toFloat() }

        val entries = mutableListOf<Entry>()
        for (i in 0..11) {
            calendar.time = today
            calendar.add(Calendar.MONTH, -i)
            val monthKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}"
            val amount = expenses[monthKey] ?: 0f
            entries.add(Entry((11 - i).toFloat(), amount))
        }
        return entries
    }

    private fun highlightButton(selectedButton: Button) {
        // Reset all buttons to unselected state
        tabToday.setBackgroundResource(R.drawable.button_unselected_background)
        tabWeek.setBackgroundResource(R.drawable.button_unselected_background)
        tabMonth.setBackgroundResource(R.drawable.button_unselected_background)
        tabYear.setBackgroundResource(R.drawable.button_unselected_background)

        // Apply selected state to the clicked button
        selectedButton.setBackgroundResource(R.drawable.button_selected_background)
    }

    private fun sendBudgetAlertNotification() {
        // Build the notification with app icon
        val builder = NotificationCompat.Builder(requireContext(), "finance_channel")
            .setSmallIcon(R.drawable.notifi2) // App icon
            .setContentTitle("Budget Alert")
            .setContentText("You have reached 90% of your monthly budget.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
            notify(1, builder.build())
        }
    }
}