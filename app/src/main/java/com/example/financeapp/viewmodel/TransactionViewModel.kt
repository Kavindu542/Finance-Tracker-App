package com.example.financeapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.financeapp.data.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions
    private var nextId: Int = 0

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        val sharedPref = getApplication<Application>().getSharedPreferences("finance", 0)
        val json = sharedPref.getString("transactions", null)
        val type = object : TypeToken<List<Transaction>>() {}.type
        val loadedTransactions: List<Transaction>? = Gson().fromJson(json, type)

        // Handle legacy data: if timestamp is null, set a default timestamp
        val defaultTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val updatedTransactions = loadedTransactions?.map { transaction ->
            if (transaction.timestamp == null) {
                transaction.copy(timestamp = defaultTimestamp)
            } else {
                transaction
            }
        } ?: emptyList()

        _transactions.value = updatedTransactions
        nextId = sharedPref.getInt("nextId", 0)
    }

    fun addTransaction(transaction: Transaction) {
        val newTransaction = transaction.copy(id = nextId)
        nextId++
        val updatedList = _transactions.value.orEmpty() + newTransaction
        _transactions.value = updatedList
        saveTransactions()
    }

    fun editTransaction(updatedTransaction: Transaction) {
        val updatedList = _transactions.value.orEmpty().map {
            if (it.id == updatedTransaction.id) updatedTransaction else it
        }
        _transactions.value = updatedList
        saveTransactions()
    }

    fun deleteTransaction(id: Int) {
        val updatedList = _transactions.value.orEmpty().filter { it.id != id }
        _transactions.value = updatedList
        saveTransactions()
    }

    fun setTransactions(newList: List<Transaction>) {
        val defaultTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val updatedList = newList.map { transaction ->
            if (transaction.timestamp == null) {
                transaction.copy(timestamp = defaultTimestamp)
            } else {
                transaction
            }
        }
        _transactions.value = updatedList
        nextId = if (updatedList.isEmpty()) 0 else updatedList.maxOf { it.id } + 1
        saveTransactions()
    }

    private fun saveTransactions() {
        val sharedPref = getApplication<Application>().getSharedPreferences("finance", 0)
        val editor = sharedPref.edit()
        val json = Gson().toJson(_transactions.value)
        editor.putString("transactions", json)
        editor.putInt("nextId", nextId)
        editor.apply()
    }

    fun getMonthlyExpenses(): Double {
        val currentMonth = SimpleDateFormat("yyyy-MM").format(Date())
        return _transactions.value.orEmpty()
            .filter { it.type == "Expense" && it.date.startsWith(currentMonth) }
            .sumOf { it.amount }
    }
}