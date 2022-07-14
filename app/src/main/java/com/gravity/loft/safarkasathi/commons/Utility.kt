package com.gravity.loft.safarkasathi.commons

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.text.format.DateUtils
import org.ocpsoft.prettytime.PrettyTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class Utility {

    companion object{
        /*
        @SuppressLint("SimpleDateFormat")
        fun getReadableDuration(dateString: String): String?{
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            dateFormat.timeZone = TimeZone.getTimeZone("GMT")
            try {
                val time: Long = dateFormat.parse(dateString)!!.time
                val now = System.currentTimeMillis()
                val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
                return ago.toString()
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return null
        }

         */

        fun getPrettyTime(startTime: String): String{
            val p = PrettyTime()
            val serverDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return p.format(LocalDateTime.parse(startTime, serverDateFormatter))
        }

        fun Uri.getName(context: Context): String {
            val returnCursor = context.contentResolver.query(this, null, null, null, null)
            val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor?.moveToFirst()
            val fileName = returnCursor?.getString(nameIndex!!)
            returnCursor?.close()
            return fileName!!
        }
    }

}