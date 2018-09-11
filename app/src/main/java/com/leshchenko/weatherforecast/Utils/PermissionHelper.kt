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
import android.support.v7.app.AlertDialog
import android.view.View
import com.leshchenko.weatherforecast.R


class PermissionHelper {
    companion object {

        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val LOCATION_PERMISSION = android.Manifest.permission.ACCESS_COARSE_LOCATION
        const val FINE_LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION

        fun isLocationPermissionGranted(context: Context) =
                ContextCompat.checkSelfPermission(context, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, FINE_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED


        fun requestLocationPermission(activity: Activity) {
            ActivityCompat.requestPermissions(activity, arrayOf(LOCATION_PERMISSION, FINE_LOCATION_PERMISSION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        /**
         * @return true - if permission is granted, false - otherwise.
         */
        fun isPermissionGranted(grantResults: IntArray): Boolean {
            return grantResults[0] == PackageManager.PERMISSION_GRANTED
        }

        fun displayExplanatorySnackBar(view: View, @StringRes message: Int, activity: Activity) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, LOCATION_PERMISSION)) {
                Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok) {
                    requestLocationPermission(activity)
                }.show()
            } else {
                displayLocationPermissionExplanatoryDialog(activity)
            }
        }

        fun displayDeviceLocationExplanatoryDialog(activity: Activity, okButtonClick: () -> Unit) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.pay_attention)
                    .setMessage(R.string.turn_on_device_location)
                    .setPositiveButton(android.R.string.yes) { dialog, _ ->
                        dialog.dismiss()
                        okButtonClick()
                    }
                    .setNegativeButton(R.string.dont_want_use_app) { dialog, _ ->
                        dialog.dismiss()
                        System.exit(0)
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
        }

        private fun displayLocationPermissionExplanatoryDialog(activity: Activity) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.pay_attention)
                    .setMessage(R.string.location_permission_explanatory_text)
                    .setPositiveButton(android.R.string.yes) { dialog, _ ->
                        dialog.dismiss()
                        openPermissionsScreen(activity, LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    .setNegativeButton(R.string.dont_want_use_app) { dialog, _ ->
                        dialog.dismiss()
                        System.exit(0)
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
        }

        private fun openPermissionsScreen(activity: Activity, requestCode: Int) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivityForResult(intent, requestCode)
        }
    }
}