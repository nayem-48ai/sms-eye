package com.example.smseye

import android.Manifest
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
            text = "SMS Forwarder Active"
            textSize = 20f
            setPadding(0, 0, 0, 50)
        }

        // পারমিশন বাটন
        val permBtn = Button(this).apply {
            text = "1. Grant SMS Permission"
            setOnClickListener {
                ActivityCompat.requestPermissions(this@MainActivity, 
                    arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS), 101)
            }
        }

        // টেস্ট বাটন - এটি সরাসরি টেলিগ্রামে মেসেজ পাঠাবে
        val testBtn = Button(this).apply {
            text = "2. Test Telegram Bot"
            setOnClickListener {
                TelegramService.sendSms("Test App", "This is a test message from your app!")
                Toast.makeText(this@MainActivity, "Test signal sent!", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(statusText)
        layout.addView(permBtn)
        layout.addView(testBtn)
        setContentView(layout)
    }
}
