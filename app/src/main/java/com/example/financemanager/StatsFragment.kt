package com.example.financemanager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanager.databinding.FragmentStatsBinding
import com.example.financemanager.databinding.ItemMonthStatBinding
import java.util.*
import com.example.financemanager.FinanceViewModel
import android.os.Bundle

// --- 1. Фрагмент статистики ---
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FinanceViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация адаптера истории
        val historyAdapter = HistoryAdapter(emptyList())
        binding.rvMonthlyHistory.adapter = historyAdapter
        binding.rvMonthlyHistory.layoutManager = LinearLayoutManager(context)

        // Наблюдаем за расходами для обновления графика и списка
        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->

            // Группируем расходы по месяцам
            val monthlyData = expenses.groupBy {
                val cal = Calendar.getInstance().apply { time = Date(it.date.time) }
                cal.get(Calendar.MONTH)
            }.mapValues { entry -> entry.value.sumOf { it.amount } }

            // --- 1. Обновление Гистограммы (Bar Chart) ---

            // Собираем данные за последние 6 месяцев (или сколько есть)
            val chartPoints = mutableListOf<Double>()
            val calendar = Calendar.getInstance()

            // Получаем данные за последние 6 месяцев (или меньше)
            for (i in 0 until 6) {
                val monthKey = calendar.get(Calendar.MONTH)
                chartPoints.add(monthlyData[monthKey] ?: 0.0)
                calendar.add(Calendar.MONTH, -1) // Переходим к предыдущему месяцу
            }
            // Переворачиваем список, чтобы хронология шла слева направо
            binding.barChartView.setData(chartPoints.reversed())

            // --- 2. Обновление списка истории (History List) ---

            val monthNames = mapOf(
                0 to "Январь", 1 to "Февраль", 2 to "Март", 3 to "Апрель",
                4 to "Май", 5 to "Июнь", 6 to "Июль", 7 to "Август",
                8 to "Сентябрь", 9 to "Октябрь", 10 to "Ноябрь", 11 to "Декабрь"
            )

            // Преобразуем данные в список для RecyclerView, сортируем по месяцу
            // Создаем список истории, сортируя его по числовому ID месяца (ключу)
            val historyList = monthlyData.toList() // Преобразуем карту в List<Pair<Int, Double>>
                .sortedByDescending { (monthId, _) -> monthId } // Сортируем по ID месяца
                .map { (monthId, total) ->
                    MonthStat(monthNames[monthId] ?: "Н/Д", total)
                }

            historyAdapter.updateData(historyList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ---------------------------------------------------------------------
// --- 2. Модель и Адаптер для списка истории (History List) ---

data class MonthStat(val month: String, val total: Double)

class HistoryAdapter(private var items: List<MonthStat>) : RecyclerView.Adapter<HistoryAdapter.VH>() {
    inner class VH(val binding: ItemMonthStatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMonthStatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val stat = items[position]
        holder.binding.tvMonthName.text = stat.month
        holder.binding.tvMonthTotal.text = "${stat.total.toInt()} ₽"
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<MonthStat>) {
        items = newItems
        notifyDataSetChanged()
    }
}

// ---------------------------------------------------------------------
// --- 3. Пользовательский вид для гистограммы (CustomBarChartView) ---

