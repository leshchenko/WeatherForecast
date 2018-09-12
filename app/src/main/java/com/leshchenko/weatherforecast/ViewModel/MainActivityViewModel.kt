package com.leshchenko.weatherforecast.ViewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.location.Location
import android.support.annotation.StringRes
import android.view.View
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherResponseInterface
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherType
import com.leshchenko.weatherforecast.R
import com.leshchenko.weatherforecast.Utils.RetrofitHelper
import com.leshchenko.weatherforecast.Utils.SingleLiveEvent
import com.leshchenko.weatherforecast.Utils.Utils
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
    var explanationText: ObservableField<String> = ObservableField(getString(R.string.location_permission_explanatory_text))

    var weatherVisibility: ObservableInt = ObservableInt(View.GONE)

    var requestLocationPermissionEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    var requestLocationEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    var displayWeatherEvent: SingleLiveEvent<Any> = SingleLiveEvent()

    var currentLocation: Location? = null
    var daysTimestamps: List<Long> = Utils.getDaysTimestampsForForecast()
    var weatherForecast: MutableList<WeatherData> = mutableListOf()

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
        currentLocation = location
        progressBarText.set(getString(R.string.requesting_weather))
        requestWeather()
    }

    private fun hideExplanationGroup() {
        explanationGroupVisibility.set(View.GONE)
    }

    private fun displayErrorExplanation() {
        hideProgressBar()
        hideWeather()
        shouldRequestLocationPermission = false
        shouldRetry = true
        explanationText.set(getString(R.string.error_explanation))
        explanationGroupVisibility.set(View.VISIBLE)

    }

    fun displayDeviceLocationExplanation() {
        hideProgressBar()
        hideWeather()
        shouldRetry = false
        shouldRequestLocationPermission = false
        explanationGroupVisibility.set(View.VISIBLE)
        explanationText.set(getString(R.string.turn_on_device_location))
    }

    fun displayLocationPermissionExplanation() {
        hideProgressBar()
        hideWeather()
        shouldRetry = false
        shouldRequestLocationPermission = true
        explanationGroupVisibility.set(View.VISIBLE)
        explanationText.set(getString(R.string.location_permission_explanatory_text))
    }

    fun fulfilWishButtonClick() {
        if (shouldRequestLocationPermission) {
            requestLocationPermissionEvent.call()
        } else {
            requestLocationEvent.call()
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
        launch {
            if (weatherForecast.isNotEmpty()) {
                displayWeather()
            } else {
                currentLocation ?: requestLocationEvent.postValue(Any())
                currentLocation?.let {
                    displayProgressBar()

                    deliverResult(arrayListOf<Any>(RetrofitHelper.requestOpenWeatherForecast(it.longitude, it.latitude),
                            RetrofitHelper.requestDarkSkyForecast(it.longitude, it.latitude)))
                }
            }
        }
    }

    fun deliverResult(result: List<Any>) {
        result.forEach {
            if (isResponseSuccessful(it)) {
                weatherForecast.addAll(getWeatherData(it))
                mergeWeather()
                displayWeather()
            } else {
                displayErrorExplanation()
            }
        }
    }

    private fun mergeWeather() {
        val weatherList = mutableListOf<WeatherData>()
        var minTemperature = Float.POSITIVE_INFINITY
        var maxTemperature = Float.NEGATIVE_INFINITY
        var weatherType = WeatherType.CLEAR
        for (i in 0 until daysTimestamps.size) {
            weatherForecast.forEach {
                val secondTimestampInSec = Utils.timeInSeconds(daysTimestamps[i])
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
                }
            }
            weatherList.add(WeatherData(Utils.timeInSeconds(daysTimestamps[i]), minTemperature, maxTemperature, weatherType))
            minTemperature = Float.POSITIVE_INFINITY
            maxTemperature = Float.NEGATIVE_INFINITY
        }
        weatherForecast.clear()
        weatherForecast.addAll(weatherList)
    }

    private fun getWeatherData(item: Any): List<WeatherData> {
        with(item as Response<*>) {
            if (item.body() is WeatherResponseInterface) {
                val weatherData = mutableListOf<WeatherData>()
                daysTimestamps.forEach {
                    weatherData.add((item.body() as WeatherResponseInterface).getWeatherForCurrentDay(Date(it)))
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

    private fun getString(@StringRes resId: Int): String {
        return getApplication<Application>().getString(resId)
    }
}