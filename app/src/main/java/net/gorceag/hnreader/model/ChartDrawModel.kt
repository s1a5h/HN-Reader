package net.gorceag.hnreader.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import net.gorceag.hnreader.R
import net.gorceag.kotlinblade.ChartFragmentAnimator

/**
 * Created by slash on 2/12/17.
 */

class ChartDrawModel(val canvasWidth: Int, val canvasHeight: Int, val model: ChartModel, val context: Context) {

    inner class ScaleNumberData(val value: String, val textX: Float, val textY: Float, val lineXStart: Float, val lineYStart: Float, val lineXEnd: Float, val lineYEnd: Float)

    val padding: Float = context.resources.getDimension(R.dimen.padding)
    val textToLine = context.resources.getDimension(R.dimen.text_to_line_margin)
    val textSize = context.resources.getDimension(R.dimen.text_size)
    val maxNumberDistance = context.resources.getDimension(R.dimen.max_number_distance)
    val distanceToLine = context.resources.getDimension(R.dimen.text_to_line_margin)
    val columnPadding = context.resources.getDimension(R.dimen.column_padding)

    lateinit private var vNumbers: Array<String>
    private var maxNumberLength: Float = 0f
    private var spacesCount: Int = 0
    private var bottomMargin: Float = 0f

    val vScaleData: Array<ScaleNumberData>
    val hScaleData: Array<ScaleNumberData>
    val grid: Array<Array<Float>>
    val barsRect: Array<Float>
    val pillars: Array<Bitmap>
    val paint = Paint()

    init {
        paint.textSize = textSize
        buildVerticalNumbers()
        vScaleData = buildVScale(canvasHeight)
        hScaleData = buildHScale(canvasWidth, canvasHeight)
        barsRect = buildBars(canvasWidth, canvasHeight)
        grid = buildGrid(canvasWidth, canvasHeight)
        pillars = buildPillarBitmaps(canvasWidth)
    }

    private fun buildVerticalNumbers() {
        vNumbers = getScaleNumbers(canvasHeight, Array(model.data.size, { i -> model.data[i][0] }).max() ?: 0)
        maxNumberLength = Array(vNumbers.size, { i -> paint.measureText(vNumbers[i]) }).max() ?: 0f
        spacesCount = vNumbers.size - 1
        bottomMargin = padding + textSize + textToLine
    }

    private fun buildVScale(height: Int): Array<ScaleNumberData> {
        val step = (height - padding * 2 - bottomMargin) / spacesCount
        return Array(vNumbers.size, { index ->
            val value = vNumbers[index]
            val textX = padding + maxNumberLength - paint.measureText(value)
            val lineY = padding * 2 + (vNumbers.size - index - 1) * step
            val textY = lineY + textSize / 2
            val lineXStart = padding + maxNumberLength + textToLine / 2
            val lineXEnd = padding + maxNumberLength + textToLine
            ScaleNumberData(value, textX, textY, lineXStart, lineY, lineXEnd, lineY)
        })
    }

    private fun buildGrid(width: Int, height: Int): Array<Array<Float>> {
        val step = (height - padding * 2 - bottomMargin) / spacesCount
        return Array(vNumbers.size, { index ->
            val lineY = padding * 2 + (vNumbers.size - index - 1) * step
            val lineXStart = padding + maxNumberLength + textToLine
            val lineXEnd = width - padding * 2
            arrayOf(lineXStart, lineY, lineXEnd, lineY)
        })
    }

    private fun getScaleNumbers(length: Int, highestValue: Int): Array<String> {
        val maxSteps = length / maxNumberDistance
        var step = 1
        while (highestValue / step > maxSteps) {
            step = increaseStep(step)
        }
        val size = 1 + highestValue / step + if (highestValue % step == 0) 0 else 1
        return Array(size, { i -> (i * step).toString() })
    }

    private fun increaseStep(step: Int): Int {
        var head = step.toString().substring(0, 1)
        val tail = step.toString().substring(1)
        when (head) {
            "1" -> head = "2"
            "2" -> head = "5"
            "5" -> head = "10"
        }
        return (head + tail).toInt()
    }

    private fun buildHScale(width: Int, height: Int): Array<ScaleNumberData> {
        val hNumbers = Array(model.data.size, { i -> (i + 1).toString() })
        val step = (width - padding * 3 - maxNumberLength - distanceToLine) / hNumbers.size
        return Array(hNumbers.size, { index ->
            val value = hNumbers[index]
            val textOffset = paint.measureText(value) / 2
            val lineX = padding + maxNumberLength + distanceToLine + step / 2 + index * step
            val textX = lineX - textOffset
            val textY = height - padding
            val lineYStart = height - padding - textSize - distanceToLine
            val lineYEnd = lineYStart + distanceToLine / 2
            ScaleNumberData(value, textX, textY, lineX, lineYStart, lineX, lineYEnd)
        })
    }

    private fun buildBars(width: Int, height: Int): Array<Float> {
        val textSize = context.resources.getDimension(R.dimen.text_size)
        val left = padding + maxNumberLength + distanceToLine
        val bottom = padding + textSize + distanceToLine
        return arrayOf(left, padding, width - padding, height - bottom)
    }

    private fun buildPillarBitmaps(width: Int): Array<Bitmap> {
        val vPixelCount = vScaleData.first().lineYStart - vScaleData.last().lineYStart
        val vValue = vScaleData.last().value.toFloat()
        val vMeter = vPixelCount / vValue
        val hPixelCount = width - padding * 3 - maxNumberLength - distanceToLine
        val columnCount = model.data.size
        val imageColumnWidth = hPixelCount / columnCount
        return Array(model.data.size, { index ->
            val pillarModel = model.data[index]
            val height = pillarModel[0] * vMeter
            val bitmap = Bitmap.createBitmap(imageColumnWidth.toInt(), (height + columnPadding).toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            paint.color = pillarModel[1]
            canvas.drawRect(columnPadding, columnPadding, imageColumnWidth.toInt() - columnPadding, height + columnPadding, paint)
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            bitmap
        })
    }
}