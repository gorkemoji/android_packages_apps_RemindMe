package com.gorkemoji.remindme.onboarding

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gorkemoji.remindme.MainActivity
import com.gorkemoji.remindme.databinding.FragmentThirdScreenBinding

class ThirdScreen : Fragment() {
    private lateinit var binding: FragmentThirdScreenBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentThirdScreenBinding.inflate(inflater, container, false)
        val view = binding.root

        val start = binding.start

        start.setOnClickListener {
            saveMode("first_start", "false", "preferences")
            startActivity(Intent(activity, MainActivity::class.java))
            onDestroyView()
        }

        return view
    }

    private fun saveMode(type: String, data: String, file: String) {
        val pref: SharedPreferences = requireContext().getSharedPreferences(file, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()

        editor.putString(type, data)
        editor.apply()
    }
}