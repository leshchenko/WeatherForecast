package com.leshchenko.weatherforecast.Utils

import java.util.*

class Utils {
    companion object {
        fun isTimestampsFromOneDay(firstTimestamp: Long, secondTimestamp: Long): Boolean {
            val calendar = Calendar.getInstance()
            calendar.time = Date(firstTimestamp )
            val firstDay = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.time = Date(secondTimestamp)
            val secondDay = calendar.get(Calendar.DAY_OF_MONTH)
            return firstDay == secondDay
        }
    }
}