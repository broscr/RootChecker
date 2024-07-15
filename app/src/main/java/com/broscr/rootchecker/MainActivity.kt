package com.broscr.rootchecker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textview)

        textView.text =
            buildString {
                append("Rooted: => ${RootChecker.isRooted()} \nEmulator => ${RootChecker.isEmulator()} ")
                append(
                    "\nProxy => ${RootChecker.isUsingProxy()} \nDebuggable => ${
                        RootChecker.isDebuggable(
                            this@MainActivity
                        )
                    }"
                )
            }
    }
}