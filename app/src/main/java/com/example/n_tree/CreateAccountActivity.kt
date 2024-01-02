package com.example.n_tree

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class CreateAccountActivity : ComponentActivity() {
    private lateinit var fieldPhotoView: ImageView

    companion object {
        val IMAGE_REQUEST_CODE = 100
    }

    fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            fieldPhotoView.setImageURI(data?.data)
        }
    }

    fun imageViewToByteArray(imageView: ImageView): ByteArray {
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        title = "N-Tree"

        val createBtn = findViewById<Button>(R.id.create_account_btn)
        val textFieldNickname = findViewById<EditText>(R.id.create_account_text_field_nickname)
        val textFieldFirstName = findViewById<EditText>(R.id.create_account_text_field_first_name)
        val textFieldLastName = findViewById<EditText>(R.id.create_account_text_field_last_name)
        val textFieldDescription = findViewById<EditText>(R.id.create_account_text_field_description)
        val fieldPhotoBtn = findViewById<Button>(R.id.create_account_upload_photo_button)
        fieldPhotoView = findViewById<ImageView>(R.id.create_account_uploaded_photo_view)
        val errorTextView = findViewById<TextView>(R.id.create_account_error_text_view)

        fieldPhotoBtn.setOnClickListener {
            pickImageGallery()
        }

        createBtn.setOnClickListener {
            val URL = "http://185.69.154.93/api/account"
            if (URL.isNotEmpty()) {
                val byteArray = imageViewToByteArray(fieldPhotoView)
                val imageBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)

                val fetchData = OkHttpClient()
                val formBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("nickname", textFieldNickname.text.toString())
                    .addFormDataPart("first_name", textFieldFirstName.text.toString())
                    .addFormDataPart("last_name", textFieldLastName.text.toString())
                    .addFormDataPart("description", textFieldDescription.text.toString())
                    .addFormDataPart("photo", textFieldNickname.text.toString()+"acc_photo.jpg", imageBody)
                    .build()

                val request = Request.Builder()
                    .url(URL)
                    .post(formBody)
                    .addHeader("Authorization", "Bearer " + getToken(applicationContext))
                    .build()

                fetchData.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                val errorCode: String
                                val errorMessage: String

                                if (response.code >= 500) {
                                    errorCode = "Request failed with status code: ${response.code}"
                                    errorMessage = "Server Error"
                                } else {
                                    errorCode = "Request failed with status code: ${response.code}"
                                    errorMessage = response.body?.string().toString()
                                }
                                Log.e("TAG", errorCode)
                                Log.e("TAG", errorMessage)

                                val intent = Intent(this@CreateAccountActivity, CreateAccountActivity::class.java)
                                intent.putExtra("error", errorCode + "\n" + errorMessage)
                                finish()
                                startActivity(intent)
                            } else {
                                val i = Intent(this@CreateAccountActivity, MyAccountActivity::class.java)
                                startActivity(i)
                            }
                        }
                    }
                })
            } else {
                println("Url was empty")
            }
        }

        val error = intent.getStringExtra("error")
        if (error != null) {
            errorTextView.text = error
            errorTextView.postDelayed({
                errorTextView.visibility = View.GONE
            }, 5000)
        }
    }
}
