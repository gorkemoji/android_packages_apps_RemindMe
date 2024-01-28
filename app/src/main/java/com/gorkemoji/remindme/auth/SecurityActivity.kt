package com.gorkemoji.remindme.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gorkemoji.remindme.R
import com.gorkemoji.remindme.SettingsActivity
import com.gorkemoji.remindme.databinding.ActivitySecurityBinding

class SecurityActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecurityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bioTitle.setOnClickListener {
            startActivity(Intent(this, BiometricScreenActivity::class.java))
            finish()
        }

        binding.pinTitle.setOnClickListener {
            startActivity(Intent(this, PasswordScreenActivity::class.java))
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
    }
}