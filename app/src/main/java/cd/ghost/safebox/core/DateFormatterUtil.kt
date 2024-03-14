package cd.ghost.safebox.core

import android.icu.text.SimpleDateFormat
import android.os.Build
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateFormatterUtil {
    fun formatLikeFull(timeMiles: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val instant = Instant.ofEpochMilli(timeMiles)
            val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss").format(dateTime)
        } else {
            val date = Date(timeMiles)
            SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.getDefault()).format(date)
        }
    }

    fun formatDate(timeMiles: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val instant = Instant.ofEpochMilli(timeMiles)
            val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            DateTimeFormatter.ofPattern("MMM d HH:mm").format(dateTime)
        } else {
            val date = Date(timeMiles)
            SimpleDateFormat("MMM d HH:mm", Locale.getDefault()).format(date)
        }
    }
}