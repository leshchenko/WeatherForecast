package com.leshchenko.weatherforecast.model.interfaces

import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException

interface LocationResultCallback {
    fun transmitLocation(location: Location?)
    fun resolvableApiExceptionHappened(exception: ResolvableApiException)
}