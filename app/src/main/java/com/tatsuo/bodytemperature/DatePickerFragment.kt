package com.tatsuo.bodytemperature

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment

class DatePickerFragment(var year: Int, var month: Int, var day: Int): DialogFragment(), DatePickerDialog.OnDateSetListener {
    interface OnDateSelectedListener {
        fun onDateSelected(year: Int, month: Int, day: Int)
    }
    private lateinit var listener: DatePickerFragment.OnDateSelectedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DatePickerFragment.OnDateSelectedListener){
            listener = context
        }
    }

    fun setListener(customListener: DatePickerFragment.OnDateSelectedListener){
        listener = customListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return DatePickerDialog(this.context as Context, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        listener.onDateSelected(year, month, day)
    }
}
