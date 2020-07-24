package com.tatsuo.bodytemperature


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tatsuo.bodytemperature.TemperatureListFragment.OnListFragmentInteractionListener
import com.tatsuo.bodytemperature.db.Temperature
import com.tatsuo.bodytemperature.dummy.DummyContent.DummyItem
import kotlinx.android.synthetic.main.listitem_temperature.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class TemperatureListViewAdapter(
        private val mValues: List<Temperature>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<TemperatureListViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Temperature
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.listitem_temperature, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val temperature = mValues[position]

        val dateFormat = SimpleDateFormat(getDateFormatString())
        holder.mDateString.text = dateFormat.format(temperature.date)

        if(ConfigManager.loadUseFahrenheitFlag()){
            holder.mTemperatureString.text = String.format("%.1f", temperature.getFahrenheitTemperature()) + "°F"
            if (temperature.getFahrenheitTemperature() >= 100.0) {
                holder.mTemperatureString.setTextColor(Color.RED)
            } else {
                holder.mTemperatureString.setTextColor(Color.BLACK)
            }
        } else {
            holder.mTemperatureString.text = String.format("%.1f", temperature.temperature) + "℃"
            if (temperature.temperature >= 38.0) {
                holder.mTemperatureString.setTextColor(Color.RED)
            } else {
                holder.mTemperatureString.setTextColor(Color.BLACK)
            }
        }
        holder.mConditionMemoString.text = (temperature.getConditionString() + " " + temperature.memo).trim()

        with(holder.mView) {
            tag = temperature
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mDateString: TextView = mView.dateString
        val mTemperatureString: TextView = mView.temperatureString
        val mConditionMemoString: TextView = mView.conditionMemoString

        override fun toString(): String {
            return super.toString() // + " '" + mContentView.text + "'"
        }
    }
}
