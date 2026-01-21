package com.example.smseye

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
        
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        // Root Drawer Layout
        drawerLayout = DrawerLayout(this)
        
        // Main Content Container
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F8F9FA"))
        }

        // Side Navigation View
        val navView = NavigationView(this)
        navView.setNavigationItemSelectedListener { item ->
            when(item.itemId) {
                1 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    }
                }
                2 -> Toast.makeText(this, "SMS Eye v2.0\nStatus: Active", Toast.LENGTH_LONG).show()
            }
            drawerLayout.closeDrawers()
            true
        }
        navView.menu.add(0, 1, 0, "Battery Optimization").setIcon(android.R.drawable.ic_lock_idle_low_battery)
        navView.menu.add(0, 2, 0, "About App").setIcon(android.R.drawable.ic_dialog_info)

        // --- UI Header ---
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 60, 40, 40)
            setBackgroundColor(Color.WHITE)
        }
        val menuBtn = Button(this).apply { text = "☰"; textSize = 20f; background = null }
        menuBtn.setOnClickListener { drawerLayout.openDrawer(Gravity.START) }
        
        val title = TextView(this).apply {
            text = "SMS EYE PRO"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setPadding(30, 0, 0, 0)
        }
        header.addView(menuBtn)
        header.addView(title)
        container.addView(header)

        // --- Scrollable Form ---
        val scroll = ScrollView(this)
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }
        scroll.addView(form)
        container.addView(scroll)

        // Status Switch
        val statusSwitch = Switch(this).apply {
            text = "Service Active Status"
            isChecked = prefs.getBoolean("is_active", true)
            setPadding(0, 20, 0, 40)
        }

        // Mode Spinner
        val modeSpinner = Spinner(this)
        val modes = arrayOf("Telegram Only", "Firebase Only", "Both")
        val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, modes)
        modeSpinner.adapter = adapter
        modeSpinner.setSelection(prefs.getInt("mode_index", 0))

        // Input Fields
        val tokenInput = createStyledInput("Telegram Bot Token", prefs.getString("bot_token", ""))
        val chatInput = createStyledInput("Telegram Chat ID", prefs.getString("chat_id", ""))
        val fbScriptInput = createStyledInput("Paste Firebase Script Here", prefs.getString("fb_script", ""))

        // Dynamic Visibility
        fun toggleInputs(pos: Int) {
            tokenInput.visibility = if (pos == 0 || pos == 2) View.VISIBLE else View.GONE
            chatInput.visibility = if (pos == 0 || pos == 2) View.VISIBLE else View.GONE
            fbScriptInput.visibility = if (pos == 1 || pos == 2) View.VISIBLE else View.GONE
        }
        modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) = toggleInputs(pos)
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Sender List Area
        senderListLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 20, 0, 20) }
        val addSenderBox = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val newSenderInput = EditText(this).apply { hint = "Sender Name"; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        val addBtn = Button(this).apply { text = "+" }
        addBtn.setOnClickListener {
            val name = newSenderInput.text.toString().trim()
            if (name.isNotEmpty()) { addSenderTag(name); newSenderInput.text.clear() }
        }
        addSenderBox.addView(newSenderInput); addSenderBox.addView(addBtn)

        // Edit/Save Button
        val editBtn = Button(this).apply {
            text = "Edit Configuration"
            setBackgroundColor(Color.LTGRAY)
        }

        fun setEnabledState(enabled: Boolean) {
            statusSwitch.isEnabled = enabled
            modeSpinner.isEnabled = enabled
            tokenInput.isEnabled = enabled
            chatInput.isEnabled = enabled
            fbScriptInput.isEnabled = enabled
            newSenderInput.isEnabled = enabled
            addBtn.isEnabled = enabled
        }
        setEnabledState(false)

        editBtn.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true
                editBtn.text = "Save Configuration"
                editBtn.setBackgroundColor(Color.parseColor("#4CAF50"))
                editBtn.setTextColor(Color.WHITE)
                setEnabledState(true)
            } else {
                val script = fbScriptInput.text.toString()
                val extractedId = "\"projectId\":\\s*\"(.*?)\"".toRegex().find(script)?.groups?.get(1)?.value ?: ""
                
                prefs.edit().apply {
                    putBoolean("is_active", statusSwitch.isChecked)
                    putString("bot_token", tokenInput.text.toString())
                    putString("chat_id", chatInput.text.toString())
                    putString("fb_script", script)
                    putString("fb_id", extractedId)
                    putInt("mode_index", modeSpinner.selectedItemPosition)
                    putString("allowed_senders", allowedSenders.joinToString(","))
                    apply()
                }
                isEditMode = false
                editBtn.text = "Edit Configuration"
                editBtn.setBackgroundColor(Color.LTGRAY)
                editBtn.setTextColor(Color.BLACK)
                setEnabledState(false)
                Toast.makeText(this, "Configuration Saved!", Toast.LENGTH_SHORT).show()
            }
        }

        // Add to Form
        form.addView(statusSwitch)
        form.addView(modeSpinner)
        form.addView(tokenInput); form.addView(chatInput); form.addView(fbScriptInput)
        form.addView(TextView(this).apply { text = "Forwarding List:"; setPadding(0, 30, 0, 10) })
        form.addView(addSenderBox); form.addView(senderListLayout)
        form.addView(editBtn)

        // Load Senders
        val savedSenders = prefs.getString("allowed_senders", "") ?: ""
        if (savedSenders.isNotEmpty()) savedSenders.split(",").forEach { addSenderTag(it) }

        drawerLayout.addView(container)
        setContentView(drawerLayout)
        checkPermissions()
    }

    private fun addSenderTag(name: String) {
        if (allowedSenders.contains(name)) return
        allowedSenders.add(name)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 5, 0, 5) }
        val t = TextView(this).apply { text = name; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        val d = Button(this).apply { text = "X"; setTextColor(Color.RED); background = null }
        d.setOnClickListener {
            if (isEditMode) {
                senderListLayout.removeView(row)
                allowedSenders.remove(name)
            }
        }
        row.addView(t); row.addView(d)
        senderListLayout.addView(row)
    }

    private fun createStyledInput(hint: String, text: String?) = EditText(this).apply {
        setHint(hint)
        setText(text)
        setPadding(30, 30, 30, 30)
        background = GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(2, Color.parseColor("#DDDDDD"))
            cornerRadius = 12f
        }
    }

    private fun checkPermissions() {
        val perms = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        ActivityCompat.requestPermissions(this, perms, 101)
    }
}
