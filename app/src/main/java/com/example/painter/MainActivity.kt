package com.example.painter

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.Instrumentation.ActivityResult
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener

class MainActivity : AppCompatActivity() {

    private var drawingView : DrawingView? = null
    private var ibCurrentBrushColor : ImageButton? = null

    var tvCustomColorBrush : TextView? = null
    /*permission result launcher for one permission*/
    private val cameraResultLauncher : ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
            {
                /*renaming 'it' to isGranted */
                isGranted -> if(isGranted){
                    Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "permission DENIED", Toast.LENGTH_SHORT).show()
                }
            }

    /*permission result launcher for MULTIPLE permissions*/

    private val cameraAndLocationResultLauncher : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            /*renaming 'it : Map<String, Boolean>!' to permissions */
                permissions -> permissions.entries.forEach {
                    /* Entry<String!, Boolean!> in map*/
                    val permissionName = it.key
                    val isGranted = it.value
                    if (isGranted) {
                        /*check which permission is granted*/
                        if (permissionName == Manifest.permission.CAMERA) {
                            Toast.makeText(this, "permission granted for CAMERA", Toast.LENGTH_SHORT).show()
                        } else if (permissionName == Manifest.permission.ACCESS_COARSE_LOCATION){
                            Toast.makeText(this, "permission granted for approximate LOCATION", Toast.LENGTH_SHORT).show()
                        } else{
                            Toast.makeText(this, "permission granted for precise LOCATION", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        if (permissionName == Manifest.permission.CAMERA) {
                            Toast.makeText(this, "permission DENIED for CAMERA", Toast.LENGTH_SHORT).show()
                        } else if (permissionName == Manifest.permission.ACCESS_COARSE_LOCATION){
                            Toast.makeText(this, "permission DENIED for approximate LOCATION", Toast.LENGTH_SHORT).show()
                        } else{
                            Toast.makeText(this, "permission DENIED for precise LOCATION", Toast.LENGTH_SHORT).show()
                        }
                    }
                 }

        }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val llBrushPalette = findViewById<LinearLayout>(R.id.llBrushPalette)
        ibCurrentBrushColor = llBrushPalette[0] as ImageButton // import view.get to get views inside a layout // not really needed, but it's here so we know we can use it lolz
        ibCurrentBrushColor!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))

        drawingView = findViewById(R.id.drawingView)
        drawingView!!.setBrushSize(10f)

        val btnCameraPermission : ImageButton = findViewById(R.id.ibSetBackground)
        btnCameraPermission.setOnClickListener {
            // if we have already asked for permission, yet the user did not grant access, display a dialog
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {/*check for the android version*/ /*Android M => Android 4.1*/
                showRationalDialog("Painter Requires Camera", "Camera cannot be used because camera access is denied")
            } else { // request for permission first
                cameraAndLocationResultLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }


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

    private fun showRationalDialog(title: String, message: String) {
        val builder : AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create()
        builder.show()
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