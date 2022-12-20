package com.example.painter

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener

class MainActivity : AppCompatActivity() {

    private var drawingView : DrawingView? = null
    private var ibCurrentBrushColor : ImageButton? = null

    var tvCustomColorBrush : TextView? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val llBrushPalette = findViewById<LinearLayout>(R.id.llBrushPalette)
        ibCurrentBrushColor = llBrushPalette[0] as ImageButton // import view.get to get views inside a layout // not really needed, but it's here so we know we can use it lolz
        ibCurrentBrushColor!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))

        drawingView = findViewById(R.id.drawingView)
        drawingView!!.setBrushSize(10f)


        val ibBrushSize = findViewById<ImageButton>(R.id.ibBrushSize)
        ibBrushSize.setOnClickListener { showBrushSizeSelectorDialog() }

        val ibBlackBrush = findViewById<ImageButton>(R.id.ibBlackBrush)
        ibBlackBrush.setOnClickListener { brushColorClicked(ibBlackBrush) }

        val ibRedBrush = findViewById<ImageButton>(R.id.ibRedBrush)
        ibRedBrush.setOnClickListener { brushColorClicked(ibRedBrush) }

        val ibGreenBrush = findViewById<ImageButton>(R.id.ibGreenBrush)
        ibGreenBrush.setOnClickListener { brushColorClicked(ibGreenBrush) }

        val ibBlueBrush = findViewById<ImageButton>(R.id.ibBlueBrush)
        ibBlueBrush.setOnClickListener { brushColorClicked(ibBlueBrush) }

        val ibYellowBrush = findViewById<ImageButton>(R.id.ibYellowBrush)
        ibYellowBrush.setOnClickListener { brushColorClicked(ibYellowBrush) }

        val ibPinkBrush = findViewById<ImageButton>(R.id.ibPinkBrush)
        ibPinkBrush.setOnClickListener { brushColorClicked(ibPinkBrush) }

        val ibCyanBrush = findViewById<ImageButton>(R.id.ibCyanBrush)
        ibCyanBrush.setOnClickListener { brushColorClicked(ibCyanBrush) }

        val ibSkinBrush = findViewById<ImageButton>(R.id.ibSkinBrush)
        ibSkinBrush.setOnClickListener {brushColorClicked(ibSkinBrush) }

        val ibRandomBrush = findViewById<ImageButton>(R.id.ibRandomBrush)
        ibRandomBrush.setOnClickListener { brushColorClicked(ibRandomBrush) }

        tvCustomColorBrush = findViewById<TextView>(R.id.tvCustomColorBrush)
        tvCustomColorBrush?.setOnClickListener {
            openColorPickerDialogue()
            brushColorClicked(tvCustomColorBrush)
        }
    }

    private fun openColorPickerDialogue() {
        val colorPickerDialogue = AmbilWarnaDialog(this, Color.RED, true,
            object : OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    drawingView!!.setBrushColor("#${String.format("%x", color)}")
                    tvCustomColorBrush?.setBackgroundColor(color)

                }
            })
        colorPickerDialogue.show()
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

    private fun brushColorClicked(view: View?) {
        if (view is TextView) {
            ibCurrentBrushColor!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_default)) // reset to un-clicked bg
        }
        else if (view != ibCurrentBrushColor) {
            val ibBrushColor = view as ImageButton
            ibBrushColor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed)) // set to clicked bg
            ibCurrentBrushColor!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_default)) // reset to un-clicked bg
            val colorTag = ibBrushColor.tag.toString()
            drawingView!!.setBrushColor(colorTag) // change brush color
            ibCurrentBrushColor = ibBrushColor // set to current

            tvCustomColorBrush?.setBackgroundColor(Color.RED) //! needs to be optimized
        }
    }
}