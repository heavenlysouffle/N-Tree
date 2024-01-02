package com.example.n_tree

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MyAccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)
        title = "N-Tree"

        val textField = findViewById<TextView>(R.id.my_account_text_field)

        val URL = "http://185.69.154.93/api/my-account"
        if (URL.isNotEmpty()) {
            val fetchData = OkHttpClient()
            val request = Request.Builder()
                .url(URL)
                .get()
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
                        } else {
                            val body = response.body?.string()
                            val jsonObject = JSONObject(body.toString())
                            val nickname = jsonObject.getString("nickname").toString()
                            val firstName = jsonObject.getString("first_name").toString()
                            val lastName = jsonObject.getString("last_name").toString()
                            val description = jsonObject.getString("description").toString()
                            val photoUrl = jsonObject.getString("photo").toString()
                        // todo: create Link property and loop array<Link>
                        // val nickname = jsonObject.getString("links")
                        }
                    }
                }
            })
        } else {
            println("Url was empty")
        }
    }
}