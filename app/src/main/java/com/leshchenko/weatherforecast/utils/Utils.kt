package com.leshchenko.weatherforecast.utils

import android.content.Context
import android.net.ConnectivityManager
import java.util.*


class Utils {
    companion object {
        fun isTimestampsFromOneDay(firstTimestampInSec: Long, secondTimestampInSec: Long): Boolean {
            val calendar = Calendar.getInstance()
            calendar.time = Date(firstTimestampInSec * 1000)
            val firstDay = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.time = Date(secondTimestampInSec * 1000)
            val secondDay = calendar.get(Calendar.DAY_OF_MONTH)
            return firstDay == secondDay
        }

        fun timeInSeconds(time: Long): Long {
            return time / 1000
        }

        fun getDaysTimestampsForForecast(): List<Long> {
            val timestamps = mutableListOf<Long>()
            val calendar = Calendar.getInstance()
            calendar.time = Date(System.currentTimeMillis())
            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            timestamps.add(calendar.timeInMillis)
            for (i in 1..4) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                timestamps.add(calendar.timeInMillis)
            }
            return timestamps
        }

        fun getHourTimestampsForForecast(): List<Long> {
            val timestamps = mutableListOf<Long>()
            val calendar = Calendar.getInstance()
            calendar.time = Date(System.currentTimeMillis())
            calendar.set(Calendar.HOUR_OF_DAY, 1)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            timestamps.add(calendar.timeInMillis)
            for (i in 1..23) {
                calendar.add(Calendar.HOUR_OF_DAY, 1)
                timestamps.add(calendar.timeInMillis)
            }
            return timestamps
        }

        fun isTimestampsFromOneHour(firstTimestampInSec: Long, secondTimestampInSec: Long): Boolean {
            val calendar = Calendar.getInstance()
            calendar.time = Date(firstTimestampInSec * 1000)
            val firstHour = calendar.get(Calendar.HOUR_OF_DAY)
            calendar.time = Date(secondTimestampInSec * 1000)
            val secondHour = calendar.get(Calendar.HOUR_OF_DAY)
            return firstHour == secondHour
        }

        fun isOnline(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }
}