package com.gorkemoji.remindme.onboarding

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.gorkemoji.remindme.MainActivity
import com.gorkemoji.remindme.R
import com.gorkemoji.remindme.auth.PasswordActivity
import com.gorkemoji.remindme.databinding.FragmentFirstScreenBinding
import com.gorkemoji.remindme.databinding.FragmentThirdScreenBinding

class ThirdScreen : Fragment() {
    private lateinit var binding: FragmentThirdScreenBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentThirdScreenBinding.inflate(inflater, container, false)
        val view = binding.root

        val start = binding.start

        start.setOnClickListener {
            startActivity(Intent(activity, PasswordActivity::class.java))
            onDestroyView()
        }

        return view
    }
}