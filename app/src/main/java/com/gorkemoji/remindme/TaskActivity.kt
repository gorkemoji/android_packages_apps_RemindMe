package com.gorkemoji.remindme

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gorkemoji.remindme.database.ToDo
import com.gorkemoji.remindme.database.ToDoDatabase
import com.gorkemoji.remindme.databinding.ActivityTaskBinding
import com.gorkemoji.remindme.databinding.DialogSecurityChoiceBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class TaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskBinding
    private val calendar = Calendar.getInstance()
    private var isReminderSet = false
    private var isLocked = false
    private var lockType: String = "null"
    private var password: String = "null"

    private val database by lazy { ToDoDatabase.getDatabase(this) }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val themeColor = loadMode("theme_color", "preferences") ?: "blue"
        setThemeColor(themeColor)

        updateComponentColors(getThemeColorResource(themeColor))

        val mode = intent.getIntExtra("mode", 1)
        val id = intent.getLongExtra("id", -1L)
        val taskName = intent.getStringExtra("taskName")
        val checkBoxState = intent.getBooleanExtra("cbState", false)
        val reminderTime = intent.getLongExtra("reminderTime", 0)
        val reminderState = intent.getBooleanExtra("reminderState", false)
        val lockState = intent.getBooleanExtra("lockState", false)

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
        binding.secureChk.isChecked = lockState
        binding.setDateTimeBtn.isEnabled = reminderState

        updateReminderViews(reminderState)

        binding.reminderChk.setOnCheckedChangeListener { _, isChecked ->
            binding.setDateTimeBtn.isEnabled = isChecked
            updateReminderViews(isChecked)
        }

        binding.secureChk.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showSecurityChoiceDialog(getThemeColorResource(themeColor))
            else {
                isLocked = false
                lockType = "null"
                password = "null"
            }
        }

        binding.setDateTimeBtn.setOnClickListener { showDateTimePicker() }

        binding.saveBtn.setOnClickListener {
            if (binding.taskText.text?.isNotBlank() == true) {
                if (binding.reminderChk.isChecked && !isReminderSet) Toast.makeText(this, resources.getText(R.string.enter_a_date), Toast.LENGTH_SHORT).show()
                else {
                    val toDoTitle: String = binding.taskText.text.toString()
                    val dueDate: Long? = if (binding.reminderChk.isChecked) calendar.timeInMillis else null
                    val isReminderOn = binding.reminderChk.isChecked
                    val isLocked = binding.secureChk.isChecked

                    GlobalScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.IO) {
                            when (mode) {
                                1 -> {
                                    val newTaskId = database.getDao().insert(ToDo(toDoTitle = toDoTitle, isChecked = false, isReminderOn = isReminderOn, dueDate = dueDate, isLocked = isLocked, lockType = lockType, password = password))
                                    if (isReminderOn) setReminder(calendar, newTaskId)
                                }
                                2 -> {
                                    if (!isLocked) {
                                        lockType = "null"
                                        password = "null"
                                    }
                                    val updatedTask = ToDo(id = id, toDoTitle = toDoTitle, isChecked = checkBoxState, isReminderOn = isReminderOn, dueDate = dueDate, isLocked = isLocked, lockType = lockType, password = password)
                                    database.getDao().update(updatedTask)
                                    if (isReminderOn)
                                        if (!isReminderSet) setReminder(calendar, id)
                                    else cancelReminder(id)
                                }
                            }
                        }
                        startActivity(Intent(this@TaskActivity, MainActivity::class.java))
                        finish()
                    }
                }
            } else Toast.makeText(this, resources.getText(R.string.task_cannot_be_empty), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateReminderViews(isChecked: Boolean) {
        if (isChecked && isReminderSet) {
            binding.setDateTimeBtn.text = resources.getString(R.string.change_reminder)
            updateReminderDateTimeText()
        }
    }

    private fun updateReminderDateTimeText() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        binding.reminderChk.text = formattedDate
    }

    private fun showDateTimePicker() {
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    isReminderSet = true
                    updateReminderViews(true)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

                timePickerDialog.setOnDismissListener { updateReminderDateTimeText() }
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.setOnDismissListener { updateReminderDateTimeText() }
        datePickerDialog.show()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setReminder(calendar: Calendar, taskId: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("notificationID", taskId.toInt())
            putExtra("taskName", binding.taskText.text.toString())
        }
        val pendingIntent = PendingIntent.getBroadcast(this, taskId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun cancelReminder(taskId: Long) {
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, taskId.toInt(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
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

    private fun showSecurityChoiceDialog(colorResId: Int) {
        val dialogBinding = DialogSecurityChoiceBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        updateIconColors(colorResId, dialogBinding)
        //binding.secureChk.isChecked = true

        dialogBinding.passwordTxt.setOnClickListener {
            showPasswordInputDialog()
            dialog.dismiss()
        }

        dialogBinding.biometricTxt.setOnClickListener {
            isLocked = true
            lockType = "biometric"
            dialog.dismiss()
        }

        //dialog.setOnDismissListener { if (!isLocked) binding.secureChk.isChecked = false }

        dialog.show()
    }

    private fun showPasswordInputDialog() {
        val input = EditText(this).apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.enter_new_password))
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val password = input.text.toString()
                if (password.isNotEmpty()) {
                    isLocked = true
                    lockType = "password"
                    this.password = password
                } else {
                    Toast.makeText(this, R.string.password_cannot_be_empty, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> /*binding.secureChk.isChecked = false*/ }.show()
    }

    private fun setThemeColor(color: String) {
        when (color) {
            "red" -> setTheme(R.style.AppTheme_Red)
            "blue" -> setTheme(R.style.Theme_RemindMe)
            "yellow" -> setTheme(R.style.AppTheme_Yellow)
            "green" -> setTheme(R.style.AppTheme_Green)
        }
    }

    private fun updateComponentColors(colorResId: Int) {
        val colorStateList = ContextCompat.getColorStateList(this, colorResId)
        binding.setDateTimeBtn.backgroundTintList = colorStateList
        binding.saveBtn.backgroundTintList = colorStateList
        binding.taskLayout.hintTextColor = colorStateList
        binding.taskLayout.boxStrokeColor = ContextCompat.getColor(this, colorResId)
        binding.tipsIcon.imageTintList = colorStateList
        binding.tipsTxt.setTextColor(ContextCompat.getColor(this, colorResId))
    }

    private fun updateIconColors(colorResId: Int, dialogBinding: DialogSecurityChoiceBinding) {
        val colorStateList = ContextCompat.getColorStateList(this, colorResId)
        dialogBinding.biometricIcon.imageTintList = colorStateList
        dialogBinding.passwordIcon.imageTintList = colorStateList
    }

    private fun getThemeColorResource(color: String): Int {
        return when (color) {
            "red" -> R.color.red
            "blue" -> R.color.app_accent
            "yellow" -> R.color.yellow
            "green" -> R.color.green
            else -> R.color.app_accent
        }
    }
}