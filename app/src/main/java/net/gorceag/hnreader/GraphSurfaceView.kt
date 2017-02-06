package net.gorceag.kotlinblade

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.view.SurfaceHolder
import android.view.SurfaceView
import net.gorceag.hnreader.R
import java.util.concurrent.*

/**
 * Created by slash on 2/5/17.
 */
class GraphSurfaceView(activity: Activity) : SurfaceView(activity), SurfaceHolder.Callback{
    class Drawer(val holder: SurfaceHolder, val context: Context) : Runnable {
        val paint: Paint = Paint()

        override fun run() {

            val canvas = holder.lockCanvas()
            canvas.drawColor(context.getResources().getColor(R.color.colorAccent))
            holder.unlockCanvasAndPost(canvas)
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