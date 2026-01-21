package com.example.smseye

import android.Manifest
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val statusText = TextView(this).apply {
            text = "SMS Forwarder is Running"
            textSize = 20f
        }

        val btn = Button(this).apply {
            text = "Grant SMS Permission"
            setOnClickListener {
                ActivityCompat.requestPermissions(this@MainActivity, 
                    arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS), 101)
            }
        }

        layout.addView(statusText)
        layout.addView(btn)
        setContentView(layout)
    }
}
