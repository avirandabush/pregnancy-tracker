package com.avirandabush.pregnancytracker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class CountdownType {
    PREGNANCY, NAUSEA
}

class MainActivity : AppCompatActivity(), SettingsBottomSheet.SettingsListener {

    private lateinit var countdownHandler: Handler
    private lateinit var countdownRunnable: Runnable
    private lateinit var preferencesManager: PreferencesManager

    private var pregnancyStartDate = Calendar.getInstance()
    private val pregnancyDurationInWeeks = 40
    private val nauseaDurationInWeeks = 15

    private var currentCountDown = CountdownType.PREGNANCY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val settingsButton: ImageButton = findViewById(R.id.settingsBtn)
        settingsButton.setOnClickListener {
            val modal = SettingsBottomSheet()
            modal.show(supportFragmentManager, "SettingsBottomSheet")
        }

        findViewById<Button>(R.id.pregnancyBtn).setOnClickListener {
            startCountdownLoop(CountdownType.PREGNANCY)
        }

        findViewById<Button>(R.id.nauseaBtn).setOnClickListener {
            startCountdownLoop(CountdownType.NAUSEA)
        }

        preferencesManager = PreferencesManager(this)
        pregnancyStartDate = preferencesManager.getStartDate()
        updateBackgroundByGender(preferencesManager.getGender())
        startCountdownLoop(CountdownType.PREGNANCY)
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownHandler.removeCallbacks(countdownRunnable)
    }

    private fun startCountdownLoop(type: CountdownType) {
        currentCountDown = type
        updateButtonsUI()

        if (::countdownHandler.isInitialized && ::countdownRunnable.isInitialized) {
            countdownHandler.removeCallbacks(countdownRunnable)
        }

        countdownHandler = Handler(Looper.getMainLooper())
        countdownRunnable = object : Runnable {
            override fun run() {
                updateCountdown(type)
                countdownHandler.postDelayed(this, 1000)
            }
        }

        countdownHandler.post(countdownRunnable)
    }

    private fun updateCountdown(type: CountdownType) {
        val now = Calendar.getInstance()
        val dueDate = pregnancyStartDate.clone() as Calendar

        val weeksToAdd = when (type) {
            CountdownType.PREGNANCY -> pregnancyDurationInWeeks
            CountdownType.NAUSEA -> nauseaDurationInWeeks
        }

        dueDate.add(Calendar.WEEK_OF_YEAR, weeksToAdd)

        val millisLeft = dueDate.timeInMillis - now.timeInMillis

        if (millisLeft <= 0) {
            setCountdownValues(0, 0, 0, 0, 0)
            return
        }

        val days = TimeUnit.MILLISECONDS.toDays(millisLeft)
        val hours = TimeUnit.MILLISECONDS.toHours(millisLeft) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisLeft) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisLeft) % 60

        val weeksPassed = getWeeksBetween(pregnancyStartDate, now)

        setCountdownValues(days, hours, minutes, seconds, weeksPassed)
    }

    private fun getWeeksBetween(start: Calendar, end: Calendar): Int {
        val millisDiff = end.timeInMillis - start.timeInMillis
        return (millisDiff / (1000 * 60 * 60 * 24 * 7)).toInt()
    }

    private fun setCountdownValues(days: Long, hours: Long, minutes: Long, seconds: Long, weeks: Int) {
        findViewById<TextView>(R.id.daysValue).text = days.toString()
        findViewById<TextView>(R.id.hoursValue).text = hours.toString()
        findViewById<TextView>(R.id.minutesValue).text = minutes.toString()
        findViewById<TextView>(R.id.secondsValue).text = seconds.toString()
        findViewById<TextView>(R.id.weeksValue).text = weeks.toString()
    }

    private fun updateButtonsUI() {
        val pregnancyBtn = findViewById<Button>(R.id.pregnancyBtn)
        val nauseaBtn = findViewById<Button>(R.id.nauseaBtn)

        if (currentCountDown == CountdownType.PREGNANCY) {
            pregnancyBtn.setTextColor(ContextCompat.getColor(this, R.color.secondary))
            pregnancyBtn.setBackgroundResource(R.drawable.toggle_left_selected)
            nauseaBtn.setTextColor(ContextCompat.getColor(this, R.color.primary))
            nauseaBtn.setBackgroundResource(R.drawable.toggle_right_unselected)
        } else {
            pregnancyBtn.setTextColor(ContextCompat.getColor(this, R.color.primary))
            pregnancyBtn.setBackgroundResource(R.drawable.toggle_left_unselected)
            nauseaBtn.setTextColor(ContextCompat.getColor(this, R.color.secondary))
            nauseaBtn.setBackgroundResource(R.drawable.toggle_right_selected)
        }
    }

    private fun updateBackgroundByGender(gender: String) {
        val root = findViewById<View>(R.id.main)
        val color = when (gender) {
            "male" -> R.color.backgroundBoy
            "female" -> R.color.backgroundGirl
            else -> R.color.natural
        }
        root.setBackgroundColor(ContextCompat.getColor(this, color))
    }

    override fun onGenderChanged(gender: String) {
        preferencesManager.saveGender(gender)
        updateBackgroundByGender(gender)
    }

    override fun onDateChanged(startDate: Calendar) {
        pregnancyStartDate = startDate
        preferencesManager.saveStartDate(startDate)
        startCountdownLoop(currentCountDown)
    }
}
