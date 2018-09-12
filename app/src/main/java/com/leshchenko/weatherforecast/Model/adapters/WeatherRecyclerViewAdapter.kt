package com.leshchenko.weatherforecast.Model.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.leshchenko.weatherforecast.Model.Interfaces.WeatherData
import com.leshchenko.weatherforecast.R
import com.leshchenko.weatherforecast.Utils.setImageByWeatherType
import java.text.DateFormat
import java.util.*


class WeatherRecyclerViewAdapter(private var weatherList: List<WeatherData>, private val itemClick: (time: Long) -> Unit) : RecyclerView.Adapter<WeatherViewHolder>() {
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

            val timeInMillis = weather.time * 1000
            dateTextView?.text = DateFormat.getDateInstance(DateFormat.LONG).format(Date(timeInMillis))
            weatherImage?.setImageByWeatherType(weather.weatherType)
            val context = holder.itemView.context
            minTemperatureTextView?.text = String.format(context.getString(R.string.temperature), weather.minTemp)
            maxTemperatureTextView?.text = String.format(context.getString(R.string.temperature), weather.maxTemp)
            precipProbabilityTextView?.text = String.format(context.getString(R.string.precip_probability), "%.1f".format(weather.precipProbability))
        }
    }
}


class WeatherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val dateTextView: TextView? = view.findViewById(R.id.weatherDate)
    val weatherImage: ImageView? = view.findViewById(R.id.weatherImage)
    val minTemperatureTextView: TextView? = view.findViewById(R.id.minTemperature)
    val maxTemperatureTextView: TextView? = view.findViewById(R.id.maxTemperature)
    val precipProbabilityTextView: TextView? = view.findViewById(R.id.precipProbability)
}