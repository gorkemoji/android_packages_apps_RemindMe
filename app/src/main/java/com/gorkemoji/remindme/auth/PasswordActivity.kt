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
import com.gorkemoji.remindme.databinding.ActivityPasswordBinding

class PasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.next.isClickable = false
        binding.next.isEnabled = false

        if (loadMode("passkey", "auth").isNullOrEmpty()) createPassword()
        else enterPassword()
    }

    private fun enterPassword() {
        binding.title.text = getString(R.string.enter_your_password)

        binding.passArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.next.isClickable = true
                binding.next.isEnabled = true
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.next.setOnClickListener {
            if (checkPassword()) {
                saveMode("is_locked", "false", "auth")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else handleIncorrectPassword()
        }
    }

    private fun createPassword() {
        binding.passArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.next.isClickable = true
                binding.next.isEnabled = true
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.next.setOnClickListener {
            saveMode("passkey", binding.passArea.text.toString(), "auth")
            saveMode("is_locked", "false", "auth")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun checkPassword(): Boolean { return binding.passArea.text.toString() == loadMode("passkey", "auth") }

    private fun handleIncorrectPassword() {
        binding.title.text = getString(R.string.try_again)

        vibratePhone()

        binding.passArea.text?.clear()
        binding.lockIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.red))
        binding.passEntry.boxStrokeColor = ContextCompat.getColor(applicationContext, R.color.red)
        binding.passArea.isActivated = false
        binding.passArea.isClickable = false
        binding.passArea.isEnabled = false
        binding.next.isClickable = false
        binding.next.isEnabled = false

        val timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) { binding.passArea.hint = getString(R.string.countdown_format, millisUntilFinished / 1000, getString(R.string.seconds)) }

            override fun onFinish() {
                binding.title.text = getString(R.string.enter_your_password)

                binding.lockIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.app_accent))
                binding.passEntry.boxStrokeColor = ContextCompat.getColor(applicationContext, R.color.app_accent)
                binding.passArea.hint = ""
                binding.passArea.isActivated = true
                binding.passArea.isClickable = true
                binding.passArea.isEnabled = true
                binding.next.isClickable = true
                binding.next.isEnabled = true
            }
        }
        timer.start()
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
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
}