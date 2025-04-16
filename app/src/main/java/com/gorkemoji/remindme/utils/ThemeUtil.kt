package com.gorkemoji.remindme.utils

import android.app.Activity
import android.content.Intent
import com.gorkemoji.remindme.R

class ThemeUtil {
    companion object {
        private var currentTheme: Int = 0
        const val THEME_DEFAULT = 0
        const val THEME_CRIMSON = 1
        const val THEME_OLIVE_GREEN = 2
        const val THEME_AMBER = 3
        const val THEME_LILAC = 4

        fun applyTheme(activity: Activity, theme: Int) {
            currentTheme = theme
            activity.finish()
            activity.startActivity(Intent(activity, activity.javaClass))
        }

        fun onActivityCreateSetTheme(activity: Activity, currentTheme: Int) {
            when (currentTheme) {
                THEME_DEFAULT -> activity.setTheme(R.style.Theme_RemindMe)
                THEME_CRIMSON -> activity.setTheme(R.style.Theme_RemindMe_Crimson)
                THEME_OLIVE_GREEN -> activity.setTheme(R.style.Theme_RemindMe_OliveGreen)
                THEME_AMBER -> activity.setTheme(R.style.Theme_RemindMe_Amber)
                THEME_LILAC -> activity.setTheme(R.style.Theme_RemindMe_Lilac)
            }
        }
    }
}