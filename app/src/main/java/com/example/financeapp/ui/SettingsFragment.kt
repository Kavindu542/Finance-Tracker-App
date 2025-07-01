package com.example.financeapp.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.financeapp.R
import com.example.financeapp.data.Transaction
import com.example.financeapp.viewmodel.SettingsViewModel
import com.example.financeapp.viewmodel.TransactionViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val transactionViewModel: TransactionViewModel by activityViewModels()

    // Launcher for picking a JSON file
    private val pickJsonFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { importTransactionsFromJson(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val budgetEdit: EditText = view.findViewById(R.id.budget_edit)
        val currencySpinner: Spinner = view.findViewById(R.id.currency_spinner)
        val saveBtn: Button = view.findViewById(R.id.save_settings_btn)
        val exportBtn: Button = view.findViewById(R.id.export_btn)
        val importBtn: Button = view.findViewById(R.id.import_btn)



        // Currency spinner setup
        val currencies = arrayOf("USD", "EUR", "LKR", "INR")
        currencySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        currencySpinner.setSelection(currencies.indexOf(settingsViewModel.currency))

        // Budget setup
        budgetEdit.setText(if (settingsViewModel.budget > 0) settingsViewModel.budget.toString() else "")

        // Save settings
        saveBtn.setOnClickListener {
            val budgetStr = budgetEdit.text.toString()
            val budget = budgetStr.toFloatOrNull() ?: 0f
            if (budget < 0) {
                Toast.makeText(requireContext(), "Budget cannot be negative", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            settingsViewModel.budget = budget
            settingsViewModel.currency = currencySpinner.selectedItem.toString()
            Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
        }

        // Export transactions to Downloads folder
        exportBtn.setOnClickListener {
            val transactions = transactionViewModel.transactions.value ?: return@setOnClickListener
            exportTransactionsToJson(transactions)
        }

        // Import transactions from a JSON file
        importBtn.setOnClickListener {
            // Launch file picker for JSON files
            pickJsonFileLauncher.launch(arrayOf("application/json"))
        }

        return view
    }

    private fun exportTransactionsToJson(transactions: List<Transaction>) {
        try {
            // Generate JSON
            val gson = Gson()
            val json = gson.toJson(transactions)

            // Create a file in the Downloads folder
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fileName = "transactions_$timestamp.json"
            val file = File(downloadsDir, fileName)

            // Write JSON to file
            FileOutputStream(file).use { fos ->
                fos.write(json.toByteArray())
                fos.flush()
            }

            // Notify user
            Toast.makeText(requireContext(), "Transactions exported to Downloads/$fileName", Toast.LENGTH_LONG).show()

            // Optional: Share the file
            shareJsonFile(file)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to export transactions: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importTransactionsFromJson(uri: Uri) {
        try {
            // Read JSON from file
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader()?.use { it.readText() }
            inputStream?.close()

            if (json.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Invalid or empty JSON file", Toast.LENGTH_SHORT).show()
                return
            }

            // Parse JSON
            val gson = Gson()
            val type = object : TypeToken<List<Transaction>>() {}.type
            val transactions: List<Transaction> = gson.fromJson(json, type)

            // Update TransactionViewModel
            transactionViewModel.setTransactions(transactions)
            Toast.makeText(requireContext(), "Transactions imported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to import transactions: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareJsonFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share transactions JSON"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to share file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}