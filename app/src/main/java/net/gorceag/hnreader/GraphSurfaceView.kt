package net.gorceag.kotlinblade

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.SurfaceHolder
import android.view.SurfaceView
import net.gorceag.hnreader.R
import java.util.*
import java.util.concurrent.*

/**
 * Created by slash on 2/5/17.
 */
class GraphSurfaceView(activity: Activity, val data: Map<Int, Int>) : SurfaceView(activity), SurfaceHolder.Callback {
    inner class Drawer(val holder: SurfaceHolder, val context: Context) : Runnable {
        val paint: Paint
        lateinit var vNumbers: Array<String>
        lateinit var hNumbers: Array<String>
        var leftMargin: Float = 0f
        var bottomMargin: Float = 0f
        val topMargin: Float = context.resources.getDimension(R.dimen.padding) * 2
        val rightMargin: Float = context.resources.getDimension(R.dimen.padding) * 2

        init {
            paint = Paint()
            paint.textSize = context.resources.getDimension(R.dimen.text_size)
        }

        fun initScreenRelatedData(width: Int, height: Int) {
            var max = data.values.max()
            vNumbers = assembleRulerScale(height, if (max != null) max else 0)
            max = data.keys.max()
            hNumbers = assembleRulerScale(width, if (max != null) max else 0)
            setBottomMargin()
            setLeftMargin()
        }

        private fun assembleRulerScale(length: Int, highestValue: Int): Array<String> {
            val maxSteps = length / context.resources.getDimension(R.dimen.max_number_distance)
            var step = 1
            while (highestValue / step > maxSteps) {
                step = increaseStep(step)
            }
            val size = 1 + highestValue / step + if (highestValue % step == 0) 0 else 1
            return Array(size, { i -> (i * step).toString()})
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

        private fun setBottomMargin() {
            val resources = context.resources
            val padding = resources.getDimension(R.dimen.padding)
            val textSize = resources.getDimension(R.dimen.text_size)
            val textToLine = resources.getDimension(R.dimen.text_to_line_margin)
            bottomMargin = padding + textSize + textToLine
        }

        private fun setLeftMargin() {
            val resources = context.resources
            val padding = resources.getDimension(R.dimen.padding)
            var textWidth = Array(vNumbers.size, { i -> paint.measureText(vNumbers.get(i))}).max()
            if (textWidth == null) {
                textWidth = 0f
            }
            val textToLine = resources.getDimension(R.dimen.text_to_line_margin)
            leftMargin = padding + textWidth + textToLine
        }

        override fun run() {
            val canvas = holder.lockCanvas()
            canvas.drawColor(context.getResources().getColor(R.color.colorAccent))
            drawRulers(canvas)
            drawBars(canvas)
            drawData(canvas)
            holder.unlockCanvasAndPost(canvas)
        }

        private fun drawRulers(canvas: Canvas) {
            val padding: Float = context.resources.getDimension(R.dimen.padding)
            var numberOfSpaces = vNumbers.size - 1
            val vStep = canvas.height / numberOfSpaces - topMargin / numberOfSpaces - bottomMargin / numberOfSpaces
            numberOfSpaces = hNumbers.size - 1
            val hStep = canvas.width / numberOfSpaces - rightMargin / numberOfSpaces - leftMargin / numberOfSpaces
            drawVerticalRuler(canvas, vStep, padding)
            drawHorizontalRuler(canvas, hStep, padding)
        }

        private fun drawVerticalRuler(canvas: Canvas, step: Float, padding: Float) {
            for ((index, value) in vNumbers.withIndex()) {
                val xOffset = paint.measureText(value) + context.resources.getDimension(R.dimen.text_to_line_margin)
                val y = topMargin + (vNumbers.size - index - 1) * step
                canvas.drawText(value, leftMargin - xOffset, y + context.resources.getDimension(R.dimen.text_size) / 2, paint)
                canvas.drawLine(leftMargin - context.resources.getDimension(R.dimen.text_to_line_margin) / 2, y, canvas.width - padding, y, paint)
            }
        }

        private fun drawHorizontalRuler(canvas: Canvas, step: Float, padding: Float) {
            for ((index, value) in hNumbers.withIndex()) {
                val xOffset = paint.measureText(value) / 2
                val x = leftMargin + index * step
                canvas.drawText(value, x - xOffset, canvas.height - padding, paint)
                val y = canvas.height - bottomMargin
                canvas.drawLine(x, y + context.resources.getDimension(R.dimen.text_to_line_margin) / 2, x, y, paint)
            }
        }

        private fun drawBars(canvas: Canvas) {
            val padding = context.resources.getDimension(R.dimen.padding)
            canvas.drawLine(leftMargin, padding, leftMargin, canvas.height - bottomMargin, paint)
            canvas.drawLine(leftMargin, canvas.height - bottomMargin, canvas.width - padding, canvas.height - bottomMargin, paint)
        }

        private fun drawData(canvas: Canvas) {
            val hPixels = canvas.width - leftMargin - rightMargin
            val vPixels = canvas.height - bottomMargin - topMargin
            val hValue = hNumbers.last().toFloat()
            val vValue = vNumbers.last().toFloat()

            val hMeter = hPixels / hValue
            val vMeter = vPixels / vValue
            for (pair in data) {
                val x = pair.key * hMeter + leftMargin
                val y = canvas.height - pair.value * vMeter - bottomMargin
//                val x = 2000 * hMeter + leftMargin
//                val y = canvas.height - 300 * vMeter - bottomMargin
                canvas.drawLine(x, y, x, canvas.height - bottomMargin, paint)
            }
        }
    }

    val fps: Int = context.resources.getInteger(R.integer.fps)
    val mSec: Float = 1000f / fps
    var drawer: Drawer = Drawer(getHolder(), activity)

    init {
        getHolder().addCallback(this)
    }

    lateinit var viewRefreshExecutor: ScheduledExecutorService
    lateinit var viewRefreshFuture: ScheduledFuture<*>

    private fun startDraw() {
        viewRefreshExecutor = Executors.newSingleThreadScheduledExecutor()
        viewRefreshFuture = viewRefreshExecutor.scheduleAtFixedRate(drawer, mSec.toLong(), mSec.toLong(), TimeUnit.MILLISECONDS)
    }

    private fun stopDraw() {
        viewRefreshFuture.cancel(true)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        startDraw()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        drawer.initScreenRelatedData(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        stopDraw()
    }

}