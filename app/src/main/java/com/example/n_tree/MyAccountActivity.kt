package com.example.n_tree

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Log
import android.util.Xml
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.n_tree.model.Link
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
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors


class MyAccountActivity : ComponentActivity() {
    private val executor = Executors.newSingleThreadExecutor()

    @Throws(IOException::class)
    fun drawableFromUrl(url: String?): Drawable {
        var result: Drawable? = null
        val future = executor.submit<Drawable> {
            val x: Bitmap
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            val input = connection.inputStream
            x = BitmapFactory.decodeStream(input)
            BitmapDrawable(Resources.getSystem(), x)
        }
        try {
            result = future.get()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)
        title = "N-Tree"

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

                                if (errorMessage == "{\"errors\":[\"Account not found.\"]}") {
                                    val i = Intent(this@MyAccountActivity, CreateAccountActivity::class.java)
                                    startActivity(i)
                                }
                            }
                            Log.e("TAG", errorCode)
                            Log.e("TAG", errorMessage)
                        } else {
                            val body = response.body?.string()
                            val jsonObject = JSONObject(body.toString())
                            Log.e("TAG", jsonObject.toString(2))
                            val nickname = jsonObject.getString("nickname").toString()
                            val firstName = jsonObject.getString("first_name").toString()
                            val lastName = jsonObject.getString("last_name").toString()
                            val description = jsonObject.getString("description").toString()
                            val photoUrl = jsonObject.getString("photo").toString()

                            val linksJsonArray = jsonObject.getJSONArray("links")
                            val links = mutableListOf<Link>()
                            for (i in 0 until linksJsonArray.length()) {
                                val linkJsonObject = linksJsonArray.getJSONObject(i)
                                val link = Link(
                                    linkJsonObject.getString("network"),
                                    linkJsonObject.getString("url")
                                )
                                links.add(link)
                            }

                            val serializer = Xml.newSerializer()
                            val writer = StringWriter()
                            serializer.setOutput(writer)
                            serializer.startDocument("UTF-8", true)
                            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                            serializer.startTag("", "links")

                            for (link in links) {
                                serializer.startTag("", "link")
                                serializer.attribute("", "network", link.network)
                                serializer.attribute("", "url", link.url)
                                serializer.endTag("", "link")
                            }

                            serializer.endTag("", "links")
                            serializer.endDocument()

                            val xmlString = writer.toString()

                            runOnUiThread {
                                val photoView = findViewById<ImageView>(R.id.my_account_photo_view)
                                val drawable = drawableFromUrl(photoUrl)
                                val nicknameView = findViewById<TextView>(R.id.my_account_text_field_nickname)
                                val firstNameView = findViewById<TextView>(R.id.my_account_text_field_first_name)
                                val lastNameView = findViewById<TextView>(R.id.my_account_text_field_last_name)
                                val descriptionView = findViewById<TextView>(R.id.my_account_text_field_description)

                                photoView.setImageDrawable(drawable)
                                nicknameView.text = "Псевдонім: $nickname"
                                firstNameView.text = "Ім'я: $firstName"
                                lastNameView.text = "Прізвище: $lastName"
                                descriptionView.text = "Опис: $description"

                                val linearLayout = findViewById<LinearLayout>(R.id.my_account_links_layout)
                                val factory = XmlPullParserFactory.newInstance()
                                val parser = factory.newPullParser()
                                parser.setInput(StringReader(xmlString))
                                var eventType = parser.eventType
                                while (eventType != XmlPullParser.END_DOCUMENT) {
                                    if (eventType == XmlPullParser.START_TAG && parser.name == "link") {
                                        val network = parser.getAttributeValue(null, "network")
                                        val url = parser.getAttributeValue(null, "url")
                                        val textView = TextView(this@MyAccountActivity)
                                        val spannableString = SpannableString("$network: $url")
                                        val urlSpan = object : URLSpan(url) {
                                            override fun onClick(widget: View) {
                                                val intent = Intent(Intent.ACTION_VIEW)
                                                intent.data = Uri.parse(url)
                                                startActivity(intent)
                                            }
                                        }
                                        spannableString.setSpan(urlSpan, spannableString.indexOf(url), spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                        textView.text = spannableString
                                        textView.movementMethod = LinkMovementMethod.getInstance()
                                        textView.gravity = Gravity.CENTER_HORIZONTAL
                                        textView.setTextColor(AppCompatResources.getColorStateList(this@MyAccountActivity, R.color.green_dark))

                                        linearLayout.addView(textView)
                                    }
                                    eventType = parser.next()
                                }
                                val buttonCreateLink: Button = findViewById(R.id.account_create_link_btn)
                                buttonCreateLink.setOnClickListener(
                                    View.OnClickListener {
                                        val i: Intent = Intent(this@MyAccountActivity, LinkCreateActivity::class.java)
                                        startActivity(i)
                                    }
                                )

                                val buttonPosts: Button = findViewById(R.id.account_to_posts_btn)
                                buttonPosts.background.alpha = 128
                                buttonPosts.setOnClickListener(
                                    View.OnClickListener {
                                        val i: Intent = Intent(this@MyAccountActivity, PostsActivity::class.java)
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