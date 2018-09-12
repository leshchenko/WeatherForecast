package com.leshchenko.weatherforecast.Utils

import com.leshchenko.weatherforecast.Model.responses.AccuWeatherLocationResponse
import com.leshchenko.weatherforecast.Model.responses.AccuWeatherResponse
import com.leshchenko.weatherforecast.Model.responses.DarkSkyResponse
import com.leshchenko.weatherforecast.Model.responses.OpenWeatherResponse
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class WeatherRepository {
    companion object {
        private const val OPEN_WEATHER_API_ENDPOINT = "https://api.openweathermap.org/data/2.5/"
        const val OPEN_WEATHER_API_KEY = "210475a62f8486190ea75daac2348be9"

        private const val DARK_SKY_API_ENDPOINT = "https://api.darksky.net/forecast/"
        private const val DARK_SKY_API_KEY = "e75a20d2cab932ccf5eaeebe69fd2b33"


        private const val ACCU_WEATHER_FORECAST_ENDPONT = "http://dataservice.accuweather.com/forecasts/v1/daily/5day/"
        private const val ACCU_WEATHER_LOCATION_ENDPOINT = "http://dataservice.accuweather.com/locations/v1/cities/geoposition/search"
        private const val ACCU_WEATHER_API_KEY = "yG0Tt2h8KyUlbOGF9OyXNjYUcefTyHrF"

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

        fun requestDarkSkyForecastForDay(longitude: Double, latitude: Double, time: Long): Response<DarkSkyResponse> {
            val url = generateDarkSkyForecastUrlForDay(longitude, latitude, time)
            return retrofit.create(WeatherService::class.java).getDarkSkyForecast(url).execute()
        }

        // From the DarkSky doc, url should follow the next template -  https://api.darksky.net/forecast/[key]/[latitude],[longitude],[time]
        private fun generateDarkSkyForecastUrlForDay(longitude: Double, latitude: Double, time: Long): String {
            return "$DARK_SKY_API_ENDPOINT$DARK_SKY_API_KEY/$latitude,$longitude,$time"
        }

        // From the DarkSky doc, url should follow the next template -  https://api.darksky.net/forecast/[key]/[latitude],[longitude]
        private fun generateDarkSkyForecastUrl(longitude: Double, latitude: Double): String {
            return "$DARK_SKY_API_ENDPOINT$DARK_SKY_API_KEY/$latitude,$longitude"
        }

        fun requestAccuWeatherForecast(locationKey: String): Response<AccuWeatherResponse> {
            return retrofit.create(WeatherService::class.java).getAccuWeatherForecast(generateAccuWeatherForecastUrl(locationKey)).execute()
        }

        //From AccuWeather doc, url should follow the next template -  http://dataservice.accuweather.com/forecasts/v1/daily/5day/[locationKey]?[apikey]&[details]&[metric]
        private fun generateAccuWeatherForecastUrl(locationKey: String): String {
            return "$ACCU_WEATHER_FORECAST_ENDPONT$locationKey?apikey=$ACCU_WEATHER_API_KEY&details=true&metric=true"
        }

        fun requestLocationForAccuWeather(longitude: Double, latitude: Double): Response<AccuWeatherLocationResponse> {
            return retrofit.create(WeatherService::class.java).getAccuWeatherLocationKey(generateAccuWeatherLocationUrl(longitude, latitude)).execute()
        }

        //From AccuWeather doc, url should follow the next template -  http://dataservice.accuweather.com/locations/v1/cities/geoposition/search?[apikey],[q=lat,lot]
        private fun generateAccuWeatherLocationUrl(longitude: Double, latitude: Double): String {
            return "$ACCU_WEATHER_LOCATION_ENDPOINT?apikey=$ACCU_WEATHER_API_KEY&q=$latitude,$longitude"
        }
    }
}