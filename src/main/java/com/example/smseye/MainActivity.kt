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
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#F8F9FA")) }

        // Toolbar
        val toolbar = LinearLayout(this).apply {
            setPadding(40, 50, 40, 50)
            setBackgroundColor(Color.WHITE)
            elevation = 10f
        }
        val menuBtn = Button(this).apply { text = "☰"; background = null; textSize = 22f }
        menuBtn.setOnClickListener { drawerLayout.openDrawer(Gravity.START) }
        toolbar.addView(menuBtn)
        toolbar.addView(TextView(this).apply { text = "SMS EYE PRO"; textSize = 20f; setPadding(30, 0, 0, 0) })
        content.addView(toolbar)

        val scroll = ScrollView(this)
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(60, 40, 60, 40) }
        scroll.addView(form)
        content.addView(scroll)

        // Inputs
        val statusSwitch = Switch(this).apply { text = "Forwarding Status"; isChecked = prefs.getBoolean("is_active", true); setPadding(0, 0, 0, 40) }
        val modeSpinner = Spinner(this).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Telegram", "Firestore", "Both")) }
        modeSpinner.setSelection(prefs.getInt("mode_index", 0))

        val tokenInput = createInput("Telegram Bot Token", prefs.getString("bot_token", ""))
        val chatInput = createInput("Telegram Chat ID", prefs.getString("chat_id", ""))
        val fbScriptInput = createInput("Firebase Full Script", prefs.getString("fb_script", ""))

        // Whitelist Filter
        val filterTitle = TextView(this).apply { text = "Whitelist Senders"; setPadding(0, 40, 0, 10); textSize = 16f }
        senderListLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val addRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val newIn = EditText(this).apply { hint = "e.g. Bkash"; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        val addBtn = Button(this).apply { text = "Add" }
        addBtn.setOnClickListener { val n = newIn.text.toString().trim(); if(n.isNotEmpty()){ addTag(n); newIn.text.clear() } }
        addRow.addView(newIn); addRow.addView(addBtn)

        val editBtn = Button(this).apply { 
            text = "Edit Configuration"
            setBackgroundColor(Color.parseColor("#6200EE"))
            setTextColor(Color.WHITE)
        }

        editBtn.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true
                editBtn.text = "Save Config"
                fbScriptInput.setText(prefs.getString("fb_script", ""))
                toggleFields(true, tokenInput, chatInput, fbScriptInput, statusSwitch, addBtn)
            } else {
                val script = fbScriptInput.text.toString()
                val pId = "\"projectId\":\\s*\"(.*?)\"".toRegex().find(script)?.groups?.get(1)?.value ?: ""
                val aKey = "\"apiKey\":\\s*\"(.*?)\"".toRegex().find(script)?.groups?.get(1)?.value ?: ""

                prefs.edit().apply {
                    putBoolean("is_active", statusSwitch.isChecked)
                    putString("bot_token", tokenInput.text.toString())
                    putString("chat_id", chatInput.text.toString())
                    putString("fb_script", script); putString("fb_id", pId); putString("fb_key", aKey)
                    putInt("mode_index", modeSpinner.selectedItemPosition)
                    putString("allowed_senders", allowedSenders.joinToString(","))
                    apply()
                }
                isEditMode = false
                editBtn.text = "Edit Configuration"
                fbScriptInput.setText(if(script.length > 40) script.take(35) + "..." else script)
                toggleFields(false, tokenInput, chatInput, fbScriptInput, statusSwitch, addBtn)
                Toast.makeText(this, "Configuration Saved!", Toast.LENGTH_SHORT).show()
            }
        }

        form.addView(statusSwitch); form.addView(modeSpinner)
        form.addView(tokenInput); form.addView(chatInput); form.addView(fbScriptInput)
        form.addView(filterTitle); form.addView(addRow); form.addView(senderListLayout); form.addView(editBtn)

        // Nav View
        val nav = NavigationView(this)
        nav.menu.add(0, 1, 0, "Battery Optimization").setIcon(android.R.drawable.ic_lock_idle_low_battery)
        nav.menu.add(0, 2, 0, "About App").setIcon(android.R.drawable.ic_dialog_info)
        nav.setNavigationItemSelectedListener {
            if(it.itemId == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply { data = Uri.parse("package:$packageName") })
                }
            } else if(it.itemId == 2) showAbout()
            drawerLayout.closeDrawers(); true
        }

        drawerLayout.addView(content)
        drawerLayout.addView(nav, DrawerLayout.LayoutParams(700, -1).apply { gravity = Gravity.START })
        setContentView(drawerLayout)

        val saved = prefs.getString("allowed_senders", "") ?: ""
        if (saved.isNotEmpty()) saved.split(",").forEach { addTag(it) }
        toggleFields(false, tokenInput, chatInput, fbScriptInput, statusSwitch, addBtn)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS), 101)
    }

    private fun addTag(name: String) {
        if (allowedSenders.contains(name)) return
        allowedSenders.add(name)
        val r = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 5, 0, 5) }
        r.addView(TextView(this).apply { text = name; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) })
        val d = Button(this).apply { text = "X"; setTextColor(Color.RED); background = null }
        d.setOnClickListener { if(isEditMode){ senderListLayout.removeView(r); allowedSenders.remove(name) } }
        r.addView(d); senderListLayout.addView(r)
    }

    private fun showAbout() = AlertDialog.Builder(this).setTitle("SMS Eye").setMessage("Version 3.5\nFirestore Rest API Mode").show()
    private fun createInput(h: String, v: String?) = EditText(this).apply { hint = h; setText(v); setPadding(30, 30, 30, 30) }
    private fun toggleFields(e: Boolean, vararg v: View) { v.forEach { it.isEnabled = e } }
}
