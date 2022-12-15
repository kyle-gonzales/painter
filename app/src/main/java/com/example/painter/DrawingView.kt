package com.example.painter

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


// creating a custom view
class DrawingView(context : Context, attrs: AttributeSet) : View(context, attrs) {

    private var drawPaint : Paint? = null // contains style, color, and information about how to draw the geometry
    private var drawPath : CustomPath? = null // brush stroke from our custom path class
    private var canvasBitmap : Bitmap? = null
    private var canvasPaint : Paint? = null

    private var brushSize : Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas : Canvas? = null
    private val paths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        drawPaint = Paint()
        drawPath = CustomPath(color, brushSize)

        drawPaint!!.color = color // since we explicitly initialized paint right before accessing one of its attributes, we can use the !! operator.
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND // used whenever the paint's style is STROKE or STROKE_AND_FILL
        canvasPaint = Paint(Paint.DITHER_FLAG)
        brushSize = 20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX= event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath!!.color = color
                drawPath!!.brushThickness = brushSize

                drawPath!!.reset()

                drawPath!!.moveTo(touchX!!, touchY!!) // sets the x,y to the area where you tapped the screen
                drawPath!!.lineTo(touchX!!, touchY!!) // creates a point
            }

            MotionEvent.ACTION_MOVE -> {
                drawPath!!.lineTo(touchX!!, touchY!!) // draws as you slide your finger on the screen
            }

            MotionEvent.ACTION_UP -> {
                paths.add(drawPath!!)
                drawPath = CustomPath(color, brushSize) // reset the path to a blank path
            }
            else -> return false
        }
        invalidate()
        return true
    }

    // change Canvas to Canvas? if error
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)

        for (path in paths) { // continuously draw the paths
            drawPaint!!.strokeWidth = path.brushThickness
            drawPaint!!.color = path.color
            canvas.drawPath(path, drawPaint!!)
        }

        if (!drawPath!!.isEmpty) { // start a new path
            drawPaint!!.strokeWidth = drawPath!!.brushThickness
            drawPaint!!.color = drawPath!!.color
            canvas.drawPath(drawPath!!, drawPaint!!)
        }
    }

    internal inner class CustomPath(var color: Int, var brushThickness : Float) : Path() { // each stroke drawn on the canvas

    }

}