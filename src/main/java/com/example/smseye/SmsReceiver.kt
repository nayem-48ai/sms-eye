package com.example.smseye

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val fullMessage = StringBuilder()
            val sender = messages[0].displayOriginatingAddress

            // খণ্ডিত SMS গুলোকে জোড়া লাগানো
            for (sms in messages) {
                fullMessage.append(sms.messageBody)
            }

            val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val isFilterOn = prefs.getBoolean("is_filter_on", false)
            val allowedSenders = prefs.getString("allowed_senders", "")?.split(",")?.map { it.trim().lowercase() } ?: listOf()

            if (!isFilterOn || allowedSenders.contains(sender.lowercase())) {
                TelegramService.forwardSms(context, sender, fullMessage.toString())
            }
        }
    }
}
