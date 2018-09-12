package com.leshchenko.weatherforecast.Utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View


class PermissionHelper {
    companion object {

        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val LOCATION_PERMISSION = android.Manifest.permission.ACCESS_COARSE_LOCATION
        private const val FINE_LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION

        fun isLocationPermissionGranted(context: Context) =
                ContextCompat.checkSelfPermission(context, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, FINE_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED


        fun requestLocationPermission(activity: Activity) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, LOCATION_PERMISSION)) {
                ActivityCompat.requestPermissions(activity, arrayOf(LOCATION_PERMISSION, FINE_LOCATION_PERMISSION), LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                openPermissionsScreen(activity)
            }

        }

        /**
         * @return true - if permission is granted, false - otherwise.
         */
        fun isPermissionGranted(grantResults: IntArray): Boolean {
            grantResults.forEach {
                if (it == PackageManager.PERMISSION_DENIED) {
                    return false
                }
            }
            return true
        }

        fun displayExplanatorySnackBar(view: View, @StringRes message: Int, activity: Activity) {
            Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok) {
                requestLocationPermission(activity)
            }.show()
        }

        private fun openPermissionsScreen(activity: Activity) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivityForResult(intent, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }
}