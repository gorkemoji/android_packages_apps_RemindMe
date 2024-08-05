package com.gorkemoji.remindme.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import com.gorkemoji.remindme.MainActivity
import com.gorkemoji.remindme.R
import com.gorkemoji.remindme.SettingsActivity
import com.gorkemoji.remindme.databinding.ActivityPasswordBinding

class PasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordBinding
    private var prev: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prev = intent.getStringExtra("prevActivity")

        if (loadMode("is_last", "auth") == "true") {
            saveMode("passkey", "", "auth")
            enterPin()
            saveMode("is_last", "false", "auth")
        }

        binding.next.isClickable = false
        binding.next.isEnabled = false

        if (loadMode("passkey", "auth").isNullOrEmpty())
            createPin()
        else if (loadMode("is_changing", "auth") == "true")
            changePin(1)
        else
            enterPin()
    }

    private fun enterPin() {
        binding.title.text = getString(R.string.enter_your_pin)

        binding.pinArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (checkDigit()) {
                    binding.next.isClickable = true
                    binding.next.isEnabled = true
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.next.setOnClickListener {
            if (checkPin()) {
                saveMode("is_locked", "false", "auth")
                navigateToPreviousActivity()
                finish()
            } else
                handleIncorrectPin(1)
        }
    }

    private fun createPin() {
        binding.pinArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (checkDigit()) {
                    binding.next.isClickable = true
                    binding.next.isEnabled = true
                } else {
                    binding.next.isClickable = false
                    binding.next.isEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.next.setOnClickListener {
            saveMode("passkey", binding.pinArea.text.toString(), "auth")
            saveMode("is_locked", "false", "auth")
            navigateToPreviousActivity()
            finish()
        }
    }

    private fun changePin(pageNum: Int) {
        if (pageNum == 1) {
            binding.title.text = getString(R.string.enter_your_pin)

            binding.pinArea.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (checkDigit() && checkPin()) {
                        binding.next.isClickable = true
                        binding.next.isEnabled = true
                    } else {
                        binding.next.isClickable = false
                        binding.next.isEnabled = false
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            binding.next.setOnClickListener { changePin(2) }

        } else {
            binding.title.text = getString(R.string.enter_new_pin)
            binding.pinArea.text?.clear()

            binding.pinArea.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (checkDigit()) {
                        binding.next.isClickable = true
                        binding.next.isEnabled = true
                    } else {
                        binding.next.isClickable = false
                        binding.next.isEnabled = false
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            binding.next.setOnClickListener {
                if (checkPin())
                    handleIncorrectPin(2)
                else {
                    saveMode("passkey", binding.pinArea.text.toString(), "auth")
                    saveMode("is_changing", "false", "auth")
                    navigateToPreviousActivity()
                    finish()
                }
            }
        }
    }

    private fun checkDigit() : Boolean {
        return binding.pinArea.text.toString().count { it.isDigit() } == 4
    }

    private fun checkPin(): Boolean {
        return binding.pinArea.text.toString() == loadMode("passkey", "auth")
    }

    private fun handleIncorrectPin(mode: Int) {
        when (mode) {
            1 -> binding.title.text = getString(R.string.try_again)
            2 -> binding.title.text = getString(R.string.cant_be_same)
        }

        vibratePhone()
        binding.pinArea.text?.clear()
        binding.lockIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.red))
        binding.pinEntry.boxStrokeColor = ContextCompat.getColor(applicationContext, R.color.red)
        binding.pinArea.isActivated = false
        binding.pinArea.isClickable = false
        binding.pinArea.isEnabled = false
        binding.next.isClickable = false
        binding.next.isEnabled = false

        val timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.pinArea.hint = getString(R.string.countdown_format, millisUntilFinished / 1000, getString(R.string.seconds))
            }

            override fun onFinish() {
                when (mode) {
                    1 -> binding.title.text = getString(R.string.enter_your_pin)
                    2 -> binding.title.text = getString(R.string.enter_new_pin)
                }

                binding.lockIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.app_accent))
                binding.pinEntry.boxStrokeColor = ContextCompat.getColor(applicationContext, R.color.app_accent)
                binding.pinArea.hint = ""
                binding.pinArea.isActivated = true
                binding.pinArea.isClickable = true
                binding.pinArea.isEnabled = true
                binding.next.isClickable = true
                binding.next.isEnabled = true
            }
        }
        timer.start()
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }

    private fun navigateToPreviousActivity() {
        val intent = when (prev) {
            "MainActivity" -> Intent(this, MainActivity::class.java)
            "SettingsActivity" -> Intent(this, SettingsActivity::class.java)
            else -> null
        }
        intent?.let { startActivity(it) }
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