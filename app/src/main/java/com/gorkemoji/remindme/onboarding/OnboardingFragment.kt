package com.gorkemoji.remindme.onboarding

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.gorkemoji.remindme.databinding.FragmentOnboardingBinding

class OnboardingFragment : FragmentActivity() {
    private lateinit var binding: FragmentOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentOnboardingBinding.inflate(layoutInflater)
        val view = binding.root
        val fragmentList = arrayListOf(FirstScreen(), SecondScreen(), ThirdScreen(), FourthScreen())
        val adapter = ViewPagerAdapter(fragmentList, supportFragmentManager, lifecycle)
        val vp = binding.viewPager
       // val indicator = binding.wormDotsIndicator

        vp.adapter = adapter
        //indicator.attachTo(vp)

        setContentView(view)
    }
}