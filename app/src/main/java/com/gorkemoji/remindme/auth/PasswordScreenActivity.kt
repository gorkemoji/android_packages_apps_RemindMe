package com.gorkemoji.remindme.auth

import android.app.ActivityOptions
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.view.isVisible
import com.gorkemoji.remindme.R
import com.gorkemoji.remindme.databinding.ActivityPasswordScreenBinding

class PasswordScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordScreenBinding
    private val switchDelay = 300L
    private var travelling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPasswordScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var isPinSet = !loadMode("passkey", "auth").isNullOrBlank()

        binding.onOffSwitch.isChecked = isPinSet
        binding.chnPass.isVisible = isPinSet

        if (isBioSet()) {
            binding.onOffSwitch.isVisible = false
            binding.desc.text = getString(R.string.already_set)
        }

        val debounceHandler = Handler(Looper.getMainLooper())

        binding.onOffSwitch.setOnCheckedChangeListener { _, isChecked ->
            debounceHandler.removeCallbacksAndMessages(null)
            debounceHandler.postDelayed({
                if (isChecked) {
                    travelling = true
                    val intent = Intent(this@PasswordScreenActivity, PasswordActivity::class.java)
                    intent.putExtra("prevActivity", "PasswordScreenActivity")
                    startActivity(intent, ActivityOptions.makeCustomAnimation(this, R.anim.slide_out_bottom, R.anim.slide_in_bottom).toBundle())
                    finish()
                } else {
                    removeAuth("passkey")
                    removeAuth("is_locked")
                    removeAuth("is_changing")
                }
            }, switchDelay)
        }

        binding.chnPass.setOnClickListener {
            travelling = true
            saveMode("is_changing", "true", "auth")
            val intent = Intent(this@PasswordScreenActivity, PasswordActivity::class.java)
            intent.putExtra("prevActivity", "PasswordScreenActivity")
            startActivity(intent, ActivityOptions.makeCustomAnimation(this, R.anim.slide_out_bottom, R.anim.slide_in_bottom).toBundle())
            finish()
        }
    }

    private fun isBioSet(): Boolean {
        return loadMode("biometrics", "auth") == "true"
    }

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

        val isLocked = loadMode("is_locked", "auth") == "true"
        val isBiometricsEnabled = loadMode("biometrics", "auth") == "true"
        val isPasskeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (!isLocked && (isBiometricsEnabled || isPasskeySet))
            saveMode("is_locked", "true", "auth")
    }

    override fun onPause() {
        super.onPause()

        val isLocked = loadMode("is_locked", "auth") == "true"
        val isBiometricsEnabled = loadMode("biometrics", "auth") == "true"
        val isPasskeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (!isLocked && (isBiometricsEnabled || isPasskeySet))
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
}