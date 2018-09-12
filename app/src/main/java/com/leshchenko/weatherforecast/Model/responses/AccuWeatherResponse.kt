package com.leshchenko.weatherforecast.Model.responses

import com.leshchenko.weatherforecast.Model.Interfaces.ExtendedWeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherResponseInterface
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherType
import com.leshchenko.weatherforecast.Utils.Utils
import java.util.*

class AccuWeatherResponse(val DailyForecasts: List<DailyForecast>) : WeatherResponseInterface {
    override fun getWeatherForCurrentDay(date: Date): WeatherData {
        return getAverageWeatherData(date)
    }

    override fun getExtendedWeatherForCurrentDay(date: Date): List<ExtendedWeatherData> {
        // Available in payed version
        return mutableListOf()
    }

    private fun getAverageWeatherData(date: Date): WeatherData {
        val weatherList = getWeatherDataForCurrentDay(date)
        var minTempSum = 0f
        var maxTempSum = 0f
        var precipProbability = 0f
        var weatherType = WeatherType.CLEAR
        if (weatherList.isEmpty()) {
            return WeatherData(-1L, -1f, -1f, weatherType, -1f)
        } else {
            weatherList.forEach {
                maxTempSum += it.Temperature.Maximum.Value
                minTempSum += it.Temperature.Minimum.Value
                precipProbability += it.Day.PrecipitationProbability
                if (getWeatherType(it).vitalLevel > weatherType.vitalLevel) {
                    weatherType = getWeatherType(it)
                }
            }
            return WeatherData(weatherList.first().EpochDate, minTempSum / weatherList.size, maxTempSum / weatherList.size, weatherType,
                    precipProbability / weatherList.size)
        }
    }

    private fun getPrecipProbability(weatherType: WeatherType): Float {
        return if (weatherType == WeatherType.CLEAR) {
            0f
        } else {
            1f
        }
    }

    private fun getWeatherDataForCurrentDay(date: Date): List<DailyForecast> {
        val weatherDataList = mutableListOf<DailyForecast>()
        DailyForecasts.forEach {
            if (Utils.isTimestampsFromOneDay(it.EpochDate, Utils.timeInSeconds(date.time))) {
                weatherDataList.add(it)
            }
        }
        return weatherDataList
    }

    private fun getWeatherType(data: DailyForecast): WeatherType {
        var weatherType = WeatherType.CLEAR
        val precipList = mutableListOf(data.Day.RainProbability, data.Day.SnowProbability, data.Day.ThunderstormProbability)
        when (precipList.indexOf(precipList.max())) {
            0 -> weatherType = WeatherType.RAIN
            1 -> weatherType = WeatherType.SNOW
            2 -> weatherType = WeatherType.THUNDERSTORM
        }
        return weatherType
    }
}

data class DailyForecast(
        val EpochDate: Long,
        val Temperature: Temperature,
        val Day: Day
)

data class Day(
        val PrecipitationProbability: Float,
        val ThunderstormProbability: Float,
        val RainProbability: Float,
        val SnowProbability: Float
)

data class Maximum(
        val Value: Float
)

data class Minimum(
        val Value: Float
)

data class Temperature(
        val Minimum: Minimum,
        val Maximum: Maximum
)

data class AccuWeatherLocationResponse(val Key: String)