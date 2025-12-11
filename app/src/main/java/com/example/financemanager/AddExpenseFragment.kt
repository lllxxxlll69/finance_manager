package com.example.financemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.financemanager.databinding.FragmentAddExpenseBinding
import kotlin.math.roundToInt

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    // Используем 'by lazy' для безопасного доступа к binding
    private val binding get() = _binding!!

    private val viewModel: FinanceViewModel by activityViewModels()

    // Переменная для хранения ID выбранной категории
    private var selectedCategoryId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Наблюдаем за категориями, чтобы обновить Spinner
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            setupCategorySpinner(categories)
        }

        // --- Обработчик кнопки добавления ---
        binding.btnAddExpense.setOnClickListener {
            addExpense()
        }
    }

    /**
     * Настраивает выпадающий список (Spinner) для выбора категории.
     */
    private fun setupCategorySpinner(categories: List<Category>) {
        val categoryNames = categories.map { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // --- Обработчик выбора категории ---
        binding.spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Сохраняем ID выбранной категории
                selectedCategoryId = categories.getOrNull(position)?.id
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                selectedCategoryId = null
            }
        }
    }

    /**
     * Считывает данные, добавляет расход через ViewModel и выполняет навигацию.
     */
    private fun addExpense() {
        val amountText = binding.etAmount.text.toString()
        val categoryId = selectedCategoryId

        if (amountText.isEmpty() || categoryId == null) {
            Toast.makeText(context, "Введите сумму и выберите категорию.", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = try {
            amountText.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Неверный формат суммы.", Toast.LENGTH_SHORT).show()
            return
        }

        // Сумма не может быть нулевой (после округления)
        if (amount.roundToInt() == 0) {
            Toast.makeText(context, "Сумма не может быть нулевой.", Toast.LENGTH_SHORT).show()
            return
        }

        // Вызываем ViewModel для добавления расхода
        val success = viewModel.addExpense(amount, categoryId)

        if (success) {
            Toast.makeText(context, "Расход ${amount.roundToInt()} ₽ добавлен!", Toast.LENGTH_SHORT).show()
            // Возвращаемся на предыдущий экран (HomeFragment)
            findNavController().popBackStack()
        } else {
            // Если addExpense вернул false (например, при попытке отрицательной коррекции, превышающей остаток)
            Toast.makeText(context, "Ошибка: Не удалось добавить расход/коррекцию.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Обязательно очищаем binding, чтобы избежать утечек памяти
        _binding = null
    }
}