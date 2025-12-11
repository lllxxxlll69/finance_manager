package com.example.financemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financemanager.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FinanceViewModel by activityViewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Инициализация адаптера для отображения категорий/расходов
        categoryAdapter = CategoryAdapter(
            items = emptyList(),
            expenseMap = emptyMap(),

            // ЛОГИКА УДАЛЕНИЯ: Обнуляет расходы категории за текущий месяц (по вашему требованию)
            onDeleteClicked = { category ->
                viewModel.clearExpensesForCategory(category.id) // <-- НОВАЯ ФУНКЦИЯ
                Toast.makeText(context, "Расходы категории '${category.name}' за месяц обнулены.", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvCategoriesSummary.adapter = categoryAdapter
        binding.rvCategoriesSummary.layoutManager = LinearLayoutManager(context)

        // 2. Единый наблюдатель LiveData для автоматического обновления всех элементов UI
        val updateUiObserver = androidx.lifecycle.Observer<Any> {

            val categories = viewModel.categories.value.orEmpty()
            val currentExpensesMap = viewModel.getCurrentMonthExpensesGroupedByCategory()
            val total = viewModel.totalExpense.value ?: 0.0

            // ФИЛЬТРАЦИЯ: Показываем только те категории, у которых есть расходы > 0
            val filteredCategories = categories.filter {
                (currentExpensesMap[it.id] ?: 0.0) > 0
            }

            // Обновляем общую сумму (Статистика 1)
            binding.tvTotalExpenses.text = String.format("%,.0f ₽", total)

            // Обновляем список категорий: передаем ТОЛЬКО отфильтрованные категории
            categoryAdapter.updateData(filteredCategories, currentExpensesMap)

            // Отрисовка круговой диаграммы (Статистика 2)
            if (isAdded && total >= 0) {
                val sizeInPx = (150 * resources.displayMetrics.density).toInt()

                val chartBitmap = PieChartRenderer.createChart(
                    categories = filteredCategories, // Диаграмма также использует отфильтрованный список
                    expenses = currentExpensesMap,
                    total = total,
                    size = sizeInPx
                )
                binding.pieChartView.setImageBitmap(chartBitmap)
            }
        }

        viewModel.categories.observe(viewLifecycleOwner, updateUiObserver)
        viewModel.expenses.observe(viewLifecycleOwner, updateUiObserver)

        // 3. Обработчик кнопки добавления расхода
        binding.fabAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addExpenseFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}