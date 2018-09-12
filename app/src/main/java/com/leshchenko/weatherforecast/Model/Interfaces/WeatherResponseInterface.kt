package com.leshchenko.weatherforecast.Model.Interfaces

import java.util.*

interface WeatherResponseInterface {
    fun getWeatherForCurrentDay(date: Date): WeatherData
    fun getExtendedWeatherForCurrentDay(date: Date): List<ExtendedWeatherData>
}

data class ExtendedWeatherData(
        val time: Long,
        val temperature: Float,
        val weatherType: WeatherType,
        val cloudiness: Float,
        val windSpeed: Float,
        val pressure: Float,
        val humidity: Float,
        val precipProbability: Float)

data class WeatherData(val time: Long, val minTemp: Float, val maxTemp: Float, val weatherType: WeatherType, val precipProbability: Float)

enum class WeatherType(val vitalLevel: Int) {
    RAIN(1),
    SNOW(1),
    CLEAR(0),
    THUNDERSTORM(2)
}