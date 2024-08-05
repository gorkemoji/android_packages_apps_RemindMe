package com.gorkemoji.remindme

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ToDoAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: ToDoDatabase
    private val list = arrayListOf<ToDo>()
    private var travelling = false

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*val isLocked = loadMode("is_locked", "auth") == "true"
        val biometricsEnabled = loadMode("biometrics", "auth") == "true"
        val passkeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (isLocked)
            navigateToAuthActivity(biometricsEnabled, passkeySet)*/

        setupTheme()
        checkFirstStart()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            checkAndRequestNotificationPermission()

        database = ToDoDatabase.getDatabase(this)
        adapter = ToDoAdapter(list, database.getDao(), MainScope())

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigationView()
        setupRecyclerView()
        observeDatabaseChanges()

        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(binding.recyclerView)

        binding.addBtn.setOnClickListener {
            navigateToTaskActivity(1)
        }
    }
/*
    private fun navigateToAuthActivity(biometricsEnabled: Boolean, passkeySet: Boolean) {
        var intent = Intent(this, PasswordActivity::class.java)
        val animationBundle = ActivityOptions.makeCustomAnimation(this, R.anim.slide_out_bottom, R.anim.slide_in_bottom).toBundle()

        if (biometricsEnabled && !passkeySet)
            intent = Intent(this, BiometricActivity::class.java)

        travelling = true
        intent.putExtra("prevActivity", "MainActivity")
        startActivity(intent, animationBundle)
        finish()
    }
*/
    private fun setupTheme() {
        val theme = when (isDarkMode(this)) {
            true -> "dark"
            false -> "light"
        }
        AppCompatDelegate.setDefaultNightMode(if (theme == "dark") AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        saveMode("theme", theme, "preferences")
    }

    private fun checkFirstStart() {
        if (loadMode("first_start", "preferences").isNullOrEmpty()) {
            startActivity(Intent(this, OnboardingFragment::class.java))
            finish()
        }
    }

    private fun setupBottomNavigationView() {
        binding.bottomNavigationView.selectedItemId = R.id.tasks
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.settings) {
                travelling = true
                val animationBundle = ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
                startActivity(Intent(this, SettingsActivity::class.java), animationBundle)
                true
            } else item.itemId == R.id.tasks
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun observeDatabaseChanges() {
        database.getDao().getAll().observe(this, Observer { newList ->
            list.apply {
                clear()
                addAll(newList?.reversed() ?: emptyList())
                adapter.notifyDataSetChanged()
            }
        })
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            val position = viewHolder.adapterPosition
            if (swipeDir == ItemTouchHelper.LEFT) {
                deleteTask(position)
            } else if (swipeDir == ItemTouchHelper.RIGHT) {
                updateTask(position)
            }
        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.5f

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                if (dX > 0) setIcon(c, viewHolder, dX, R.drawable.ic_edit, "#e88f2c")
                else setIcon(c, viewHolder, dX, R.drawable.ic_delete, "#b80f0a")
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun deleteTask(position: Int) {
        MainScope().launch {
            database.getDao().delete(list[position])
        }
        adapter.notifyItemRemoved(position)
    }

    private fun updateTask(position: Int) {
        travelling = true
        val intent = Intent(this, TaskActivity::class.java).apply {
            val animationBundle = ActivityOptions.makeCustomAnimation(this@MainActivity, R.anim.slide_out_bottom, R.anim.slide_in_bottom).toBundle()
            putExtra("mode", 2)
            putExtra("id", list[position].id)
            putExtra("taskName", list[position].toDoTitle)
            putExtra("cbState", list[position].isChecked)
            if (list[position].isReminderOn) {
                putExtra("reminderState", list[position].isReminderOn)
                putExtra("reminderTime", list[position].dueDate)
            }
            startActivity(this, animationBundle)
        }
        adapter.notifyItemChanged(position)
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
        val biometricsEnabled = loadMode("biometrics", "auth") == "true"
        val passkeySet = !loadMode("passkey", "auth").isNullOrBlank()
        val isLocked = loadMode("is_locked", "auth") == "true"
        if (!isLocked && (biometricsEnabled || passkeySet) && !travelling) {
            saveMode("is_locked", "true", "auth")
        }
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

    private fun isDarkMode(context: Context): Boolean {
        val configuration = context.resources.configuration
        return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkAndRequestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)
        } else {
            handleNotificationPermissionGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            handleNotificationPermissionGranted()
        } else {
            handleNotificationPermissionDenied()
        }
    }

    private fun handleNotificationPermissionGranted() {
        Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
    }

    private fun handleNotificationPermissionDenied() {
        Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToTaskActivity(mode: Int) {
        travelling = true
        val intent = Intent(this, TaskActivity::class.java).apply {
            val animationBundle = ActivityOptions.makeCustomAnimation(this@MainActivity, R.anim.slide_out_bottom, R.anim.slide_in_bottom).toBundle()
            putExtra("mode", mode)
            startActivity(this, animationBundle)
        }
        finish()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        saveMode("is_locked", "true", "auth")
        finishAffinity()
    }
}