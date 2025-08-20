package com.avirandabush.pregnancytracker


import android.content.Context
import java.util.*

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("pregnancy_prefs", Context.MODE_PRIVATE)

    fun saveStartDate(calendar: Calendar) {
        prefs.edit()
            .putLong("pregnancy_start_date", calendar.timeInMillis)
            .apply()
    }

    fun getStartDate(): Calendar {
        val millis = prefs.getLong("pregnancy_start_date", -1)
        return if (millis != -1L) {
            Calendar.getInstance().apply { timeInMillis = millis }
        } else {
            // Default: today - 28 days
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -28)
            }
        }
    }

    fun saveGender(gender: String) {
        prefs.edit().putString("gender", gender).apply()
    }

    fun getGender(): String {
        return prefs.getString("gender", "unknown") ?: "unknown"
    }
}
