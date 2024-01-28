package com.gorkemoji.remindme.onboarding

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.gorkemoji.remindme.auth.PasswordActivity
import com.gorkemoji.remindme.databinding.FragmentOnboardingBinding

class OnboardingFragment : FragmentActivity() {
    private lateinit var binding: FragmentOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!loadMode("passkey", "auth").isNullOrEmpty() && loadMode("is_locked", "auth") == "true") {
            startActivity(Intent(this, PasswordActivity::class.java))
            finish()
        }

        super.onCreate(savedInstanceState)

        binding = FragmentOnboardingBinding.inflate(layoutInflater)
        val view = binding.root
        val fragmentList = arrayListOf(FirstScreen(), SecondScreen(), ThirdScreen())
        val adapter = ViewPagerAdapter(fragmentList, supportFragmentManager, lifecycle)
        val vp = binding.viewPager
        val indicator = binding.wormDotsIndicator

        vp.adapter = adapter
        indicator.attachTo(vp)

        setContentView(view)
    }

    private fun loadMode(type: String, file: String): String? {
        val pref : SharedPreferences = applicationContext.getSharedPreferences(file, Context.MODE_PRIVATE)

        return pref.getString(type, "")
    }
}