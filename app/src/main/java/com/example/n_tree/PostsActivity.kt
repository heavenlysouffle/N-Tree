package com.example.n_tree

import android.os.Bundle
import androidx.activity.ComponentActivity

class PostsActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        title = "N-Tree"
    }
}