package com.gorkemoji.remindme.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.gorkemoji.remindme.MainActivity
import com.gorkemoji.remindme.R
import com.gorkemoji.remindme.databinding.FragmentFourthScreenBinding

class FourthScreen : Fragment() {
    private lateinit var binding: FragmentFourthScreenBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFourthScreenBinding.inflate(inflater, container, false)
        val view = binding.root

        val start = binding.start

        start.setOnClickListener {
            // notification permissions
            askNotificationPermission()
        }

        return view
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permission already granted
                proceedToMainActivity()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // For devices below Android 13 (TIRAMISU), proceed directly
            proceedToMainActivity()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(requireContext(), "Notifications permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Snackbar.make(requireView(), getString(R.string.txt_error_post_notification), Snackbar.LENGTH_LONG).setAction(getString(R.string.goto_settings)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val settingsIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    startActivity(settingsIntent)
                }
            }.show()
        }
        // Proceed to MainActivity regardless of permission result
        proceedToMainActivity()
    }


    private fun proceedToMainActivity() {
        saveStart()
        startActivity(Intent(activity, MainActivity::class.java))
        activity?.finish()
    }

    private fun saveStart() {
        val pref: SharedPreferences = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()

        editor.putString("first_start", "false")
        editor.apply()
    }
}
