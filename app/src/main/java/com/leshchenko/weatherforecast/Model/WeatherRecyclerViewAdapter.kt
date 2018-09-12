package com.leshchenko.weatherforecast.Model

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherData
import com.leshchenko.weatherforecast.R
import com.leshchenko.weatherforecast.Utils.setImageByWeatherType
import java.util.*


class WeatherRecyclerViewAdapter(private val weatherList: List<WeatherData>, private val itemClick: (time: Long) -> Unit) : RecyclerView.Adapter<WeatherViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.weather_list_item, parent, false)
        return WeatherViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weather = weatherList[position]
        holder.itemView.setOnClickListener { itemClick(weather.time) }
        with(holder) {
            dateTextView.text = Date(weather.time * 1000).toString()
            weatherImage.setImageByWeatherType(weather.weatherType)
            minTemperatureTextView.text = "${weather.minTemp} C"
            maxTemperatureTextView.text = "${weather.maxTemp} C"
        }
    }
}

class WeatherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val dateTextView = view.findViewById<TextView>(R.id.weatherDate)
    val weatherImage = view.findViewById<ImageView>(R.id.weatherImage)
    val minTemperatureTextView = view.findViewById<TextView>(R.id.minTemperature)
    val maxTemperatureTextView = view.findViewById<TextView>(R.id.maxTemperature)
}