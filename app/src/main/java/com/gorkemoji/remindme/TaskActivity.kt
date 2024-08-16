package com.gorkemoji.remindme

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.View
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

        selectTipText()

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
            updateReminderDateTimeText(mode)
            isReminderSet = true
        }

        if (reminderState && mode == 2) {
            updateReminderViews(mode)
        }

        binding.secureTxt.visibility = if (isLocked) View.GONE else View.VISIBLE
        binding.secureTxt.setOnClickListener { showSecurityChoiceDialog(getThemeColorResource(themeColor)) }
        binding.reminderTxt.setOnClickListener { showDateTimePicker(mode) }

        binding.saveBtn.setOnClickListener {
            if (binding.taskText.text?.isNotBlank() == true) {
                val toDoTitle: String = binding.taskText.text.toString()
                val dueDate: Long? = if (isReminderSet) calendar.timeInMillis else null

                GlobalScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        when (mode) {
                            1 -> {
                                val newTaskId = database.getDao().insert(ToDo(toDoTitle = toDoTitle, isChecked = false, isReminderOn = isReminderSet, dueDate = dueDate, isLocked = isLocked, lockType = lockType, password = password))
                                if (isReminderSet) setReminder(calendar, newTaskId)
                            }
                            2 -> {
                                if (!isLocked) {
                                    lockType = "null"
                                    password = "null"
                                }
                                val updatedTask = ToDo(id = id, toDoTitle = toDoTitle, isChecked = checkBoxState, isReminderOn = isReminderSet, dueDate = dueDate, isLocked = isLocked, lockType = lockType, password = password)
                                database.getDao().update(updatedTask)
                                if (isReminderSet) setReminder(calendar, id) else cancelReminder(id)
                            }
                        }
                    }
                    startActivity(Intent(this@TaskActivity, MainActivity::class.java))
                    finish()
                }
            } else Toast.makeText(this, resources.getText(R.string.task_cannot_be_empty), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateReminderViews(mode: Int) {
        if (isReminderSet) updateReminderDateTimeText(mode)
    }

    private fun updateReminderDateTimeText(mode: Int) {
        if (mode == 2 || isReminderSet) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(calendar.time)
            binding.reminderTxt.text = formattedDate
        }
    }

    private fun showDateTimePicker(mode: Int) {
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)

            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                isReminderSet = true
                updateReminderViews(mode)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

            timePickerDialog.setOnDismissListener {
                if (isReminderSet) {
                    updateReminderDateTimeText(mode)
                }
            }
            timePickerDialog.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.setOnDismissListener {
            if (isReminderSet) {
                updateReminderDateTimeText(mode)
            }
        }
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

        dialogBinding.passwordTxt.setOnClickListener {
            showPasswordInputDialog()
            dialog.dismiss()
        }

        dialogBinding.biometricTxt.setOnClickListener {
            isLocked = true
            lockType = "biometric"
            binding.secureIcon.setImageResource(R.drawable.ic_lock_30)
            binding.secureTxt.text = resources.getString(R.string.locked)
            dialog.dismiss()
        }
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
                    binding.secureIcon.setImageResource(R.drawable.ic_lock_30)
                    binding.secureTxt.text = resources.getString(R.string.locked)
                } else Toast.makeText(this, R.string.password_cannot_be_empty, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null).show()
    }

    private fun selectTipText() {
        val tipsArray = resources.getStringArray(R.array.tips_array)
        val randomIndex = (tipsArray.indices).random()
        binding.tipsTxt.text = tipsArray[randomIndex]
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
        binding.saveBtn.backgroundTintList = colorStateList
        binding.taskLayout.hintTextColor = colorStateList
        binding.taskLayout.boxStrokeColor = ContextCompat.getColor(this, colorResId)
        binding.reminderIcon.imageTintList = colorStateList
        binding.secureIcon.imageTintList = colorStateList
        binding.reminderTxt.setTextColor(ContextCompat.getColor(this, colorResId))
        binding.secureTxt.setTextColor(ContextCompat.getColor(this, colorResId))
        binding.tipsIcon.imageTintList = colorStateList
        binding.tipsTxt.setTextColor(ContextCompat.getColor(this, colorResId))
    }

    private fun updateIconColors(colorResId: Int, dialogBinding: DialogSecurityChoiceBinding) {
        val colorStateList = ContextCompat.getColorStateList(this, colorResId)
        dialogBinding.biometricIcon.imageTintList = colorStateList
        dialogBinding.passwordIcon.imageTintList = colorStateList
    }

    private fun getThemeColorResource(themeColor: String): Int {
        return when (themeColor) {
            "red" -> R.color.red
            "yellow" -> R.color.yellow
            "green" -> R.color.green
            else -> R.color.app_accent
        }
    }
}