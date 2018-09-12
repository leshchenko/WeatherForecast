package com.leshchenko.weatherforecast.View

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.leshchenko.weatherforecast.Model.adapters.ExtendedWeatherRecyclerAdapter
import com.leshchenko.weatherforecast.R
import com.leshchenko.weatherforecast.ViewModel.DetailsActivityViewModel
import com.leshchenko.weatherforecast.databinding.ActivityDetailsBinding
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : AppCompatActivity() {
    companion object {
        const val DATE_KEY = "date_key"
        const val LATITUDE_KEY = "latitude_key"
        const val LONGITUDE_KEY = "longitude_key"
    }

    val viewModel by lazy {
        ViewModelProviders.of(this).get(DetailsActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityDetailsBinding = ActivityDetailsBinding.inflate(layoutInflater)
        activityDetailsBinding.viewModel = viewModel
        setContentView(activityDetailsBinding.root)
        viewModel.displayWeatherEvent.observe(this, Observer {
            weatherRecyclerView.layoutManager = LinearLayoutManager(baseContext)
            weatherRecyclerView.adapter = ExtendedWeatherRecyclerAdapter(viewModel.weatherForecast)
            weatherRecyclerView.adapter.notifyDataSetChanged()
        })
        viewModel.time = intent.getLongExtra(DATE_KEY, System.currentTimeMillis())
        viewModel.latitude = intent.getDoubleExtra(LATITUDE_KEY, 0.0)
        viewModel.longitude = intent.getDoubleExtra(LONGITUDE_KEY, 0.0)
        viewModel.requestWeather()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
