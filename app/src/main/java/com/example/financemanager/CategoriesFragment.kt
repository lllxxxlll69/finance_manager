package com.example.financemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financemanager.databinding.FragmentCategoriesBinding

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FinanceViewModel by activityViewModels()

    // КРИТИЧЕСКИ ВАЖНО: Используем адаптер БЕЗ ЛОГИКИ РАСХОДОВ
    private lateinit var categoryAdapter: CategoryManagementAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация CategoryManagementAdapter
        categoryAdapter = CategoryManagementAdapter(
            items = emptyList(),

            onDeleteClicked = { category ->
                // Используем полное удаление категории
                viewModel.deleteCategory(category.id)
                Toast.makeText(context, "Категория '${category.name}' удалена!", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvCategories.adapter = categoryAdapter
        binding.rvCategories.layoutManager = LinearLayoutManager(context)

        // Наблюдение ТОЛЬКО за категориями
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            // Передаем только список категорий в адаптер. Расходы здесь не используются.
            categoryAdapter.updateData(categories)

            val isListEmpty = categories.isEmpty()
            binding.tvNoCategories.visibility = if (isListEmpty) View.VISIBLE else View.GONE
            binding.rvCategories.visibility = if (isListEmpty) View.GONE else View.VISIBLE
        }

        // !!!!!!!! КОД ДЛЯ viewModel.expenses.observe УДАЛЕН !!!!!!!!
        // Это гарантирует, что список не будет пытаться обновить суммы при добавлении расхода.

        binding.btnAddCategory.setOnClickListener {
            addCategory()
        }
    }

    private fun addCategory() {
        val categoryName = binding.etNewCategory.text.toString().trim()

        if (categoryName.isEmpty()) {
            Toast.makeText(context, "Введите название категории.", Toast.LENGTH_SHORT).show()
            return
        }

        if (viewModel.addCategory(categoryName)) {
            Toast.makeText(context, "Категория '$categoryName' добавлена!", Toast.LENGTH_SHORT).show()
            binding.etNewCategory.setText("")
        } else {
            Toast.makeText(context, "Категория с таким именем уже существует.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}