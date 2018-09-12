package com.leshchenko.weatherforecast.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.leshchenko.weatherforecast.model.interfaces.ExtendedWeatherData
import com.leshchenko.weatherforecast.model.interfaces.WeatherResponseInterface
import com.leshchenko.weatherforecast.model.interfaces.WeatherType
import com.leshchenko.weatherforecast.R
import com.leshchenko.weatherforecast.utils.SingleLiveEvent
import com.leshchenko.weatherforecast.utils.Utils
import com.leshchenko.weatherforecast.utils.WeatherRepository
import com.leshchenko.weatherforecast.utils.getString
import kotlinx.coroutines.experimental.launch
import retrofit2.Response
import java.util.*

class DetailsActivityViewModel(application: Application) : AndroidViewModel(application) {

    var progressBarGroupVisibility: ObservableInt = ObservableInt(View.GONE)
    var progressBarText: ObservableField<String> = ObservableField(getString(R.string.requesting_weather))

    var explanationGroupVisibility: ObservableInt = ObservableInt(View.VISIBLE)
    var fulfilButtonVisibility: ObservableInt = ObservableInt(View.VISIBLE)
    var explanationText: ObservableField<String> = ObservableField()

    private var weatherVisibility: ObservableInt = ObservableInt(View.GONE)

    var displayWeatherEvent = SingleLiveEvent<Any>()

    var weatherForecast = mutableListOf<ExtendedWeatherData>()

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var time = 0L

    private val hourTimestamps: List<Long> = Utils.getHourTimestampsForForecast()

    private fun displayNetworkError() {
        hideProgressBar()
        hideWeather()
        explanationText.set(getString(R.string.network_error))
        explanationGroupVisibility.set(View.VISIBLE)
        fulfilButtonVisibility.set(View.VISIBLE)
    }

    private fun displayWeather() {
        hideProgressBar()
        hideExplanationGroup()
        weatherVisibility.set(View.VISIBLE)
        displayWeatherEvent.postValue(Any())
    }

    private fun hideWeather() {
        weatherVisibility.set(View.GONE)
    }

    private fun hideExplanationGroup() {
        explanationGroupVisibility.set(View.GONE)
        fulfilButtonVisibility.set(View.GONE)
    }

    private fun displayErrorExplanation() {
        hideProgressBar()
        hideWeather()
        explanationText.set(getString(R.string.error_explanation))
        explanationGroupVisibility.set(View.VISIBLE)
        fulfilButtonVisibility.set(View.VISIBLE)

    }

    private fun displayNoWeather() {
        hideWeather()
        hideProgressBar()
        explanationText.set(getString(R.string.no_weather))
        fulfilButtonVisibility.set(View.GONE)
        explanationGroupVisibility.set(View.VISIBLE)
    }

    private fun displayProgressBar() {
        hideExplanationGroup()
        hideWeather()
        progressBarGroupVisibility.set(View.VISIBLE)
    }

    fun fulfilWishButtonClick() {
        requestWeather()
    }

    private fun hideProgressBar() {
        progressBarGroupVisibility.set(View.GONE)
    }


    fun requestWeather() {
        if (Utils.isOnline(getApplication())) {
            launch {
                if (weatherForecast.isNotEmpty()) {
                    displayWeather()
                } else {
                    displayProgressBar()
                    deliverResult(arrayListOf<Any>(WeatherRepository.requestOpenWeatherForecast(longitude, latitude),
                            WeatherRepository.requestDarkSkyForecastForDay(longitude, latitude, time)))
                }
            }
        } else {
            displayNetworkError()
        }
    }

    private fun deliverResult(result: List<Any>) {
        result.forEach {
            if (isResponseSuccessful(it)) {
                weatherForecast.addAll(getWeatherData(it))
            } else {
                displayErrorExplanation()
            }
        }
        if (weatherForecast.isEmpty()) {
            displayNoWeather()
        } else {
            mergeWeather()
            displayWeather()
        }
    }

    private fun mergeWeather() {
        val weatherList = mutableListOf<ExtendedWeatherData>()
        // Define variables for calculating average value
        var temperature = 0f
        var weatherType: WeatherType = WeatherType.CLEAR
        var cloudiness = 0f
        var windSpeed = 0f
        var pressure = 0f
        var humidity = 0f
        var precipProbability = 0f
        var countOfRepeat = 0
        //Go through each hour in a day and calculate needed data
        for (i in 0 until hourTimestamps.size) {
            weatherForecast.forEach {
                val secondTimestampInSec = Utils.timeInSeconds(hourTimestamps[i])
                //  Add all values from one day
                if (Utils.isTimestampsFromOneHour(it.time, secondTimestampInSec)) {
                    temperature += it.temperature
                    if (it.weatherType.vitalLevel > weatherType.vitalLevel) {
                        weatherType = it.weatherType
                    }
                    cloudiness += it.cloudiness
                    windSpeed += it.windSpeed
                    pressure += it.pressure
                    humidity += it.humidity
                    precipProbability += it.precipProbability
                    countOfRepeat++
                }
            }
            // Add weather data if there is data for current time.
            if (countOfRepeat != 0) {
                val extendedWeatherData = ExtendedWeatherData(Utils.timeInSeconds(hourTimestamps[i]),
                        temperature / countOfRepeat, weatherType, cloudiness / countOfRepeat,
                        windSpeed / countOfRepeat, pressure / countOfRepeat, humidity / countOfRepeat,
                        precipProbability / countOfRepeat)
                weatherList.add(extendedWeatherData)
            }
            // Reset values.
            temperature = 0f
            weatherType = WeatherType.CLEAR
            cloudiness = 0f
            windSpeed = 0f
            pressure = 0f
            humidity = 0f
            precipProbability = 0f
            countOfRepeat = 0
        }
        weatherForecast.clear()
        weatherForecast.addAll(weatherList)
    }

    private fun getWeatherData(item: Any): List<ExtendedWeatherData> {
        with(item as Response<*>) {
            if (item.body() is WeatherResponseInterface) {
                val weatherData = mutableListOf<ExtendedWeatherData>()
                weatherData.addAll((item.body() as WeatherResponseInterface).getExtendedWeatherForCurrentDay(Date(time * 1000)))
                return weatherData
            } else {
                throw IllegalArgumentException("Item should be inherited from \"WeatherResponseInterface\" class")
            }
        }
    }

    private fun isResponseSuccessful(response: Any): Boolean {
        if (response is Response<*>) {
            return response.isSuccessful
        } else {
            throw IllegalArgumentException("Item should be inherited from Retrofit \"Response\" class")
        }
    }
}
