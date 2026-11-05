package com.example.ai.utils

import android.content.Context
import android.net.Uri
import android.provider.Telephony

data class SmsMessageData(
    val address: String,
    val body: String,
    val dateSent: Long
)

object SmsReader {

    fun readFinancialSms(context: Context, limit: Int = 20): List<SmsMessageData> {
        val messages = mutableListOf<SmsMessageData>()
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        val keywords = arrayOf("debited", "credited", "spent", "received", "a/c", "upi", "inr", "rs", "bank", "vpa", "paid")

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

                while (it.moveToNext() && messages.size < limit) {
                    val body = it.getString(bodyIdx) ?: ""
                    val lowerBody = body.lowercase()

                    // Check if SMS matches financial keywords
                    val isFinancial = keywords.any { keyword -> lowerBody.contains(keyword) }
                    if (isFinancial) {
                        val address = it.getString(addressIdx) ?: "Bank"
                        val dateSent = it.getLong(dateIdx)
                        messages.add(SmsMessageData(address, body, dateSent))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return messages
    }
}
