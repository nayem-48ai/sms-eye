package com.example.smseye

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var senderListLayout: LinearLayout
    private val allowedSenders = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val scroll = ScrollView(this)
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 60, 50, 60)
            setBackgroundColor(Color.parseColor("#F3F6FB"))
        }
        scroll.addView(rootLayout)

        // Header Card (Glassy Look)
        val header = CardView(this, "Service Status")
        val statusSwitch = Switch(this).apply {
            text = "Active Service"
            isChecked = prefs.getBoolean("is_active", true)
            textSize = 18f
        }
        rootLayout.addView(header)
        rootLayout.addView(statusSwitch)

        // Credentials Section
        val tokenInput = createEditText("Telegram Bot Token", prefs.getString("bot_token", ""))
        val chatInput = createEditText("Telegram Chat ID", prefs.getString("chat_id", ""))
        val fbInput = createEditText("Firebase Project ID", prefs.getString("fb_id", "autopay-c8eea"))

        // Mode Selector
        val modeSpinner = Spinner(this)
        modeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Telegram", "Firebase", "Both"))
        modeSpinner.setSelection(prefs.getInt("mode_index", 0))

        // Dynamic Visibility Logic
        fun updateVisibility(position: Int) {
            tokenInput.visibility = if (position == 0 || position == 2) View.VISIBLE else View.GONE
            chatInput.visibility = if (position == 0 || position == 2) View.VISIBLE else View.GONE
            fbInput.visibility = if (position == 1 || position == 2) View.VISIBLE else View.GONE
        }
        
        modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) = updateVisibility(position)
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        rootLayout.addView(modeSpinner)
        rootLayout.addView(tokenInput); rootLayout.addView(chatInput); rootLayout.addView(fbInput)

        // Sender Filter Section with Plus Icon
        val filterTitle = TextView(this).apply { text = "Allowed Senders List"; setPadding(0, 40, 0, 10); textSize = 18f }
        rootLayout.addView(filterTitle)

        senderListLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val addSenderLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val newSenderInput = EditText(this).apply { hint = "Add Sender (e.g. Bkash)"; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
        val addBtn = Button(this).apply { text = "+"; textSize = 20f }
        
        addBtn.setOnClickListener {
            val name = newSenderInput.text.toString().trim()
            if (name.isNotEmpty()) {
                addSenderTag(name)
                newSenderInput.text.clear()
            }
        }
        
        addSenderLayout.addView(newSenderInput); addSenderLayout.addView(addBtn)
        rootLayout.addView(addSenderLayout); rootLayout.addView(senderListLayout)

        // Load Saved Senders
        val saved = prefs.getString("allowed_senders", "") ?: ""
        if (saved.isNotEmpty()) saved.split(",").forEach { addSenderTag(it) }

        // Action Buttons
        val saveBtn = createStyledButton("SAVE CONFIGURATION", "#4CAF50")
        saveBtn.setOnClickListener {
            prefs.edit().apply {
                putBoolean("is_active", statusSwitch.isChecked)
                putString("bot_token", tokenInput.text.toString())
                putString("chat_id", chatInput.text.toString())
                putString("fb_id", fbInput.text.toString())
                putInt("mode_index", modeSpinner.selectedItemPosition)
                putString("allowed_senders", allowedSenders.joinToString(","))
                apply()
            }
            Toast.makeText(this, "All Settings Saved!", Toast.LENGTH_SHORT).show()
        }

        rootLayout.addView(saveBtn)
        setContentView(scroll)
        checkPermissions()
    }

    private fun addSenderTag(name: String) {
        if (allowedSenders.contains(name)) return
        allowedSenders.add(name)
        val tagView = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL
            setPadding(10, 10, 10, 10)
        }
        val txt = TextView(this).apply { text = name; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
        val del = Button(this).apply { text = "X"; setTextColor(Color.RED); background = null }
        del.setOnClickListener {
            senderListLayout.removeView(tagView)
            allowedSenders.remove(name)
        }
        tagView.addView(txt); tagView.addView(del)
        senderListLayout.addView(tagView)
    }

    private fun createEditText(hint: String, value: String?) = EditText(this).apply {
        setHint(hint)
        setText(value)
        background = GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(2, Color.LTGRAY)
            cornerRadius = 15f
        }
        setPadding(30, 30, 30, 30)
    }

    private fun createStyledButton(txt: String, color: String) = Button(this).apply {
        text = txt
        setTextColor(Color.WHITE)
        background = GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = 20f
        }
    }

    private fun CardView(context: Context, title: String) = TextView(context).apply {
        text = title
        textSize = 22f
        setPadding(0, 20, 0, 20)
        setTextColor(Color.BLACK)
    }

    private fun checkPermissions() {
        val perms = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        ActivityCompat.requestPermissions(this, perms, 101)
    }
}
