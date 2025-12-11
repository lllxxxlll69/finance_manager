package com.example.financemanager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanager.databinding.ItemCategoryManagementBinding

class CategoryManagementAdapter(
    // Принимает только список категорий
    private var items: List<Category>,
    private val onDeleteClicked: (Category) -> Unit
) : RecyclerView.Adapter<CategoryManagementAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(val binding: ItemCategoryManagementBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, onDeleteClicked: (Category) -> Unit) {

            binding.tvCategoryName.text = category.name

            // Установка цвета индикатора
            try {
                val colorInt = Color.parseColor(category.colorHex)
                binding.colorIndicator.setBackgroundColor(colorInt)
                // Установка цвета вертикальной полосы (рамки)
                binding.categoryColorBar.setBackgroundColor(colorInt)
            } catch (e: IllegalArgumentException) {
                binding.colorIndicator.setBackgroundColor(Color.GRAY)
                binding.categoryColorBar.setBackgroundColor(Color.GRAY)
            }

            // Обработчик удаления
            binding.btnDelete.setOnClickListener {
                onDeleteClicked(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        // Используем новую разметку
        val binding = ItemCategoryManagementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(items[position], onDeleteClicked)
    }

    override fun getItemCount(): Int = items.size

    /**
     * Метод для обновления данных в списке
     */
    fun updateData(newCategories: List<Category>) {
        this.items = newCategories
        notifyDataSetChanged()
    }
}