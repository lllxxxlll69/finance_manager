
package com.example.financemanager

import android.graphics.*
import android.util.Log
import androidx.core.graphics.drawable.toDrawable
import kotlin.math.roundToInt

object PieChartRenderer {

    fun createChart(categories: List<Category>, expenses: Map<String, Double>, total: Double, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // RectF для рисования дуг
        val rectF = RectF(0f, 0f, size.toFloat(), size.toFloat())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        var startAngle = -90f // Начинаем сверху

        if (total == 0.0 || categories.isEmpty()) {
            // Если расходов нет, рисуем пустой круг (серая заглушка)
            paint.color = Color.parseColor("#FF424242")
            // Рисуем полный круг
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

            // Рисуем отверстие
            val holePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            holePaint.color = Color.parseColor("#FF121212") // Цвет фона
            canvas.drawCircle(size / 2f, size / 2f, size * 0.35f, holePaint)

            return bitmap
        }

        // 1. Итерируемся по категориям, которые должны быть показаны
        for (category in categories) {
            val amount = expenses[category.id] ?: 0.0

            // Если сумма больше нуля, рисуем сегмент
            if (amount > 0) {
                // Рассчитываем угол
                val sweepAngle = (amount / total * 360).toFloat()

                try {
                    // Установка цвета
                    paint.color = Color.parseColor(category.colorHex)
                } catch (e: IllegalArgumentException) {
                    paint.color = Color.GRAY
                }

                // Рисуем сегмент
                canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)

                // Смещаем начальный угол для следующего сегмента
                startAngle += sweepAngle
            }
        }

        // 3. Рисуем отверстие в центре (эффект пончика)
        val holePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        holePaint.color = Color.parseColor("#FF121212") // Цвет фона
        canvas.drawCircle(size / 2f, size / 2f, size * 0.35f, holePaint)

        return bitmap
    }
}