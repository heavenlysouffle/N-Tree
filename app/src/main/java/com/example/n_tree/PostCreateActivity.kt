package com.example.n_tree

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class PostCreateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        title = "N-Tree"

        val nickname = intent.getStringExtra("nickname")
        val page = intent.getStringExtra("page")

        val textFieldPost = findViewById<EditText>(R.id.text_field_post)
        val errorTextView = findViewById<TextView>(R.id.post_create_error_text_view)

        val buttonRegister: Button = findViewById(R.id.post_create_btn)
        buttonRegister.setOnClickListener {
            val URL = "http://185.69.154.93/api/post"
            if (URL.isNotEmpty()) {
                val fetchData = OkHttpClient()
                val formBody = FormBody.Builder()
                    .add("content", textFieldPost.text.toString())
                    .add("tags[]", ":)")
                    .build()

                Log.i("TAG", textFieldPost.text.toString())

                val request = Request.Builder()
                    .url(URL)
                    .addHeader("Authorization", "Bearer " + getToken(applicationContext))
                    .post(formBody)
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

                                val intent = Intent(this@PostCreateActivity, PostCreateActivity::class.java)
                                intent.putExtra("error", errorCode + "\n" + errorMessage)
                                finish()
                                startActivity(intent)
                            } else {
                                val i = Intent(this@PostCreateActivity, PostsActivity::class.java)
                                i.putExtra("page", page)
                                i.putExtra("nickname", nickname)
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