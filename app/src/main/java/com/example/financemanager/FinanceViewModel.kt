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

    // --- ДОСТУПНЫЕ ЦВЕТА (20 вариантов) ---
    companion object {
        val AVAILABLE_COLORS = listOf(
            "#FFC62828", // 1. Красный
            "#FFD81B60", // 2. Розовый
            "#FF9C27B0", // 3. Фиолетовый
            "#FF5E35B1", // 4. Глубокий фиолетовый
            "#FF3F51B5", // 5. Индиго
            "#FF03A9F4", // 6. Светло-синий
            "#FF00BCD4", // 7. Голубой
            "#FF009688", // 8. Бирюзовый
            "#FF4CAF50", // 9. Зеленый
            "#FF8BC34A", // 10. Светло-зеленый
            "#FFCDDC39", // 11. Лайм
            "#FFFFEB3B", // 12. Желтый
            "#FFFFC107", // 13. Янтарь
            "#FFFF9800", // 14. Оранжевый
            "#FFFF5722", // 15. Глубокий оранжевый
            "#FF795548", // 16. Коричневый
            "#FF9E9E9E", // 17. Серый
            "#FF607D8B", // 18. Сине-серый
            "#FF000000", // 19. Черный
            "#FFBDBDBD"  // 20. Светло-серый
        )
    }

    // Инициализация SharedPreferences и GSON для сохранения
    private val sharedPreferences = application.getSharedPreferences("finance_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    // --- LIVE DATA ---
    private val _expenses = MutableLiveData<List<Expense>>(emptyList())
    val expenses: LiveData<List<Expense>> = _expenses

    private val _categories = MutableLiveData<List<Category>>(emptyList())
    val categories: LiveData<List<Category>> = _categories

    private val _totalExpense = MutableLiveData<Double>(0.0)
    val totalExpense: LiveData<Double> = _totalExpense

    // --- ИНИЦИАЛИЗАЦИЯ ---
    init {
        loadData()
        if (_categories.value.isNullOrEmpty()) {
            addDefaultCategories()
        }
        updateCalculations()
    }

    // --- ЛОГИКА ДАННЫХ ---

    fun addExpense(amount: Double, categoryId: String): Boolean {
        if (amount <= 0) return false

        val newExpense = Expense(
            id = UUID.randomUUID().toString(),
            amount = amount,
            date = Date(),
            categoryId = categoryId
        )

        _expenses.value = _expenses.value.orEmpty() + listOf(newExpense)

        updateCalculations()
        saveData()
        return true
    }

    /**
     * Добавляет новую категорию с ограничением в 20 шт. и авто-назначением цвета.
     */
    fun addCategory(name: String): Boolean {
        val currentCategories = _categories.value.orEmpty()
        val trimmedName = name.trim()

        // 1. ПРОВЕРКА ОГРАНИЧЕНИЯ: Не более 20 категорий
        if (currentCategories.size >= AVAILABLE_COLORS.size) {
            return false
        }

        // 2. ПРОВЕРКА УНИКАЛЬНОСТИ
        if (currentCategories.any { it.name.equals(trimmedName, ignoreCase = true) }) {
            return false
        }

        // 3. ОПРЕДЕЛЕНИЕ СЛЕДУЮЩЕГО ЦВЕТА (по индексу)
        val colorIndex = currentCategories.size // Индекс следующего цвета
        val nextColor = AVAILABLE_COLORS[colorIndex]

        // 4. СОЗДАНИЕ КАТЕГОРИИ
        val newCategory = Category(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            colorHex = nextColor
        )

        _categories.value = currentCategories + listOf(newCategory)

        saveData()
        return true
    }

    /**
     * ПОЛНОЕ УДАЛЕНИЕ КАТЕГОРИИ: Удаляет категорию и все связанные расходы (за все время).
     */
    fun deleteCategory(categoryId: String) {
        _categories.value = _categories.value?.filter { it.id != categoryId }
        _expenses.value = _expenses.value?.filter { it.categoryId != categoryId }

        updateCalculations()
        saveData()
    }

    /**
     * УДАЛЕНИЕ ОТДЕЛЬНОГО РАСХОДА: Удаляет транзакцию, уменьшая статистику.
     */
    fun deleteExpense(expenseId: String) {
        _expenses.value = _expenses.value?.filter { it.id != expenseId }

        updateCalculations()
        saveData()
    }

    /**
     * Обнуляет все расходы, связанные с данной категорией, только за ТЕКУЩИЙ МЕСЯЦ.
     * Используется на HomeFragment при "удалении" категории.
     */
    fun clearExpensesForCategory(categoryId: String) {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        _expenses.value = _expenses.value?.filter { expense ->
            if (expense.categoryId == categoryId) {
                val expenseCal = Calendar.getInstance().apply { time = expense.date }
                val isCurrentMonth = expenseCal.get(Calendar.MONTH) == currentMonth && expenseCal.get(Calendar.YEAR) == currentYear

                // Если это расход нужной категории и текущего месяца, он УДАЛЯЕТСЯ
                return@filter !isCurrentMonth
            }
            // Все остальные расходы сохраняются
            true
        }

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
        // Обновляет общую сумму расходов, которая используется для статистики и диаграммы
        _totalExpense.value = getCurrentMonthExpensesGroupedByCategory().values.sum()
    }

    // --- МЕТОДЫ ПЕРСИСТЕНТНОСТИ (Сохранение/Загрузка) ---

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
        // Используем первые 4 цвета из нового списка
        _categories.value = listOf(
            Category(UUID.randomUUID().toString(), "Еда и продукты", AVAILABLE_COLORS[0]),
            Category(UUID.randomUUID().toString(), "Транспорт", AVAILABLE_COLORS[5]),
            Category(UUID.randomUUID().toString(), "Развлечения", AVAILABLE_COLORS[2]),
            Category(UUID.randomUUID().toString(), "Дом", AVAILABLE_COLORS[8])
        )
        saveData()
    }
}