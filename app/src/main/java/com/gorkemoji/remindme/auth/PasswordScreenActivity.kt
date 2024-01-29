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
                    startActivity(Intent(this, PasswordActivity::class.java), ActivityOptions.makeCustomAnimation(this, R.anim.slide_out_bottom, R.anim.slide_in_bottom).toBundle())
                    finish()
                } else {
                    saveMode("passkey", "", "auth")
                   /* saveMode("is_last", "true", "auth")
                    startActivity(Intent(this, PasswordActivity::class.java))
                    finish()*/
                }
            }, switchDelay)
        }

        binding.chnPass.setOnClickListener {
            saveMode("is_changing", "true", "auth")
            startActivity(Intent(this, PasswordActivity::class.java), ActivityOptions.makeCustomAnimation(this, R.anim.slide_out_bottom, R.anim.slide_in_bottom).toBundle())
            finish()
        }
    }

    private fun isBioSet(): Boolean {
        if (loadMode("biometrics", "auth") == "true")
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
        startActivity(Intent(this, SecurityActivity::class.java), ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_bottom, R.anim.slide_out_bottom).toBundle())
        finish()
    }
}