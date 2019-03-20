package com.example.tripod

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class CustomCanvas(context: Context) : View(context) {
    private val staticPaint = Paint()
    private val dynamicPaint = Paint()


    var yStart = 0f
    var yFinish = 0f

    var vxStart = 0f
    var vxFinish = 0f
    var vyStart = 0f
    var vyFinish = 0f

    var angle = 0

    fun onSensorChanged(roll: Float) {
        angle = -roll.toInt()

        if (angle < 90) {
            this.yStart = (height / 2).toFloat() + (height / 2) * (angle.toFloat() / 90)
            this.yFinish = (height / 2).toFloat() - (height / 2) * (angle.toFloat() / 90)

            this.vxStart = (width / 2).toFloat() + (width / 2) * (angle.toFloat() / 90)
            this.vxFinish = (width / 2).toFloat() - (width / 2) * (angle.toFloat() / 90)

            this.vyStart = yStart + height / 2
            this.vyFinish = yFinish - height / 2
        } else {
            this.yStart = (height / 2).toFloat() - (height / 2) * (angle.toFloat() / 90)
            this.yFinish = (height / 2).toFloat() + (height / 2) * (angle.toFloat() / 90)

            this.vxStart = (width / 2).toFloat() - (width / 2) * (angle.toFloat() / 90)
            this.vxFinish = (width / 2).toFloat() + (width / 2) * (angle.toFloat() / 90)

            this.vyStart = yStart - height / 2
            this.vyFinish = yFinish + height / 2
        }


    }

    override fun onDraw(canvas: Canvas) {
        val width = width
        val height = height

        staticPaint.strokeWidth = 4f

        canvas.drawLine(0f, (height / 2).toFloat(), width.toFloat(), (height / 2).toFloat(), staticPaint)
        canvas.drawLine(
            (width / 2).toFloat(),
            (height - 10).toFloat(),
            (width / 2).toFloat(),
            (10).toFloat(),
            staticPaint
        )
        dynamicPaint.color = if (angle != 0) Color.RED else Color.GREEN
        dynamicPaint.strokeWidth = 6f

        val dx = width.toFloat() - 0f
        val dy = yFinish - yStart

        val ox = 0f + (dx - dy) / 2
        val oy = yStart + (dx + dy) / 2

        canvas.drawLine(0f, yStart, width.toFloat(), yFinish, dynamicPaint)
        canvas.drawLine(ox, oy, ox + dy, oy - dx, dynamicPaint)
        invalidate()
    }
}