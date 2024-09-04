package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gorkemoji.remindme.data.model.ToDo
import com.gorkemoji.remindme.adapter.ToDoAdapter
import com.gorkemoji.remindme.data.db.ToDoDatabase
import com.gorkemoji.remindme.databinding.ActivityMainBinding
import com.gorkemoji.remindme.onboarding.OnboardingFragment
import com.gorkemoji.remindme.viewmodel.ToDoViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ToDoAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: ToDoDatabase
    private lateinit var viewModel: ToDoViewModel
    private lateinit var player: MediaPlayer
    private lateinit var themeColor: String
    private var doneTasks: Int? = null
    private val list = arrayListOf<ToDo>()
    private var fabVisible = false

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val temp = loadMode("done_tasks", "reports")

        if (temp != null)
            if (temp.isNotEmpty()) doneTasks = temp.let { Integer.parseInt(it) }

        themeColor = loadMode("theme_color", "preferences") ?: "blue"
        setThemeColor(themeColor)
        updateComponentColors(getThemeColorResource(themeColor))

        player = MediaPlayer.create(this, R.raw.pencil_done)
        checkFirstStart()

        database = ToDoDatabase.getDatabase(this)
        adapter = ToDoAdapter(this, list, database.getDao(), MainScope(), player)

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[ToDoViewModel::class.java]

        setupRecyclerView()
        setViewModel()

        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(binding.recyclerView)

        binding.fabExpand.setOnClickListener {
            if (!fabVisible) showFabMenu()
            else closeFabMenu()
        }

        binding.fabWrite.setOnClickListener { addTask() }
        binding.fabSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.fabReport.setOnClickListener { startActivity(Intent(this, ReportActivity::class.java)) }
    }

    private fun checkFirstStart() {
        if (loadMode("first_start", "preferences").isNullOrEmpty()) {
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            saveMode("start_date", currentDate, "reports")
            saveMode("created_tasks", "0", "reports")
            saveMode("done_tasks", "0", "reports")
            startActivity(Intent(this, OnboardingFragment::class.java))
        }
    }

    private fun showFabMenu() {
        fabVisible = true

        binding.fabWrite.visibility = View.VISIBLE
        binding.fabReport.visibility = View.VISIBLE
        binding.fabSettings.visibility = View.VISIBLE
    }

    private fun closeFabMenu() {
        fabVisible = false

        binding.fabWrite.visibility = View.INVISIBLE
        binding.fabReport.visibility = View.INVISIBLE
        binding.fabSettings.visibility = View.INVISIBLE
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun setViewModel() {
        viewModel.allToDos.observe(this) { list ->
            list?.let { adapter.updateList(it) }
        }
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            val position = viewHolder.adapterPosition

            when (swipeDir) {
                ItemTouchHelper.LEFT -> deleteTask(position)
                ItemTouchHelper.RIGHT -> {
                    if (list[position].isLocked) promptUnlock(list[position], getThemeColorResource(themeColor))
                    else if (!list[position].isChecked) updateTask(position)
                    else adapter.notifyItemChanged(position)
                }
            }
        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.5f

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                val position = viewHolder.adapterPosition
                if (dX > 0) {
                    if (list[position].isLocked) setIcon(c, viewHolder, dX, R.drawable.ic_unlock, R.color.green)
                    else if (!list[position].isChecked) setIcon(c, viewHolder, dX, R.drawable.ic_edit, R.color.yellow)
                } else setIcon(c, viewHolder, dX, R.drawable.ic_delete, R.color.red)
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun promptUnlock(toDo: ToDo, colorResId: Int) {
        when (toDo.lockType) {
            "biometric" -> showBiometricPrompt(toDo)
            "password" -> showPasswordDialog(toDo, colorResId)
        }
    }

    private fun showPasswordDialog(toDo: ToDo, colorResId: Int) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(ContextCompat.getColor(applicationContext, colorResId)) }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.enter_your_password))
            .setView(input)
            .setPositiveButton(resources.getText(R.string.unlock)) { _, _ ->
                val password = input.text.toString()
                if (password == toDo.password) isToDoUnlocked(toDo, true)
                else {
                    Toast.makeText(this, resources.getText(R.string.wrong_password), Toast.LENGTH_SHORT).show()
                    adapter.notifyItemChanged(list.indexOf(toDo))
                }
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                adapter.notifyItemChanged(list.indexOf(toDo))
                dialog.dismiss()
            }.show()
    }

    private fun showBiometricPrompt(toDo: ToDo) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isToDoUnlocked(toDo, true)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, resources.getText(R.string.auth_failed), Toast.LENGTH_SHORT).show()
                    isToDoUnlocked(toDo, false)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_auth))
            .setSubtitle(getString(R.string.log_in_with_biometric))
            .setNegativeButtonText(getString(R.string.cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun isToDoUnlocked(toDo: ToDo, status: Boolean) {
        if (status) {
            toDo.isLocked = false
            toDo.lockType = "null"
            toDo.password = "null"

            MainScope().launch {
                database.getDao().update(toDo)

                val position = list.indexOfFirst { it.id == toDo.id }
                if (position != -1) adapter.notifyItemChanged(position)
            }
        } else Toast.makeText(applicationContext, resources.getText(R.string.failed_to_unlock), Toast.LENGTH_SHORT).show()
    }

    private fun addTask() {
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra("mode", 1)

        startActivity(intent)
    }

    private fun updateTask(position: Int) {
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra("mode", 2)
        intent.putExtra("id", list[position].id)
        intent.putExtra("taskName", list[position].toDoTitle)
        intent.putExtra("cbState", list[position].isChecked)
        intent.putExtra("font", list[position].font)

        if (list[position].isLocked) intent.putExtra("lockState", list[position].isLocked)

        if (list[position].isReminderOn) {
            intent.putExtra("reminderState", list[position].isReminderOn)
            intent.putExtra("reminderTime", list[position].dueDate)
        }

        startActivity(intent)
        adapter.notifyItemChanged(position)
    }

    private fun deleteTask(position: Int) {
        viewModel.deleteToDo(list[position])
        adapter.notifyItemRemoved(position)
    }

    private fun setIcon(c: Canvas, viewHolder: RecyclerView.ViewHolder, dX: Float, iconRes: Int, colorRes: Int) {
        val itemView: View = viewHolder.itemView
        val icon = ContextCompat.getDrawable(this, iconRes)
        val width = icon?.intrinsicWidth ?: 0
        val height = icon?.intrinsicHeight ?: 0
        val itemHeight = itemView.height
        val iconTop = itemView.top + (itemHeight - height) / 2
        val iconMargin = (itemHeight - height) / 2
        val iconLeft: Int
        val iconRight: Int

        if (dX > 0) {
            iconLeft = itemView.left + iconMargin
            iconRight = itemView.left + iconMargin + width
        } else {
            iconLeft = itemView.right - iconMargin - width
            iconRight = itemView.right - iconMargin
        }
        val iconBottom = iconTop + height

        val backgroundColor = ContextCompat.getColor(this, colorRes)

        ColorDrawable(backgroundColor).apply {
            setBounds(if (dX > 0) itemView.left else (itemView.right + dX).toInt(), itemView.top, if (dX > 0) (itemView.left + dX).toInt() else itemView.right, itemView.bottom)
            draw(c)
        }
        icon?.apply {
            bounds = Rect(iconLeft, iconTop, iconRight, iconBottom)
            draw(c)
        }
    }

    fun incrementTaskDone() {
        doneTasks = doneTasks!! + 1
        saveMode("done_tasks", doneTasks.toString(), "reports")
    }

    private fun loadMode(type: String, file: String): String? {
        val pref: SharedPreferences = getSharedPreferences(file, Context.MODE_PRIVATE)
        return pref.getString(type, "")
    }

    private fun saveMode(type: String, data: String, file: String) {
        val pref: SharedPreferences = applicationContext.getSharedPreferences(file, Context.MODE_PRIVATE)
        with(pref.edit()) {
            putString(type, data)
            apply()
        }
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
        binding.fabWrite.backgroundTintList = colorStateList
        binding.fabReport.backgroundTintList = colorStateList
        binding.fabExpand.backgroundTintList = colorStateList
        binding.fabSettings.backgroundTintList = colorStateList
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