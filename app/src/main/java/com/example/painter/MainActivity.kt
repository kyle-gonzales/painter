package com.example.painter

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener

class MainActivity : AppCompatActivity() {

    private var drawingView : DrawingView? = null
    private var ibCurrentBrushColor : ImageButton? = null
    private var ibUndo : ImageButton? = null
    private var tvCustomColorBrush : TextView? = null
    private var ibSave : ImageButton? = null

    private val openGalleryLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == RESULT_OK && result.data != null)  {
            val imageBackground : ImageView = findViewById(R.id.ivBackground)
            imageBackground.setImageURI(result.data?.data) // path towards an image on your device, not the actual image
        }
    }
    /*permission result launcher for multiple permissions*/
    private val externalStorageResultLauncher : ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissions ->
        permissions.entries.forEach {
            var permissionName = it.key
            var isGranted = it.value

            if (isGranted) {
                if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
//                    Toast.makeText(this, "access granted", Toast.LENGTH_SHORT).show()
                    val pickIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ) // run intent to go to other application
                    openGalleryLauncher.launch(pickIntent)
                } else if (permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    Toast.makeText(
                        this,
                        "write external storage access granted",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "access granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    Toast.makeText(this, "read external storage access denied", Toast.LENGTH_SHORT)
                        .show()

                } else if (permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    Toast.makeText(this, "write external storage access denied", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, "access denied", Toast.LENGTH_SHORT)
                        .show()
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

        tvCustomColorBrush = findViewById(R.id.tvCustomColorBrush)
        tvCustomColorBrush?.setOnClickListener {
            openColorPickerDialogue()
            brushColorClicked(tvCustomColorBrush)
        }
        ibUndo = findViewById(R.id.ibUndo)
        ibUndo?.setOnClickListener { drawingView?.undoPath() }

        // permissions
        val ibSetBackground : ImageButton = findViewById(R.id.ibSetBackground)
        ibSetBackground.setOnClickListener {
            requestStoragePermission()
        }

        ibSave = findViewById(R.id.ibSave)
        ibSave?.setOnClickListener {
            requestStoragePermission()
        }
    }

    private fun viewToBitmap(view : View) : Bitmap {
        val bitmap : Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background

        if (bgDrawable != null) {
            bgDrawable.draw(canvas) // draw background into the canvas
        } else {
            canvas.drawColor(Color.WHITE) // fill the background with white
        }

        view.draw(canvas) //draw the canvas on the view

        return bitmap
    }

    private fun requestStoragePermission() {
        // if we have already asked for permission, yet the user did not grant access, display a dialog
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showRationalDialog("Painter Requires Access to External Storage", "User files cannot be used because storage access is denied")
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showRationalDialog("Painter Requires Access to External Storage", "User files cannot be used because storage access is denied")
        }else {
            // request for permission
            externalStorageResultLauncher.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ))
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