package com.leshchenko.weatherforecast.View

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.leshchenko.weatherforecast.R

class DetailsActivity : AppCompatActivity() {
    companion object {
        const val DATE_KEY = "date_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
    }
}
