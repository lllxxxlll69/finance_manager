package com.example.financemanager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanager.databinding.ItemCategoryBinding
import kotlin.math.roundToInt

// Адаптер для отображения списка категорий
class CategoryAdapter(
    private var items: List<Category>,
    private var expenseMap: Map<String, Double>, // Карта расходов (ID категории -> Сумма)
    // Колбэк для обработки удаления, который будет задан во фрагменте
    private val onDeleteClicked: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // Класс ViewHolder для хранения ссылок на элементы разметки
    class CategoryViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, amount: Double, onDeleteClicked: (Category) -> Unit) {

            // Установка имени и суммы
            binding.tvCategoryName.text = category.name
            binding.tvCategoryAmount.text = "${amount.roundToInt()} ₽"

            // 1. Установка цвета индикатора
            try {
                // ПРЕОБРАЗОВАНИЕ: Преобразуем Hex-строку (String) в Int-цвет
                val colorInt = Color.parseColor(category.colorHex)
                binding.colorIndicator.setBackgroundColor(colorInt)
            } catch (e: IllegalArgumentException) {
                // Если Hex-код невалидный, используем цвет по умолчанию (серый)
                binding.colorIndicator.setBackgroundColor(Color.GRAY)
            }

            // 2. Установка обработчика удаления
            binding.btnDelete.setOnClickListener {
                // Вызываем колбэк, переданный из фрагмента
                onDeleteClicked(category)
            }

            // 3. Управление видимостью кнопки удаления
            // Кнопка видна, только если задан обработчик onDeleteClicked (т.е., во CategoriesFragment)
            // Мы проверяем, что переданная функция не является пустой заглушкой.
            binding.btnDelete.visibility = if (onDeleteClicked != {}) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = items[position]
        // Получаем сумму расходов для этой категории (или 0.0, если расходов нет)
        val amount = expenseMap[category.id] ?: 0.0

        holder.bind(category, amount, onDeleteClicked)
    }

    override fun getItemCount(): Int = items.size

    /**
     * Обновляет данные в адаптере и уведомляет о изменениях.
     */
    fun updateData(newCategories: List<Category>, newExpenseMap: Map<String, Double>) {
        // Сортируем категории по сумме расходов в убывающем порядке,
        // чтобы самые крупные траты были вверху
        val sortedCategories = newCategories.sortedByDescending { newExpenseMap[it.id] ?: 0.0 }

        this.items = sortedCategories
        this.expenseMap = newExpenseMap
        notifyDataSetChanged()
        //big dick
    }
}
