package com.leshchenko.weatherforecast.Utils

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
            timestamps.add(calendar.timeInMillis)
            for (i in 1..4) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                timestamps.add(calendar.timeInMillis)
            }
            return timestamps
        }
    }
}