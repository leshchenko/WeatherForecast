package com.leshchenko.weatherforecast.model.interfaces

import com.leshchenko.weatherforecast.model.responses.AccuWeatherLocationResponse
import com.leshchenko.weatherforecast.model.responses.AccuWeatherResponse
import com.leshchenko.weatherforecast.model.responses.DarkSkyResponse
import com.leshchenko.weatherforecast.model.responses.OpenWeatherResponse
import com.leshchenko.weatherforecast.utils.WeatherRepository
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


interface WeatherService {
    @GET("forecast")
    fun getOpenWeatherForecast(@Query("lat") latitude: Double,
                               @Query("lon") longitude: Double,
                               @Query("appid") apiKey: String = WeatherRepository.OPEN_WEATHER_API_KEY,
                               @Query("units") units: String = "metric"): Call<OpenWeatherResponse>

    @GET
    fun getDarkSkyForecast(@Url url: String,
                           @Query("exclude") excludedData: String = "minutely,currently",
                           @Query("units") units: String = "si"): Call<DarkSkyResponse>

    @GET
    fun getAccuWeatherLocationKey(@Url url: String): Call<AccuWeatherLocationResponse>

    @GET
    fun getAccuWeatherForecast(@Url url: String): Call<AccuWeatherResponse>
}