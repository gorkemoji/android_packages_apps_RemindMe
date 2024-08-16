package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gorkemoji.remindme.auth.BiometricActivity
import com.gorkemoji.remindme.auth.PasswordActivity
import com.gorkemoji.remindme.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private var isTransitioning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val themeColor = loadMode("theme_color", "preferences") ?: "blue"
        setThemeColor(themeColor)
        updateComponentColors(getThemeColorResource(themeColor))

        val themes = resources.getStringArray(R.array.themes)
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, themes)

        binding.autoCompleteTextView.setAdapter(adapter)

        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedTheme = resources.getStringArray(R.array.themes)[position]

            val color = when (selectedTheme) {
                getString(R.string.red) -> "red"
                getString(R.string.blue) -> "blue"
                getString(R.string.yellow) -> "yellow"
                getString(R.string.green) -> "green"
                else -> "blue"
            }

            saveMode("theme_color", color, "preferences")
            setThemeColor(themeColor)
            showToast(resources.getString(R.string.theme_applied))
        }


        /* binding.colorRed.setOnClickListener {
             saveMode("theme_color", "red", "preferences")
             setThemeColor("red")
             showToast(resources.getString(R.string.theme_applied))
         }

        binding.colorBlue.setOnClickListener {
            saveMode("theme_color", "blue", "preferences")
            setThemeColor("blue")
            showToast(resources.getString(R.string.theme_applied))
        }

        binding.colorYellow.setOnClickListener {
            saveMode("theme_color", "yellow", "preferences")
            setThemeColor("yellow")
            showToast(resources.getString(R.string.theme_applied))
        }

        binding.colorGreen.setOnClickListener {
            saveMode("theme_color", "green", "preferences")
            setThemeColor("green")
            showToast(resources.getString(R.string.theme_applied))
        }*/

        binding.btnBackupRestore.setOnClickListener {
            isTransitioning = true
            startActivity(Intent(this, BackupRestoreActivity::class.java))
        }

        /*
        binding.btnSecurity.setOnClickListener {
            isTransitioning = true
            startActivity(Intent(this, SecurityActivity::class.java))
        } */
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
        val pref: SharedPreferences = applicationContext.getSharedPreferences(file, Context.MODE_PRIVATE)
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
        binding.btnBackupRestore.backgroundTintList = colorStateList
        binding.boxLayout.boxStrokeColor = ContextCompat.getColor(this, colorResId)
        binding.boxLayout.hintTextColor = colorStateList
        binding.boxLayout.setEndIconTintList(colorStateList)
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

    private fun showToast(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
}