package com.tatsuo.bodytemperature

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.tatsuo.bodytemperature.db.Temperature
import com.tatsuo.bodytemperature.db.TemperatureDatabase
import kotlinx.android.synthetic.main.activity_input_temperature.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class InputTemperatureActivity : AppCompatActivity(), TimePickerFragment.OnTimeSelectedListener, DatePickerFragment.OnDateSelectedListener {

    lateinit var temperature : Temperature

    companion object {
        lateinit var database: TemperatureDatabase
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_temperature)

        setTitle("体温の記録")

        database = Room.databaseBuilder(this, objectOf<TemperatureDatabase>(), "temperature_database.db").build()

        val temperatureId = intent.getLongExtra("TEMPERATURE_ID", -999L)
        if(temperatureId != -999L) {
            val dao = database.temperatureDao()
            val handler = Handler()
            val myExecutor = Executors.newSingleThreadExecutor()
            myExecutor.execute() {
                val temperatureList = dao.loadTemperatureById(temperatureId)
                if (temperatureList.size >= 1) {
                    temperature = temperatureList.get(0)
                }
                handler.post(Runnable() {
                    makeView()
                })
            }
        } else {
            temperature = Temperature(0, getUUID(), Date(), ConfigManager().loadTargetPersonId(), 0.0, 0, "", "")
            makeView()
            temperatureText.requestFocus()
        }

        changeDateButton.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.time = temperature.date
            val year: Int = cal.get(Calendar.YEAR)
            val month: Int = cal.get(Calendar.MONTH)
            val day: Int = cal.get(Calendar.DAY_OF_MONTH)

            DatePickerFragment(year, month, day).show(supportFragmentManager, "datePicker")
        }

        changeTimeButton.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.time = temperature.date
            val hour: Int = cal.get(Calendar.HOUR_OF_DAY)
            val minute: Int = cal.get(Calendar.MINUTE)

            TimePickerFragment(hour, minute).show(supportFragmentManager, "timePicker")
        }

        changeConditionButton.setOnClickListener {
            val intent = Intent(this, SelectConditionActivity::class.java)
            intent.putExtra("CONDITIONS", temperature.conditions)
            startActivityForResult(intent, 1)
        }

        clearConditionButton.setOnClickListener {
            temperature.conditions = ""
            makeView()
        }

        deleteButton.setOnClickListener {
            deleteTemperature()
        }

        temperatureText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                val string = s.toString()
                if (start == 1 && count == 1 &&
                        (string == "33" || string == "34" || string == "35" || string == "36"
                            || string == "37" || string == "38" || string == "39"
                            || string == "40" || string == "41" || string == "42")) {
                    temperatureText.text.append('.')
                }
            }
            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private fun makeView() {
        val dateFormat = SimpleDateFormat("M'月'd'日('E')'")
        dateText.text = dateFormat.format(temperature.date)

        val timeFormat = SimpleDateFormat("H:mm")
        timeText.text = String.format("%5s",timeFormat.format(temperature.date))

        if(temperature.temperature != 0.0) {
            val temperatureText: EditText = findViewById((R.id.temperatureText))
            temperatureText.setText(String.format("%.1f", temperature.temperature))
        }

        conditionText.text = temperature.getConditionString()

        if(temperature.conditions == ""){
            clearConditionButton.visibility = View.GONE
            // conditionText.visibility == View.GONE
        } else {
            clearConditionButton.visibility = View.VISIBLE
            // conditionText.visibility == View.VISIBLE
        }

        // val memoText : EditText = findViewById(R.id.memoText)
        memoText.setText(temperature.memo)

        if(temperature.id == 0L){
            deleteButton.visibility = View.GONE
        } else {
            deleteButton.visibility = View.VISIBLE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_input_temperature, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveTemperature -> {
                val inputError = inputCheck()
                if(inputError != ""){
                    Toast.makeText(this, inputError, Toast.LENGTH_LONG).show()
                    return true
                }

                saveTemperature()
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != 1) { return }

        if (resultCode == Activity.RESULT_OK && data != null) {
            temperature.conditions = data.getStringExtra("CONDITIONS")
            makeView()
        }

    }

    private fun inputCheck() : String {
        var retString : String = ""

        if(temperatureText.text.toString() == ""){
            return "体温を入力してください"
        }

        if(temperatureText.text.toString().toDouble() == 0.0){
            return "体温の値が正しくありません"
        }

        return retString
    }

    private fun saveTemperature() {
        val temperatureDouble : Double = temperatureText.text.toString().toDouble()
        // Log.e("***Temperature***","temperatureDouble -> "+temperatureDouble)
        temperature.temperature = temperatureDouble
        temperature.memo = memoText.text.toString()

        val dao = database.temperatureDao()
        val myExecutor = Executors.newSingleThreadExecutor()
        myExecutor.execute() {
            TemperatureApplication.dbUpdating = true
            dao.insert(temperature)
            TemperatureApplication.dbUpdating = false
            Log.e("***Temperature***","Inserted. record count : "+dao.count())
        }
        ConfigManager().saveUpdatedDataFlag(true)
    }

    private fun deleteTemperature() {
        AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                .setTitle("")
                .setMessage("体温の記録を削除してよろしいですか？")
                .setPositiveButton("OK", { dialog, which ->
                    val dao = database.temperatureDao()
                    val myExecutor = Executors.newSingleThreadExecutor()
                    myExecutor.execute() {
                        TemperatureApplication.dbUpdating = true
                        dao.delete(temperature)
                        TemperatureApplication.dbUpdating = false
                        Log.e("***Temperature***","Deleted.")
                    }
                    ConfigManager().saveUpdatedDataFlag(true)
                    finish()
                })
                .setNegativeButton("キャンセル", { dialog, which ->
                })
                .show()
    }

    override fun onDateSelected(year: Int, month: Int, day: Int){
        val cal = Calendar.getInstance()
        cal.time = temperature.date
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)

        temperature.date = cal.time
        makeView()
    }

    override fun onTimeSelected(hour: Int, minute: Int){
        val cal = Calendar.getInstance()
        cal.time = temperature.date
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)

        temperature.date = cal.time
        makeView()
    }

}

internal inline fun <reified T : Any> objectOf() = T::class.java
