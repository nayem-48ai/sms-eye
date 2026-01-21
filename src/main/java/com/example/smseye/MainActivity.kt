package com.example.smseye

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
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

        // UI Header
        layout.addView(TextView(this).apply { text = "SMS Forwarder Pro"; textSize = 26f; setTextColor(android.graphics.Color.BLUE); setPadding(0,0,0,40) })

        // Inputs
        val tokenInput = EditText(this).apply { hint = "Telegram Bot Token"; setText(prefs.getString("bot_token", "")) }
        val chatInput = EditText(this).apply { hint = "Telegram Chat ID"; setText(prefs.getString("chat_id", "")) }
        val firebaseInput = EditText(this).apply { hint = "Firebase Project ID (e.g. autopay-c8eea)"; setText(prefs.getString("fb_id", "autopay-c8eea")) }
        
        // Mode Selector
        val modeText = TextView(this).apply { text = "Select Mode:"; setPadding(0, 20, 0, 10) }
        val modeSpinner = Spinner(this)
        val modes = arrayOf("Telegram Only", "Firebase Only", "Both")
        modeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modes)
        modeSpinner.setSelection(prefs.getInt("mode_index", 0))

        // Filter UI
        val filterSwitch = Switch(this).apply { text = "Enable Sender Filter"; isChecked = prefs.getBoolean("is_filter_on", false) }
        val senderInput = EditText(this).apply { hint = "Allowed Senders (e.g. BKash, MyGP)"; setText(prefs.getString("allowed_senders", "")) }

        // Buttons
        val saveBtn = Button(this).apply { text = "Save Settings"; setBackgroundColor(android.graphics.Color.GREEN) }
        val testBtn = Button(this).apply { text = "Test Telegram & Firebase"; setBackgroundColor(android.graphics.Color.LTGRAY) }
        val optimizeBtn = Button(this).apply { text = "Disable Battery Optimization"; setBackgroundColor(android.graphics.Color.YELLOW) }

        // Logic for Save
        saveBtn.setOnClickListener {
            prefs.edit().apply {
                putString("bot_token", tokenInput.text.toString())
                putString("chat_id", chatInput.text.toString())
                putString("fb_id", firebaseInput.text.toString())
                putInt("mode_index", modeSpinner.selectedItemPosition)
                putBoolean("is_filter_on", filterSwitch.isChecked)
                putString("allowed_senders", senderInput.text.toString())
                apply()
            }
            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show()
        }

        // Logic for Test
        testBtn.setOnClickListener {
            TelegramService.forwardSms(this, "Test-Sender", "This is a clean test message.")
            Toast.makeText(this, "Test Signal Sent!", Toast.LENGTH_SHORT).show()
        }

        // Battery Optimization Logic
        optimizeBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }

        // Add views to layout
        layout.apply {
            addView(tokenInput); addView(chatInput); addView(firebaseInput); addView(modeText)
            addView(modeSpinner); addView(filterSwitch); addView(senderInput)
            addView(saveBtn); addView(testBtn); addView(optimizeBtn)
        }

        setContentView(scroll)
        checkPermissions()
    }

    private fun checkPermissions() {
        val perms = mutableListOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.INTERNET)
        val missing = perms.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) ActivityCompat.requestPermissions(this, missing.toTypedArray(), 101)
    }
}
