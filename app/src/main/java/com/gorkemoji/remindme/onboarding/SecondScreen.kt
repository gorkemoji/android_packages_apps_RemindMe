package com.gorkemoji.remindme.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.gorkemoji.remindme.R
import com.gorkemoji.remindme.databinding.FragmentSecondScreenBinding

class SecondScreen : Fragment() {
    private lateinit var binding: FragmentSecondScreenBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSecondScreenBinding.inflate(inflater, container, false)
        val view = binding.root

        val next = binding.next
        val vp = activity?.findViewById<ViewPager2>(R.id.view_pager)

        next.setOnClickListener { vp?.currentItem = 2 }

        return view
    }
}