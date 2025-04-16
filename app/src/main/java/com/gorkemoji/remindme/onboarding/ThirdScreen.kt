package com.gorkemoji.remindme.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.gorkemoji.remindme.R
import com.gorkemoji.remindme.databinding.FragmentThirdScreenBinding

class ThirdScreen : Fragment() {
    private lateinit var binding: FragmentThirdScreenBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentThirdScreenBinding.inflate(inflater, container, false)
        val view = binding.root
        val next = binding.next

        val vp = activity?.findViewById<ViewPager2>(R.id.view_pager)

        binding.fileIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.metallic_blue)

        next.setOnClickListener { vp?.currentItem = 3 }

        return view
    }
}