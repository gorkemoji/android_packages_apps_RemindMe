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
import androidx.core.view.isVisible
import com.gorkemoji.remindme.auth.BiometricActivity
import com.gorkemoji.remindme.auth.PasswordActivity
import com.gorkemoji.remindme.auth.SecurityActivity
import com.gorkemoji.remindme.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val switchDelay = 300L
    private var travelling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*val isLocked = loadMode("is_locked", "auth") == "true"
        val biometricsEnabled = loadMode("biometrics", "auth") == "true"
        val passkeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (isLocked)
            navigateToAuthActivity(biometricsEnabled, passkeySet)
         */

        binding.security.isEnabled = false // debugging
        binding.security.isClickable = false // debugging

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
                travelling = true
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

        binding.darkModeSwitch.isVisible = false
        binding.darkModeText.isVisible = false

        /*
        binding.security.setOnClickListener {
            if (!loadMode("passkey", "auth").isNullOrEmpty())
              //  saveMode("is_changing", "true", "auth")

            travelling = true
            val animationBundle = ActivityOptions.makeCustomAnimation(this, R.anim.slide_out_bottom, R.anim.slide_in_bottom).toBundle()
            startActivity(Intent(this, SecurityActivity::class.java), animationBundle)
            finish()
        }*/
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
    override fun onStop() {
        super.onStop()

        val isBiometricsEnabled = !loadMode("biometrics", "auth").isNullOrBlank() && loadMode("biometrics", "auth") == "true"
        val passkeySet = !loadMode("passkey", "auth").isNullOrBlank()
        val isLocked = loadMode("is_locked", "auth") == "true"

        if (!isLocked && (isBiometricsEnabled || passkeySet))
            if (!travelling)
                saveMode("is_locked", "true", "auth")
    }

    override fun onResume() {
        super.onResume()

        if (loadMode("is_locked", "auth") == "true") {
            if (loadMode("biometrics", "auth") == "true")
                startActivity(Intent(this, BiometricActivity::class.java))
            else
                startActivity(Intent(this, PasswordActivity::class.java))
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