package ubb.pdm.gamestop.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun String.toDate(
    format: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    timeZone: TimeZone = TimeZone.getTimeZone("UTC")
): Date {
    val parser = SimpleDateFormat(format, Locale.getDefault())
    parser.timeZone = timeZone
    return try {
        parser.parse(this) ?: Date()
    } catch (_: Exception) {
        Date()
    }
}

fun Date.formatTo(
    format: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    timeZone: TimeZone = TimeZone.getDefault()
): String {
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    formatter.timeZone = timeZone

    return try {
        formatter.format(this)
    } catch (_: Exception) {
        ""
    }
}

fun Long.toDate(): Date = Date(this)