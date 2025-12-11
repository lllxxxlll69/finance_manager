package com.example.financemanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.Date
import java.util.UUID

// --- МОДЕЛИ ДАННЫХ ---
data class Expense(
    val id: String,
    val amount: Double,
    val date: Date,
    val categoryId: String
)

data class Category(
    val id: String,
    val name: String,
    val colorHex: String
)

data class PieChartData(
    val amount: Double,
    val colorHex: String
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    // Инициализация SharedPreferences и GSON для сохранения
    private val sharedPreferences = application.getSharedPreferences("finance_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _expenses = MutableLiveData<List<Expense>>(emptyList())
    val expenses: LiveData<List<Expense>> = _expenses

    private val _categories = MutableLiveData<List<Category>>(emptyList())
    val categories: LiveData<List<Category>> = _categories

    private val _totalExpense = MutableLiveData<Double>(0.0)
    val totalExpense: LiveData<Double> = _totalExpense

    init {
        loadData()
        if (_categories.value.isNullOrEmpty()) {
            addDefaultCategories()
        }
        updateCalculations()
    }

    // --- ЛОГИКА ---
    fun addExpense(amount: Double, categoryId: String): Boolean {
        if (amount <= 0) return false
        val newExpense = Expense(UUID.randomUUID().toString(), amount, Date(), categoryId)
        _expenses.value = _expenses.value.orEmpty() + listOf(newExpense)
        updateCalculations()
        saveData()
        return true
    }

    fun addCategory(name: String): Boolean {
        if (_categories.value.orEmpty().any { it.name.equals(name, ignoreCase = true) }) return false
        val newCategory = Category(UUID.randomUUID().toString(), name.trim(), "#FF03DAC5")
        _categories.value = _categories.value.orEmpty() + listOf(newCategory)
        saveData()
        return true
    }

    // МЕТОД УДАЛЕНИЯ: Фильтрует список и публикует изменение, затем сохраняет
    fun deleteCategory(categoryId: String) {
        _categories.value = _categories.value?.filter { it.id != categoryId }
        _expenses.value = _expenses.value?.filter { it.categoryId != categoryId }
        updateCalculations()
        saveData()
    }

    fun getCurrentMonthExpensesGroupedByCategory(): Map<String, Double> {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        return _expenses.value.orEmpty()
            .filter { expense ->
                val expenseCal = Calendar.getInstance().apply { time = expense.date }
                expenseCal.get(Calendar.MONTH) == currentMonth && expenseCal.get(Calendar.YEAR) == currentYear
            }
            .groupBy { it.categoryId }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }

    private fun updateCalculations() {
        _totalExpense.value = getCurrentMonthExpensesGroupedByCategory().values.sum()
    }

    // --- ПЕРСИСТЕНТНОСТЬ (GSON/SharedPreferences) ---
    private fun loadData() {
        val expensesJson = sharedPreferences.getString("expenses", null)
        val categoriesJson = sharedPreferences.getString("categories", null)

        expensesJson?.let {
            val type = object : TypeToken<List<Expense>>() {}.type
            _expenses.value = gson.fromJson(it, type)
        }
        categoriesJson?.let {
            val type = object : TypeToken<List<Category>>() {}.type
            _categories.value = gson.fromJson(it, type)
        }
    }

    private fun saveData() {
        with(sharedPreferences.edit()) {
            putString("expenses", gson.toJson(_expenses.value))
            putString("categories", gson.toJson(_categories.value))
            apply()
        }
    }

    private fun addDefaultCategories() {
        _categories.value = listOf(
            Category(UUID.randomUUID().toString(), "Еда и продукты", "#FFFF5252"),
            Category(UUID.randomUUID().toString(), "Транспорт", "#FF03A9F4"),
            Category(UUID.randomUUID().toString(), "Развлечения", "#FF8080FF"),
            Category(UUID.randomUUID().toString(), "Дом", "#FF4CAF50")
        )
        saveData()
    }
}