package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.gorkemoji.remindme.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val DEBOUNCE_DELAY = 300L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when (loadMode("theme")) {
                "dark" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    binding.darkModeSwitch.isChecked = true
                }

                "light" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    binding.darkModeSwitch.isChecked = false
                }
            }

        binding.bottomNavigationView.selectedItemId = R.id.settings

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.tasks) {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                overridePendingTransition(0, 0)
                true
            } else item.itemId == R.id.settings
        }

        var debounceHandler = Handler(Looper.getMainLooper())

        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            debounceHandler.removeCallbacksAndMessages(null)
            debounceHandler.postDelayed({
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    saveMode("dark", "theme")
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    saveMode("light", "theme")
                }
            }, DEBOUNCE_DELAY)
        }
    }

    private fun loadMode(type: String): String? {
        val pref : SharedPreferences = applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        return pref.getString("theme", type)
    }

    private fun saveMode(data: String, type: String) {
        val pref : SharedPreferences = applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = pref.edit()

        editor.putString("theme", data)
        editor.apply()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finishAffinity()"))
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}