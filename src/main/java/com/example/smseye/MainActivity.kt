package com.example.smseye

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var senderListLayout: LinearLayout
    private val allowedSenders = mutableListOf<String>()
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Root Layout with Drawer
        drawerLayout = DrawerLayout(this)
        val mainContent = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40,40,40,40) }
        val navView = NavigationView(this)
        drawerLayout.addView(mainContent)
        drawerLayout.addView(navView, DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.MATCH_PARENT).apply { gravity = android.view.Gravity.START })

        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        // Side Nav Header & Menu
        navView.setNavigationItemSelectedListener { item ->
            when(item.itemId) {
                1 -> startBatteryOptimization()
                2 -> Toast.makeText(this, "SMS Eye v1.0 - Secure Forwarder", Toast.LENGTH_LONG).show()
            }
            drawerLayout.closeDrawers(); true
        }
        navView.menu.add(0, 1, 0, "Battery Optimization").setIcon(android.R.drawable.ic_lock_idle_low_battery)
        navView.menu.add(0, 2, 0, "About App").setIcon(android.R.drawable.ic_dialog_info)

        // UI Elements
        val statusSwitch = Switch(this).apply { text = "Service Status"; isChecked = prefs.getBoolean("is_active", true) }
        val modeSpinner = Spinner(this).apply { adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Telegram", "Firebase", "Both")) }
        
        val tokenInput = EditText(this).apply { hint = "Telegram Bot Token"; setText(prefs.getString("bot_token", "")) }
        val chatInput = EditText(this).apply { hint = "Telegram Chat ID"; setText(prefs.getString("chat_id", "")) }
        val fbScriptInput = EditText(this).apply { 
            hint = "Paste Firebase Web Script Here"
            minLines = 3
            setText(prefs.getString("fb_script", ""))
        }

        val editSaveBtn = Button(this).apply { text = "Edit Configuration" }
        
        // Initial State (Disabled)
        fun setInputState(enabled: Boolean) {
            tokenInput.isEnabled = enabled; chatInput.isEnabled = enabled; fbScriptInput.isEnabled = enabled
            modeSpinner.isEnabled = enabled; statusSwitch.isEnabled = enabled
        }
        setInputState(false)

        editSaveBtn.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true
                editSaveBtn.text = "Save Configuration"
                setInputState(true)
            } else {
                val script = fbScriptInput.text.toString()
                val projectId = extractValue(script, "projectId")
                
                prefs.edit().apply {
                    putBoolean("is_active", statusSwitch.isChecked)
                    putString("bot_token", tokenInput.text.toString())
                    putString("chat_id", chatInput.text.toString())
                    putString("fb_script", script)
                    putString("fb_id", projectId)
                    putInt("mode_index", modeSpinner.selectedItemPosition)
                    putString("allowed_senders", allowedSenders.joinToString(","))
                    apply()
                }
                isEditMode = false
                editSaveBtn.text = "Edit Configuration"
                setInputState(false)
                Toast.makeText(this, "Configuration Saved!", Toast.LENGTH_SHORT).show()
            }
        }

        // Add Views
        mainContent.apply {
            addView(TextView(this@MainActivity).apply { text = "SMS EYE PRO"; textSize = 24f; setTextColor(Color.BLUE) })
            addView(statusSwitch); addView(modeSpinner); addView(tokenInput)
            addView(chatInput); addView(fbScriptInput); addView(editSaveBtn)
        }

        setContentView(drawerLayout)
        checkPermissions()
    }

    private fun extractValue(script: String, key: String): String {
        val pattern = "\"$key\":\\s*\"(.*?)\"".toRegex()
        return pattern.find(script)?.groups?.get(1)?.value ?: ""
    }

    private fun startBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply { data = Uri.parse("package:$packageName") }
            startActivity(intent)
        }
    }

    private fun checkPermissions() {
        val perms = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        ActivityCompat.requestPermissions(this, perms, 101)
    }
}
