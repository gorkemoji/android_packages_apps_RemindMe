package com.gorkemoji.remindme.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gorkemoji.remindme.databinding.ActivitySecurityBinding

class SecurityActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecurityBinding
    private var isTransitioning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* val isLocked = loadMode("is_locked", "auth") == "true"
        val isBiometricsEnabled = loadMode("biometrics", "auth") == "true"
        val isPasskeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (isLocked) navigateToAuthActivity(isBiometricsEnabled, isPasskeySet) */

        binding.bioBtn.setOnClickListener {
            isTransitioning = true
            //startActivity(Intent(this, BiometricScreenActivity::class.java))
        }

        binding.passBtn.setOnClickListener {
            isTransitioning = true
            //startActivity(Intent(this, PasswordScreenActivity::class.java))
        }
    }

    /* private fun navigateToAuthActivity(biometricsEnabled: Boolean, passkeySet: Boolean) {
        isTransitioning = true
        var intent = Intent(this, PasswordActivity::class.java)
        if (biometricsEnabled && !passkeySet) intent = Intent(this, BiometricActivity::class.java)

        startActivity(intent)
    } */

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