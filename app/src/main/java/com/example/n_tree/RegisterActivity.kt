package com.example.n_tree

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        title = "N-Tree"

        val registerBtn = findViewById<Button>(R.id.register_btn)
        val textFieldEmail = findViewById<EditText>(R.id.register_text_field_email)
        val textFieldPassword = findViewById<EditText>(R.id.register_text_field_password)
        val textFieldNickname = findViewById<EditText>(R.id.register_text_field_nickname)

        registerBtn.setOnClickListener {
            val URL = "http://185.69.154.93/api/auth/register"
            if (URL.isNotEmpty()) {
                val fetchData = OkHttpClient()
                val formBody = FormBody.Builder()
                    .add("email", textFieldEmail.text.toString())
                    .add("nickname", textFieldNickname.text.toString())
                    .add("password", textFieldPassword.text.toString())
                    .build()

                val request = Request.Builder()
                    .url(URL)
                    .post(formBody)
                    .build()

                fetchData.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                Log.e("TAG", "Request failed with status code: ${response.code}")
                                Log.e("TAG", "Response body: ${response.body?.string()}")
                            } else {
                                val i = Intent(this@RegisterActivity, LoginActivity::class.java)
                                startActivity(i)
                            }
                        }
                    }
                })
            } else {
                println("Url was empty")
            }
        }
    }
}