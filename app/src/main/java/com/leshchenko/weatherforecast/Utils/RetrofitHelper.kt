package com.leshchenko.weatherforecast.Utils

import com.leshchenko.weatherforecast.Model.responses.DarkSkyResponse
import com.leshchenko.weatherforecast.Model.responses.OpenWeatherResponse
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitHelper {
    companion object {
        const val OPEN_WEATHER_API_ENDPOINT = "https://api.openweathermap.org/data/2.5/"
        const val OPEN_WEATHER_API_KEY = "210475a62f8486190ea75daac2348be9"

        const val DARK_SKY_API_ENDPOINT = "https://api.darksky.net/forecast/"
        const val DARK_SKY_API_KEY = "e75a20d2cab932ccf5eaeebe69fd2b33"

        private val retrofit by lazy {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
            Retrofit.Builder()
                    .client(client)
                    .baseUrl(OPEN_WEATHER_API_ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

        }

        fun requestOpenWeatherForecast(longitude: Double, latitude: Double): Response<OpenWeatherResponse> =
                retrofit.create(WeatherService::class.java).getOpenWeatherForecast(latitude, longitude).execute()

        fun requestDarkSkyForecast(longitude: Double, latitude: Double): Response<DarkSkyResponse> {
            val url = generateDarkSkyForecastUrl(longitude, latitude)
            return retrofit.create(WeatherService::class.java).getDarkSkyForecast(url).execute()
        }

        // From the DarkSky doc, url should follow the next template -  https://api.darksky.net/forecast/[key]/[latitude],[longitude]
        private fun generateDarkSkyForecastUrl(longitude: Double, latitude: Double): String {
            return "$DARK_SKY_API_ENDPOINT$DARK_SKY_API_KEY/$latitude,$longitude"
        }
    }
}