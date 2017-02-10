package net.gorceag.kotlinblade

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.SurfaceHolder
import android.view.SurfaceView
import net.gorceag.hnreader.R
import net.gorceag.hnreader.chart.ChartFragment
import net.gorceag.hnreader.model.GraphModel
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.*

/**
 * Created by slash on 2/5/17.
 */
class ChartFragmentAnimator(val fragment: ChartFragment, val model: GraphModel, val backgroundImage: Bitmap, val position: IntArray) : SurfaceView(fragment.activity), SurfaceHolder.Callback {
    inner class Drawer(val holder: SurfaceHolder, val context: Context) : Thread() {
        lateinit var paint: Paint
        lateinit var vNumbers: Array<String>
        lateinit var hNumbers: Array<String>
        var leftMargin: Float = 0f
        var bottomMargin: Float = 0f
        val topMargin: Float = context.resources.getDimension(R.dimen.padding) * 2
        val rightMargin: Float = context.resources.getDimension(R.dimen.padding) * 2
        var isRunning = false
        var state = 1
        var progress = 0f

        init {
//            holder.setFormat(PixelFormat.TRANSPARENT);
            setPaint()
            paint.textSize = context.resources.getDimension(R.dimen.text_size)
        }

        private fun setPaint() {
            paint = Paint()
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            paint.strokeWidth = context.resources.getDimension(R.dimen.list_separator_height)
            paint.style = Paint.Style.FILL
        }

        fun initScreenRelatedData(width: Int, height: Int) {
            var max = model.data.keys.max()
            vNumbers = assembleRulerScale(height, if (max != null) max else 0)
            max = model.distance
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
            var textWidth = Array(vNumbers.size, { i -> paint.measureText(vNumbers.get(i)) }).max()
            if (textWidth == null) {
                textWidth = 0f
            }
            val textToLine = resources.getDimension(R.dimen.text_to_line_margin)
            leftMargin = padding + textWidth + textToLine
        }

        private fun updateProgress(delta: Long) {
            when (state) {
                1, 2, 3, 5, 6, 7 -> progress += (delta.toFloat() / avgAnimationStepTime)
            }
            if (progress >= 1) {
                updateState()
            }
        }

        fun updateState() {
            progress = 0f
            when (state) {
                7 -> terminateParent()
            }
            state++
        }

        private fun terminateParent() {
            val handler = Handler(context.mainLooper)
            handler.post { fragment.terminate() }
        }

        override fun run() {
            var oldStartTime = System.currentTimeMillis()
            while (isRunning) {
                val startTime = System.currentTimeMillis()
                val delta = startTime - oldStartTime
                oldStartTime = startTime
                updateProgress(delta)
                showFps(delta)
                val canvas = holder.lockCanvas()
                if (canvas != null) {
                    drawChoreography(canvas)
                    holder.unlockCanvasAndPost(canvas)
                }
                manageWait(startTime)
            }
        }

        private fun drawChoreography(canvas: Canvas) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            when (state) {
                1 -> drawBackgroundExpand(canvas)
                2 -> drawFadeInRulers(canvas)
                3 -> drawRisePillars(canvas)
                4 -> drawIdle(canvas)
                5 -> drawCollapsePillars(canvas)
                6 -> drawFadeOutRulers(canvas)
                7 -> drawBackgroundCollapse(canvas)
            }
        }


        private fun showFps(delta: Long) {
            val cores = Runtime.getRuntime().availableProcessors()
            println("NUMBER OF CORES: " + cores)
            if (delta != 0L) {
                val actualFps = 1000L / delta
                println("FPS: " + actualFps)
            }
        }

        private fun manageWait(startTime: Long) {
            val endTime = System.currentTimeMillis()
            val wait = mSec - (endTime - startTime)
            if (wait > 0) {
                Thread.sleep(wait)
            }
        }


        private fun getPath(canvas: Canvas, drawProgress: Float): Path {

            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            val xStep = width * weight
            val xMiddle = width / 2
            val yStrafeEnd = height * drawProgress
            val yStrafeMiddle = yStrafeEnd * 1.2f

            val path = Path()
            path.moveTo(0f, 0f)
            path.lineTo(width, 0f)
            path.lineTo(width, yStrafeEnd)

            path.cubicTo(width, yStrafeEnd, width - xStep, yStrafeMiddle, xMiddle, yStrafeMiddle)
            path.cubicTo(xMiddle, yStrafeMiddle, xStep, yStrafeMiddle, 0f, yStrafeEnd)
            path.lineTo(0f, 0f)
            return path
        }

        private fun drawBackgroundExpand(canvas: Canvas) {
            canvas.drawBitmap(backgroundImage, 0f, 0f, paint)
            val path = getPath(canvas, progress)

            val fromColor = ContextCompat.getColor(context, R.color.colorPrimary)
            val toColor = ContextCompat.getColor(context, R.color.neutral)
            val redDiff = Color.red(toColor) - Color.red(fromColor)
            val greenDiff = Color.green(toColor) - Color.green(fromColor)
            val blueDiff = Color.blue(toColor) - Color.blue(fromColor)

            val color = Color.argb(
                    255,
                    Color.red(fromColor) + (redDiff * progress).toInt(),
                    Color.green(fromColor) + (greenDiff * progress).toInt(),
                    Color.blue(fromColor) + (blueDiff * progress).toInt()
            );

            paint.color = color
            canvas.drawPath(path, paint)
            paint.color = ContextCompat.getColor(context, R.color.neutral)
        }

        private fun drawFadeInRulers(canvas: Canvas) {
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.color = ContextCompat.getColor(context, R.color.black)
            paint.alpha = (255 * progress).toInt()
            drawRulers(canvas)
            drawBars(canvas)
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            paint.alpha = 255
        }

        private fun drawRisePillars(canvas: Canvas) {
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.color = ContextCompat.getColor(context, R.color.black)
            drawRulers(canvas)
            drawBars(canvas)
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            drawData(canvas)
        }

        private fun drawIdle(canvas: Canvas) {
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.color = ContextCompat.getColor(context, R.color.black)
            drawRulers(canvas)
            drawBars(canvas)
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            drawData(canvas)
        }

        private fun drawCollapsePillars(canvas: Canvas) {
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.color = ContextCompat.getColor(context, R.color.black)
            drawRulers(canvas)
            drawBars(canvas)
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            drawData(canvas)
        }

        private fun drawFadeOutRulers(canvas: Canvas) {
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.color = ContextCompat.getColor(context, R.color.black)
            paint.alpha = (255 * (1 - progress)).toInt()
            drawRulers(canvas)
            drawBars(canvas)
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            paint.alpha = 255
        }

        private fun drawBackgroundCollapse(canvas: Canvas) {

            canvas.drawBitmap(backgroundImage, 0f, 0f, paint)
            val path = getPath(canvas, (1 - progress))

            val fromColor = ContextCompat.getColor(context, R.color.colorPrimary)
            val toColor = ContextCompat.getColor(context, R.color.neutral)
            val redDiff = Color.red(toColor) - Color.red(fromColor)
            val greenDiff = Color.green(toColor) - Color.green(fromColor)
            val blueDiff = Color.blue(toColor) - Color.blue(fromColor)

            val color = Color.argb(
                    255,
                    Color.red(fromColor) + (redDiff * (1 - progress)).toInt(),
                    Color.green(fromColor) + (greenDiff * (1 - progress)).toInt(),
                    Color.blue(fromColor) + (blueDiff * (1 - progress)).toInt()
            )

            paint.color = color
            canvas.drawPath(path, paint)
            paint.color = ContextCompat.getColor(context, R.color.neutral)
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

        fun initiate() {
            isRunning = true
            start()
        }

        fun terminate() {
            isRunning = false
        }

        private fun drawData(canvas: Canvas) {
            val hPixels = canvas.width - leftMargin - rightMargin
            val vPixels = canvas.height - bottomMargin - topMargin
            val hValue = hNumbers.last().toFloat()
            val vValue = vNumbers.last().toFloat()

            val columnCount = model.data.size
            val columnSpacing = context.resources.getDimension(R.dimen.column_spacing)
            val columnWidth = hPixels / columnCount - columnSpacing / columnCount - columnSpacing

            val vMeter = vPixels / vValue
            val bottom = canvas.height - bottomMargin
            paint.setShadowLayer(10.0f, 0.0f, 2.0f, ContextCompat.getColor(context, R.color.black));
            for ((index, key) in model.data.keys.withIndex()) {
                val left = leftMargin + columnSpacing + index * (columnWidth + columnSpacing)
                val top = canvas.height - key * vMeter - bottomMargin
                val color = model.data.get(key)
                if (color != null) {
                    paint.color = color
                }
                canvas.drawRect(left, top, left + columnWidth, bottom, paint)
            }
            paint.setShadowLayer(0f, 0f, 0f, 0);
            paint.color = ContextCompat.getColor(context, R.color.neutral)
        }
    }

    val fps: Int = context.resources.getInteger(R.integer.fps)
    val avgAnimationStepTime = context.resources.getInteger(R.integer.avg_animation_step_time)

    val weight: Float
    val mSec: Long = (1000f / fps).toLong()
    var drawer: Drawer = Drawer(holder, fragment.activity)

    init {
        val out: TypedValue = TypedValue()
        context.resources.getValue(R.dimen.expand_bubble_weight, out, true)
        weight = out.float
        holder.addCallback(this)

    }

    fun roundUp() {
        if (drawer.state == 4) {
            drawer.updateState()
        }
    }

    private fun startDraw() {
        drawer.initiate()
    }

    fun stopDraw() {
        drawer.terminate()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        startDraw()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        drawer.initScreenRelatedData(width, height)
//        drawer.draw()

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        stopDraw()
    }

}