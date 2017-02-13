package net.gorceag.hnreader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.view.SurfaceHolder

/**
 * Created by slash on 2/13/17.
 *
 * Any fragment that wishes to have a transition animation should host a SurfaceView with an inner AnimationDrawer
 * This is a convenient template method for creating phased animations
 */

abstract class AnimationDrawer(val holder: SurfaceHolder, val context: Context, val callback: AnimatorCallback) : Thread() {
    protected var isRunning = false
    protected lateinit var paint: Paint
    protected var progress = 0f
    protected val avgAnimationStepTime = context.resources.getInteger(R.integer.avg_animation_step_time)
    private val fpsMillis: Long = (1000f / context.resources.getInteger(R.integer.fps)).toLong()
    var state = 1

    abstract protected fun updateProgress(delta: Long)
    abstract fun updateState()
    abstract protected fun drawChoreography(canvas: Canvas)

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


    private fun manageWait(startTime: Long) {
        val endTime = System.currentTimeMillis()
        val wait = fpsMillis - (endTime - startTime)
        if (wait > 0) {
            Thread.sleep(wait)
        }
    }

    private fun showFps(delta: Long) {  // while debugging can be used to monitor the gpu load
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


    protected fun sendIdleMessage() {
        val handler = Handler(context.mainLooper)
        handler.post { callback.initiated() }
    }

    protected fun terminateParent() {
        val handler = Handler(context.mainLooper)
        handler.post { callback.terminated() }
    }
}