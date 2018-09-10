package com.leshchenko.weatherforecast.Model.Interfaces

import android.location.Location

interface LocationResultCallback {
    fun transmitLocation(location: Location?)
}