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
import org.json.JSONObject
import java.io.IOException

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        title = "N-Tree"

        val textFieldEmail = findViewById<EditText>(R.id.text_field_email)
        val textFieldPassword = findViewById<EditText>(R.id.text_field_password)
        val errorTextView = findViewById<TextView>(R.id.login_error_text_view)

        val buttonLogin: Button = findViewById(R.id.login_btn)
        buttonLogin.background.alpha = 128
        val buttonRegister: Button = findViewById(R.id.login_to_register_btn)

        buttonRegister.setOnClickListener(
            View.OnClickListener {
                val i: Intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(i)
            }
        )

        buttonLogin.setOnClickListener {
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

                                val intent = Intent(this@LoginActivity, LoginActivity::class.java)
                                intent.putExtra("error", errorCode + "\n" + errorMessage)
                                finish()
                                startActivity(intent)
                            } else {
                                val body = response.body?.string()
                                val jsonObject = JSONObject(body.toString())
                                val token = jsonObject.getString("token").toString()

                                val savedToken = setToken(applicationContext, token)

                                Log.i("TAG", savedToken)
                                val intent = Intent(this@LoginActivity, MyAccountActivity::class.java)
                                startActivity(intent)
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