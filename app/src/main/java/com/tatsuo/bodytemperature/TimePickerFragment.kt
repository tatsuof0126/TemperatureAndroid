package com.tatsuo.bodytemperature

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment

class TimePickerFragment(var hour: Int, var minute: Int): DialogFragment(), TimePickerDialog.OnTimeSetListener{
    interface OnTimeSelectedListener {
        fun onTimeSelected(hour: Int, minute: Int)
    }
    private lateinit var  listener: OnTimeSelectedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTimeSelectedListener){
            listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(context, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        listener.onTimeSelected(hourOfDay, minute)
    }
}
