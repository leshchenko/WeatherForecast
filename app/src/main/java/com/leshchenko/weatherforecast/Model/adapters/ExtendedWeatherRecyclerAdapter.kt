package com.leshchenko.weatherforecast.Model.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.leshchenko.weatherforecast.Model.Interfaces.ExtendedWeatherData
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherData
import com.leshchenko.weatherforecast.R
import com.leshchenko.weatherforecast.Utils.setImageByWeatherType
import java.text.DateFormat
import java.util.*


class ExtendedWeatherRecyclerAdapter(private val weatherList: List<ExtendedWeatherData>) : RecyclerView.Adapter<ExtendedWeatherViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtendedWeatherViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.extended_weather_list_item, parent, false)
        return ExtendedWeatherViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }

    override fun onBindViewHolder(holder: ExtendedWeatherViewHolder, position: Int) {
        val weather = weatherList[position]
        with(holder) {
            dateTextView.text = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(weather.time * 1000))
            weatherImage.setImageByWeatherType(weather.weatherType)
            val context = holder.itemView.context
            temperatureTextView.text = String.format(context.getString(R.string.temperature), weather.temperature)
            cloudinessTextView.text = String.format(context.getString(R.string.cloudiness), "%.1f".format(weather.cloudiness))
            windSpeedTextView.text = String.format(holder.itemView.context.getString(R.string.wind_speed), weather.windSpeed)
            pressureTextView.text = String.format(holder.itemView.context.getString(R.string.pressure), weather.pressure)
            humidityTextView.text = String.format(holder.itemView.context.getString(R.string.humidity), "%.1f".format(weather.humidity))
            precipProbabilityTextView.text = String.format(context.getString(R.string.precip_probability), "%.1f".format(weather.precipProbability))
        }
    }
}

class ExtendedWeatherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val dateTextView = view.findViewById<TextView>(R.id.weatherDate)
    val weatherImage = view.findViewById<ImageView>(R.id.weatherImage)
    val temperatureTextView = view.findViewById<TextView>(R.id.temperature)
    val cloudinessTextView = view.findViewById<TextView>(R.id.cloudiness)
    val windSpeedTextView = view.findViewById<TextView>(R.id.windSpeed)
    val pressureTextView = view.findViewById<TextView>(R.id.pressure)
    val humidityTextView = view.findViewById<TextView>(R.id.humidity)
    val precipProbabilityTextView = view.findViewById<TextView>(R.id.precipProbability)
}