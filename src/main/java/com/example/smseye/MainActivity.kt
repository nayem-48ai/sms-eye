package com.example.smseye

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private var isEditMode = false
    private val allowedSenders = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        // Root Layout (Drawer)
        drawerLayout = DrawerLayout(this)
        val contentBase = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F0F2F5")) 
        }

        // --- Custom ToolBar (Glassy) ---
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 50, 40, 50)
            setBackgroundColor(Color.WHITE)
            elevation = 10f
        }
        val menuBtn = Button(this).apply { text = "☰"; background = null; textSize = 20f }
        menuBtn.setOnClickListener { drawerLayout.openDrawer(Gravity.START) }
        toolbar.addView(menuBtn)
        toolbar.addView(TextView(this).apply { text = "SMS Eye Pro"; textSize = 20f; setPadding(30,0,0,0) })
        contentBase.addView(toolbar)

        // --- Form Container ---
        val scroll = ScrollView(this)
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 40, 50, 40) }
        scroll.addView(form)
        contentBase.addView(scroll)

        // Inputs
        val statusSwitch = Switch(this).apply { text = "Service Active"; isChecked = prefs.getBoolean("is_active", true); setPadding(0,20,0,20) }
        val modeSpinner = Spinner(this).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Telegram", "Firebase", "Both")) }
        
        val tokenInput = createInput("Telegram Token", prefs.getString("bot_token", ""))
        val chatInput = createInput("Chat ID", prefs.getString("chat_id", ""))
        
        // Firebase Script Box (Collapsible)
        val fbScriptInput = EditText(this).apply {
            hint = "Paste Firebase Script"
            val savedScript = prefs.getString("fb_script", "")
            setText(if (savedScript!!.length > 50) savedScript.substring(0, 40) + "..." else savedScript)
            setBackgroundResource(android.R.drawable.edit_text)
        }

        val editBtn = Button(this).apply { text = "Edit Configuration"; setBackgroundColor(Color.parseColor("#6200EE")); setTextColor(Color.WHITE) }

        // Logic for Edit/Save
        editBtn.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true
                editBtn.text = "Save & Sync"
                fbScriptInput.setText(prefs.getString("fb_script", "")) // Show full script
                toggleFields(true, tokenInput, chatInput, fbScriptInput, statusSwitch)
            } else {
                val script = fbScriptInput.text.toString()
                val projectId = "\"projectId\":\\s*\"(.*?)\"".toRegex().find(script)?.groups?.get(1)?.value ?: ""
                
                prefs.edit().apply {
                    putBoolean("is_active", statusSwitch.isChecked)
                    putString("bot_token", tokenInput.text.toString())
                    putString("chat_id", chatInput.text.toString())
                    putString("fb_script", script)
                    putString("fb_id", projectId)
                    putInt("mode_index", modeSpinner.selectedItemPosition)
                    apply()
                }
                isEditMode = false
                editBtn.text = "Edit Configuration"
                fbScriptInput.setText(if (script.length > 50) script.substring(0, 40) + "..." else script)
                toggleFields(false, tokenInput, chatInput, fbScriptInput, statusSwitch)
                Toast.makeText(this, "Synced with Firebase: $projectId", Toast.LENGTH_SHORT).show()
            }
        }

        form.addView(statusSwitch); form.addView(modeSpinner)
        form.addView(tokenInput); form.addView(chatInput); form.addView(fbScriptInput)
        form.addView(editBtn)

        // --- Side Nav View ---
        val navView = NavigationView(this)
        navView.setNavigationItemSelectedListener {
            if (it.itemId == 1) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply { data = Uri.parse("package:$packageName") }
                startActivity(intent)
            }
            drawerLayout.closeDrawers(); true
        }
        navView.menu.add(0, 1, 0, "Optimize Battery").setIcon(android.R.drawable.ic_lock_idle_low_battery)
        
        drawerLayout.addView(contentBase)
        drawerLayout.addView(navView, DrawerLayout.LayoutParams(700, -1).apply { gravity = Gravity.START })
        
        setContentView(drawerLayout)
        toggleFields(false, tokenInput, chatInput, fbScriptInput, statusSwitch)
        checkPermissions()
    }

    private fun createInput(hint: String, value: String?) = EditText(this).apply {
        setHint(hint); setText(value); setPadding(30, 30, 30, 30)
    }

    private fun toggleFields(enable: Boolean, vararg views: View) {
        views.forEach { it.isEnabled = enable }
    }

    private fun checkPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS), 101)
    }
}
