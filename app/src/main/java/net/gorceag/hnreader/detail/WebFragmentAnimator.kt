package net.gorceag.hnreader.detail

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.SurfaceHolder
import android.view.SurfaceView
import net.gorceag.hnreader.R

/**
 * Created by slash on 2/9/17.
 */
class WebFragmentAnimator(val fragment: WebFragment, activity: Activity, val backgroundImage: Bitmap, var position: Array<Float>) : SurfaceView(activity), SurfaceHolder.Callback {


    inner class Drawer(val holder: SurfaceHolder, val context: Context) : Thread() {
        val paint: Paint
        var isRunning = false
        lateinit var middlegroundImage: Bitmap
        var foregroundImage: Bitmap
        var iter: Int = 0
        var iter2: Int = 0
        var state = 1
        var progress = 0f
        var maxDistance = 0f

        init {
            holder.setFormat(PixelFormat.TRANSPARENT)
            paint = Paint()
//            paint.setShadowLayer(10.0f, 0.0f, 2.0f, ContextCompat.getColor(context, R.color.shadow))
            paint.color = ContextCompat.getColor(context, R.color.neutral)
            paint.strokeWidth = context.resources.getDimension(R.dimen.list_separator_height)

            paint.style = Paint.Style.FILL
            verifyPosition()
            foregroundImage = Bitmap.createBitmap(backgroundImage, 0, position[0].toInt(), backgroundImage.width, (position[1] - position[0]).toInt())
//            val upperDistance = position[0]
//            val lowerDistance = backgroundImage.height - position[1]
            val max = arrayOf(position[0], backgroundImage.height - position[1]).max()
            if (max != null) {
                maxDistance = max
            }
//            maxDistance = if (upper > backgroundImage.height - position[1]) position[0] else canvas.height - lowerYEnd
        }

        private fun verifyPosition() {
            if (position[0] < 0) {
                position[0] = 0f
            }
            if (backgroundImage.height < position[1]) {
                position[1] = backgroundImage.height.toFloat()
            }
        }

        private fun updateProgress(delta: Long) {
            when (state) {
                1, 2, 4, 5 -> {
                    progress += (delta / 1000f)
                }
            }
            println(progress)
            if (progress >= 1) {
                updateState()
            }
        }

        fun updateState() {
            progress = 0f
            when (state) {
                1 -> {
                    val handler = Handler(context.mainLooper)
                    handler.post { fragment.enableBackGround() }
                }
                2 -> {
                    isClickable = false
                }
                3 -> {
                    isClickable = true
                }
                4 -> {
                    val handler = Handler(context.mainLooper)
                    handler.post { fragment.disableBackGround() }
                }
                5 -> {
                    fragment.terminate()
                }
            }
            state++
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
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    when (state) {
                        1 -> {
                            canvas.drawBitmap(backgroundImage, 0f, 0f, paint)
                            drawBase(canvas)
                            paint.alpha = (255 * (1f - progress)).toInt()
                            canvas.drawBitmap(foregroundImage, 0f, position[0], paint)
                            paint.alpha = 255
                        }
                        2 -> {
                            paint.alpha = (255 * (1f - progress)).toInt()
                            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
                            paint.alpha = 255
                        }
                        4 -> {
                            canvas.drawBitmap(backgroundImage, 0f, 0f, paint)
                            drawBase2(canvas)
                            drawImage(canvas)
                        }
                        5 -> {
                            paint.alpha = (255 * (1f - progress)).toInt()
                            canvas.drawBitmap(backgroundImage, 0f, 0f, paint)
                            canvas.drawRect(0f, position[0], canvas.width.toFloat(), position[1], paint)
                            paint.alpha = 255
                        }
                    }
//                    pg(canvas)
//                    canvas.drawColor(ContextCompat.getColor(context, R.color.colorAccent))
//                    canvas.drawBitmap(backgroundImage, 0f, 0f, paint)


//                    drawBase(canvas)

//                    canvas.drawRect(0f, position[0] - iter, canvas.width.toFloat(), position[1] - 3 + iter, paint)
//                    paint.setShadowLayer(0f, 0f, 0f, ContextCompat.getColor(context, R.color.shadow));
                    iter += 10
                    iter2 -= 1
//                paint.color = ContextCompat.getColor(context, R.color.colorAccent)
                    holder.unlockCanvasAndPost(canvas)
                }
                manageWait(startTime)
            }
        }

        fun drawImage(canvas: Canvas) {
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

//            canvas.drawRect(0f, 0f, width, height, paint)

            val path = Path()
            path.moveTo(0f, upperYEnd)
            path.cubicTo(0f, upperYEnd, xStep, upperYMiddle, xMiddle, upperYMiddle)
            path.cubicTo(xMiddle, upperYMiddle, width - xStep, upperYMiddle, width, upperYEnd)
            path.lineTo(width, 0f)
            path.lineTo(0f, 0f)
            path.lineTo(0f, upperYEnd)
//            path.lineTo(width, upperYEnd)
            path.moveTo(width, lowerYEnd)
            path.cubicTo(width, lowerYEnd, width - xStep, lowerYMiddle, xMiddle, lowerYMiddle)
            path.cubicTo(xMiddle, lowerYMiddle, xStep, lowerYMiddle, 0f, lowerYEnd)
            path.lineTo(0f, height)
            path.lineTo(width, height)
            path.lineTo(width, lowerYEnd)

//                        val path = Path()
//            path.moveTo(200f, 200f)
//            path.lineTo(600f, 200f)
//            path.lineTo(600f, 600f)
//            path.lineTo(200f, 600f)
//            path.lineTo(200f, 200f)
//
//            path.moveTo(200f, 700f)
//            path.lineTo(600f, 700f)
//            path.lineTo(600f, 1100f)
//            path.lineTo(200f, 1100f)
//            path.lineTo(200f, 700f)


            var batmap2 = Bitmap.createBitmap(backgroundImage.width, backgroundImage.height, Bitmap.Config.ARGB_8888)
            val canvas2 = Canvas(batmap2)
            canvas2.drawBitmap(middlegroundImage, 0f, 0f, paint)
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//            paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
            canvas2.drawPath(path, paint)
            paint.setXfermode(null)
            paint.alpha = (255 * (1f - progress)).toInt()
            canvas.drawBitmap(batmap2, 0f, 0f, paint)
            paint.alpha = 255


//            paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
//            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
//            paint.color = ContextCompat.getColor(context, R.color.green)
//            canvas.drawCircle(300f, 300f, 100f, paint)
//            val path = Path()
//            path.moveTo(200f, 200f)
//            path.lineTo(600f, 200f)
//            path.lineTo(600f, 600f)
//            path.lineTo(200f, 600f)
//            path.lineTo(200f, 200f)
//
//            path.moveTo(200f, 700f)
//            path.lineTo(600f, 700f)
//            path.lineTo(600f, 1100f)
//            path.lineTo(200f, 1100f)
//            path.lineTo(200f, 700f)

//            paint.style = Paint.Style.FILL

//            var batmap2 = Bitmap.createBitmap(backgroundImage.width, backgroundImage.height, Bitmap.Config.ARGB_8888)
//            val canvas2 = Canvas(batmap2)
//            canvas2.drawBitmap(backgroundImage, 0f, 0f, paint)
//            canvas2.drawBitmap(backgroundImage, 0f, 0f, paint)
//            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//            canvas2.drawPath(path, paint)
//            canvas2.drawPath(path2, paint)

//            paint.alpha = (255 * (1f - progress)).toInt()
//            paint.setXfermode(null)
//            canvas.drawBitmap(batmap2, 0f, 0f, paint)
        }

        val weight = 0.2f

        private fun drawBase(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val xStep = width * weight
            val xMiddle = width / 2
            val yStrafeEnd = maxDistance * progress
            val yStrafeMiddle = yStrafeEnd * 1.3f
            val upperYEnd = position[0] - yStrafeEnd
            val upperYMiddle = position[0] - yStrafeMiddle
            val lowerYEnd = position[1] + yStrafeEnd
            val lowerYMiddle = position[1] + yStrafeMiddle

            val path = Path()
            path.moveTo(0f, upperYEnd)
            path.cubicTo(0f, upperYEnd, xStep, upperYMiddle, xMiddle, upperYMiddle)
            path.cubicTo(xMiddle, upperYMiddle, width - xStep, upperYMiddle, width, upperYEnd)
//            path.lineTo(width, upperYEnd)
            path.lineTo(width, lowerYEnd)
            path.cubicTo(width, lowerYEnd, width - xStep, lowerYMiddle, xMiddle, lowerYMiddle)
            path.cubicTo(xMiddle, lowerYMiddle, xStep, lowerYMiddle, 0f, lowerYEnd)
//            path.lineTo(0f, lowerYEnd)
            path.lineTo(0f, upperYEnd)

            canvas.drawPath(path, paint)
            paint.style = Paint.Style.STROKE
            paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
            canvas.drawPath(path, paint)

            paint.style = Paint.Style.FILL
            paint.color = ContextCompat.getColor(context, R.color.neutral)
        }

        private fun drawBase2(canvas: Canvas) {
            val width = canvas.width.toFloat()
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
//            path.lineTo(width, upperYEnd)
            path.lineTo(width, lowerYEnd)
            path.cubicTo(width, lowerYEnd, width - xStep, lowerYMiddle, xMiddle, lowerYMiddle)
            path.cubicTo(xMiddle, lowerYMiddle, xStep, lowerYMiddle, 0f, lowerYEnd)
//            path.lineTo(0f, lowerYEnd)
            path.lineTo(0f, upperYEnd)

            canvas.drawPath(path, paint)
            paint.style = Paint.Style.STROKE
            paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
            canvas.drawPath(path, paint)
//            canvas.drawBitmap(foregroundImage, 0f, position[0], paint)

            paint.style = Paint.Style.FILL
            paint.color = ContextCompat.getColor(context, R.color.neutral)

        }

        private fun manageWait(startTime: Long) {
            val endTime = System.currentTimeMillis()
//            println("FOR: " + mSec + ", SAFE MARGIN : " + (mSec - (endTime - startTime)))
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
            backgroundImage.recycle()
            foregroundImage.recycle()
        }
    }

    val fps: Int = context.resources.getInteger(R.integer.fps)
    val mSec: Long = (1000f / fps).toLong()
    var drawer: Drawer = Drawer(holder, activity)

    init {
        getHolder().addCallback(this)
    }

    fun finalize(bitmap: Bitmap) {
        drawer.middlegroundImage = bitmap
        drawer.updateState()
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