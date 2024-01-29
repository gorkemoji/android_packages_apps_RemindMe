package com.gorkemoji.remindme

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.gorkemoji.remindme.auth.PasswordActivity
import com.gorkemoji.remindme.auth.SecurityActivity
import com.gorkemoji.remindme.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val switchDelay = 300L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!loadMode("passkey", "auth").isNullOrEmpty()) {
           // binding.btnPassword.text = getString(R.string.change_pin)

            if (loadMode("is_locked", "auth") == "true") {
                startActivity(Intent(this, PasswordActivity::class.java))
                finish()
            }
        } else {
           // binding.btnPassword.text = getString(R.string.secure)
        }

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

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.tasks) {
                val animationBundle = ActivityOptions.makeCustomAnimation(applicationContext, 0, 0).toBundle()
                startActivity(Intent(applicationContext, MainActivity::class.java), animationBundle)
                true
            } else item.itemId == R.id.settings
        }

        val debounceHandler = Handler(Looper.getMainLooper())

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
            }, switchDelay)
        }

        binding.security.setOnClickListener {
            if (!loadMode("passkey", "auth").isNullOrEmpty()) {
              //  saveMode("is_changing", "true", "auth")
            }
            val animationBundle = ActivityOptions.makeCustomAnimation(this, R.anim.slide_out_bottom, R.anim.slide_in_bottom).toBundle()
            startActivity(Intent(this, SecurityActivity::class.java), animationBundle)
            finish()
        }
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