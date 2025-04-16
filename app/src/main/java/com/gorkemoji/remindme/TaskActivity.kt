package com.gorkemoji.remindme

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.gorkemoji.remindme.data.model.ToDo
import com.gorkemoji.remindme.databinding.ActivityTaskBinding
import com.gorkemoji.remindme.databinding.DialogSecurityChoiceBinding
import com.gorkemoji.remindme.receiver.ReminderReceiver
import com.gorkemoji.remindme.utils.ThemeUtil
import com.gorkemoji.remindme.viewmodel.ToDoViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class TaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskBinding
    private lateinit var viewModel: ToDoViewModel
    private var themeColor by Delegates.notNull<Int>()
    private val calendar = Calendar.getInstance()
    private var isReminderSet = false
    private var isLocked = false
    private var lockType: String = "null"
    private var password: String = "null"
    private var createdTasks: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        themeColor = loadMode("theme_color", "preferences")?.let {
            if (it.isNotEmpty()) Integer.parseInt(it) else 0
        } ?: 0

        ThemeUtil.onActivityCreateSetTheme(this, themeColor)

        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val temp = loadMode("created_tasks", "reports")

        if (temp != null)
            if (temp.isNotEmpty()) createdTasks = temp.let { Integer.parseInt(it) }

        val themes = resources.getStringArray(R.array.font_array)
        val fontAdapter = ArrayAdapter(this, R.layout.dropdown_item, themes)

        val priorities = resources.getStringArray(R.array.priority_array)
        val priorityAdapter = ArrayAdapter(this, R.layout.dropdown_item, priorities)

        binding.autoCompleteTextView.setAdapter(fontAdapter)
        binding.priorityAutoCompleteTextView.setAdapter(priorityAdapter)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[ToDoViewModel::class.java]

        selectTipText()

        val mode = intent.getIntExtra("mode", 1)
        val id = intent.getLongExtra("id", -1L)
        val taskName = intent.getStringExtra("taskName")
        val checkBoxState = intent.getBooleanExtra("cbState", false)
        val reminderTime = intent.getLongExtra("reminderTime", 0)
        val reminderState = intent.getBooleanExtra("reminderState", false)
        var fontName = intent.getStringExtra("font")
        var priority = intent.getIntExtra("priority", 0)

        fontName = fontName.takeUnless { it.isNullOrBlank() } ?: "null"

        val selectedFontPosition = when (fontName) {
            "pacifico" -> themes.indexOf(getString(R.string.pacifico_font))
            else -> themes.indexOf(getString(R.string.default_font))
        }

        binding.autoCompleteTextView.setText(fontAdapter.getItem(selectedFontPosition), false)
        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedFont = resources.getStringArray(R.array.font_array)[position]

            fontName = when (selectedFont) {
                getString(R.string.default_font) -> "default"
                getString(R.string.pacifico_font) -> "pacifico"
                else -> "null"
            }
        }

        val selectedPriorityPosition = when (priority) {
            0 -> priorities.indexOf(getString(R.string.low))
            1 -> priorities.indexOf(getString(R.string.medium))
            else -> priorities.indexOf(getString(R.string.high))
        }

        binding.priorityAutoCompleteTextView.setText(priorityAdapter.getItem(selectedPriorityPosition), false)
        binding.priorityAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedPriority = resources.getStringArray(R.array.priority_array)[position]

            priority = when (selectedPriority) {
                getString(R.string.low) -> 0
                getString(R.string.medium) -> 1
                else -> 2
            }
        }

        if (mode == 2) {
            binding.taskText.setText(taskName)
            binding.taskText.hint = null
        }

        if (reminderTime > 0) {
            calendar.timeInMillis = reminderTime
            updateReminderDateTimeButton(mode)
            isReminderSet = true
        }

        if (reminderState && mode == 2) updateReminderViews(mode)

        binding.removeReminder.setOnClickListener {
            isReminderSet = false
            binding.reminderButton.text = resources.getString(R.string.set_reminder)
            binding.removeReminder.visibility = View.GONE
            makeButtonOutlined(binding.reminderButton)
        }

        binding.lockButton.setOnClickListener {

            if (isLocked) {
                isLocked = false
                lockType = "null"
                password = "null"
                binding.lockButton.icon = getDrawable(R.drawable.ic_lock_outlined_24)
                binding.lockButton.text = resources.getString(R.string.lock)
                makeButtonOutlined(binding.lockButton)
                return@setOnClickListener
            }

            showSecurityChoiceDialog(getThemeColorResource(themeColor.toString()))

        }
        binding.reminderButton.setOnClickListener { showDateTimePicker(mode) }

        binding.saveBtn.setOnClickListener {
            if (binding.taskText.text?.isNotBlank() == true) {
                val toDoTitle: String = binding.taskText.text.toString()
                val dueDate: Long? = if (isReminderSet) calendar.timeInMillis else null

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        when (mode) {
                            1 -> {
                                /*var newTaskId by Delegates.notNull<Long>()
                                lifecycleScope.launch {
                                    newTaskId = withContext(Dispatchers.IO) { viewModel.insertToDo(ToDo(toDoTitle = toDoTitle, isChecked = false, isReminderOn = isReminderSet, dueDate = dueDate, isLocked = isLocked, lockType = lockType, password = password, font = fontName ?: "default")) }
                                }*/

                                val newTaskId = withContext(Dispatchers.IO) {
                                    viewModel.insertToDo(ToDo(
                                        toDoTitle = toDoTitle,
                                        isChecked = false,
                                        isReminderOn = isReminderSet,
                                        dueDate = dueDate,
                                        isLocked = isLocked,
                                        lockType = lockType,
                                        password = password,
                                        font = fontName ?: "default",
                                        priority = priority
                                    ))
                                }

                                if (isReminderSet) setReminder(calendar, newTaskId)
                                incrementTaskCount()
                            }
                            2 -> {
                                if (!isLocked) {
                                    lockType = "null"
                                    password = "null"
                                }
                                val updatedTask = ToDo(id = id, toDoTitle = toDoTitle, isChecked = checkBoxState, isReminderOn = isReminderSet, dueDate = dueDate, isLocked = isLocked, lockType = lockType, password = password, font = fontName!!, priority = priority)
                                viewModel.updateToDo(updatedTask)
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

    private fun incrementTaskCount() {
        createdTasks = createdTasks!! + 1
        saveMode("created_tasks", createdTasks.toString(), "reports")
    }

    private fun updateReminderViews(mode: Int) { if (isReminderSet) updateReminderDateTimeButton(mode) }

    private fun makeButtonFilled(button: MaterialButton) {
        val color = ContextCompat.getColor(this, getThemeColorResource(themeColor.toString()))
        button.setBackgroundColor(color)
        button.setTextColor(ContextCompat.getColor(this, R.color.white))
        button.setIconTintResource(R.color.white)
        button.strokeWidth = 0
    }

    private fun makeButtonOutlined(button: MaterialButton) {
        val color = ContextCompat.getColor(this, getThemeColorResource(themeColor.toString()))
        button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        button.setTextColor(color)
        button.iconTint = ColorStateList.valueOf(color)
        button.strokeWidth = 2
        button.strokeColor = ColorStateList.valueOf(color)
    }

    private fun updateReminderDateTimeButton(mode: Int) {
        if (mode == 2 || isReminderSet) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(calendar.time)
            binding.reminderButton.text = formattedDate
            makeButtonFilled(binding.reminderButton)
            binding.removeReminder.visibility = View.VISIBLE
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
                if (isReminderSet) updateReminderDateTimeButton(mode)
            }
            timePickerDialog.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.setOnDismissListener {
            if (isReminderSet) updateReminderDateTimeButton(mode)
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
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
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


        dialogBinding.passwordTxt.setOnClickListener {
            showPasswordInputDialog(colorResId)
            dialog.dismiss()
        }

        if (!checkIfBiometricsAvailable()) {
            dialogBinding.biometricTxt.visibility = View.GONE
            dialogBinding.biometricIcon.visibility = View.GONE
        }

        dialogBinding.biometricTxt.setOnClickListener {
            isLocked = true
            lockType = "biometric"
            binding.lockButton.icon = getDrawable(R.drawable.ic_lock_outlined_24)
            binding.lockButton.text = resources.getString(R.string.unlock)
            makeButtonFilled(binding.lockButton)
            dialog.dismiss()
        }
        dialog.window?.setDimAmount(0.7f)
        dialog.show()
    }

    private fun showPasswordInputDialog(colorResId: Int) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(ContextCompat.getColor(applicationContext, colorResId))
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.enter_new_password))
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val password = input.text.toString()
                if (password.isNotEmpty()) {
                    isLocked = true
                    lockType = "password"
                    this.password = password
                    binding.lockButton.icon = getDrawable(R.drawable.ic_unlock_30)
                    binding.lockButton.text = resources.getString(R.string.unlock)
                    makeButtonFilled(binding.lockButton)
                } else Toast.makeText(this, R.string.password_cannot_be_empty, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show().window?.setDimAmount(0.7f)
    }

    private fun checkIfBiometricsAvailable(): Boolean {
        val biometricManager = androidx.biometric.BiometricManager.from(this)

        return biometricManager.canAuthenticate() == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun saveMode(type: String, data: String, file: String) {
        val pref: SharedPreferences = applicationContext.getSharedPreferences(file, Context.MODE_PRIVATE)
        with(pref.edit()) {
            putString(type, data)
            apply()
        }
    }

    private fun selectTipText() {
        val tipsArray = resources.getStringArray(R.array.tips_array)
        val randomIndex = (tipsArray.indices).random()
        binding.tipsTxt.text = tipsArray[randomIndex]
    }

    private fun getThemeColorResource(themeColor: String): Int {
        return when (themeColor) {
            "1" -> R.color.crimson
            "2" -> R.color.olive_green
            "3" -> R.color.amber
            "4" -> R.color.lilac
            else -> R.color.metallic_blue
        }
    }
}