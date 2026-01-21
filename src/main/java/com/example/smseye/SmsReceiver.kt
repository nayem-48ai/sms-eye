package com.example.smseye

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        
        // চেক সার্ভিস অ্যাক্টিভ কি না
        if (!prefs.getBoolean("is_active", true)) return

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val fullMessage = StringBuilder()
            val sender = messages[0].displayOriginatingAddress ?: "Unknown"

            for (sms in messages) { fullMessage.append(sms.messageBody) }

            val allowedString = prefs.getString("allowed_senders", "") ?: ""
            val allowedList = allowedString.split(",").map { it.trim().lowercase() }

            if (allowedString.isEmpty() || allowedList.contains(sender.lowercase())) {
                TelegramService.forwardSms(context, sender, fullMessage.toString())
            }
        }
    }
}
