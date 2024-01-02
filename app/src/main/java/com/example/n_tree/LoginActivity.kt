package com.example.n_tree

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        title = "N-Tree"

        val textFieldEmail = findViewById<EditText>(R.id.text_field_email)
        val textFieldPassword = findViewById<EditText>(R.id.text_field_password)

        val buttonRegister: Button = findViewById(R.id.login_btn)
        buttonRegister.setOnClickListener {
            val URL = "http://185.69.154.93/api/auth/login"
            if (URL.isNotEmpty()) {
                val fetchData = OkHttpClient()
                val formBody = FormBody.Builder()
                    .add("email", textFieldEmail.text.toString())
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
                                val body = response.body?.string()
                                val jsonObject = JSONObject(body.toString())
                                val token = jsonObject.getString("token").toString()

                                val savedToken = setToken(applicationContext, token)

                                Log.i("TAG", savedToken)
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