package com.example.financemanager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class CustomBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var barData: List<Double> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val accentColor = ContextCompat.getColor(context, R.color.accent_green)
    private val trackColor = Color.DKGRAY

    private val BAR_WIDTH_FACTOR = 0.7f // Процент ширины, который занимает сам столбец
    private val BAR_SPACING_FACTOR = 0.3f // Процент ширины, который занимает интервал

    fun setData(data: List<Double>) {
        barData = data
        // Вызываем перерисовку, чтобы применить новые данные
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (barData.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()

        // Находим максимальное значение для нормализации
        val maxVal = barData.maxOrNull() ?: 1.0
        val maxScale = if (maxVal == 0.0) 1.0 else maxVal * 1.1 // Небольшой запас сверху

        val totalBars = barData.size
        // Рассчитываем ширину одной секции (столбец + интервал)
        val sectionWidth = width / totalBars
        val barWidth = sectionWidth * BAR_WIDTH_FACTOR
        val spacing = sectionWidth * BAR_SPACING_FACTOR

        // Определяем нижнюю линию (ось)
        val bottomLineY = height

        // Отрисовка нижней линии
        paint.color = trackColor
        paint.strokeWidth = 4f
        canvas.drawLine(0f, bottomLineY, width, bottomLineY, paint)

        barData.forEachIndexed { index, value ->
            // Нормализуем высоту, чтобы она не превышала 90% высоты View
            val normalizedHeight = (value / maxScale).toFloat() * height * 0.9f

            val left = index * sectionWidth + spacing / 2
            val right = left + barWidth
            val top = bottomLineY - normalizedHeight // Верхняя точка

            // Рисуем сам столбец
            paint.color = accentColor
            val rect = RectF(left, top, right, bottomLineY)
            // Закругленные углы (как на скриншоте)
            canvas.drawRoundRect(rect, 8f, 8f, paint)
        }
    }
}