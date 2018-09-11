package com.leshchenko.weatherforecast.Model

import com.leshchenko.weatherforecast.Model.Interfaces.ExtendedWeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherResponseInterface
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherType
import com.leshchenko.weatherforecast.Utils.Utils
import java.util.*


class DarkSkyResponse(val hourly: Hourly, val daily: Daily) : WeatherResponseInterface {
    override fun getWeatherForCurrentDay(date: Date): WeatherData {
        val weatherDataList = mutableListOf<DailyData>()
        daily.data.forEach {
            if (Utils.isTimestampsFromOneDay(it.time, date.time)) {
                weatherDataList.add(it)
            }
        }
        return getAverageWeatherData(weatherDataList)
    }

    override fun getExtendedWeatherForCurrentDay(date: Date): List<ExtendedWeatherData> {
        val extendedWeatherData = mutableListOf<ExtendedWeatherData>()
        getWeatherDataForCurrentDay(date).forEach {
            extendedWeatherData.add(getExtendedWeatherData(it))
        }
        return extendedWeatherData
    }

    fun getAverageWeatherData(data: List<DailyData>): WeatherData {
        var minTempSum = 0f
        var maxTempSum = 0f
        var weatherType = WeatherType.CLEAR
        data.forEach {
            maxTempSum += it.temperatureMax
            minTempSum += it.temperatureMin
            if (getWeatherType(it.precipType).vitalLevel > weatherType.vitalLevel) {
                weatherType = getWeatherType(it.precipType)
            }
        }
        return WeatherData(minTempSum / data.size, maxTempSum / data.size, weatherType)
    }

    fun getWeatherDataForCurrentDay(date: Date): List<HourlyData> {
        val weatherDataList = mutableListOf<HourlyData>()
        hourly.data.forEach {
            if (Utils.isTimestampsFromOneDay(it.time, date.time)) {
                weatherDataList.add(it)
            }
        }
        return weatherDataList
    }

    private fun getWeatherType(data: String?): WeatherType {
        var weatherType = WeatherType.CLEAR
        data?.let {
            when (data) {
                "rain" -> weatherType = WeatherType.RAIN
                "snow" -> weatherType = WeatherType.SNOW
            }
        }
        return weatherType
    }

    fun getExtendedWeatherData(data: HourlyData): ExtendedWeatherData {
        return ExtendedWeatherData(data.time, data.temperature, getWeatherType(data.precipType),
                data.cloudCover, data.windSpeed, data.pressure, data.humidity)
    }

}

data class Daily(
        val summary: String,
        val icon: String,
        val data: List<DailyData>
)

data class DailyData(
        val time: Long,
        val precipIntensity: Float,
        val precipProbability: Float,
        val precipType: String?,
        val humidity: Float,
        val pressure: Float,
        val windSpeed: Float,
        val cloudCover: Float,
        val temperatureMin: Float,
        val temperatureMax: Float
)

data class Hourly(
        val summary: String,
        val icon: String,
        val data: List<HourlyData>
)

data class HourlyData(
        val time: Long,
        val precipProbability: Float,
        val precipType: String?,
        val temperature: Float,
        val humidity: Float,
        val pressure: Float,
        val windSpeed: Float,
        val cloudCover: Float
)