package com.leshchenko.weatherforecast.View

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.leshchenko.weatherforecast.Model.Interfaces.LocationResultCallback
import com.leshchenko.weatherforecast.R
import com.leshchenko.weatherforecast.Utils.LocationData
import com.leshchenko.weatherforecast.Utils.PermissionHelper
import com.leshchenko.weatherforecast.ViewModel.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), LocationResultCallback {
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    }

    override fun transmitLocation(location: Location?) {
        location ?: return
        dataText.text = "Longitude => ${location.longitude},  latitude => ${location.latitude}"
    }

    private val TAG = "MainActivity"
    val locationData: LocationData  by lazy {
        LocationData(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkLocationPermission()
        getData.setOnClickListener {
            viewModel.requestWeather()
        }
    }

    private fun checkLocationPermission() {
        if (PermissionHelper.isLocationPermissionGranted(baseContext)) {
            locationData.getLocation()
        } else {
            PermissionHelper.requestLocationPermission(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionHelper.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (PermissionHelper.isPermissionGranted(grantResults)) {
                    locationData.getLocation()
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
                if (resultCode == Activity.RESULT_CANCELED) {
                    PermissionHelper.displayDeviceLocationExplanatoryDialog(this) { locationData.getLocation() }
                } else {
                    locationData.getLocation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationData.onDisable()
    }
}