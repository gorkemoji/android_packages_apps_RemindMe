package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.gorkemoji.remindme.databinding.ActivitySettingsBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

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

        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveMode("dark", "theme")
                binding.darkModeSwitch.isEnabled = false
                runBlocking {
                    delay(3000)
                }
                binding.darkModeSwitch.isEnabled = true
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveMode("light", "theme")
                binding.darkModeSwitch.isEnabled = false
                runBlocking {
                    delay(3000)
                }
                binding.darkModeSwitch.isEnabled = true
            }
        }
    }

    private fun saveMode(data: String, type: String) {
        val pref : SharedPreferences = applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = pref.edit()

        editor.putString("theme", data)
        editor.apply()
    }

    private fun loadMode(type: String): String? {
        val pref : SharedPreferences = applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        return pref.getString("theme", type)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finishAffinity()"))
    override fun onBackPressed() {
        finishAffinity()
    }
}