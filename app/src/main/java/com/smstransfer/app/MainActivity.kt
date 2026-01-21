package com.smstransfer.app

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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        statusText = findViewById(R.id.statusText)
        val requestButton: Button = findViewById(R.id.requestButton)
        
        statusText.text = "অ্যাপ শুরু হয়েছে\nSMS ফরওয়ার্ডার"
        
        requestButton.setOnClickListener {
            checkAndRequestSmsPermission()
        }
        
        // Check permission on startup
        checkPermissionStatus()
    }
    
    private fun checkPermissionStatus() {
        if (hasSmsPermission()) {
            statusText.text = "✅ পারমিশন অনুমোদিত\nSMS স্বয়ংক্রিয়ভাবে ফরওয়ার্ড হবে"
            SmsReceiver.startSmsForwarding(this)
        } else {
            statusText.text = "❌ SMS পারমিশন প্রয়োজন\nনিচের বাটনে ক্লিক করুন"
        }
    }
    
    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun checkAndRequestSmsPermission() {
        if (!hasSmsPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_SMS),
                SMS_PERMISSION_CODE
            )
        } else {
            Toast.makeText(this, "পারমিশন ইতিমধ্যে আছে", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                statusText.text = "✅ পারমিশন অনুমোদিত\nSMS স্বয়ংক্রিয়ভাবে ফরওয়ার্ড হবে"
                Toast.makeText(this, "SMS পারমিশন অনুমোদিত!", Toast.LENGTH_LONG).show()
                SmsReceiver.startSmsForwarding(this)
            } else {
                statusText.text = "❌ পারমিশন প্রত্যাখ্যান\nঅ্যাপটি কাজ করবে না"
                Toast.makeText(this, "SMS পারমিশন প্রয়োজন", Toast.LENGTH_LONG).show()
            }
        }
    }
}
