package com.example.smseye

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
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
            setPadding(40, 40, 40, 40)
        }
        scroll.addView(layout)

        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        // UI Elements
        val tokenInput = EditText(this).apply { hint = "Telegram Bot Token"; setText(prefs.getString("bot_token", "")) }
        val chatInput = EditText(this).apply { hint = "Telegram Chat ID"; setText(prefs.getString("chat_id", "")) }
        
        val modeSpinner = Spinner(this)
        modeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Telegram", "Firebase", "Both"))

        val filterSwitch = Switch(this).apply { 
            text = "Enable Sender Filter"
            isChecked = prefs.getBoolean("is_filter_on", false)
        }
        
        val senderInput = EditText(this).apply { 
            hint = "Allowed Senders (e.g. Bkash, MyGP, 017...)"
            setText(prefs.getString("allowed_senders", ""))
        }

        val saveBtn = Button(this).apply {
            text = "Save Settings"
            setOnClickListener {
                prefs.edit().apply {
                    putString("bot_token", tokenInput.text.toString())
                    putString("chat_id", chatInput.text.toString())
                    putString("forward_mode", modeSpinner.selectedItem.toString())
                    putBoolean("is_filter_on", filterSwitch.isChecked)
                    putString("allowed_senders", senderInput.text.toString())
                    apply()
                }
                Toast.makeText(this@MainActivity, "Settings Saved!", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(TextView(this).apply { text = "Configuration Settings"; textSize = 22f })
        layout.addView(tokenInput); layout.addView(chatInput); layout.addView(modeSpinner)
        layout.addView(filterSwitch); layout.addView(senderInput); layout.addView(saveBtn)

        setContentView(scroll)
        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 101)
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions() // অ্যাপে ফিরে আসলে আবার পারমিশন চেক করবে
    }
}
