package net.gorceag.kotlinblade

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.SurfaceHolder
import android.view.SurfaceView
import net.gorceag.hnreader.R
import java.util.concurrent.*

/**
 * Created by slash on 2/5/17.
 */
class GraphSurfaceView(activity: Activity, val data: Map<Int, Int>) : SurfaceView(activity), SurfaceHolder.Callback {
    class Drawer(val holder: SurfaceHolder, val context: Context) : Runnable {
        val paint: Paint
        val vNumbers: Array<String>
        val hNumbers: Array<String>

        init {
            paint = Paint()
            paint.textSize = context.resources.getDimension(R.dimen.text_size)
            vNumbers = parseVertical()
            hNumbers = parseHorizontal()
        }

        private fun parseVertical(): Array<String> {
            return Array(15, { i -> (i * i * i).toString() })
        }

        private fun parseHorizontal(): Array<String> {
            return Array(6, { i -> (i * 2).toString() })
        }

        override fun run() {

            val canvas = holder.lockCanvas()

            canvas.drawColor(context.getResources().getColor(R.color.colorAccent))
            val yOffset = getYOffset(context)
            val xOffset = drawVerticalRuler(vNumbers, canvas, yOffset) + context.resources.getDimension(R.dimen.text_to_line_margin)

            drawHorizontalRuler(hNumbers, canvas, xOffset)

            drawBars(xOffset, yOffset, canvas)


//            canvas.drawText("hello", 50f, 50f, paint)
            holder.unlockCanvasAndPost(canvas)
        }

        private fun getYOffset(context: Context): Float {
            val resources = context.resources
            val padding = resources.getDimension(R.dimen.padding)
            val textSize = resources.getDimension(R.dimen.text_size)
            val textToLine = resources.getDimension(R.dimen.text_to_line_margin)
            return padding + textSize + textToLine
        }

        private fun drawVerticalRuler(numbers: Array<String>, canvas: Canvas, yOffset: Float): Float {

            val padding: Float = context.resources.getDimension(R.dimen.padding)
            val numberOfSpaces = numbers.size - 1
            val step = canvas.height / numberOfSpaces - padding / numberOfSpaces - yOffset / numberOfSpaces
            var biggestXOffset: Float = 0f

            for (value in numbers) {
                val xOffset = paint.measureText(value)
                if (biggestXOffset < xOffset) {
                    biggestXOffset = xOffset
                }
            }

            for ((index, value) in numbers.withIndex()) {
                val xOffset = paint.measureText(value)
                canvas.drawText(value, padding + biggestXOffset - xOffset, padding + (numbers.size - index - 1) * step, paint)
            }
            return biggestXOffset + padding
        }

        private fun drawHorizontalRuler(numbers: Array<String>, canvas: Canvas, axeOffset: Float) {

            val padding: Float = context.resources.getDimension(R.dimen.padding)
            val numberOfSpaces = numbers.size - 1
            val step = canvas.width / numberOfSpaces - padding / numberOfSpaces - axeOffset / numberOfSpaces
            val yCoord = canvas.height - padding


            for ((index, value) in numbers.withIndex()) {
                val xOffset = paint.measureText(value) / 2
                canvas.drawText(value, axeOffset + index * step - xOffset, yCoord, paint)
            }
        }

        private fun drawBars(xOffset: Float, yOffset: Float, canvas: Canvas) {
            val padding = context.resources.getDimension(R.dimen.padding)
            canvas.drawLine(xOffset, padding, xOffset, canvas.height - yOffset, paint)
            canvas.drawLine(xOffset, canvas.height - yOffset, canvas.width - padding, canvas.height - yOffset, paint)
        }

    }

    val fps: Int = 60
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
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        stopDraw()
    }

}