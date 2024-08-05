package com.gorkemoji.remindme

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gorkemoji.remindme.auth.BiometricActivity
import com.gorkemoji.remindme.auth.PasswordActivity
import com.gorkemoji.remindme.database.ToDo
import com.gorkemoji.remindme.database.ToDoDatabase
import com.gorkemoji.remindme.databinding.ActivityTaskBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class TaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskBinding
    private val calendar = Calendar.getInstance()
    private var isReminderSet = false

    private val database by lazy {
        ToDoDatabase.getDatabase(this)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!loadMode("passkey", "auth").isNullOrEmpty() && loadMode(
                "is_locked",
                "auth"
            ) == "true"
        ) {
            startActivity(Intent(this, PasswordActivity::class.java))
            finish()
        }

        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mode = intent.getIntExtra("mode", 1)
        val id = intent.getLongExtra("id", -1L)
        val taskName = intent.getStringExtra("taskName")
        val checkBoxState = intent.getBooleanExtra("cbState", false)
        val reminderTime = intent.getLongExtra("reminderTime", 0)
        val reminderState = intent.getBooleanExtra("reminderState", false)

        if (mode == 2) {
            binding.taskText.setText(taskName)
            binding.taskText.hint = null
        }

        if (reminderTime > 0) {
            calendar.timeInMillis = reminderTime
            updateReminderDateTimeText()
            isReminderSet = true
        }

        binding.reminderChk.isChecked = reminderState
        binding.setDateTimeBtn.isEnabled = reminderState

        updateReminderViews(reminderState)

        binding.reminderChk.setOnCheckedChangeListener { _, isChecked ->
            binding.setDateTimeBtn.isEnabled = isChecked
            updateReminderViews(isChecked)
        }

        binding.setDateTimeBtn.setOnClickListener {
            showDateTimePicker()
        }

        binding.saveBtn.setOnClickListener {
            if (binding.taskText.text?.isNotBlank() == true) {
                if (binding.reminderChk.isChecked && !isReminderSet) {
                    Toast.makeText(
                        this,
                        "Please set date and time for the reminder",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val toDoTitle: String = binding.taskText.text.toString()
                    val dueDate: Long? =
                        if (binding.reminderChk.isChecked) calendar.timeInMillis else null
                    val isReminderOn = binding.reminderChk.isChecked

                    GlobalScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.IO) {
                            if (reminderState)
                                cancelReminder()
                            when (mode) {
                                1 -> database.getDao().insert(
                                    ToDo(
                                        toDoTitle = toDoTitle,
                                        isChecked = false,
                                        isReminderOn = isReminderOn,
                                        dueDate = dueDate
                                    )
                                )

                                2 -> {
                                    database.getDao().update(
                                        ToDo(
                                            id = id,
                                            toDoTitle = toDoTitle,
                                            isChecked = checkBoxState,
                                            isReminderOn = isReminderOn,
                                            dueDate = dueDate
                                        )
                                    )

                                    // Update the reminder if it is on
                                    if (isReminderOn) {
                                        setReminder(calendar)
                                    } else {
                                        // If reminder is turned off, cancel any existing reminder
                                        cancelReminder()
                                    }
                                }
                            }
                        }

                        // Start MainActivity after database operation completes
                        startActivity(Intent(this@TaskActivity, MainActivity::class.java))
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Task name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateReminderViews(isChecked: Boolean) {
        if (isChecked && isReminderSet) {
            binding.alarmIcon.visibility = View.VISIBLE
            binding.reminderDate.visibility = View.VISIBLE
            updateReminderDateTimeText()
        } else {
            binding.alarmIcon.visibility = View.GONE
            binding.reminderDate.visibility = View.GONE
        }
    }

    private fun updateReminderDateTimeText() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        binding.reminderDate.text = formattedDate
    }

    private fun showDateTimePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    isReminderSet = true
                    updateReminderViews(true)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

                timePickerDialog.setOnDismissListener {
                    updateReminderDateTimeText()
                }

                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.setOnDismissListener {
            updateReminderDateTimeText()
        }

        datePickerDialog.show()
    }


    @SuppressLint("ScheduleExactAlarm")
    private fun setReminder(calendar: Calendar) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun cancelReminder() {
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun loadMode(type: String, file: String): String? {
        val pref: SharedPreferences = getSharedPreferences(file, Context.MODE_PRIVATE)
        return pref.getString(type, "")
    }

    private fun saveMode(type: String, data: String, file: String) {
        val pref: SharedPreferences = getSharedPreferences(file, Context.MODE_PRIVATE)
        with(pref.edit()) {
            putString(type, data)
            apply()
        }
    }

    override fun onStop() {
        super.onStop()

        val isLocked = loadMode("is_locked", "auth") == "true"
        val isBiometricsEnabled = loadMode("biometrics", "auth") == "true"
        val isPasskeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (!isLocked && (isBiometricsEnabled || isPasskeySet))
            saveMode("is_locked", "true", "auth")
    }

    override fun onPause() {
        super.onPause()

        val isLocked = loadMode("is_locked", "auth") == "true"
        val isBiometricsEnabled = loadMode("biometrics", "auth") == "true"
        val isPasskeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (!isLocked && (isBiometricsEnabled || isPasskeySet))
            saveMode("is_locked", "true", "auth")
    }

    override fun onResume() {
        super.onResume()

        if (loadMode("is_locked", "auth") == "true") {
            val intent = if (loadMode("biometrics", "auth") == "true") Intent(this, BiometricActivity::class.java)
            else Intent(this, PasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
