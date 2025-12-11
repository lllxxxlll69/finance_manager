package com.example.financemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financemanager.databinding.FragmentHomeBinding
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FinanceViewModel by activityViewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Инициализация адаптера
        categoryAdapter = CategoryAdapter(
            items = emptyList(),
            expenseMap = emptyMap(),
            // Для главного экрана удаление не используется, но конструктор требует колбэк
            onDeleteClicked = {}
        )
        binding.rvTopCategories.adapter = categoryAdapter
        binding.rvTopCategories.layoutManager = LinearLayoutManager(context)
        binding.rvTopCategories.isNestedScrollingEnabled = false


        // 2. Наблюдение за общим балансом (сумма трат за ТЕКУЩИЙ месяц)
        viewModel.totalExpense.observe(viewLifecycleOwner) { total ->
            binding.tvTotalBalance.text = "${total.roundToInt()} ₽"
        }

        // 3. Наблюдение за расходами и категориями
        viewModel.expenses.observe(viewLifecycleOwner) {

            // Получаем расходы только за ТЕКУЩИЙ МЕСЯЦ
            val currentMonthExpensesMap = viewModel.getCurrentMonthExpensesGroupedByCategory()
            val categoriesList = viewModel.categories.value.orEmpty()

            // ЛОГИКА ДЛЯ КРУГОВОЙ ДИАГРАММЫ
            val pieChartData = currentMonthExpensesMap
                .mapNotNull { (id, amount) ->
                    if (amount > 0) {
                        categoriesList.find { it.id == id }?.let { category ->
                            PieChartData(amount, category.colorHex)
                        }
                    } else {
                        null
                    }
                }

            binding.pieChartView.setData(pieChartData)

            // ЛОГИКА ДЛЯ РАЗДЕЛА "ВАШИ КАТЕГОРИИ" (все категории с тратами > 0)
            val categoriesWithExpenses = currentMonthExpensesMap.entries
                .filter { it.value > 0.0 }
                .sortedByDescending { it.value } // Сортировка по убыванию суммы
                .mapNotNull { (id, amount) ->
                    categoriesList.find { it.id == id }
                }

            categoryAdapter.updateData(categoriesWithExpenses, currentMonthExpensesMap)

            // Управление видимостью списка
            if (categoriesWithExpenses.isEmpty()) {
                binding.rvTopCategories.visibility = View.GONE
            } else {
                binding.rvTopCategories.visibility = View.VISIBLE
            }
        }

        // 4. ОБРАБОТЧИК КНОПКИ ДОБАВЛЕНИЯ РАСХОДА (FAB)
        binding.fabAddExpense.setOnClickListener {
            // Переход на AddExpenseFragment (действие должно быть определено в nav_graph.xml)
            findNavController().navigate(R.id.action_homeFragment_to_addExpenseFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}