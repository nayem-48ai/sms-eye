package com.example.smseye

import android.Manifest
import android.annotation.SuppressLint
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
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        scroll.addView(layout)

        // Title
        layout.addView(TextView(this).apply { text = "SMS Eye Config"; textSize = 24f; setPadding(0, 0, 0, 40) })

        // Forward Mode Spinner
        layout.addView(TextView(this).apply { text = "Forward Mode:" })
        val modeSpinner = Spinner(this)
        val modes = arrayOf("Telegram", "Firebase", "Both")
        modeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modes)
        layout.addView(modeSpinner)

        // Telegram Inputs Group
        val tgLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val tokenInput = EditText(this).apply { hint = "Telegram Bot Token"; setText(prefs.getString("bot_token", "")) }
        val chatInput = EditText(this).apply { hint = "Telegram Chat ID"; setText(prefs.getString("chat_id", "")) }
        tgLayout.addView(tokenInput)
        tgLayout.addView(chatInput)
        layout.addView(tgLayout)

        // Firebase Inputs Group
        val fbLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE }
        val fbUrlInput = EditText(this).apply { hint = "Firebase DB URL (e.g. project-id.firebaseio.com)"; setText(prefs.getString("fb_url", "")) }
        fbLayout.addView(fbUrlInput)
        layout.addView(fbLayout)

        // Toggle visibility based on mode
        modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                tgLayout.visibility = if (pos == 1) View.GONE else View.VISIBLE
                fbLayout.visibility = if (pos == 0) View.GONE else View.VISIBLE
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        modeSpinner.setSelection(modes.indexOf(prefs.getString("forward_mode", "Telegram")))

        // Filter Logic
        val filterSwitch = Switch(this).apply { text = "Specific Senders Only"; isChecked = prefs.getBoolean("is_filter_on", false) }
        val senderInput = EditText(this).apply { hint = "Enter Senders (comma separated)"; setText(prefs.getString("allowed_senders", "")) }
        layout.addView(filterSwitch); layout.addView(senderInput)

        // Test Button
        val testBtn = Button(this).apply {
            text = "Send Test Message"
            setOnClickListener {
                saveData(modeSpinner.selectedItem.toString(), tokenInput.text.toString(), chatInput.text.toString(), fbUrlInput.text.toString(), filterSwitch.isChecked, senderInput.text.toString())
                TelegramService.forwardSms(this@MainActivity, "Test", "Success! Connection Established.")
                Toast.makeText(this@MainActivity, "Testing...", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(testBtn)

        // Background Permission Button
        val batteryBtn = Button(this).apply {
            text = "Allow Background Running"
            setOnClickListener { requestIgnoreBatteryOptimizations() }
        }
        layout.addView(batteryBtn)

        setContentView(scroll)
        checkPermissions()
    }

    private fun saveData(mode: String, token: String, cid: String, fUrl: String, fOn: Boolean, senders: String) {
        prefs.edit().apply {
            putString("forward_mode", mode)
            putString("bot_token", token)
            putString("chat_id", cid)
            putString("fb_url", fUrl)
            putBoolean("is_filter_on", fOn)
            putString("allowed_senders", senders)
            apply()
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) ActivityCompat.requestPermissions(this, missing.toTypedArray(), 101)
    }

    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
}
