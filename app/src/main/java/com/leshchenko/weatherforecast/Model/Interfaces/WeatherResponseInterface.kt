package com.leshchenko.weatherforecast.Model.Interfaces

import java.util.*

interface WeatherResponseInterface {
    fun getAverageWeatherForCurrentDay(date: Date): WeatherData
    fun getExtendedWeatherForCurrentDay(date: Date): List<ExtendedWeatherData>
}

data class ExtendedWeatherData(
        val time: Long,
        val minTemp: Float,
        val maxTemp: Float,
        val weatherType: WeatherType,
        val cloudiness: Float,
        val windSpeed: Float,
        val pressure: Float,
        val humidity: Float)

data class WeatherData(val minTemp: Float, val maxTemp: Float, val weatherType: WeatherType)

enum class WeatherType(val vitalLevel: Int) {
    RAIN(2),
    SNOW(2),
    CLEAR(0),
    CLOUDS(1),
    THUNDERSTORM(3)
}