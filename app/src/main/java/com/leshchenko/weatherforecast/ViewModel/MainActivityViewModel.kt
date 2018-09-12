package com.leshchenko.weatherforecast.ViewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.location.Location
import android.view.View
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherResponseInterface
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherType
import com.leshchenko.weatherforecast.R
import com.leshchenko.weatherforecast.Utils.WeatherRepository
import com.leshchenko.weatherforecast.Utils.SingleLiveEvent
import com.leshchenko.weatherforecast.Utils.Utils
import com.leshchenko.weatherforecast.Utils.getString
import kotlinx.coroutines.experimental.launch
import retrofit2.Response
import java.util.*

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private var shouldRequestLocationPermission = true
    private var shouldRetry = false

    var resolvableApiExceptionHappened = false

    var progressBarGroupVisibility: ObservableInt = ObservableInt(View.GONE)
    var progressBarText: ObservableField<String> = ObservableField()

    var explanationGroupVisibility: ObservableInt = ObservableInt(View.VISIBLE)
    var fulfilButtonVisibility: ObservableInt = ObservableInt(View.VISIBLE)
    var explanationText: ObservableField<String> = ObservableField(getString(R.string.location_permission_explanatory_text))

    private var weatherVisibility: ObservableInt = ObservableInt(View.GONE)

    var requestLocationPermissionEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    var requestLocationEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    var displayWeatherEvent: SingleLiveEvent<Any> = SingleLiveEvent()

    var currentLocation: Location? = null
    var weatherForecast: MutableList<WeatherData> = mutableListOf()

    private var daysTimestamps: List<Long> = Utils.getDaysTimestampsForForecast()


    private fun displayNetworkError() {
        hideProgressBar()
        hideWeather()
        shouldRetry = true
        shouldRequestLocationPermission = false
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

    fun setLocation(location: Location?) {
        currentLocation ?: kotlin.run {
            currentLocation = location
            progressBarText.set(getString(R.string.requesting_weather))
            requestWeather()
        }
    }

    private fun hideExplanationGroup() {
        explanationGroupVisibility.set(View.GONE)
        fulfilButtonVisibility.set(View.GONE)
    }

    private fun displayErrorExplanation() {
        hideProgressBar()
        hideWeather()
        shouldRequestLocationPermission = false
        shouldRetry = true
        explanationText.set(getString(R.string.error_explanation))
        fulfilButtonVisibility.set(View.VISIBLE)
        explanationGroupVisibility.set(View.VISIBLE)

    }

    fun displayDeviceLocationExplanation() {
        hideProgressBar()
        hideWeather()
        shouldRetry = false
        shouldRequestLocationPermission = false
        fulfilButtonVisibility.set(View.VISIBLE)
        explanationGroupVisibility.set(View.VISIBLE)
        explanationText.set(getString(R.string.turn_on_device_location))
    }

    fun displayLocationPermissionExplanation() {
        hideProgressBar()
        hideWeather()
        shouldRetry = false
        shouldRequestLocationPermission = true
        explanationGroupVisibility.set(View.VISIBLE)
        fulfilButtonVisibility.set(View.VISIBLE)
        explanationText.set(getString(R.string.location_permission_explanatory_text))
    }

    private fun displayNoWeather() {
        hideWeather()
        hideProgressBar()
        explanationText.set(getString(R.string.no_weather))
        fulfilButtonVisibility.set(View.GONE)
        explanationGroupVisibility.set(View.VISIBLE)
    }

    fun fulfilWishButtonClick() {
        when {
            shouldRequestLocationPermission -> requestLocationPermissionEvent.call()
            shouldRetry -> requestWeather()
            else -> requestLocationEvent.call()
        }
    }

    fun displayProgressBar() {
        hideExplanationGroup()
        hideWeather()
        if (currentLocation != null) {
            progressBarText.set(getString(R.string.requesting_weather))
        } else {
            progressBarText.set(getString(R.string.defining_location))
        }
        progressBarGroupVisibility.set(View.VISIBLE)
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
                    currentLocation ?: requestLocationEvent.postValue(Any())
                    currentLocation?.let {
                        displayProgressBar()
                        val requestAccuWeatherLocation = WeatherRepository.requestLocationForAccuWeather(it.longitude, it.latitude)
                        if (requestAccuWeatherLocation.isSuccessful && requestAccuWeatherLocation.body() != null) {
                            val requestOpenWeatherForecast = WeatherRepository.requestOpenWeatherForecast(it.longitude, it.latitude)
                            val requestDarkSkyForecast = WeatherRepository.requestDarkSkyForecast(it.longitude, it.latitude)
                            val requestAccuWeatherForecast = WeatherRepository.requestAccuWeatherForecast(requestAccuWeatherLocation.body()!!.Key)
                            deliverResult(arrayListOf<Any>(requestOpenWeatherForecast, requestDarkSkyForecast, requestAccuWeatherForecast))
                        }
                    }
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
        val weatherList = mutableListOf<WeatherData>()
        // Define variables for calculating needed data
        var minTemperature = Float.POSITIVE_INFINITY
        var maxTemperature = Float.NEGATIVE_INFINITY
        var weatherType = WeatherType.CLEAR
        var precipProbability = 0f
        var countOfRepeat = 0
        // Go through all days in the loop
        for (i in 0 until daysTimestamps.size) {
            weatherForecast.forEach {
                val secondTimestampInSec = Utils.timeInSeconds(daysTimestamps[i])
                // Calculate needed data for each day
                if (Utils.isTimestampsFromOneDay(it.time, secondTimestampInSec)) {
                    if (it.maxTemp > maxTemperature) {
                        maxTemperature = it.maxTemp
                    }
                    if (it.minTemp < minTemperature) {
                        minTemperature = it.minTemp
                    }
                    if (it.weatherType.vitalLevel > weatherType.vitalLevel) {
                        weatherType = it.weatherType
                    }
                    precipProbability += it.precipProbability
                    countOfRepeat++
                }
            }

            weatherList.add(WeatherData(Utils.timeInSeconds(daysTimestamps[i]), minTemperature, maxTemperature, weatherType,
                    precipProbability / countOfRepeat))
            // Reset variables
            minTemperature = Float.POSITIVE_INFINITY
            maxTemperature = Float.NEGATIVE_INFINITY
            countOfRepeat = 0
            precipProbability = 0f
            weatherType = WeatherType.CLEAR
        }
        weatherForecast.clear()
        weatherForecast.addAll(weatherList)
    }

    private fun getWeatherData(item: Any): List<WeatherData> {
        with(item as Response<*>) {
            if (item.body() is WeatherResponseInterface) {
                val weatherData = mutableListOf<WeatherData>()
                daysTimestamps.forEach {
                    (item.body() as WeatherResponseInterface).getWeatherForCurrentDay(Date(it))?.let { weatherData.add(it) }
                }
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