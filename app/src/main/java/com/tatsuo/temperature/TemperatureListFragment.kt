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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.tatsuo.temperature.db.Temperature
import com.tatsuo.temperature.db.TemperatureDatabase
import kotlinx.android.synthetic.main.fragment_temperature_list.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

    private var temperatureList: List<Temperature> = listOf()

    private val REQUEST_CODE_SEND_TEMPERATURE_LIST = 101

    private val handler = Handler()
    private var runnable = Runnable {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        database = Room.databaseBuilder(requireActivity().applicationContext, objectOf<TemperatureDatabase>(), "temperature_database.db").build()
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

        val personName = ConfigManager().loadTargetPersonName()
        if(personName != "あなた"){
            requireActivity().setTitle(personName+"さん")
        } else {
            requireActivity().setTitle("体温の記録")
        }
    }

    override fun onResume() {
        super.onResume()

        if(ConfigManager().loadUpdatedDataFlag()) {
            updateViewAdapter()
            ConfigManager().saveUpdatedDataFlag(false)
        }

    }

    private fun updateViewAdapter() {
        val dao = database.temperatureDao()
        val myExecutor = Executors.newSingleThreadExecutor()
        myExecutor.execute() {
            temperatureList = dao.getAllTemperatureData(ConfigManager().loadTargetPersonId())

            runnable = Runnable {
                temperatureListView.adapter = TemperatureListViewAdapter(temperatureList, listener)
                if (temperatureList.size == 0) {
                    temperatureListView.visibility = View.GONE
                    nodatemessage1.visibility = View.VISIBLE
                    nodatemessage2.visibility = View.VISIBLE
                } else {
                    temperatureListView.visibility = View.VISIBLE
                    nodatemessage1.visibility = View.GONE
                    nodatemessage2.visibility = View.GONE
                }
            }
            handler.post(runnable)
        }
    }

    private fun sendTemperatureList(){
        if(hasPermission() == false){
            requestPermission(REQUEST_CODE_SEND_TEMPERATURE_LIST)
            return
        }

        val file = File(Environment.getExternalStorageDirectory().toString() + "/capture.png")
        file.getParentFile().mkdir()

        saveCapture(temperatureListView, file)

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
        if (requestCode == REQUEST_CODE_SEND_TEMPERATURE_LIST) {
            sendTemperatureList()
        }
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
        Log.e("***Temperature***","TempListFragment : onOptionsItemSelected")

        return when (item.itemId) {
            R.id.sendTemperature -> {
                Log.e("***Temperature***","TempListFragment : sendTemperature")
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
