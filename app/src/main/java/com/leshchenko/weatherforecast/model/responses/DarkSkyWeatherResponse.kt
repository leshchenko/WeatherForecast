package com.leshchenko.weatherforecast.model.responses

import com.leshchenko.weatherforecast.model.interfaces.ExtendedWeatherData
import com.leshchenko.weatherforecast.model.interfaces.WeatherData
import com.leshchenko.weatherforecast.model.interfaces.WeatherResponseInterface
import com.leshchenko.weatherforecast.model.interfaces.WeatherType
import com.leshchenko.weatherforecast.utils.Utils
import java.util.*


class DarkSkyResponse(private val hourly: Hourly, private val daily: Daily) : WeatherResponseInterface {
    override fun getWeatherForCurrentDay(date: Date): WeatherData? {
        val weatherDataList = mutableListOf<DailyData>()
        daily.data.forEach {
            if (Utils.isTimestampsFromOneDay(it.time, Utils.timeInSeconds(date.time))) {
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

    private fun getAverageWeatherData(data: List<DailyData>): WeatherData? {
        // Define variables for calculating average value.
        var minTempSum = 0f
        var maxTempSum = 0f
        var precipProbability = 0f
        var weatherType = WeatherType.CLEAR

        return if (data.isEmpty()) {
            null
        } else {
            // Go through all available weather forecasts to calculate average values
            data.forEach {
                maxTempSum += it.temperatureMax
                minTempSum += it.temperatureMin
                precipProbability += it.precipProbability
                if (getWeatherType(it.precipType).vitalLevel > weatherType.vitalLevel) {
                    weatherType = getWeatherType(it.precipType)
                }
            }
            // Move precip probability in percents
            val precipProbabilityInPercents = (precipProbability / data.size) * 100
            WeatherData(data.first().time, minTempSum / data.size, maxTempSum / data.size, weatherType,
                    precipProbabilityInPercents)
        }
    }

    private fun getWeatherDataForCurrentDay(date: Date): List<HourlyData> {
        val weatherDataList = mutableListOf<HourlyData>()
        hourly.data.forEach {
            if (Utils.isTimestampsFromOneDay(it.time, Utils.timeInSeconds(date.time))) {
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

    private fun getExtendedWeatherData(data: HourlyData): ExtendedWeatherData {
        // Move precip probability in percents
        val precipProbability = (data.precipProbability) * 100
        return ExtendedWeatherData(data.time, data.temperature, getWeatherType(data.precipType),
                data.cloudCover, data.windSpeed, data.pressure, data.humidity, precipProbability)
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
