package com.leshchenko.weatherforecast.model.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.leshchenko.weatherforecast.model.interfaces.ExtendedWeatherData
import com.leshchenko.weatherforecast.model.interfaces.WeatherData
import com.leshchenko.weatherforecast.model.interfaces.WeatherResponseInterface
import com.leshchenko.weatherforecast.model.interfaces.WeatherType
import com.leshchenko.weatherforecast.utils.Utils
import java.util.*

class AccuWeatherResponse(@SerializedName("DailyForecasts") @Expose private val forecasts: List<DailyForecast>) : WeatherResponseInterface {
    override fun getWeatherForCurrentDay(date: Date): WeatherData? {
        return getAverageWeatherData(date)
    }

    override fun getExtendedWeatherForCurrentDay(date: Date): List<ExtendedWeatherData> {
        // Available in payed version
        return mutableListOf()
    }

    private fun getAverageWeatherData(date: Date): WeatherData? {
        val weatherList = getWeatherDataForCurrentDay(date)
        // Define variables for calculating average value.
        var minTempSum = 0f
        var maxTempSum = 0f
        var precipProbability = 0f
        var weatherType = WeatherType.CLEAR
        return if (weatherList.isEmpty()) {
            null
        } else {
            // Go through all available weather forecasts to calculate average values
            weatherList.forEach {
                maxTempSum += it.temperature.maximum.value
                minTempSum += it.temperature.minimum.value
                precipProbability += it.day.precipitationProbability
                if (getWeatherType(it).vitalLevel > weatherType.vitalLevel) {
                    weatherType = getWeatherType(it)
                }
            }
            WeatherData(weatherList.first().time, minTempSum / weatherList.size, maxTempSum / weatherList.size, weatherType,
                    precipProbability / weatherList.size)
        }
    }

    private fun getWeatherDataForCurrentDay(date: Date): List<DailyForecast> {
        val weatherDataList = mutableListOf<DailyForecast>()
        forecasts.forEach {
            if (Utils.isTimestampsFromOneDay(it.time, Utils.timeInSeconds(date.time))) {
                weatherDataList.add(it)
            }
        }
        return weatherDataList
    }

    private fun getWeatherType(data: DailyForecast): WeatherType {
        var weatherType = WeatherType.CLEAR
        val precipList = mutableListOf(data.day.rainProbability, data.day.snowProbability, data.day.thunderstormProbability)
        when (precipList.indexOf(precipList.max())) {
            0 -> weatherType = WeatherType.RAIN
            1 -> weatherType = WeatherType.SNOW
            2 -> weatherType = WeatherType.THUNDERSTORM
        }
        return weatherType
    }
}

data class DailyForecast(
        @SerializedName("EpochDate") @Expose val time: Long,
        @SerializedName("Temperature") @Expose val temperature: Temperature,
        @SerializedName("Day") @Expose val day: Day
)

data class Day(
        @SerializedName("PrecipitationProbability") @Expose val precipitationProbability: Float,
        @SerializedName("ThunderstormProbability") @Expose val thunderstormProbability: Float,
        @SerializedName("RainProbability") @Expose val rainProbability: Float,
        @SerializedName("SnowProbability") @Expose val snowProbability: Float
)

data class Maximum(
        @SerializedName("Value") @Expose val value: Float
)

data class Minimum(
        @SerializedName("Value") @Expose val value: Float
)

data class Temperature(
        @SerializedName("Minimum") @Expose val minimum: Minimum,
        @SerializedName("Maximum") @Expose val maximum: Maximum
)

data class AccuWeatherLocationResponse(@SerializedName("Key") @Expose val key: String)