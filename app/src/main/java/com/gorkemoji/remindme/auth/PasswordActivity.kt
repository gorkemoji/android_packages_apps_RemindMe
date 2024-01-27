package com.gorkemoji.remindme.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.gorkemoji.remindme.MainActivity
import com.gorkemoji.remindme.R
import com.gorkemoji.remindme.SettingsActivity
import com.gorkemoji.remindme.TaskActivity
import com.gorkemoji.remindme.databinding.ActivityPasswordBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.Timer
import java.util.TimerTask

class PasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.next.isClickable = false
        binding.next.isEnabled = false

        if (loadMode("passkey", "auth").isNullOrEmpty()) {
            binding.pinArea.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (binding.pinArea.text.toString().filter { it.isDigit() }.count() == 4) {
                        binding.next.isClickable = true
                        binding.next.isEnabled = true
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            binding.next.setOnClickListener {
                saveMode("passkey", binding.pinArea.text.toString(), "auth")
                saveMode("first_start", "false", "preferences")
                saveMode("is_locked", "false", "auth")
                Toast.makeText(
                    this@PasswordActivity,
                    getString(R.string.pin_created, binding.pinArea.text.toString()),
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        /*else if(loadMode("is_changing", "auth") == "true"){
            binding.title.text = getString(R.string.enter_your_pin)
            binding.pinArea.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (binding.pinArea.text.toString().filter { it.isDigit() }.count() == 4 && binding.pinArea.text.toString() == loadMode("passkey", "auth")) {
                        binding.next.isClickable = true
                        binding.next.isEnabled = true
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            binding.next.setOnClickListener {
                binding.title.text = getString(R.string.enter_new_pin)
                binding.pinArea.addTextChangedListener(object: TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (binding.pinArea.text.toString().filter { it.isDigit() }.count() == 4) {
                            binding.next.isClickable = true
                            binding.next.isEnabled = true
                        }
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })

                binding.next.setOnClickListener {
                    saveMode("passkey", binding.pinArea.text.toString(), "auth")
                    Toast.makeText(this@PasswordActivity, getString(R.string.pin_created, binding.pinArea.text.toString()), Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                }
            }
        }*/
        else {
            binding.title.text = getString(R.string.enter_your_pin)
            binding.pinArea.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (binding.pinArea.text.toString().filter { it.isDigit() }.count() == 4) {
                        binding.next.isClickable = true
                        binding.next.isEnabled = true
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            binding.next.setOnClickListener {
                if (binding.pinArea.text.toString() == loadMode("passkey", "auth")) {
                    saveMode("is_locked", "false", "auth")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    binding.title.text = getString(R.string.try_again)
                    binding.pinArea.text?.clear()
                    //  binding.lockIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.red))
                    binding.pinEntry.boxStrokeColor = resources.getColor(R.color.red)
                    binding.pinArea.isActivated = false
                    binding.pinArea.isClickable = false
                    binding.pinArea.isEnabled = false
                    binding.next.isClickable = false
                    binding.next.isEnabled = false

                    val timer = object : CountDownTimer(5000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            binding.pinArea.hint = getString(
                                R.string.countdown_format,
                                millisUntilFinished / 1000,
                                getString(R.string.seconds)
                            )
                        }

                        override fun onFinish() {
                            //  binding.lockIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.app_accent))
                            binding.pinEntry.boxStrokeColor = resources.getColor(R.color.app_accent)
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
            }
        }
    }

        private fun loadMode(type: String, file: String): String? {
            val pref: SharedPreferences =
                applicationContext.getSharedPreferences(file, Context.MODE_PRIVATE)

            return pref.getString(type, "")
        }

        private fun saveMode(type: String, data: String, file: String) {
            val pref: SharedPreferences =
                applicationContext.getSharedPreferences(file, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()

            editor.putString(type, data)
            editor.apply()
        }

        @Deprecated("Deprecated in Java", ReplaceWith("finishAffinity()"))
        override fun onBackPressed() {
            super.onBackPressed()
            saveMode("is_locked", "true", "auth")
            finishAffinity()
        }
    }