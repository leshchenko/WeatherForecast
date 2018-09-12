package com.leshchenko.weatherforecast.View

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.leshchenko.weatherforecast.Model.Interfaces.LocationResultCallback
import com.leshchenko.weatherforecast.Model.adapters.WeatherRecyclerViewAdapter
import com.leshchenko.weatherforecast.R
import com.leshchenko.weatherforecast.Utils.LocationData
import com.leshchenko.weatherforecast.Utils.PermissionHelper
import com.leshchenko.weatherforecast.ViewModel.MainActivityViewModel
import com.leshchenko.weatherforecast.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), LocationResultCallback {
    override fun resolvableApiExceptionHappened(exception: ResolvableApiException) {
        viewModel.resolvableApiExceptionHappened = true
        exception.startResolutionForResult(this, LocationData.REQUEST_CHECK_SETTINGS)
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    }

    override fun transmitLocation(location: Location?) {
        viewModel.setLocation(location)
        locationData.onDisable()
    }

    val locationData: LocationData  by lazy {
        LocationData(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        setContentView(binding.root)
        viewModel.requestLocationPermissionEvent.observe(this, Observer {
            PermissionHelper.requestLocationPermission(this)
        })
        viewModel.requestLocationEvent.observe(this, Observer {
            requestLocation()
        })
        viewModel.displayWeatherEvent.observe(this, Observer {
            displayWeather()
        })
        checkLocationPermission()
    }

    private fun displayWeather() {
        weatherRecyclerView.layoutManager = LinearLayoutManager(baseContext)
        weatherRecyclerView.adapter = WeatherRecyclerViewAdapter(viewModel.weatherForecast, this::weatherItemClick)
        weatherRecyclerView.adapter.notifyDataSetChanged()
    }

    private fun weatherItemClick(time: Long) {
        val intent = Intent(baseContext, DetailsActivity::class.java)
        intent.putExtra(DetailsActivity.DATE_KEY, time)
        intent.putExtra(DetailsActivity.LONGITUDE_KEY, viewModel.currentLocation?.longitude)
        intent.putExtra(DetailsActivity.LATITUDE_KEY, viewModel.currentLocation?.latitude)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionHelper.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (PermissionHelper.isPermissionGranted(grantResults)) {
                    requestLocation()
                } else {
                    PermissionHelper.displayExplanatorySnackBar(findViewById(android.R.id.content),
                            R.string.location_permission_snackbar_text, this)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PermissionHelper.LOCATION_PERMISSION_REQUEST_CODE -> {
                checkLocationPermission()
            }
            LocationData.REQUEST_CHECK_SETTINGS -> {
                viewModel.resolvableApiExceptionHappened = false
                if (resultCode == Activity.RESULT_CANCELED) {
                    viewModel.displayDeviceLocationExplanation()
                } else {
                    requestLocation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationData.onDisable()
    }

    private fun requestLocation() {
        if (viewModel.resolvableApiExceptionHappened) {
            return
        }
        if (viewModel.currentLocation == null) {
            viewModel.displayProgressBar()
            locationData.getLocation()
        } else {
            viewModel.requestWeather()
        }
    }

    private fun checkLocationPermission() {
        if (PermissionHelper.isLocationPermissionGranted(baseContext)) {
            requestLocation()
        } else {
            viewModel.displayLocationPermissionExplanation()
        }
    }
}