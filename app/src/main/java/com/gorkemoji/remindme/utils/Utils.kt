package com.gorkemoji.remindme.utils

import android.app.Activity
import android.content.Intent
import com.gorkemoji.remindme.R

class Utils {
    companion object {
        private var currentTheme: Int = 0
        const val THEME_DEFAULT = 0
        const val THEME_RED = 1
        const val THEME_GREEN = 2
        const val THEME_YELLOW = 3

        fun applyTheme(activity: Activity, theme: Int) {
            currentTheme = theme
            activity.finish()
            activity.startActivity(Intent(activity, activity.javaClass))
        }

        fun onActivityCreateSetTheme(activity: Activity, currentTheme: Int) {
            when (currentTheme) {
                THEME_DEFAULT -> activity.setTheme(R.style.Theme_RemindMe)
                THEME_RED -> activity.setTheme(R.style.Theme_RemindMe_Red)
                THEME_GREEN -> activity.setTheme(R.style.Theme_RemindMe_Green)
                THEME_YELLOW -> activity.setTheme(R.style.Theme_RemindMe_Yellow)
            }
        }
    }
}