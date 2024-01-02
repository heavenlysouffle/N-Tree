package com.example.n_tree

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.ComponentActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LinkCreateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_create)
        title = "N-Tree"

        val spinner = findViewById<Spinner>(R.id.spinner)

        ArrayAdapter.createFromResource(
            this,
            R.array.social_media_platforms,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        val textFieldLink = findViewById<EditText>(R.id.text_field_link)

        val buttonRegister: Button = findViewById(R.id.login_btn)
        buttonRegister.setOnClickListener {
            val URL = "http://185.69.154.93/api/link"
            if (URL.isNotEmpty()) {
                val fetchData = OkHttpClient()
                val formBody = FormBody.Builder()
                    .add("network", spinner.selectedItem.toString())
                    .add("url", textFieldLink.text.toString())
                    .build()

                Log.i("TAG", spinner.selectedItem.toString())
                Log.i("TAG", textFieldLink.text.toString())

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
                                Log.e("TAG", "Request failed with status code: ${response.code}")
                                Log.e("TAG", "Response body: ${response.body?.string()}")
                            } else {
                                val body = response.body?.string()
                                val jsonObject = JSONObject(body.toString())
                                val id = jsonObject.getString("id")

                                Log.i("TAG", id)
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