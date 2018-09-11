package com.leshchenko.weatherforecast.Model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.leshchenko.weatherforecast.Model.Interfaces.ExtendedWeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherResponseInterface
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherType
import com.leshchenko.weatherforecast.Utils.Utils
import java.util.*


class OpenWeatherResponse(val list: List<Data>) : WeatherResponseInterface {

    override fun getAverageWeatherForCurrentDay(date: Date): WeatherData {
        return getAverageWeatherData(date)
    }

    override fun getExtendedWeatherForCurrentDay(date: Date): List<ExtendedWeatherData> {
        val extendedWeatherData = mutableListOf<ExtendedWeatherData>()
        getWeatherDataForCurrentDay(date).forEach {
            extendedWeatherData.add(getExtendedWeatherData(it))
        }
        return extendedWeatherData
    }

    private fun getAverageWeatherData(date: Date): WeatherData {

        val weatherList = getWeatherDataForCurrentDay(date)
        var minTempSum = 0f
        var maxTempSum = 0f
        var weatherType = WeatherType.CLEAR
        weatherList.forEach {
            maxTempSum += it.main.maxTemperature
            minTempSum += it.main.minTemperature
            if (getWeatherType(it).vitalLevel > weatherType.vitalLevel) {
                weatherType = getWeatherType(it)
            }
        }
        return WeatherData(minTempSum / weatherList.size, maxTempSum / weatherList.size, weatherType)
    }

    private fun getWeatherDataForCurrentDay(date: Date): List<Data> {
        val weatherDataList = mutableListOf<Data>()
        list.forEach {
            if (Utils.isTimestampsFromOneDay(it.time, date.time)) {
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
                in 801..804 -> weatherType = WeatherType.CLOUDS
                800 -> weatherType = WeatherType.CLEAR
            }
        }
        return weatherType
    }

    private fun getExtendedWeatherData(data: Data): ExtendedWeatherData {
        return ExtendedWeatherData(data.time, data.main.minTemperature, data.main.maxTemperature, getWeatherType(data),
                data.clouds.all, data.wind.speed, data.main.pressure, data.main.humidity)
    }
}


data class Data(@SerializedName("dt") @Expose val time: Long, val main: Main, val weather: List<Weather>, val clouds: Clouds, val wind: Wind)

data class Main(@SerializedName("temp_min") @Expose val minTemperature: Float,
                @SerializedName("temp_max") @Expose val maxTemperature: Float,
                val pressure: Float, val humidity: Float)

data class Weather(val main: String, val id: Int)
data class Clouds(val all: Float)
data class Wind(val speed: Float)