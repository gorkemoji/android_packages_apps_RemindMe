package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gorkemoji.remindme.auth.BiometricActivity
import com.gorkemoji.remindme.auth.PasswordActivity
import com.gorkemoji.remindme.database.ToDo
import com.gorkemoji.remindme.database.ToDoAdapter
import com.gorkemoji.remindme.database.ToDoDatabase
import com.gorkemoji.remindme.databinding.ActivityMainBinding
import com.gorkemoji.remindme.onboarding.OnboardingFragment
import com.gorkemoji.remindme.sound.SoundManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ToDoAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: ToDoDatabase
    private lateinit var soundManager: SoundManager
    private val list = arrayListOf<ToDo>()
    private var fabVisible = false
    private var isTransitioning = false

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* val isLocked = loadMode("is_locked", "auth") == "true"
        val isBiometricsEnabled = loadMode("biometrics", "auth") == "true"
        val isPasskeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (isLocked) navigateToAuthActivity(isBiometricsEnabled, isPasskeySet) */

        val themeColor = loadMode("theme_color", "preferences") ?: "blue"
        setThemeColor(themeColor)

        updateComponentColors(getThemeColorResource(themeColor))

        soundManager = SoundManager(this)
        checkFirstStart()

        database = ToDoDatabase.getDatabase(this)
        adapter = ToDoAdapter(list, database.getDao(), MainScope(), soundManager)

        setupRecyclerView()
        observeDatabaseChanges()

        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(binding.recyclerView)

        binding.fabExpand.setOnClickListener {
            if (!fabVisible) showFabMenu()
            else closeFabMenu()
        }

        binding.fabWrite.setOnClickListener { addTask() }
        binding.fabSettings.setOnClickListener { navigateToSettingsActivity() }
    }

    private fun checkFirstStart() { if (loadMode("first_start", "preferences").isNullOrEmpty()) startActivity(Intent(this, OnboardingFragment::class.java)) }

    private fun showFabMenu() {
        fabVisible = true

        binding.fabWrite.visibility = View.VISIBLE
        binding.fabSettings.visibility = View.VISIBLE
    }

    private fun closeFabMenu() {
        fabVisible = false

        binding.fabWrite.visibility = View.INVISIBLE
        binding.fabSettings.visibility = View.INVISIBLE
    }

    private fun navigateToSettingsActivity() {
        isTransitioning = true
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    /* private fun navigateToAuthActivity(biometricsEnabled: Boolean, passkeySet: Boolean) {
        isTransitioning = true
        var intent = Intent(this, PasswordActivity::class.java)
        if (biometricsEnabled && !passkeySet) intent = Intent(this, BiometricActivity::class.java)

        startActivity(intent)
    } */

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun observeDatabaseChanges() {
        database.getDao().getAll().observe(this) { newList ->
            list.apply {
                clear()
                addAll(newList?.reversed() ?: emptyList())
                adapter.notifyDataSetChanged()
            }
        }
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            val position = viewHolder.adapterPosition
            if (swipeDir == ItemTouchHelper.LEFT) deleteTask(position)
            else if (swipeDir == ItemTouchHelper.RIGHT) {
                if (!list[position].isChecked) updateTask(position)
                else adapter.notifyItemChanged(position)
            }
        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.5f

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                if (dX > 0) {
                    val position = viewHolder.adapterPosition
                    if (!list[position].isChecked) setIcon(c, viewHolder, dX, R.drawable.ic_edit, "#e88f2c")
                } else setIcon(c, viewHolder, dX, R.drawable.ic_delete, "#b80f0a")
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun addTask() {
        isTransitioning = true
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra("mode", 1)

        startActivity(intent)
    }

    private fun updateTask(position: Int) {
        isTransitioning = true
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra("mode", 2)
        intent.putExtra("id", list[position].id)
        intent.putExtra("taskName", list[position].toDoTitle)
        intent.putExtra("cbState", list[position].isChecked)

        if (list[position].isReminderOn) {
            intent.putExtra("reminderState", list[position].isReminderOn)
            intent.putExtra("reminderTime", list[position].dueDate)
        }

        startActivity(intent)
        adapter.notifyItemChanged(position)
    }

    private fun deleteTask(position: Int) {
        MainScope().launch { database.getDao().delete(list[position]) }
        adapter.notifyItemRemoved(position)
    }

    private fun setIcon(c: Canvas, viewHolder: RecyclerView.ViewHolder, dX: Float, iconRes: Int, color: String) {
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

        ColorDrawable(Color.parseColor(color)).apply {
            setBounds(if (dX > 0) itemView.left else (itemView.right + dX).toInt(), itemView.top, if (dX > 0) (itemView.left + dX).toInt() else itemView.right, itemView.bottom)
            draw(c)
        }
        icon?.apply {
            bounds = Rect(iconLeft, iconTop, iconRight, iconBottom)
            draw(c)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isTransitioning) saveLockState()
    }

    override fun onPause() {
        super.onPause()
        if (!isTransitioning) saveLockState()
    }

    override fun onResume() {
        super.onResume()

        if (loadMode("is_locked", "auth") == "true") {
            val intent = if (loadMode("biometrics", "auth") == "true") Intent(this, BiometricActivity::class.java)
            else Intent(this, PasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveLockState() {
        val isLocked = loadMode("is_locked", "auth") == "true"
        val isBiometricsEnabled = loadMode("biometrics", "auth") == "true"
        val isPasskeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (!isLocked && (isBiometricsEnabled || isPasskeySet)) saveMode("is_locked", "true", "auth")
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