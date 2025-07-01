package com.example.financeapp.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CircularProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 40f
        color = Color.LTGRAY
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 60f
        textAlign = Paint.Align.CENTER

    }

    private val categoryPaints = mutableListOf<Paint>()
    private val oval = RectF()
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f

    private var totalAmount: Float = 0f
    private var categoryData: List<CategoryBreakdownItem> = emptyList()
    private var currency: String = "$"

    data class CategoryBreakdownItem(
        val category: String,
        val amount: Float,
        val percentage: Float,
        val color: Int // Added color field
    )

    fun setData(totalAmount: Float, categoryData: List<CategoryBreakdownItem>, currency: String) {
        this.totalAmount = totalAmount
        this.categoryData = categoryData
        this.currency = currency

        // Create a paint for each category using the provided color
        categoryPaints.clear()
        categoryData.forEach { item ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 40f
                color = item.color // Use the color provided for this category
            }
            categoryPaints.add(paint)
        }

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = (Math.min(w, h) / 2f) - 50f // Adjust radius to fit within the view
        oval.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background circle
        canvas.drawArc(oval, 0f, 360f, false, backgroundPaint)

        // Draw category arcs
        var startAngle = -90f // Start from the top
        categoryData.forEachIndexed { index, item ->
            val sweepAngle = (item.percentage / 100f) * 360f
            canvas.drawArc(oval, startAngle, sweepAngle, false, categoryPaints[index])
            startAngle += sweepAngle
        }

        // Draw total amount in the center
        val text = "$currency${String.format("%.2f", totalAmount)}"
        canvas.drawText(text, centerX, centerY + (textPaint.textSize / 3), textPaint)
    }
}