package net.gorceag.hnreader.detail

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import net.gorceag.hnreader.AnimatorCallback
import net.gorceag.hnreader.R

/**
 * Created by slash on 2/9/17.
 */
class WebFragmentAnimator(val fragment: AnimatorCallback, activity: Context, val backgroundImage: Bitmap, var position: Array<Float>) : SurfaceView(activity), SurfaceHolder.Callback {

    inner class Drawer(val holder: SurfaceHolder, val context: Context) : Thread() {
        lateinit var paint: Paint
        var isRunning = false
        lateinit var middlegroundImage: Bitmap
        lateinit var foregroundImage: Bitmap
        var state = 1
        var progress = 0f
        var maxDistance = 0f

        init {
            holder.setFormat(PixelFormat.TRANSPARENT)
            setPaint()
            trimPosition()
            setForegroundImage()
            setDistance()
        }

        private fun setDistance() {
            val max = arrayOf(position[0], backgroundImage.height - position[1]).max()
            if (max != null) {
                maxDistance = max
            }
        }

        private fun setForegroundImage() {
            foregroundImage = Bitmap.createBitmap(backgroundImage, 0, position[0].toInt(), backgroundImage.width, (position[1] - position[0]).toInt())
        }

        private fun setPaint() {
            paint = Paint()
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            paint.strokeWidth = context.resources.getDimension(R.dimen.list_separator_height)
            paint.style = Paint.Style.FILL
        }

        private fun trimPosition() {
            if (position[0] < 0) {
                position[0] = 0f
            }
            if (backgroundImage.height < position[1]) {
                position[1] = backgroundImage.height.toFloat()
            }
        }

        private fun updateProgress(delta: Long) {
            when (state) {
                1, 2, 4, 5, 6 -> progress += (delta.toFloat() / avgAnimationStepTime)
            }
            if (progress >= 1) {
                updateState()
            }
        }

        fun updateState() {
            progress = 0f
            when (state) {
                1 -> enableParentBackground(true)
                2 -> {
                    isClickable = false
                    sendIdleMessage()
                }
                3 -> isClickable = true
                4 -> enableParentBackground(false)
                6 -> terminateParent()
            }
            state++
        }

        private fun enableParentBackground(enable: Boolean) {
            val handler = Handler(context.mainLooper)
            handler.post { fragment.enableBackgroundContent(enable) }
        }

        private fun sendIdleMessage() {
            val handler = Handler(context.mainLooper)
            handler.post { fragment.initiated() }
        }

        private fun terminateParent() {
            val handler = Handler(context.mainLooper)
            handler.post { fragment.terminated() }
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
                1 -> drawCellExpand(canvas)
                2 -> drawFadeOutRect(canvas)
                4 -> drawFadeMiddleImage(canvas)
                5 -> drawCellCollapse(canvas)
                6 -> drawFadeOutBackground(canvas)
            }
        }

        private fun drawCellExpand(canvas: Canvas) {
            canvas.drawBitmap(backgroundImage, 0f, 0f, paint)
            drawBase(canvas, AccelerateInterpolator(2f).getInterpolation(progress))
            paint.alpha = (255 * (1f - progress)).toInt()
            canvas.drawBitmap(foregroundImage, 0f, position[0], paint)
            paint.alpha = 255
        }

        private fun drawFadeOutRect(canvas: Canvas) {
            paint.alpha = (255 * (1f - progress)).toInt()
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.alpha = 255
        }

        private fun drawFadeMiddleImage(canvas: Canvas) {
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.alpha = (255 * (1f - progress)).toInt()
            canvas.drawBitmap(middlegroundImage, 0f, 0f, paint)
            paint.alpha = 255
        }

        private fun drawCellCollapse(canvas: Canvas) {
            canvas.drawBitmap(backgroundImage, 0f, 0f, paint)
            drawBase(canvas, DecelerateInterpolator(1f).getInterpolation(1 - progress))
//            drawInnerImage(canvas)
        }

        private fun drawFadeOutBackground(canvas: Canvas) {
            paint.alpha = (255 * (1f - progress)).toInt()
            canvas.drawBitmap(backgroundImage, 0f, 0f, paint)
            canvas.drawRect(0f, position[0], canvas.width.toFloat(), position[1], paint)
            paint.alpha = 255
        }

        fun drawInnerImage(canvas: Canvas) {
            val bitmap2 = getCroppedBitmap(getOuterPath(canvas))
            paint.alpha = (255 * (1f - progress)).toInt()
            canvas.drawBitmap(bitmap2, 0f, 0f, paint)
            paint.alpha = 255
        }

        private fun getCroppedBitmap(path: Path): Bitmap {
            val bitmap = Bitmap.createBitmap(backgroundImage.width, backgroundImage.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(middlegroundImage, 0f, 0f, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            canvas.drawPath(path, paint)
            paint.xfermode = null
            return bitmap
        }


        private fun getOuterPath(canvas: Canvas): Path {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            val xStep = width * weight
            val xMiddle = width / 2
            val yStrafeEnd = maxDistance * (1 - progress)
            val yStrafeMiddle = yStrafeEnd * 1.3f
            val upperYEnd = position[0] - yStrafeEnd
            val upperYMiddle = position[0] - yStrafeMiddle
            val lowerYEnd = position[1] + yStrafeEnd
            val lowerYMiddle = position[1] + yStrafeMiddle

            val path = Path()
            path.moveTo(0f, upperYEnd)
            path.cubicTo(0f, upperYEnd, xStep, upperYMiddle, xMiddle, upperYMiddle)
            path.cubicTo(xMiddle, upperYMiddle, width - xStep, upperYMiddle, width, upperYEnd)
            path.lineTo(width, 0f)
            path.lineTo(0f, 0f)
            path.lineTo(0f, upperYEnd)
            path.moveTo(width, lowerYEnd)
            path.cubicTo(width, lowerYEnd, width - xStep, lowerYMiddle, xMiddle, lowerYMiddle)
            path.cubicTo(xMiddle, lowerYMiddle, xStep, lowerYMiddle, 0f, lowerYEnd)
            path.lineTo(0f, height)
            path.lineTo(width, height)
            path.lineTo(width, lowerYEnd)
            return path
        }

        private fun getInnerPath(canvas: Canvas, drawProgress: Float): Path {
            val width = canvas.width.toFloat()
            val xStep = width * weight
            val xMiddle = width / 2
            val yStrafeEnd = maxDistance * drawProgress
            val yStrafeMiddle = yStrafeEnd * 1.3f
            val upperYEnd = position[0] - yStrafeEnd
            val upperYMiddle = position[0] - yStrafeMiddle
            val lowerYEnd = position[1] + yStrafeEnd
            val lowerYMiddle = position[1] + yStrafeMiddle

            val path = Path()
            path.moveTo(0f, upperYEnd)
            path.cubicTo(0f, upperYEnd, xStep, upperYMiddle, xMiddle, upperYMiddle)
            path.cubicTo(xMiddle, upperYMiddle, width - xStep, upperYMiddle, width, upperYEnd)
            path.lineTo(width, lowerYEnd)
            path.cubicTo(width, lowerYEnd, width - xStep, lowerYMiddle, xMiddle, lowerYMiddle)
            path.cubicTo(xMiddle, lowerYMiddle, xStep, lowerYMiddle, 0f, lowerYEnd)
            path.lineTo(0f, upperYEnd)
            return path
        }

        private fun drawBase(canvas: Canvas, drawProgress: Float) {
            val path = getInnerPath(canvas, drawProgress)
            canvas.drawPath(path, paint)
            paint.style = Paint.Style.STROKE
            paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
            canvas.drawPath(path, paint)
            paint.style = Paint.Style.FILL
            paint.color = ContextCompat.getColor(context, R.color.neutral)
        }

        private fun manageWait(startTime: Long) {
            val endTime = System.currentTimeMillis()
            val wait = mSec - (endTime - startTime)
            if (wait > 0) {
                Thread.sleep(wait)
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

        fun initiate() {
            isRunning = true
            start()
        }

        fun terminate() {
            isRunning = false
        }

        fun recycleBitmaps() {
            backgroundImage.recycle()
            middlegroundImage.recycle()
            foregroundImage.recycle()
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

    fun roundUp(bitmap: Bitmap) {
        if (drawer.state == 3) {
            drawer.middlegroundImage = bitmap
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
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        stopDraw()
    }

}