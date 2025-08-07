package com.ellabs.fitplan.managers

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import java.util.*

class CalendarHelper(private val context: Context) {

    fun addWorkoutToDeviceCalendar() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                openTimePicker(selectedYear, selectedMonth, selectedDayOfMonth)
            },
            year,
            month,
            day
        )
        datePicker.show()
    }

    private fun openTimePicker(year: Int, month: Int, dayOfMonth: Int) {
        val timePicker = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                addEventToCalendar(year, month, dayOfMonth, hourOfDay, minute)
            },
            12, 0, true
        )
        timePicker.show()
    }

    private fun addEventToCalendar(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hourOfDay: Int,
        minute: Int
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }

        val startMillis = calendar.timeInMillis
        val endMillis = startMillis + (60 * 60 * 1000)

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "Workout (FitPlan)")
            putExtra(CalendarContract.Events.DESCRIPTION, "Workout session at the gym")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
        }
        context.startActivity(intent)
    }
}