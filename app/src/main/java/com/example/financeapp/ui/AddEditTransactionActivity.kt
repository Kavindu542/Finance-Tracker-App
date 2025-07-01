package com.example.financeapp.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.financeapp.R
import com.example.financeapp.data.Transaction
import com.example.financeapp.viewmodel.SettingsViewModel
import com.example.financeapp.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddEditTransactionActivity : AppCompatActivity() {
    private val transactionViewModel: TransactionViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    // Intent extras
    companion object {
        const val EXTRA_TRANSACTION_ID = "transaction_id"
        const val EXTRA_TYPE = "type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_edit_transaction)

        val radioGroup: RadioGroup = findViewById(R.id.type_radio_group)
        val titleEdit: EditText = findViewById(R.id.title_edit)
        val amountEdit: EditText = findViewById(R.id.amount_edit)
        val categorySpinner: Spinner = findViewById(R.id.category_spinner)
        dateText = findViewById(R.id.date_text)
        timeText = findViewById(R.id.time_text)
        val saveBtn: Button = findViewById(R.id.save_btn)

        val categories = arrayOf("Food", "Transport", "Bills", "Entertainment", "Shopping", "Subscription", "Others")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        categorySpinner.adapter = adapter

        val currentDateTime = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd").format(currentDateTime.time)
        val currentTime = SimpleDateFormat("HH:mm:ss").format(currentDateTime.time)
        selectedDate = currentDate
        selectedTime = currentTime
        dateText.text = currentDate
        timeText.text = SimpleDateFormat("hh:mm a").format(currentDateTime.time)

        dateText.setOnClickListener {
            showDatePicker()
        }

        timeText.setOnClickListener {
            showTimePicker()
        }

        val transactionId = intent.getIntExtra(EXTRA_TRANSACTION_ID, -1)
        val type = intent.getStringExtra(EXTRA_TYPE) ?: "Expense"

        if (transactionId != -1) {
            val transaction = transactionViewModel.transactions.value?.find { it.id == transactionId }
            if (transaction != null) {
                titleEdit.setText(transaction.title)
                amountEdit.setText(transaction.amount.toString())
                categorySpinner.setSelection(categories.indexOf(transaction.category))
                dateText.text = transaction.date
                timeText.text = transaction.time
                selectedDate = transaction.date
                selectedTime = transaction.timestamp?.substringAfter(" ") ?: currentTime
                radioGroup.check(if (transaction.type == "Income") R.id.income_radio else R.id.expense_radio)
            }
        } else {
            radioGroup.check(if (type == "Income") R.id.income_radio else R.id.expense_radio)
        }

        saveBtn.setOnClickListener {
            val title = titleEdit.text.toString()
            val amountStr = amountEdit.text.toString()
            val category = categorySpinner.selectedItem.toString()
            val type = if (radioGroup.checkedRadioButtonId == R.id.income_radio) "Income" else "Expense"

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be positive", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val timestamp = "$selectedDate $selectedTime"
            val transaction = Transaction(
                id = if (transactionId != -1) transactionId else 0,
                title = title,
                amount = amount,
                category = category,
                timestamp = timestamp,
                type = type
            )

            if (transactionId != -1) {
                transactionViewModel.editTransaction(transaction)
            } else {
                transactionViewModel.addTransaction(transaction)
            }
            finish() // Close the activity and return to the previous screen
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = String.format("%d-%02d-%02d", year, month + 1, day)
                dateText.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedTime = String.format("%02d:%02d:00", hourOfDay, minute)
                timeText.text = SimpleDateFormat("hh:mm a").format(
                    SimpleDateFormat("HH:mm:ss").parse(selectedTime)
                )
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }
}