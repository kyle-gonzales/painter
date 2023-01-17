package com.example.painter

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import java.io.*

class MainActivity : AppCompatActivity() {

    private var drawingView : DrawingView? = null
    private var ibCurrentBrushColor : ImageButton? = null
    private var ibUndo : ImageButton? = null
    private var tvCustomColorBrush : TextView? = null
    private var ibSave : ImageButton? = null
    private var ibShare : ImageButton? = null
    private var myProgressDialog : Dialog? = null
    private var currentCustomColor : Int? = null

    private val setCurrentCustomColor = {
        if(currentCustomColor != null)
            currentCustomColor
        else
            Color.RED
    }

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
            val isGranted = it.value
            val permissionName = it.key

            if (isGranted) {
                if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
//                    Toast.makeText(this, "access granted", Toast.LENGTH_SHORT).show()
                    val pickIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ) // run intent to go to other application
                    openGalleryLauncher.launch(pickIntent)
                } else {
                    Toast.makeText(
                        this,
                        "access granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "External Storage Access is Denied", Toast.LENGTH_SHORT).show()
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

            if (isStorageAllowed()) {
//                Toast.makeText(this, "saving...", Toast.LENGTH_SHORT).show()
                showProgressDialog()
                lifecycleScope.launch {
                    val flDrawingView : FrameLayout = findViewById(R.id.flDrawingViewContainer)
                    val bitmap = viewToBitmap(flDrawingView)
                    saveBitmapFile(bitmap)
                }
            }
        }

        ibShare = findViewById(R.id.ibShare)
        ibShare?.setOnClickListener {
            if (isStorageAllowed()) {
                showProgressDialog()
                lifecycleScope.launch {
                    val flDrawingView : FrameLayout = findViewById(R.id.flDrawingViewContainer)
                    val bitmap = viewToBitmap(flDrawingView)
                    saveBitmapFileEmulator(bitmap)
                }
            }
        }
    }
    // the view contains the canvas, which contains the paths (brush strokes). the image is the background of the view. the bitmap is an image that contains the background image of the view, and the brush strokes of the canvas
    private fun viewToBitmap(view : View) : Bitmap {
        val bitmap : Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background

        if (bgDrawable != null) {
            bgDrawable.draw(canvas) // draw the view's background onto the canvas
        } else {
            canvas.drawColor(Color.WHITE) // fill the view's background with white
        }
        view.draw(canvas) //draw the canvas on the view; essentially, it puts all the elements on to the view, which is converted into a bitmap
        return bitmap
    }
    // uses MediaStore - saves file as png to InternalStorage/DCIM/Painter
    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String{
        var result = ""
        withContext(Dispatchers.IO){
            if (mBitmap != null) {
                try{
                    val name = "Painting" + System.currentTimeMillis() / 1000 + ".png"
                    val relativeLocation = Environment.DIRECTORY_DCIM + "/Painter"
//                    val absoluteLocation = this@MainActivity.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath.toString() + "/Painter"
//                    Log.d("Relative Path", relativeLocation)
                    val contentValues  = ContentValues().apply {
                        put(MediaStore.Images.ImageColumns.DISPLAY_NAME, name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")

                        // without this part causes "Failed to create new MediaStore record" exception to be invoked (uri is null below)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.ImageColumns.RELATIVE_PATH, relativeLocation)
                        }
                    }
                    val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    var stream: OutputStream? = null
                    var uri: Uri? = null

                    try {
                        uri = contentResolver.insert(contentUri, contentValues)
                        if (uri == null){
                            throw IOException("Failed to create new MediaStore record.")
                        }
                        stream = contentResolver.openOutputStream(uri)
                        if (stream == null){
                            throw IOException("Failed to get output stream.")
                        }
                        if (!mBitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)) {
                            throw IOException("Failed to save bitmap.")
                        }
                        result = "$relativeLocation/$name"
                    } catch(e: IOException) {
                        if (uri != null) {
                            contentResolver.delete(uri, null, null)
                        }
                        throw IOException(e)
                    } finally {
                        stream?.close()
                    }
                    runOnUiThread{
                        cancelProgressDialog()
                        if(result.isNotEmpty()){
                            Toast.makeText(this@MainActivity,
                                "File saved successfully: $relativeLocation/$name", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(this@MainActivity,
                                "Something went wrong. Couldn't save file...", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch(e:Exception){
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }
/* ALTERNATIVE METHOD: Files are saved to emulator on android device */
    private suspend fun saveBitmapFileEmulator (bitmap : Bitmap?) : String {
        var result = ""
        withContext(Dispatchers.IO) {
            if (bitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val file = File(externalCacheDir?.absoluteFile.toString() + File.separator + "Painter" + System.currentTimeMillis() / 1000 + ".png")
                    val fileOutput = FileOutputStream(file)
                    fileOutput.write(bytes.toByteArray()) // file is saved
                    fileOutput.close()

                    result = file.absolutePath

                    runOnUiThread {
                        if (result.isNotEmpty()) {
                            cancelProgressDialog()
                            Toast.makeText(this@MainActivity, "preparing to share...", Toast.LENGTH_SHORT).show()
                            shareFile(FileProvider.getUriForFile(baseContext, "com.example.painter.fileprovider", file)) // only share the image if the file has been successfully saved
                        } else {
                            Toast.makeText(this@MainActivity, "Failed to share file", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e : Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    private fun isStorageAllowed() : Boolean{
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission(from : String = "SET_BG") {
        // if we have already asked for permission, yet the user did not grant access, display a dialog
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showRationalDialog("Painter Requires Access to External Storage", "User files cannot be used because storage access is denied")
        } else {
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

        val colorPickerDialogue = AmbilWarnaDialog(this, setCurrentCustomColor()!!, true,
        object : OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?){
            }
            override fun onOk(dialog : AmbilWarnaDialog?, color: Int){
                drawingView!!.setBrushColor("#${String.format("%x", color)}")
                currentCustomColor = color
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

    private fun showProgressDialog() {
        myProgressDialog = Dialog(this)
        myProgressDialog?.setContentView(R.layout.progress_dialog)
        myProgressDialog?.show()
    }
    private fun cancelProgressDialog() {
        if (myProgressDialog != null) {
            myProgressDialog?.dismiss()
            myProgressDialog = null
        }
    }

    private fun brushColorClicked(view: View?) {
        if (view is TextView) { // preset-to-custom
            ibCurrentBrushColor!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_default)) // reset the current brush color to default
            openColorPickerDialogue()

            ibCurrentBrushColor = null
            return
        }

        if (view != ibCurrentBrushColor) { // preset-to-preset | custom-to-preset
            val ibBrushColor = view as ImageButton
            val colorTag = ibBrushColor.tag.toString()
            drawingView!!.setBrushColor(colorTag)

            ibBrushColor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))

            /*
            safe call operator (?) is used here instead of null assertion operator (!!) because preset-to-custom color conversion sets ibCurrentBrushColor to null.
            Thus, during custom-to-preset color conversion, the statement is ignored.

            Using null assertion would lead to null pointer exception.
             */
            ibCurrentBrushColor?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_default))
            ibCurrentBrushColor = ibBrushColor // set to current
            tvCustomColorBrush?.setBackgroundColor(setCurrentCustomColor()!!)

        }
    }
    private fun shareFile(uri : Uri) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND // opens snack  bar that allows us to send items
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri) // adds the path to file to the intent
        shareIntent.type = "image/png"

        startActivity(Intent.createChooser(shareIntent, "Share Painting"))
    }
}
    // @param : path to file
//    private fun shareFile(result : String) {
//        /*MediaScannerConnection provides a way for applications to pass a
//        newly created or downloaded media file to the media scanner service.
//        The media scanner service will read metadata from the file and add
//        the file to the media content provider.
//        The MediaScannerConnectionClient provides an interface for the
//        media scanner service to return the Uri for a newly scanned file
//        to the client of the MediaScannerConnection class.*/
//        MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null) {
//            path, uri ->
//            val shareIntent = Intent()
//            shareIntent.action = Intent.ACTION_SEND // opens snack  bar that allows us to send items
//            shareIntent.putExtra(Intent.EXTRA_STREAM, uri) // adds the path to file to the intent
//
//            shareIntent.type = "image/png"
//
//            startActivity(Intent.createChooser(shareIntent, "Share Painting"))
//        }
//    }
