package com.leshchenko.weatherforecast.Utils

import android.media.Image
import android.widget.ImageView
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherType
import com.leshchenko.weatherforecast.R

fun ImageView.setImageByWeatherType(weatherType: WeatherType) {
    when (weatherType) {
        WeatherType.CLEAR -> setImageResource(R.drawable.ic_sun)
        WeatherType.THUNDERSTORM -> setImageResource(R.drawable.ic_thunderstorm)
        WeatherType.RAIN -> setImageResource(R.drawable.ic_rain)
        WeatherType.SNOW -> setImageResource(R.drawable.ic_snow)
    }
}