package com.bluell.roomdecoration.interiordesign.common.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs


class DrawingView @JvmOverloads constructor(
    c: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    View(c, attrs, defStyle) {
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var path: Path? = null
     var bitmapPaint: Paint? = null
    private var paint: Paint? = null
    private var drawMode = false
    private var x = 0f
    private var y = 0f
    private var penSize = 10f
    private var eraserSize = 10f
    private val pathHistory = mutableListOf<Path>()
    private var lastX: Float = 0f
    private var lastY: Float = 0f

    init {
        init()
    }

    private var scaledImageWidth: Int = 0
    private var scaledImageHeight: Int = 0

    fun undo() {
        if (pathHistory.isNotEmpty()) {
            pathHistory.removeAt(pathHistory.size - 1)
            redrawCanvas()
        }
    }

    private fun redrawCanvas() {
        bitmap?.eraseColor(Color.TRANSPARENT)
        pathHistory.forEach { canvas?.drawPath(it, paint!!) }
        invalidate()
    }

    fun setScaledImageDimensions(width: Int, height: Int) {
        scaledImageWidth = width
        scaledImageHeight = height
        requestLayout() // Trigger re-measure and layout
    }
    private fun init() {
        path = Path()
        bitmapPaint = Paint(Paint.DITHER_FLAG)
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.isDither = true
        paint!!.color = Color.BLACK
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeJoin = Paint.Join.ROUND
        paint!!.strokeCap = Paint.Cap.ROUND
        paint!!.strokeWidth = penSize
        drawMode = true
        paint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        }
        canvas = Canvas(bitmap!!)
        canvas!!.drawColor(Color.TRANSPARENT)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap!!, 0f, 0f, bitmapPaint)
        path?.let { canvas.drawPath(it, paint!!) }

    }

    private fun touchStart(x: Float, y: Float) {
        path?.reset()
        path?.moveTo(x, y)
        lastX = x
        lastY = y
        path?.let { canvas!!.drawPath(it, paint!!) }
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = abs(x - lastX)
        val dy = abs(y - lastY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path?.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
            lastX = x
            lastY = y
        }
        path?.let { canvas!!.drawPath(it, paint!!) }
    }

    private fun touchUp() {
        path?.lineTo(lastX, lastY)
        path?.let { canvas!!.drawPath(it, paint!!) }
        pathHistory.add(Path(path)) // Add a copy of the path to pathHistory
        path?.reset()
        if (drawMode) {
            paint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        } else {
            paint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!drawMode) {
                    paint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                } else {
                    paint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                }
                touchStart(x, y)
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                if (!drawMode) {
                    path?.lineTo(this.x, this.y)
                    path?.reset()
                    path?.moveTo(x, y)
                }
                path?.let { canvas!!.drawPath(it, paint!!) }
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }

            else -> {}
        }
        lastX = x
        lastY = y
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        val imageView = parent as? ImageView
//        val width = imageView?.measuredWidth ?: 0
//        val height = imageView?.measuredHeight ?: 0
        setMeasuredDimension(scaledImageWidth, scaledImageHeight)
    }
    fun initializePen() {
        drawMode = true
        paint!!.isAntiAlias = true
        paint!!.isDither = true
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeJoin = Paint.Join.ROUND
        paint!!.strokeCap = Paint.Cap.ROUND
        paint!!.strokeWidth = penSize
        paint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }

    fun initializeEraser() {
        drawMode = false
        paint!!.color = Color.parseColor("#f4f4f4")
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = eraserSize
        paint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    fun clear() {
        canvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    override fun setBackgroundColor(color: Int) {
        if (canvas == null) {
            canvas = Canvas()
        }
        canvas!!.drawColor(color)
        super.setBackgroundColor(color)
    }

    fun setEraserSize(size: Float) {
        eraserSize = size
        initializeEraser()
    }

    fun setPenSize(size: Float) {
        penSize = size
        initializePen()
    }

    fun getEraserSize(): Float {
        return eraserSize
    }

    fun getPenSize(): Float {
        return penSize
    }

    @get:ColorInt
    var penColor: Int
        get() = paint!!.color
        set(color) {
            paint!!.color = color
        }

    fun loadImage(bitmap: Bitmap) {
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvas!!.setBitmap(this.bitmap)
        bitmap.recycle()
        invalidate()
    }

    fun saveImage(
        filePath: String?, filename: String, format: CompressFormat?,
        quality: Int
    ): Boolean {
        if (quality > 100) {
            Log.d("saveImage", "quality cannot be greater that 100")
            return false
        }
        val file: File
        var out: FileOutputStream? = null
        try {
            return when (format) {
                CompressFormat.PNG -> {
                    file = File(filePath, "$filename.png")
                    out = FileOutputStream(file)
                    bitmap!!.compress(CompressFormat.PNG, quality, out)
                }

                CompressFormat.JPEG -> {
                    file = File(filePath, "$filename.jpg")
                    out = FileOutputStream(file)
                    bitmap!!.compress(CompressFormat.JPEG, quality, out)
                }

                else -> {
                    file = File(filePath, "$filename.png")
                    out = FileOutputStream(file)
                    bitmap!!.compress(CompressFormat.PNG, quality, out)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }
    fun getBmp():Bitmap?{
        return bitmap
    }


    fun getBitmap():Bitmap{
        val bitmap= Bitmap.createBitmap(
            this.width,
            this.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        return bitmap
    }

    companion object {
        private const val TOUCH_TOLERANCE = 4f
    }
}