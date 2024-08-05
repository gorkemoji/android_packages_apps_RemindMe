package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gorkemoji.remindme.auth.BiometricActivity
import com.gorkemoji.remindme.auth.PasswordActivity
import com.gorkemoji.remindme.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
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

        if (!isLocked && (isBiometricsEnabled || passkeySet) && !travelling)
            saveMode("is_locked", "true", "auth")
    }

    override fun onPause() {
        super.onPause()

        val isLocked = loadMode("is_locked", "auth") == "true"
        val isBiometricsEnabled = loadMode("biometrics", "auth") == "true"
        val isPasskeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (!isLocked && (isBiometricsEnabled || isPasskeySet) && !travelling)
            saveMode("is_locked", "true", "auth")
    }

    override fun onResume() {
        super.onResume()

        if (loadMode("is_locked", "auth") == "true") {
            val intent = if (loadMode("biometrics", "auth") == "true") Intent(this, BiometricActivity::class.java)
            else Intent(this, PasswordActivity::class.java)
            startActivity(intent)
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
}