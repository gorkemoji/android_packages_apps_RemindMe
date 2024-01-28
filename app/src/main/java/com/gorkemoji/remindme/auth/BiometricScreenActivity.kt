package com.gorkemoji.remindme.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.biometric.BiometricManager
import androidx.core.view.isVisible
import com.gorkemoji.remindme.R
import com.gorkemoji.remindme.SettingsActivity
import com.gorkemoji.remindme.databinding.ActivityBiometricScreenBinding

class BiometricScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBiometricScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBiometricScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.onOffSwitch.isChecked = loadMode("biometrics", "auth") == "true"

        if (isPinSet()) {
            binding.onOffSwitch.isVisible = false

            binding.desc.text = getString(R.string.already_set)
        }

        val biometricManager = BiometricManager.from(this)

        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            binding.onOffSwitch.isEnabled = true
            binding.onOffSwitch.isClickable = true
        } else {
            binding.onOffSwitch.isEnabled = false
            binding.onOffSwitch.isClickable = false
        }

        val debounceHandler = Handler(Looper.getMainLooper())

        binding.onOffSwitch.setOnCheckedChangeListener { _, isChecked ->
            debounceHandler.removeCallbacksAndMessages(null)
            debounceHandler.postDelayed({
                if (isChecked) {
                    saveMode("biometrics", "true", "auth")
                } else {
                    saveMode("biometrics", "false", "auth")
                }
            }, 300L)
        }
    }

    private fun isPinSet(): Boolean {
        if (!loadMode("passkey", "auth").isNullOrBlank())
            return true
        return false
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, SecurityActivity::class.java))
        finish()
    }
}