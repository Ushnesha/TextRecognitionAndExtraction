package com.ushnesha.daripa.textrecognitionandextraction

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.content_text_recognition.*

class MainActivity : AppCompatActivity() {

    private val textRecognitionModels = ArrayList<TextRecognitionModel>()
    private lateinit var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bottomSheetBehavior = BottomSheetBehavior.from<LinearLayout>(bottom_sheet)

        bottom_sheet.setOnClickListener{
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)
        }

        bottom_sheet_recycler_view.layoutManager = LinearLayoutManager(this)

        bottom_sheet_recycler_view.adapter = TextRecognitionAdapter(this, textRecognitionModels)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if(resultCode == Activity.RESULT_OK){
                val imageUri = result.uri
                analyzeImage(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                showToast("There was some error : ${result.error.message}")
            }
        }
    }

    private fun analyzeImage(image : Bitmap?){
        if(image == null){
            showToast("There was some error")
            return
        }

        text_recognition_image_view.setImageBitmap(null)
        textRecognitionModels.clear()
        bottom_sheet_recycler_view.adapter?.notifyDataSetChanged()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        showProgress()

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val textRecogizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        textRecogizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {
                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)
                recognizeText(it, mutableImage)
                text_recognition_image_view.setImageBitmap(mutableImage)
                hideProgress()
                bottom_sheet_recycler_view.adapter?.notifyDataSetChanged()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            .addOnFailureListener{
                showToast("Analyze imaging failed")
                hideProgress()
            }
    }

    private fun recognizeText(visionText: FirebaseVisionText?, mutableImage: Bitmap?) {

        if(visionText == null || mutableImage == null){
            showToast("Error in recognizing text")
            return
        }

        val canvas = Canvas(mutableImage)
        val rectPaint = Paint()
        rectPaint.color = Color.RED
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 4F
        val textPaint = Paint()
        textPaint.color = Color.RED
        textPaint.textSize = 40F

        var index = 0
        for( block in visionText.textBlocks){
            for( line in block.lines){
                canvas.drawRect(line.boundingBox, rectPaint)
                canvas.drawText(index.toString(), line.cornerPoints!![2].x.toFloat(), line.cornerPoints!![2].y.toFloat(), textPaint)
                textRecognitionModels.add(TextRecognitionModel(index++, line.text))
            }
        }
    }

    private fun showProgress() {
        bottom_sheet_button_image.visibility = View.GONE
        bottom_sheet_button_progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        bottom_sheet_button_image.visibility = View.VISIBLE
        bottom_sheet_button_progress.visibility = View.GONE
    }

    private fun showToast(msg : String?){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
