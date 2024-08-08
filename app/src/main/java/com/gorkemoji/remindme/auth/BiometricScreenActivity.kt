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
import com.gorkemoji.remindme.databinding.ActivityBiometricScreenBinding

class BiometricScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBiometricScreenBinding
    private val switchDelay = 300L
    private var isTransitioning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBiometricScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* val isLocked = loadMode("is_locked", "auth") == "true"
        val isBiometricsEnabled = loadMode("biometrics", "auth") == "true"
        val isPasskeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (isLocked) navigateToAuthActivity(isBiometricsEnabled, isPasskeySet) */

        binding.onOffSwitch.isChecked = loadMode("biometrics", "auth") == "true"

        if (isPasswordSet()) {
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
                if (isChecked) saveMode("biometrics", "true", "auth")
                else {
                    removeAuth("is_locked")
                    removeAuth("biometrics")
                }
            }, switchDelay)
        }
    }

    /* private fun navigateToAuthActivity(biometricsEnabled: Boolean, passkeySet: Boolean) {
        isTransitioning = true
        var intent = Intent(this, PasswordActivity::class.java)
        if (biometricsEnabled && !passkeySet) intent = Intent(this, BiometricActivity::class.java)

        startActivity(intent)
    } */

    private fun isPasswordSet(): Boolean { return !loadMode("passkey", "auth").isNullOrBlank() }

    private fun removeAuth(type: String) {
        val pref: SharedPreferences = getSharedPreferences("auth", Context.MODE_PRIVATE)
        with(pref.edit()) {
            remove(type)
            apply()
        }
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
}