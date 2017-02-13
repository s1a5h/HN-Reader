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
import net.gorceag.hnreader.AnimationDrawer
import net.gorceag.hnreader.AnimatorCallback
import net.gorceag.hnreader.R

/**
 * Created by slash on 2/9/17.
 *
 * As a SurfaceView the class provides it's SurfaceHolder to the hosted AnimationDrawer
 */

class WebFragmentAnimator(callback: AnimatorCallback, activity: Context, backgroundImage: Bitmap, position: Array<Float>) : SurfaceView(activity), SurfaceHolder.Callback {

    inner class Drawer(holder: SurfaceHolder, context: Context, callback: AnimatorCallback, val backgroundImage: Bitmap, var position: Array<Float>) : AnimationDrawer(holder, context, callback) {
        private lateinit var foregroundImage: Bitmap
        private var maxDistance = 0f

        private val weight: Float

        lateinit var middlegroundImage: Bitmap

        init {
            val out: TypedValue = TypedValue()
            context.resources.getValue(R.dimen.expand_bubble_weight, out, true)
            weight = out.float
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

        override fun updateProgress(delta: Long) {
            when (state) {
                1, 2, 4, 5, 6 -> progress += (delta.toFloat() / avgAnimationStepTime)
            }
            if (progress >= 1) {
                updateState()
            }
        }

        override fun updateState() {
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
            handler.post { callback.enableBackgroundContent(enable) }
        }


        override fun drawChoreography(canvas: Canvas) {
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
        }

        private fun drawFadeOutBackground(canvas: Canvas) {
            paint.alpha = (255 * (1f - progress)).toInt()
            canvas.drawBitmap(backgroundImage, 0f, 0f, paint)
            canvas.drawRect(0f, position[0], canvas.width.toFloat(), position[1], paint)
            paint.alpha = 255
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

    }

    private var drawer: Drawer = Drawer(holder, activity, callback, backgroundImage, position)

    init {
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

    private fun stopDraw() {
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