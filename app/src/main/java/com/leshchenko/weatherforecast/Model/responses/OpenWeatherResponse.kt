package com.leshchenko.weatherforecast.Model.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.leshchenko.weatherforecast.Model.Interfaces.ExtendedWeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherResponseInterface
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherType
import com.leshchenko.weatherforecast.Utils.Utils
import java.util.*


class OpenWeatherResponse(private val list: List<Data>) : WeatherResponseInterface {

    override fun getWeatherForCurrentDay(date: Date): WeatherData? {
        return getAverageWeatherData(date)
    }

    override fun getExtendedWeatherForCurrentDay(date: Date): List<ExtendedWeatherData> {
        val extendedWeatherData = mutableListOf<ExtendedWeatherData>()
        getWeatherDataForCurrentDay(date).forEach {
            extendedWeatherData.add(getExtendedWeatherData(it))
        }
        return extendedWeatherData
    }

    private fun getAverageWeatherData(date: Date): WeatherData? {
        val weatherList = getWeatherDataForCurrentDay(date)
        // Define variables for calculating average value.
        var minTempSum = 0f
        var maxTempSum = 0f
        var weatherType = WeatherType.CLEAR

        return if (weatherList.isEmpty()) {
            null
        } else {
            // Go through all available weather forecasts to calculate average values
            weatherList.forEach {
                maxTempSum += it.main.maxTemperature
                minTempSum += it.main.minTemperature
                if (getWeatherType(it).vitalLevel > weatherType.vitalLevel) {
                    weatherType = getWeatherType(it)
                }
            }
            WeatherData(weatherList.first().time, minTempSum / weatherList.size, maxTempSum / weatherList.size, weatherType,
                    getPrecipProbability(weatherType))
        }
    }

    private fun getPrecipProbability(weatherType: WeatherType): Float {
        return if (weatherType == WeatherType.CLEAR) {
            0f
        } else {
            100f
        }
    }

    private fun getWeatherDataForCurrentDay(date: Date): List<Data> {
        val weatherDataList = mutableListOf<Data>()
        list.forEach {
            if (Utils.isTimestampsFromOneDay(it.time, Utils.timeInSeconds(date.time))) {
                weatherDataList.add(it)
            }
        }
        return weatherDataList
    }

    private fun getWeatherType(data: Data): WeatherType {
        var weatherType = WeatherType.CLEAR
        if (data.weather.isNotEmpty()) {
            when (data.weather.first().id) {
                in 200..232 -> weatherType = WeatherType.THUNDERSTORM
                in 500..531 -> weatherType = WeatherType.RAIN
                in 600..622 -> weatherType = WeatherType.SNOW
                800 -> weatherType = WeatherType.CLEAR
            }
        }
        return weatherType
    }

    private fun getExtendedWeatherData(data: Data): ExtendedWeatherData {
        val temperature = (data.main.minTemperature + data.main.maxTemperature) / 2
        val weatherType = getWeatherType(data)
        return ExtendedWeatherData(data.time, temperature, weatherType,
                data.clouds.all, data.wind.speed, data.main.pressure, data.main.humidity, getPrecipProbability(weatherType))
    }
}


data class Data(@SerializedName("dt") @Expose val time: Long, val main: Main, val weather: List<Weather>, val clouds: Clouds, val wind: Wind)

data class Main(@SerializedName("temp_min") @Expose val minTemperature: Float,
                @SerializedName("temp_max") @Expose val maxTemperature: Float,
                val pressure: Float, val humidity: Float)

data class Weather(val main: String, val id: Int)
data class Clouds(val all: Float)
data class Wind(val speed: Float)