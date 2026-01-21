package com.example.smsforwarder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val SMS_PERMISSION_CODE = 101
    private lateinit var statusText: TextView
    private lateinit var permissionBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // সাধারণ লেআউট প্রোগ্রামাটিকালি তৈরি (XML এড়ানোর জন্য, তবে XML ব্যবহার করাই ভালো)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.tvStatus)
        permissionBtn = findViewById(R.id.btnGrant)

        updateStatus()

        permissionBtn.setOnClickListener {
            checkAndRequestPermissions()
        }
    }

    private fun updateStatus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            statusText.text = "Status: Active ✅\nApp is ready to forward SMS."
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            permissionBtn.isEnabled = false
            permissionBtn.text = "Permission Granted"
        } else {
            statusText.text = "Status: Inactive ❌\nPermission needed."
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            permissionBtn.isEnabled = true
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
                SMS_PERMISSION_CODE
            )
        } else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateStatus()
                Toast.makeText(this, "Setup Complete!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
