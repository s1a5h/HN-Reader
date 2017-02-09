package net.gorceag.hnreader.detail

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.view.SurfaceHolder
import android.view.SurfaceView
import net.gorceag.hnreader.R

/**
 * Created by slash on 2/9/17.
 */
class WebFragmentAnimator(activity: Activity, val background: Bitmap, var position: Int) : SurfaceView(activity), SurfaceHolder.Callback {
    inner class Drawer(val holder: SurfaceHolder, val context: Context) : Thread() {
        val paint: Paint
        var isRunning = false

        init {
//            holder.setFormat(PixelFormat.TRANSPARENT);
            paint = Paint()
        }

        override fun run() {
            var oldStartTime = System.currentTimeMillis()
            while (isRunning) {
                val startTime = System.currentTimeMillis()
                val delta = startTime - oldStartTime
                oldStartTime = startTime
                showFps(delta)
                val canvas = holder.lockCanvas()
                if (canvas != null) {
                    canvas.drawColor(ContextCompat.getColor(context, R.color.colorAccent))
                canvas.drawBitmap(background, 0f, 0f, paint)
//                paint.color = ContextCompat.getColor(context, R.color.colorAccent)
                    canvas.drawCircle(200f, position.toFloat(), 100f, paint)
                    position += 4
                    holder.unlockCanvasAndPost(canvas)
                }
                manageWait(startTime)
            }
        }

        private fun manageWait(startTime: Long) {
            val endTime = System.currentTimeMillis()
            println("FOR: " + mSec + ", SAFE MARGIN : " + (mSec - (endTime - startTime)))
            val wait = mSec - (endTime - startTime)
            if (wait > 0) {
                Thread.sleep(wait)
            }
        }

        private fun showFps(delta: Long) {

            val cores = Runtime.getRuntime().availableProcessors();
            println("NUMBER OF CORES: " + cores)
            if (delta != 0L) {
                val actualFps = 1000L / delta
                println("FPS: " + actualFps)
            }
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
    val mSec: Long = (1000f / fps).toLong()
    var drawer: Drawer = Drawer(holder, activity)

    init {
        getHolder().addCallback(this)
    }

    private fun startDraw() {
        drawer.initiate()
    }

    fun stopDraw() {
        drawer.terminate()
        background.recycle()
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