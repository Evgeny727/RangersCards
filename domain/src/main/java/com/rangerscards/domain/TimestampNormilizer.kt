package com.rangerscards.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimestampNormilizer {
    // It ensures the fraction has exactly 3 digits.
    fun fixFraction(dateString: String?): String? {
        if (dateString == null) return null
        // Regex groups:
        // 1. The part before the fraction (including the dot)
        // 2. The fractional digits
        // 3. The rest (timezone information)
        val regex = """(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.)(\d+)(.*)""".toRegex()
        return regex.replace(dateString) { matchResult ->
            val prefix = matchResult.groupValues[1]
            val fraction = matchResult.groupValues[2]
            val suffix = matchResult.groupValues[3]
            // Truncate to the first 3 digits or pad with zeros if needed.
            val fixedFraction = when {
                fraction.length > 3 -> fraction.substring(0, 3)
                fraction.length < 3 -> fraction.padEnd(3, '0')
                else -> fraction
            }
            prefix + fixedFraction + suffix
        }
    }

    fun compareTimestamps(timestamp1: String, timestamp2: String): Boolean {
        if (timestamp1.isEmpty()) return true
        if (timestamp1 == "null" || timestamp2 == "null") return true
        val timestamp1Fixed = fixFraction(timestamp1).toString()
        val timestamp2Fixed = fixFraction(timestamp2).toString()
        // Define the date format
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC") // Set the timezone to UTC

        // Parse the timestamps
        val date1: Date? = format.parse(timestamp1Fixed)
        val date2: Date? = format.parse(timestamp2Fixed)
        if (date1 == null || date2 == null) return true
        return when {
            date1.before(date2) -> true
            date1.after(date2) -> false
            else -> false
        }
    }
}