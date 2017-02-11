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

    inner class ScaleNumberData(val value: String, val textX: Float, val textY: Float, val lineXStart: Float, val lineYStart: Float, val lineXEnd: Float, val lineYEnd: Float) {
    }

    inner class Drawer(val holder: SurfaceHolder, val context: Context) : Thread() {
        lateinit var paint: Paint

        lateinit var vScaleData: Array<ScaleNumberData>
        lateinit var hScaleData: Array<ScaleNumberData>
        lateinit var barsRect: Array<Float>
        lateinit var pillars: Array<Bitmap>
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
            vScaleData = getVScaleData(width, height)
            hScaleData = getHScaleData(width, height)
            barsRect = getBarsData(width, height)
            pillars = getPillarBitmaps()
        }

        private fun getVScaleData(width: Int, height: Int): Array<ScaleNumberData> {

//            var max = model.data.keys.max()
            var max = Array(model.data.size, { i -> model.data[i][0]}).max()
            val vNumbers = getScaleNumbers(height, if (max != null) max else 0)
            val padding: Float = context.resources.getDimension(R.dimen.padding)
            val maxLenght = Array(vNumbers.size, { i -> paint.measureText(vNumbers[i]) }).max()
            val maxNumberLength: Float = if (maxLenght != null) maxLenght else 0f
            val topMargin = padding * 2
            val rightMargin = padding * 2
            val textToLine = context.resources.getDimension(R.dimen.text_to_line_margin)
            val textSize = context.resources.getDimension(R.dimen.text_size)
            var numberOfSpaces = vNumbers.size - 1
            var bottomMargin = padding + textSize + textToLine
//            val step = height / numberOfSpaces - topMargin / numberOfSpaces - bottomMargin / numberOfSpaces
            val step = (height - topMargin - bottomMargin) / numberOfSpaces
            return Array(vNumbers.size, { index ->
                val value = vNumbers[index]
                val textX = padding + maxNumberLength - paint.measureText(value)
                val lineY = topMargin + (vNumbers.size - index - 1) * step
                val textY = lineY + context.resources.getDimension(R.dimen.text_size) / 2
                val lineXStart = padding + maxNumberLength + context.resources.getDimension(R.dimen.text_to_line_margin) / 2
                val lineXEnd = width - rightMargin
                ScaleNumberData(value, textX, textY, lineXStart, lineY, lineXEnd, lineY)
            })
        }

        private fun getHScaleData(widht: Int, height: Int): Array<ScaleNumberData> {

            val hNumbers = getScaleNumbers(width, model.distance)

            val vNumbers = Array(vScaleData.size, { i -> vScaleData[i].value })
            val padding: Float = context.resources.getDimension(R.dimen.padding)
            val maxLenght = Array(vNumbers.size, { i -> paint.measureText(vNumbers[i]) }).max()
            val vNumberWidth: Float = if (maxLenght != null) maxLenght else 0f
            val distanceToLine = context.resources.getDimension(R.dimen.text_to_line_margin)
            var numberOfSpaces = hNumbers.size - 1
            val step = (width - padding * 3 - vNumberWidth - distanceToLine) / numberOfSpaces
            return Array(hNumbers.size, { index ->
                val value = hNumbers[index]
                val textOffset = paint.measureText(value) / 2
                val lineX = padding + vNumberWidth + distanceToLine + index * step
                val textX = lineX - textOffset
                val textY = height - padding
                val lineYStart = height - padding - context.resources.getDimension(R.dimen.text_size) - distanceToLine
                val lineYEnd = lineYStart + distanceToLine / 2
                ScaleNumberData(value, textX, textY, lineX, lineYStart, lineX, lineYEnd)
            })
        }

        private fun getScaleNumbers(length: Int, highestValue: Int): Array<String> {
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

        private fun getBarsData(width: Int, height: Int): Array<Float> {

            val vNumbers = Array(vScaleData.size, { i -> vScaleData[i].value })
            val maxLenght = Array(vNumbers.size, { i -> paint.measureText(vNumbers[i]) }).max()
            val vNumberWidth: Float = if (maxLenght != null) maxLenght else 0f
            val padding: Float = context.resources.getDimension(R.dimen.padding)
            val distanceToLine = context.resources.getDimension(R.dimen.text_to_line_margin)
            val left = padding + vNumberWidth + distanceToLine
            val bottom = padding + context.resources.getDimension(R.dimen.text_size) + distanceToLine
            return arrayOf(left, padding, width - padding, height - bottom)
        }

        private fun getPillarBitmaps(): Array<Bitmap> {
            val hPixels = hScaleData.last().lineXStart - hScaleData.first().lineXStart
            val vPixels: Float = vScaleData.first().lineYStart - vScaleData.last().lineYStart
            val hValue = hScaleData.last().value
            val vValue: Float = vScaleData.last().value.toFloat()

            val columnCount = model.data.size
            val columnPadding = context.resources.getDimension(R.dimen.column_spacing) / 2
            val imageColumnWidth = hPixels / columnCount
            println("COLUMN WIDTH: " + imageColumnWidth)


            val vMeter = vPixels / vValue
            return Array(model.data.size, { index ->
                val pillarModel = model.data[index]
                val height = pillarModel[0] * vMeter
                val bitmap = Bitmap.createBitmap(imageColumnWidth.toInt(), (height + columnPadding).toInt(), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                println("BITMAP WIDTH: " + bitmap.width)
                paint.setShadowLayer(10.0f, 0.0f, 2.0f, ContextCompat.getColor(context, R.color.black));
                paint.color = pillarModel[1]
                canvas.drawRect(columnPadding, columnPadding, imageColumnWidth.toInt() - columnPadding, height + columnPadding, paint)
                paint.setShadowLayer(0f, 0f, 0f, 0)
                paint.color = ContextCompat.getColor(context, R.color.black)
                bitmap
            })
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
//                showFps(delta)
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
            drawData(canvas, progress)
        }

        private fun drawIdle(canvas: Canvas) {
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.color = ContextCompat.getColor(context, R.color.black)
            drawRulers(canvas)
            drawBars(canvas)
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            drawData(canvas, 1f)
        }

        private fun drawCollapsePillars(canvas: Canvas) {
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.color = ContextCompat.getColor(context, R.color.black)
            drawRulers(canvas)
            drawBars(canvas)
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            drawData(canvas, (1 - progress))
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
            for (numberData in vScaleData) {
                canvas.drawText(numberData.value, numberData.textX, numberData.textY, paint)
                canvas.drawLine(numberData.lineXStart, numberData.lineYStart, numberData.lineXEnd, numberData.lineYEnd, paint)
            }
            for (numberData in hScaleData) {
                canvas.drawText(numberData.value, numberData.textX, numberData.textY, paint)
                canvas.drawLine(numberData.lineXStart, numberData.lineYStart, numberData.lineXEnd, numberData.lineYEnd, paint)
            }
        }

        private fun drawBars(canvas: Canvas) {
            canvas.drawLine(barsRect[0], barsRect[1], barsRect[0], barsRect[3], paint)
            canvas.drawLine(barsRect[0], barsRect[3], barsRect[2], barsRect[3], paint)
        }


        fun initiate() {
            isRunning = true
            start()
        }

        fun terminate() {
            isRunning = false
        }


        private fun drawData(canvas: Canvas, progress: Float) {

            val vNumbers = Array(vScaleData.size, { i -> vScaleData[i].value })
            val maxLenght = Array(vNumbers.size, { i -> paint.measureText(vNumbers[i]) }).max()
            val vNumberWidth: Float = if (maxLenght != null) maxLenght else 0f
            val padding = context.resources.getDimension(R.dimen.padding)
            val textToLine = context.resources.getDimension(R.dimen.text_to_line_margin)
            val textSize = context.resources.getDimension(R.dimen.text_size)
            val xOffset = padding + vNumberWidth + textToLine
            val yOffset = padding + textSize + textToLine

            for ((index, pillar) in pillars.withIndex()) {
                val x = xOffset + index * pillar.width
                val y = canvas.height - yOffset - pillar.height * progress
                canvas.drawBitmap(pillar, x, y, paint)
            }
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
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        stopDraw()
    }

}