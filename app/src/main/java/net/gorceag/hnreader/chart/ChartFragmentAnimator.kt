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
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import net.gorceag.hnreader.AnimatorCallback
import net.gorceag.hnreader.R
import net.gorceag.hnreader.chart.ChartFragment
import net.gorceag.hnreader.model.ChartDrawModel
import net.gorceag.hnreader.model.ChartModel
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.*

/**
 * Created by slash on 2/5/17.
 */
class ChartFragmentAnimator(val fragment: AnimatorCallback, activity: Context, val model: ChartModel, val backgroundImage: Bitmap) : SurfaceView(activity), SurfaceHolder.Callback {

    inner class Drawer(val holder: SurfaceHolder, val context: Context) : Thread() {
        var animationState = 1

        private lateinit var paint: Paint
        lateinit var chartDrawModel: ChartDrawModel
        private var isRunning = false
        private var progress = 0f

        private val accelerator = AccelerateInterpolator(2f)
        private val decelerator = DecelerateInterpolator(2f)

        init {
            holder.setFormat(PixelFormat.TRANSPARENT);
            setPaint()
            paint.textSize = context.resources.getDimension(R.dimen.text_size)
            paint.strokeWidth = context.resources.getDimension(R.dimen.line_width)
        }

        private fun setPaint() {
            paint = Paint()
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            paint.strokeWidth = context.resources.getDimension(R.dimen.list_separator_height)
            paint.style = Paint.Style.FILL
        }


        private fun updateProgress(delta: Long) {
            when (animationState) {
                1, 2, 3, 5, 6, 7 -> progress += (delta.toFloat() / avgAnimationStepTime)
            }
            if (progress >= 1) {
                updateState()
            }
        }

        fun updateState() {
            progress = 0f
            when (animationState) {
                3 -> sendIdleMessage()
                7 -> terminateParent()
            }
            animationState++
        }


        private fun sendIdleMessage() {
            val handler = Handler(context.mainLooper)
            handler.post { fragment.initiated() }
        }

        private fun terminateParent() {
            val handler = Handler(context.mainLooper)
            handler.post { fragment.terminated() }
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

        override fun run() {
            var oldStartTime = System.currentTimeMillis()
            while (isRunning) {
                val startTime = System.currentTimeMillis()
                val delta = startTime - oldStartTime
                oldStartTime = startTime
//                showFps(delta)
                updateProgress(delta)
                updateView()
                manageWait(startTime)
            }
        }


        private fun updateView() {
            val canvas = holder.lockCanvas()
            if (canvas != null) {
                drawChoreography(canvas)
                holder.unlockCanvasAndPost(canvas)
            }
        }

        private fun drawChoreography(canvas: Canvas) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            when (animationState) {
                1 -> drawBackground(canvas, accelerator.getInterpolation(progress))
                2 -> drawEmptyChart(canvas, progress)
                3 -> drawFilledChart(canvas, decelerator.getInterpolation(progress))
                4 -> drawFilledChart(canvas, 1f)
                5 -> drawFilledChart(canvas, accelerator.getInterpolation(1 - progress))
                6 -> drawEmptyChart(canvas, 1 - progress)
                7 -> drawBackground(canvas, decelerator.getInterpolation(1 - progress))
            }
        }

        private fun buildPath(canvas: Canvas, drawProgress: Float): Path {
            val width = canvas.width.toFloat()
            val xStep = width * weight
            val xMiddle = width / 2
            val yStrafeEnd = canvas.height.toFloat() * drawProgress
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

        private fun buildColor(progress: Float): Int {
            val fromColor = ContextCompat.getColor(context, R.color.colorPrimary)
            val toColor = ContextCompat.getColor(context, R.color.neutral)
            val redDiff = Color.red(toColor) - Color.red(fromColor)
            val greenDiff = Color.green(toColor) - Color.green(fromColor)
            val blueDiff = Color.blue(toColor) - Color.blue(fromColor)

            return Color.argb(
                    255,
                    Color.red(fromColor) + (redDiff * progress).toInt(),
                    Color.green(fromColor) + (greenDiff * progress).toInt(),
                    Color.blue(fromColor) + (blueDiff * progress).toInt()
            )
        }

        private fun drawBackground(canvas: Canvas, progress: Float) {
            canvas.drawBitmap(backgroundImage, 0f, 0f, paint)
            val path = buildPath(canvas, progress)
            paint.color = buildColor(progress)
            canvas.drawPath(path, paint)
        }

        private fun drawEmptyChart(canvas: Canvas, progress: Float) {
            canvas.drawColor(ContextCompat.getColor(context, R.color.neutral))
            drawGrid(canvas, progress)
            drawScale(canvas, progress)
            drawBars(canvas, progress)
        }


        private fun drawFilledChart(canvas: Canvas, progress: Float) {
            canvas.drawColor(ContextCompat.getColor(context, R.color.neutral))
            drawGrid(canvas, 1f)
            drawData(canvas, progress)
            drawScale(canvas, 1f)
            drawBars(canvas, 1f)
        }

        private fun drawGrid(canvas: Canvas, progress: Float) {
            paint.color = ContextCompat.getColor(context, R.color.black)
            paint.alpha = (255 * progress).toInt()
            for (line in chartDrawModel.grid) {
                canvas.drawLine(line[0], line[1], line[2], line[3], paint)
            }
            paint.alpha = 255
        }

        private fun drawScaleNumber(canvas: Canvas, numberData: ChartDrawModel.ScaleNumberData) {
            canvas.drawText(numberData.value, numberData.textX, numberData.textY, paint)
            canvas.drawLine(numberData.lineXStart, numberData.lineYStart, numberData.lineXEnd, numberData.lineYEnd, paint)
        }

        private fun drawScale(canvas: Canvas, progress: Float) {
            paint.color = ContextCompat.getColor(context, R.color.black)
            paint.alpha = (255 * progress).toInt()
            chartDrawModel.vScaleData.forEach { drawScaleNumber(canvas, it) }
            chartDrawModel.hScaleData.forEach { drawScaleNumber(canvas, it) }
            paint.alpha = 255
        }

        private fun drawBars(canvas: Canvas, progress: Float) {
            paint.color = ContextCompat.getColor(context, R.color.black)
            paint.alpha = (255 * progress).toInt()
            paint.strokeWidth = context.resources.getDimension(R.dimen.bar_line_width)
            val rect = chartDrawModel.barsRect
            canvas.drawLine(rect[0], rect[1], rect[0], rect[3], paint)
            canvas.drawLine(rect[0], rect[3], rect[2], rect[3], paint)
            paint.strokeWidth = context.resources.getDimension(R.dimen.line_width)
            paint.alpha = 255
        }

        private fun drawData(canvas: Canvas, progress: Float) {
            val vNumbers = Array(chartDrawModel.vScaleData.size, { i -> chartDrawModel.vScaleData[i].value })
            val vNumberWidth: Float = Array(vNumbers.size, { i -> paint.measureText(vNumbers[i]) }).max() ?: 0f
            val padding = context.resources.getDimension(R.dimen.padding)
            val textToLine = context.resources.getDimension(R.dimen.text_to_line_margin)
            val textSize = context.resources.getDimension(R.dimen.text_size)
            val xOffset = padding + vNumberWidth + textToLine
            val yOffset = padding + textSize + textToLine

            for ((index, pillar) in chartDrawModel.pillars.withIndex()) {
                val x = xOffset + index * pillar.width
                val y = canvas.height - yOffset - pillar.height * progress
                canvas.drawBitmap(pillar, x, y, paint)
            }
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            canvas.drawRect(0f, canvas.height - yOffset, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        }

        fun initiate() {
            isRunning = true
            start()
        }

        fun terminate() {
            isRunning = false
        }

    }

    val fps: Int = context.resources.getInteger(R.integer.fps)
    val avgAnimationStepTime = context.resources.getInteger(R.integer.avg_animation_step_time)

    val weight: Float
    val mSec: Long = (1000f / fps).toLong()
    var drawer: Drawer = Drawer(holder, activity)

    init {
        val out: TypedValue = TypedValue()
        context.resources.getValue(R.dimen.expand_bubble_weight, out, true)
        weight = out.float
        holder.addCallback(this)

    }

    fun roundUp() {
        if (drawer.animationState == 4) {
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
        drawer.chartDrawModel = ChartDrawModel(width, height, model, context)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        stopDraw()
    }

}