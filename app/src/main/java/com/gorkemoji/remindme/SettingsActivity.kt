package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.gorkemoji.remindme.auth.PasswordActivity
import com.gorkemoji.remindme.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val DEBOUNCE_DELAY = 300L

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!loadMode("passkey", "auth").isNullOrEmpty() && loadMode("is_locked", "auth") == "true") {
            startActivity(Intent(this, PasswordActivity::class.java))
            finish()
        }

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when (loadMode("theme", "preferences")) {
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
                    saveMode("theme", "dark", "preferences")
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    saveMode("theme", "light", "preferences")
                }
            }, DEBOUNCE_DELAY)
        }
/*
        binding.chnPassword.setOnClickListener {
            saveMode("is_changing", "true", "auth")
            startActivity(Intent(this, PasswordActivity::class.java))
            finish()
        }*/
    }

    private fun loadMode(type: String, file: String): String? {
        val pref : SharedPreferences = applicationContext.getSharedPreferences(file, Context.MODE_PRIVATE)

        return pref.getString(type, "")
    }

    private fun saveMode(type: String, data: String, file: String) {
        val pref : SharedPreferences = applicationContext.getSharedPreferences(file, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = pref.edit()

        editor.putString(type, data)
        editor.apply()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finishAffinity()"))
    override fun onBackPressed() {
        super.onBackPressed()
        saveMode("is_locked", "true", "auth")
        finishAffinity()
    }
}