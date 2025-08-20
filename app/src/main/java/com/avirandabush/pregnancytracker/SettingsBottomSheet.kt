package com.avirandabush.pregnancytracker

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsBottomSheet: BottomSheetDialogFragment() {

    interface SettingsListener {
        fun  onGenderChanged(gender: String)
        fun  onDateChanged(startDate: Calendar)
    }

    private var listener: SettingsListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? SettingsListener
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferencesManager = PreferencesManager(requireContext())
        val savedStartDate = preferencesManager.getStartDate()
        val savedGender = preferencesManager.getGender()

        val genderGroup = view.findViewById<RadioGroup>(R.id.genderGroup)
        when (savedGender) {
            "male" -> genderGroup.check(R.id.maleGender)
            "female" -> genderGroup.check(R.id.femaleGender)
            else -> genderGroup.check(R.id.unknownGender)
        }
        genderGroup.setOnCheckedChangeListener { _, checkId ->
            val gender = when (checkId) {
                R.id.maleGender -> "male"
                R.id.femaleGender -> "female"
                else -> "unknown"
            }
            listener?.onGenderChanged(gender)
        }

        val dateButton = view.findViewById<Button>(R.id.startDateButton)
        dateButton.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(savedStartDate.time)
        dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(requireContext(),
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, day)
                    }
                    listener?.onDateChanged(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        val versionNumber = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        val versionText = view.findViewById<TextView>(R.id.versionNumber)
        versionText.text = "Version: $versionNumber"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_bottom_sheet, container, false)    }
}