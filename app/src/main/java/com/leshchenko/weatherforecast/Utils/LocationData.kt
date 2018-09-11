package com.leshchenko.weatherforecast.Utils

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Looper
import android.support.annotation.RequiresPermission
import android.support.v4.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.leshchenko.weatherforecast.Model.Interfaces.LocationResultCallback
import java.util.concurrent.TimeUnit

class LocationData @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])

constructor(private val activity: Activity,
            private val locationResultCallback: LocationResultCallback) {

    companion object {
        const val REQUEST_CHECK_SETTINGS = 100
    }

    private var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback: LocationCallback

    private var lastLocation: Location? = null

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        if (checkLocationPermissionsGranted(activity)) {
            fetchLastKnownLocation()
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                lastLocation = locationResult?.lastLocation

                locationResultCallback.transmitLocation(lastLocation)
                if (lastLocation != null) {
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
    }

    private fun checkLocationPermissionsGranted(activity: Activity) =
            ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED

    fun getLocation() {
        require(checkLocationPermissionsGranted(activity))
        if (lastLocation != null) {
            locationResultCallback.transmitLocation(lastLocation)
        }

        getCurrentLocation()
    }

    fun onDisable() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            it?.let {
                lastLocation = it
                locationResultCallback.transmitLocation(lastLocation)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val locationRequest = createLocationRequest()
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { _ ->
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }

        task.addOnFailureListener { exception ->
            when (exception) {
                is ResolvableApiException -> {
                    locationResultCallback.resolvableApiExceptionHappened(exception)
                }
                else -> {
                    locationResultCallback.transmitLocation(null)
                }
            }
        }
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest().apply {
            interval = TimeUnit.SECONDS.toMillis(5)
            numUpdates = 1
            maxWaitTime = TimeUnit.SECONDS.toMillis(5)
            fastestInterval = TimeUnit.SECONDS.toMillis(3)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}