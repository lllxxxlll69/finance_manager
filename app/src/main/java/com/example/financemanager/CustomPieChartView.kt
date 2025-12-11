package com.example.financemanager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import kotlin.math.min

// Класс CustomPieChartView наследуется от View
class CustomPieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Кисти для рисования
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    // Объект для определения области рисования диаграммы
    private val rectF = RectF()

    // Список данных, которые будут отображены
    private var data: List<PieChartData> = emptyList()
    // Общая сумма всех значений для расчета процентов
    private var totalAmount: Double = 0.0

    // Инициализация
    init {
        // Устанавливаем минимальную высоту и ширину, если они не заданы
        minimumWidth = 200
        minimumHeight = 200
    }

    /**
     * Устанавливает новые данные для диаграммы и вызывает перерисовку.
     */
    fun setData(newData: List<PieChartData>) {
        this.data = newData.filter { it.amount > 0 } // Игнорируем нулевые значения
        this.totalAmount = this.data.sumOf { it.amount }
        invalidate() // Запрос на перерисовку (вызывает onDraw)
    }

    // Вызывается при отрисовке View
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Если данных нет или общая сумма равна нулю, ничего не рисуем
        if (data.isEmpty() || totalAmount == 0.0) {
            drawNoDataText(canvas)
            return
        }

        // 1. Определение размеров области рисования
        val size = min(width, height)
        val diameter = size * 0.8f // Диаметр диаграммы (80% от меньшей стороны)
        val padding = (size - diameter) / 2

        // Устанавливаем прямоугольную область для рисования дуг
        rectF.set(padding, padding, size - padding, size - padding)

        // Начальный угол для рисования (12 часов)
        var startAngle = 270f

        // 2. Отрисовка каждого сегмента
        data.forEach { item ->
            // Расчет угла сегмента
            val sweepAngle = (item.amount / totalAmount).toFloat() * 360f

            // Установка цвета сегмента
            paint.color = try {
                item.colorHex.toColorInt()
            } catch (e: IllegalArgumentException) {
                // Если цвет невалидный, используем серый
                Color.GRAY
            }

            // Рисование дуги (сегмента)
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)

            // Переход к следующему углу
            startAngle += sweepAngle
        }
    }

    /**
     * Отрисовывает текст, если данных для диаграммы нет.
     */
    private fun drawNoDataText(canvas: Canvas) {
        paint.color = Color.parseColor("#9E9E9E") // text_gray
        paint.textSize = 30f
        paint.textAlign = Paint.Align.CENTER

        val text = "Нет данных за текущий месяц"
        val xPos = width / 2f
        val yPos = height / 2f - ((paint.descent() + paint.ascent()) / 2) // Центрирование текста

        canvas.drawText(text, xPos, yPos, paint)
    }

    // Переопределяем onMeasure для правильной работы в ConstraintLayout
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size) // Делаем View квадратным
    }
}