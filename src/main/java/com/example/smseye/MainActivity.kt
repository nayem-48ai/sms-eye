package com.example.smseye

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var senderListLayout: LinearLayout
    private val allowedSenders = mutableListOf<String>()
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        drawerLayout = DrawerLayout(this)
        val contentBase = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F7FA")) 
        }

        // --- Toolbar ---
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 50, 40, 50)
            setBackgroundColor(Color.WHITE)
            elevation = 8f
        }
        val menuBtn = Button(this).apply { text = "☰"; background = null; textSize = 22f }
        menuBtn.setOnClickListener { drawerLayout.openDrawer(Gravity.START) }
        toolbar.addView(menuBtn)
        toolbar.addView(TextView(this).apply { text = "SMS EYE PRO"; textSize = 20f; setPadding(30,0,0,0) })
        contentBase.addView(toolbar)

        val scroll = ScrollView(this)
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 40, 50, 40) }
        scroll.addView(form)
        contentBase.addView(scroll)

        // Inputs
        val statusSwitch = Switch(this).apply { text = "Forwarding Service"; isChecked = prefs.getBoolean("is_active", true); setPadding(0,20,0,40) }
        val modeSpinner = Spinner(this).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Telegram", "Firestore", "Both")) }
        modeSpinner.setSelection(prefs.getInt("mode_index", 0))

        val tokenInput = createInput("Telegram Bot Token", prefs.getString("bot_token", ""))
        val chatInput = createInput("Telegram Chat ID", prefs.getString("chat_id", ""))
        
        val fbScriptInput = EditText(this).apply {
            hint = "Paste Firebase Script Here"
            val savedScript = prefs.getString("fb_script", "") ?: ""
            setText(if (!isEditMode && savedScript.length > 40) savedScript.take(35) + "..." else savedScript)
            setBackgroundResource(android.R.drawable.edit_text)
        }

        // --- Sender Filter ---
        val filterTitle = TextView(this).apply { text = "Sender Filter (Whitelist)"; setPadding(0, 40, 0, 10); textSize = 16f }
        senderListLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val addRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val newSenderIn = EditText(this).apply { hint = "e.g. Bkash"; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        val addBtn = Button(this).apply { text = "+" }
        
        addBtn.setOnClickListener {
            val name = newSenderIn.text.toString().trim()
            if (name.isNotEmpty()) { addSenderTag(name); newSenderIn.text.clear() }
        }
        addRow.addView(newSenderIn); addRow.addView(addBtn)

        val editBtn = Button(this).apply { 
            text = "Edit Configuration"
            setBackgroundColor(Color.parseColor("#6200EE"))
            setTextColor(Color.WHITE)
        }

        editBtn.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true
                editBtn.text = "Save Configuration"
                fbScriptInput.setText(prefs.getString("fb_script", ""))
                toggleFields(true, tokenInput, chatInput, fbScriptInput, statusSwitch, addBtn, newSenderIn)
            } else {
                val script = fbScriptInput.text.toString()
                // Regex to find Project ID and API Key from script
                val projectId = "\"projectId\":\\s*\"(.*?)\"".toRegex().find(script)?.groups?.get(1)?.value ?: ""
                val apiKey = "\"apiKey\":\\s*\"(.*?)\"".toRegex().find(script)?.groups?.get(1)?.value ?: ""
                
                prefs.edit().apply {
                    putBoolean("is_active", statusSwitch.isChecked)
                    putString("bot_token", tokenInput.text.toString())
                    putString("chat_id", chatInput.text.toString())
                    putString("fb_script", script)
                    putString("fb_id", projectId)
                    putString("fb_key", apiKey)
                    putInt("mode_index", modeSpinner.selectedItemPosition)
                    putString("allowed_senders", allowedSenders.joinToString(","))
                    apply()
                }
                isEditMode = false
                editBtn.text = "Edit Configuration"
                fbScriptInput.setText(if (script.length > 40) script.take(35) + "..." else script)
                toggleFields(false, tokenInput, chatInput, fbScriptInput, statusSwitch, addBtn, newSenderIn)
                Toast.makeText(this, "Configuration Saved!", Toast.LENGTH_SHORT).show()
            }
        }

        form.addView(statusSwitch); form.addView(modeSpinner)
        form.addView(tokenInput); form.addView(chatInput); form.addView(fbScriptInput)
        form.addView(filterTitle); form.addView(addRow); form.addView(senderListLayout)
        form.addView(editBtn)

        // Side Nav Setup
        val navView = NavigationView(this)
        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                1 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply { data = Uri.parse("package:$packageName") }
                        startActivity(intent)
                    }
                }
                2 -> showAboutDialog()
            }
            drawerLayout.closeDrawers(); true
        }
        navView.menu.add(0, 1, 0, "Optimize Battery").setIcon(android.R.drawable.ic_lock_idle_low_battery)
        navView.menu.add(0, 2, 0, "About App").setIcon(android.R.drawable.ic_dialog_info)
        
        drawerLayout.addView(contentBase)
        drawerLayout.addView(navView, DrawerLayout.LayoutParams(750, -1).apply { gravity = Gravity.START })
        setContentView(drawerLayout)

        val saved = prefs.getString("allowed_senders", "") ?: ""
        if (saved.isNotEmpty()) saved.split(",").forEach { addSenderTag(it) }
        toggleFields(false, tokenInput, chatInput, fbScriptInput, statusSwitch, addBtn, newSenderIn)
        checkPermissions()
    }

    private fun addSenderTag(name: String) {
        if (allowedSenders.contains(name)) return
        allowedSenders.add(name)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val t = TextView(this).apply { text = name; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        val d = Button(this).apply { text = "X"; setTextColor(Color.RED); background = null }
        d.setOnClickListener { if(isEditMode) { senderListLayout.removeView(row); allowedSenders.remove(name) } }
        row.addView(t); row.addView(d)
        senderListLayout.addView(row)
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this).setTitle("About SMS Eye Pro")
            .setMessage("Version: 3.1\nStatus: Firestore Enabled\nDeveloper: AI Partner")
            .setPositiveButton("OK", null).show()
    }

    private fun createInput(hint: String, value: String?) = EditText(this).apply { setHint(hint); setText(value); setPadding(30, 30, 30, 30) }
    private fun toggleFields(enable: Boolean, vararg views: View) { views.forEach { it.isEnabled = enable } }
    private fun checkPermissions() { ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS), 101) }
}
