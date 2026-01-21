package com.example.smseye

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 60, 50, 60)
        }
        scroll.addView(layout)

        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        val title = TextView(this).apply {
            text = "SMS Forwarder Pro"
            textSize = 24f
            setTextColor(Color.parseColor("#6200EE"))
            setPadding(0, 0, 0, 40)
        }

        val tokenInput = EditText(this).apply { hint = "Telegram Bot Token"; setText(prefs.getString("bot_token", "")) }
        val chatInput = EditText(this).apply { hint = "Telegram Chat ID"; setText(prefs.getString("chat_id", "")) }
        val firebaseInput = EditText(this).apply { hint = "Firebase Project ID"; setText(prefs.getString("fb_id", "autopay-c8eea")) }
        
        val modeSpinner = Spinner(this)
        modeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Telegram Only", "Firebase Only", "Both"))
        modeSpinner.setSelection(prefs.getInt("mode_index", 0))

        val filterSwitch = Switch(this).apply { 
            text = "Enable Sender Filter"
            isChecked = prefs.getBoolean("is_filter_on", false)
            setPadding(0, 30, 0, 10)
        }
        
        val senderInput = EditText(this).apply { 
            hint = "Allowed Senders (e.g. Bkash, 017...)"
            setText(prefs.getString("allowed_senders", ""))
        }

        val saveBtn = Button(this).apply {
            text = "Save Settings"
            setOnClickListener {
                prefs.edit().apply {
                    putString("bot_token", tokenInput.text.toString())
                    putString("chat_id", chatInput.text.toString())
                    putString("fb_id", firebaseInput.text.toString())
                    putInt("mode_index", modeSpinner.selectedItemPosition)
                    putBoolean("is_filter_on", filterSwitch.isChecked)
                    putString("allowed_senders", senderInput.text.toString())
                    apply()
                }
                Toast.makeText(this@MainActivity, "Settings Saved!", Toast.LENGTH_SHORT).show()
            }
        }

        val testBtn = Button(this).apply {
            text = "Test Telegram & Firebase"
            setOnClickListener {
                TelegramService.forwardSms(this@MainActivity, "Test-Sender", "Hello! This is a test.")
                Toast.makeText(this@MainActivity, "Test Sent", Toast.LENGTH_SHORT).show()
            }
        }

        val batteryBtn = Button(this).apply {
            text = "Disable Battery Optimization"
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            }
        }

        layout.addView(title)
        layout.addView(tokenInput); layout.addView(chatInput); layout.addView(firebaseInput)
        layout.addView(TextView(this).apply { text = "Select Forward Mode:"; setPadding(0, 20, 0, 5) })
        layout.addView(modeSpinner); layout.addView(filterSwitch); layout.addView(senderInput)
        layout.addView(saveBtn); layout.addView(testBtn); layout.addView(batteryBtn)

        setContentView(scroll)
        checkPermissions()
    }

    private fun checkPermissions() {
        val perms = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        val missing = perms.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) ActivityCompat.requestPermissions(this, missing.toTypedArray(), 101)
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }
}
