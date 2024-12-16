package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gorkemoji.remindme.databinding.ActivitySettingsBinding
import com.gorkemoji.remindme.utils.Utils
import kotlin.properties.Delegates

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private var themeColor by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        themeColor = loadMode("theme_color", "preferences")?.let {
            if (it.isNotEmpty()) Integer.parseInt(it) else 0
        } ?: 0

        Utils.onActivityCreateSetTheme(this, themeColor)

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.background = null

        val themes = resources.getStringArray(R.array.themes)
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, themes)

        binding.autoCompleteTextView.setAdapter(adapter)

        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedTheme = resources.getStringArray(R.array.themes)[position]

            val color = when (selectedTheme) {
                getString(R.string.red) -> 1
                getString(R.string.green) -> 2
                getString(R.string.yellow) -> 3
                else -> 0
            }

            saveMode("theme_color", color.toString(), "preferences")
            Utils.applyTheme(this, color)

            restartApp()
            //showToast(resources.getString(R.string.theme_applied))
        }

        binding.btnBackupRestore.setOnClickListener { startActivity(Intent(this, BackupRestoreActivity::class.java)) }

        binding.bottomNavigation.selectedItemId = R.id.item_settings
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.item_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.item_report -> {
                    startActivity(Intent(this, ReportActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }
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

    private fun restartApp() {
        finishAffinity()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    //private fun showToast(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
}