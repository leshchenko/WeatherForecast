package com.leshchenko.weatherforecast.view

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.leshchenko.weatherforecast.model.interfaces.LocationResultCallback
import com.leshchenko.weatherforecast.model.adapters.WeatherRecyclerViewAdapter
import com.leshchenko.weatherforecast.R
import com.leshchenko.weatherforecast.utils.LocationData
import com.leshchenko.weatherforecast.utils.PermissionHelper
import com.leshchenko.weatherforecast.viewmodel.MainActivityViewModel
import com.leshchenko.weatherforecast.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), LocationResultCallback {

    private val weatherAdapter by lazy {
        WeatherRecyclerViewAdapter(viewModel.weatherForecast, this::weatherItemClick)
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    }

    private val locationData: LocationData  by lazy {
        LocationData(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        binding.weatherRecyclerView.layoutManager = LinearLayoutManager(baseContext)
        binding.weatherRecyclerView.adapter = weatherAdapter
        binding.viewModel = viewModel
        setContentView(binding.root)
        addObservers()
        checkLocationPermission()
    }

    override fun resolvableApiExceptionHappened(exception: ResolvableApiException) {
        viewModel.resolvableApiExceptionHappened = true
        exception.startResolutionForResult(this, LocationData.REQUEST_CHECK_SETTINGS)
    }

    override fun transmitLocation(location: Location?) {
        viewModel.setLocation(location)
        locationData.onDisable()
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

    private fun addObservers() {
        viewModel.requestLocationPermissionEvent.observe(this, Observer {
            PermissionHelper.requestLocationPermission(this)
        })
        viewModel.requestLocationEvent.observe(this, Observer {
            requestLocation()
        })
        viewModel.displayWeatherEvent.observe(this, Observer {
            weatherAdapter.notifyDataSetChanged()
        })
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

    private fun weatherItemClick(time: Long) {
        val intent = Intent(baseContext, DetailsActivity::class.java)
        intent.putExtra(DetailsActivity.DATE_KEY, time)
        intent.putExtra(DetailsActivity.LONGITUDE_KEY, viewModel.currentLocation?.longitude)
        intent.putExtra(DetailsActivity.LATITUDE_KEY, viewModel.currentLocation?.latitude)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }
}