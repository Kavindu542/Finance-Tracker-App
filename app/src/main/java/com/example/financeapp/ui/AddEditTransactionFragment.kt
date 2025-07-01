package com.example.financeapp.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.financeapp.R
import com.example.financeapp.data.Transaction
import com.example.financeapp.viewmodel.SettingsViewModel
import com.example.financeapp.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddEditTransactionFragment : Fragment() {
    private val transactionViewModel: TransactionViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val args: AddEditTransactionFragmentArgs by navArgs()
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_edit_transaction, container, false)

        val radioGroup: RadioGroup = view.findViewById(R.id.type_radio_group)
        val titleEdit: EditText = view.findViewById(R.id.title_edit)
        val amountEdit: EditText = view.findViewById(R.id.amount_edit)
        val categorySpinner: Spinner = view.findViewById(R.id.category_spinner)
        dateText = view.findViewById(R.id.date_text)
        timeText = view.findViewById(R.id.time_text)
        val saveBtn: Button = view.findViewById(R.id.save_btn)

        val categories = arrayOf("Food", "Transport", "Bills", "Entertainment", "Shopping", "Subscription", "Others")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, categories)
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

        if (args.transactionId != -1) {
            val transaction = transactionViewModel.transactions.value?.find { it.id == args.transactionId }
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
            radioGroup.check(if (args.type == "Income") R.id.income_radio else R.id.expense_radio)
        }

        saveBtn.setOnClickListener {
            val title = titleEdit.text.toString()
            val amountStr = amountEdit.text.toString()
            val category = categorySpinner.selectedItem.toString()
            val type = if (radioGroup.checkedRadioButtonId == R.id.income_radio) "Income" else "Expense"

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            if (amount <= 0) {
                Toast.makeText(context, "Amount must be positive", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val timestamp = "$selectedDate $selectedTime"
            val transaction = Transaction(
                id = if (args.transactionId != -1) args.transactionId else 0,
                title = title,
                amount = amount,
                category = category,
                timestamp = timestamp,
                type = type
            )

            if (args.transactionId != -1) {
                transactionViewModel.editTransaction(transaction)
            } else {
                transactionViewModel.addTransaction(transaction)
            }
            findNavController().popBackStack()
        }

        return view
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
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
            requireContext(),
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