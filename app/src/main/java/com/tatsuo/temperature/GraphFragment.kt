package com.tatsuo.temperature

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.tatsuo.temperature.db.Temperature
import com.tatsuo.temperature.db.TemperatureDatabase
import kotlinx.android.synthetic.main.fragment_graph.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [GraphFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [GraphFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GraphFragment : Fragment() , DatePickerFragment.OnDateSelectedListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private var temperatureList: List<Temperature> = listOf()

    private val REQUEST_CODE_SEND_GRAPH = 102

    private val handler = Handler()
    private var runnable = Runnable {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_graph, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val personName = ConfigManager().loadTargetPersonName()
        if(personName != "あなた"){
            requireActivity().setTitle(personName+"さん")
        } else {
            requireActivity().setTitle("体温グラフ")
        }

        database = Room.databaseBuilder(requireActivity().applicationContext, objectOf<TemperatureDatabase>(), "temperature_database.db").build()

        makeView()

        changeTargetDateButton.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.time = (requireActivity() as TemperatureMainActivity).targetDate
            val year: Int = cal.get(Calendar.YEAR)
            val month: Int = cal.get(Calendar.MONTH)
            val day: Int = cal.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerFragment(year, month, day)
            datePicker.setListener(this)
            datePicker.show(parentFragmentManager, "datePicker")
        }

        beforeButton.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.time = (requireActivity() as TemperatureMainActivity).targetDate
            cal.add(Calendar.DATE, -1)
            (requireActivity() as TemperatureMainActivity).targetDate = cal.time
            makeView()
        }

        afterButton.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.time = (requireActivity() as TemperatureMainActivity).targetDate
            cal.add(Calendar.DATE, 1)
            (requireActivity() as TemperatureMainActivity).targetDate = cal.time
            makeView()
        }

    }

    private fun makeView(){
        val dateFormat = SimpleDateFormat("M'月'd'日('E')'")
        targetDateText.text = dateFormat.format((requireActivity() as TemperatureMainActivity).targetDate)

        val graphType = ConfigManager().loadGraphType()
        graphView.graphType = graphType

        val cal = Calendar.getInstance()
        cal.time = (requireActivity() as TemperatureMainActivity).targetDate
        cal.add(Calendar.DATE, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val afterDate = cal.time
        if(graphType == 1){
            cal.add(Calendar.DATE, -3)
        } else if(graphType == 2){
            cal.add(Calendar.DATE, -7)
        }
        val beforDate = cal.time
        
        val dao = database.temperatureDao()
        val myExecutor = Executors.newSingleThreadExecutor()
        myExecutor.execute() {
            temperatureList = dao.getTemperatureData(ConfigManager().loadTargetPersonId(), beforDate, afterDate)
            graphView.targetDate = (requireActivity() as TemperatureMainActivity).targetDate
            graphView.temperatureList = temperatureList

            runnable = Runnable {
                graphView.invalidate()
            }
            handler.post(runnable)
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_graph, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sendGraph -> {
                sendGraph()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun sendGraph() {
        if(hasPermission() == false){
            requestPermission(REQUEST_CODE_SEND_GRAPH)
            return
        }

        val file = File(Environment.getExternalStorageDirectory().toString() + "/capture.png")
        file.getParentFile().mkdir()

        saveCapture(graphView, file)

        // Intentで送信
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/png"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                Intent.FLAG_GRANT_READ_URI_PERMISSION

        val uri: Uri
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file)
        } else {
            uri = FileProvider.getUriForFile(requireActivity(),
                    BuildConfig.APPLICATION_ID + ".fileprovider", file)
        }

        intent.putExtra(Intent.EXTRA_STREAM, uri)
        // intent.putExtra(Intent.EXTRA_TEXT, "")

        startActivity(Intent.createChooser(intent, "アプリケーションを選択"))
    }

    fun saveCapture(view: View, file: File) {
        // キャプチャを撮る
        val capture = getViewCapture(view)

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file, false)
            // 画像のフォーマットと画質と出力先を指定して保存
            capture!!.compress(Bitmap.CompressFormat.PNG,100, fos)
            fos.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("***CAPTURE***", "EXCEPTION", e)
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (ie: IOException) {
                    fos = null
                }

            }
        }
    }

    fun getViewCapture(view: View): Bitmap? {
        view.isDrawingCacheEnabled = true

        view.drawingCacheBackgroundColor = Color.WHITE

        // Viewのキャプチャを取得
        val cache = view.drawingCache ?: return null

        // view.getDrawingCache(false)

        val screenShot = Bitmap.createBitmap(cache)
        view.isDrawingCacheEnabled = false

        return screenShot
    }

    fun hasPermission(): Boolean {
        // API 23より前ならチェック不要
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(requestCode: Int) {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gotPermission(requestCode)
        } else {
            val toast = Toast.makeText(requireActivity(),
                    "必要な権限が許可されませんでした\n設定アプリを開き権限を確認してください", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    fun gotPermission(requestCode: Int) {
        if (requestCode == REQUEST_CODE_SEND_GRAPH) {
            sendGraph()
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GraphFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                GraphFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }

        lateinit var database: TemperatureDatabase
    }

    override fun onDateSelected(year: Int, month: Int, day: Int){
        val cal = Calendar.getInstance()
        cal.time = (requireActivity() as TemperatureMainActivity).targetDate
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)

        (requireActivity() as TemperatureMainActivity).targetDate = cal.time
        makeView()
    }

}
