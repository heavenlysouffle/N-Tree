package com.example.n_tree

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Log
import android.util.Xml
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.n_tree.model.Post
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter

class PostsActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts) //posts_layout
        title = "N-Tree"

        val nickname = intent.getStringExtra("nickname")
        val page: String = if (intent.getStringExtra("page") == "0" || intent.getStringExtra("page") == null) {
            "1"
        } else {
            intent.getStringExtra("page") ?: "1"
        }

        val URL = "http://185.69.154.93/api/post/$nickname?page=$page&per_page=5"
        if (URL.isNotEmpty()) {
            val fetchData = OkHttpClient()

            val request = Request.Builder()
                .url(URL)
                .get()
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
                            Log.i("TAG", jsonObject.toString(2))
                            val postsJsonArray = jsonObject.getJSONArray("data")

                            val posts = mutableListOf<Post>()

                            if (postsJsonArray.length() == 0) {
                                if (page != "1") {
                                    val i = Intent(this@PostsActivity, PostsActivity::class.java)
                                    val intPage: Int = page.toInt() - 1
                                    i.putExtra("page", intPage.toString())
                                    i.putExtra("nickname", nickname)
                                    startActivity(i)
                                }
                            }

                            for (i in 0 until postsJsonArray.length()) {
                                val linkJsonObject = postsJsonArray.getJSONObject(i)
                                val post = Post(
                                    linkJsonObject.getString("content"),
                                    linkJsonObject.getJSONArray("tags").toString()
                                )
                                posts.add(post)
                            }

                            val serializer = Xml.newSerializer()
                            val writer = StringWriter()
                            serializer.setOutput(writer)
                            serializer.startDocument("UTF-8", true)
                            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                            serializer.startTag("", "posts")

                            for (post in posts) {
                                serializer.startTag("", "post")
                                serializer.attribute("", "content", post.content)
                                serializer.attribute("", "tags", post.tags)
                                serializer.endTag("", "post")
                            }

                            serializer.endTag("", "posts")
                            serializer.endDocument()
                            val xmlString = writer.toString()

                            runOnUiThread {
                                val linearLayout = findViewById<LinearLayout>(R.id.posts_layout)
                                val factory = XmlPullParserFactory.newInstance()
                                val parser = factory.newPullParser()
                                parser.setInput(StringReader(xmlString))
                                var eventType = parser.eventType

                                while (eventType != XmlPullParser.END_DOCUMENT) {
                                    if (eventType == XmlPullParser.START_TAG && parser.name == "post") {
                                        val content = parser.getAttributeValue(null, "content")
                                        val tags = parser.getAttributeValue(null, "tags")
                                        val textView = TextView(this@PostsActivity)
                                        textView.text = "$content \n $tags"
                                        textView.movementMethod = LinkMovementMethod.getInstance()
                                        textView.gravity = Gravity.CENTER_HORIZONTAL
                                        linearLayout.addView(textView)
                                    }
                                    eventType = parser.next()
                                }

                                val buttonPrevPage: Button = findViewById(R.id.posts_previous_page)
                                buttonPrevPage.background.alpha = 128
                                buttonPrevPage.setOnClickListener(
                                    View.OnClickListener {
                                        val i = Intent(this@PostsActivity, PostsActivity::class.java)
                                        val intPage: Int = page.toInt() - 1
                                        i.putExtra("page", intPage.toString())
                                        i.putExtra("nickname", nickname)
                                        startActivity(i)
                                    }
                                )

                                val buttonNextPage: Button = findViewById(R.id.posts_next_page)
                                buttonNextPage.background.alpha = 128
                                buttonNextPage.setOnClickListener(
                                    View.OnClickListener {
                                        val i = Intent(this@PostsActivity, PostsActivity::class.java)
                                        val intPage: Int = page.toInt() + 1
                                        i.putExtra("page", intPage.toString())
                                        i.putExtra("nickname", nickname)
                                        startActivity(i)
                                    }
                                )
                            }
                        }
                    }
                }
            })
        } else {
            println("Url was empty")
        }
    }
}