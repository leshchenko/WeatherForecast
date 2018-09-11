package com.leshchenko.weatherforecast.ViewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.location.Location
import android.support.annotation.StringRes
import android.util.Log
import android.view.View
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherResponseInterface
import com.leshchenko.weatherforecast.R
import com.leshchenko.weatherforecast.Utils.RetrofitHelper
import com.leshchenko.weatherforecast.Utils.SingleLiveEvent
import kotlinx.coroutines.experimental.launch
import retrofit2.Response
import java.util.*

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    var shouldRequestLocationPermission = true
    var resolvableApiExceptionHappened = false
    var progressBarGroupVisibility: ObservableInt = ObservableInt(View.GONE)
    var progressBarText: ObservableField<String> = ObservableField()

    var explanationGroupVisibility: ObservableInt = ObservableInt(View.VISIBLE)
    var explanationText: ObservableField<String> = ObservableField(getString(R.string.location_permission_explanatory_text))

    var requestLocationPermissionEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    var requestLocationEvent: SingleLiveEvent<Any> = SingleLiveEvent()

    var currentLocation: Location? = null
        set(value) {
            progressBarText.set(getString(R.string.requesting_weather))
        }


    private fun hideExplanationGroup() {
        explanationGroupVisibility.set(View.GONE)
    }

    fun displayDeviceLocationExplanation() {
        hideProgressBar()
        shouldRequestLocationPermission = false
        explanationGroupVisibility.set(View.VISIBLE)
        explanationText.set(getString(R.string.turn_on_device_location))
    }

    fun displayLocationPermissionExplanation() {
        hideProgressBar()
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
        if (currentLocation != null) {
            progressBarText.set(getString(R.string.requesting_weather))
        } else {
            progressBarText.set(getString(R.string.defining_location))
        }
        progressBarGroupVisibility.set(View.VISIBLE)
    }

    fun hideProgressBar() {
        progressBarGroupVisibility.set(View.GONE)
    }

    fun requestWeather() {
        launch {
            deliverResult(arrayListOf<Any>(RetrofitHelper.requestOpenWeatherForecast(42f, 39f)))
        }
    }

    fun deliverResult(result: List<Any>) {
        val weatherList = mutableListOf<WeatherData>()
        result.forEach {
            if (isResponseSuccessful(it)) {
                weatherList.add(getWeatherData(it))
            } else {
                TODO()
            }
        }
        weatherList.forEach {
            Log.d("zlo", "Min temperature ${it.minTemp} \n max temperature ${it.maxTemp}")
        }
//        Log.d("zlo", "${result.first().body()?.list}")
//        Log.d("zlo", "${result1.substring(0, 10)}\n${result2.substring(0, 10)}")
    }

    private fun getWeatherData(item: Any): WeatherData {
        with(item as Response<*>) {
            if (item.body() is WeatherResponseInterface) {
                return (item.body() as WeatherResponseInterface).getAverageWeatherForCurrentDay(Date(System.currentTimeMillis() / 1000))
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