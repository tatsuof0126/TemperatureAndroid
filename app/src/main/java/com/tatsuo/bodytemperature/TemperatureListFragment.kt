package com.tatsuo.bodytemperature

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.tatsuo.bodytemperature.db.Temperature
import com.tatsuo.bodytemperature.db.TemperatureDatabase
import kotlinx.android.synthetic.main.fragment_temperature_list.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [TemperatureListFragment.OnListFragmentInteractionListener] interface.
 */
class TemperatureListFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1

    private var listener: OnListFragmentInteractionListener? = null

    private val temperatureList = mutableListOf<Temperature>()

    private val handler = Handler()
    private var runnable = Runnable {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        database = Room.databaseBuilder(requireActivity().applicationContext, TemperatureDatabase::class.java, "temperature_database.db").build()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_temperature_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
            }
        }

        updateViewAdapter()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val personName = ConfigManager.loadTargetPersonName()
        if(personName != "あなた" && personName != "You"){
            if (Locale.getDefault().equals(Locale.JAPAN)) {
                requireActivity().setTitle(personName+"さん")
            } else {
                requireActivity().setTitle(personName)
            }
        } else {
            requireActivity().setTitle(getString(R.string.title_temperature_list))
        }

        temperatureListView.adapter = TemperatureListViewAdapter(temperatureList, listener)
    }

    override fun onResume() {
        super.onResume()

        // Log.e("***Temperature***","onResume")

        if(ConfigManager.loadUpdatedDataFlag()) {
            ConfigManager.saveUpdatedDataFlag(false)
            // Log.e("***Temperature***","updateViewAdapter")
            updateViewAdapter()
        }

    }

    private fun updateViewAdapter() {
        val dao = database.temperatureDao()
        val myExecutor = Executors.newSingleThreadExecutor()
        myExecutor.execute() {
            while(TemperatureApplication.dbUpdating){
                // Log.e("***Temperature***","dbUpdating...")
            }

            val tempList = dao.getAllTemperatureData(ConfigManager.loadTargetPersonId())

            runnable = Runnable {
                // temperatureListView.adapter = TemperatureListViewAdapter(temperatureList, listener)
                temperatureList.clear()
                temperatureList.addAll(tempList)
                Log.e("***Temperature***","temperatureList size : "+temperatureList.size)

                temperatureListView?.adapter?.notifyDataSetChanged()
                if (temperatureList.size == 0) {
                    temperatureListView?.visibility = View.GONE
                    nodatemessage1?.visibility = View.VISIBLE
                    nodatemessage2?.visibility = View.VISIBLE
                } else {
                    temperatureListView?.visibility = View.VISIBLE
                    nodatemessage1?.visibility = View.GONE
                    nodatemessage2?.visibility = View.GONE
                }
            }
            handler.post(runnable)
        }
    }

    private fun sendTemperatureList(){
        // 送信内容の作成
        val sendBody = StringBuilder()
        sendBody.append(getString(R.string.send_body_title))
        sendBody.append("\n")
        sendBody.append("---\n")
        for(temperature in temperatureList){
            sendBody.append(makeTemperatureString(temperature))
            sendBody.append("\n")
        }

        // Intentで送信
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_subject))
        intent.putExtra(Intent.EXTRA_TEXT, sendBody.toString())

        startActivity(Intent.createChooser(intent, getString(R.string.select_application)))
    }

    private fun makeTemperatureString(temperature: Temperature) : String{
        val retString = StringBuilder()

        var dateFormatString = "E, MMM d h:mm a"
        if (Locale.getDefault().equals(Locale.JAPAN)) {
            dateFormatString = "M'月'd'日('E')' H:mm"
        }
        val dateFormat = SimpleDateFormat(dateFormatString)
        retString.append(dateFormat.format(temperature.date))
        retString.append(" ")

        if(ConfigManager.loadUseFahrenheitFlag()){
            retString.append(String.format("%.1f", temperature.getFahrenheitTemperature()) + "°F")
        } else {
            retString.append(String.format("%.1f", temperature.temperature) + "℃")
        }
        retString.append(" ")

        if(temperature.getConditionString() != "") {
            retString.append(temperature.getConditionString())
            retString.append(" ")
        }

        if(temperature.memo != ""){
            retString.append("["+temperature.memo+"]")
        }

        return retString.toString().trim()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_temperature_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Log.e("***Temperature***","TempListFragment : onOptionsItemSelected")

        return when (item.itemId) {
            R.id.sendTemperature -> {
                // Log.e("***Temperature***","TempListFragment : sendTemperature")
                sendTemperatureList()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

            /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: Temperature?)
    }

    companion object {

        lateinit var database: TemperatureDatabase

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                TemperatureListFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}
