package com.leshchenko.weatherforecast.Model.Interfaces

import com.leshchenko.weatherforecast.Model.DarkSkyResponse
import com.leshchenko.weatherforecast.Model.OpenWeatherResponse
import com.leshchenko.weatherforecast.Utils.RetrofitHelper
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


interface WeatherService {
    @GET("forecast")
    fun getOpenWeatherForecast(@Query("lat") latitude: Double,
                               @Query("lon") longitude: Double,
                               @Query("appid") apiKey: String = RetrofitHelper.OPEN_WEATHER_API_KEY,
                               @Query("units") units: String = "metric"): Call<OpenWeatherResponse>

    @GET
    fun getDarkSkyForecast(@Url url: String,
                           @Query("exclude") excludedData: String = "minutely,currently",
                           @Query("units") units: String = "si"): Call<DarkSkyResponse>
}