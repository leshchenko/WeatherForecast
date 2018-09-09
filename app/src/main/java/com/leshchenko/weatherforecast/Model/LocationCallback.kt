package com.leshchenko.weatherforecast.Model

import android.location.Location

interface LocationResultCallback {
    fun transmitLocation(location: Location?)
}