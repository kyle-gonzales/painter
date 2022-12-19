package com.example.painter

import android.app.Dialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private var drawingView : DrawingView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)
        drawingView!!.setBrushSize(10f)
        drawingView!!.setBrushColor(Color.BLUE)

        val ibBrushSize = findViewById<ImageButton>(R.id.ibBrushSize)
        ibBrushSize.setOnClickListener { showBrushSizeSelectorDialog() }

        val ibBlackBrush = findViewById<ImageButton>(R.id.ibBlackBrush)
        ibBlackBrush.setOnClickListener { drawingView!!.setBrushColor(Color.BLACK) }

        val ibRedBrush = findViewById<ImageButton>(R.id.ibRedBrush)
        ibRedBrush.setOnClickListener { drawingView!!.setBrushColor(Color.RED) }

        val ibGreenBrush = findViewById<ImageButton>(R.id.ibGreenBrush)
        ibGreenBrush.setOnClickListener { drawingView!!.setBrushColor(Color.GREEN) }

        val ibBlueBrush = findViewById<ImageButton>(R.id.ibBlueBrush)
        ibBlueBrush.setOnClickListener { drawingView!!.setBrushColor(Color.BLUE) }

        val ibYellowBrush = findViewById<ImageButton>(R.id.ibYellowBrush)
        ibYellowBrush.setOnClickListener { drawingView!!.setBrushColor(Color.YELLOW) }

        val ibPinkBrush = findViewById<ImageButton>(R.id.ibPinkBrush)
        ibPinkBrush.setOnClickListener { drawingView!!.setBrushColor(Color.MAGENTA) }

        val ibCyanBrush = findViewById<ImageButton>(R.id.ibCyanBrush)
        ibCyanBrush.setOnClickListener { drawingView!!.setBrushColor(Color.CYAN) }

    }

    private fun showBrushSizeSelectorDialog() { // create your own dialog
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.ibSmallBrush) // creates small button dialog
        smallBtn.setOnClickListener {
            drawingView!!.setBrushSize(10f) // set the brush size after clicking on the button
            brushDialog.dismiss() // close the dialog after clicking the button
        }
        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.ibMediumBrush)
        mediumBtn.setOnClickListener {
            drawingView!!.setBrushSize(20f)
            brushDialog.dismiss() // close the dialog after clicking the button
        }
        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.ibLargeBrush)
        largeBtn.setOnClickListener {
            drawingView!!.setBrushSize(30f)
            brushDialog.dismiss() // close the dialog after clicking the button
        }
        brushDialog.show()
    }
}