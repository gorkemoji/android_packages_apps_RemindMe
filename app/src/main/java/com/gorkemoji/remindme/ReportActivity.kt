package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.gorkemoji.remindme.databinding.ActivityReportBinding
import com.gorkemoji.remindme.utils.Utils
import kotlin.properties.Delegates

class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportBinding
    private var themeColor by Delegates.notNull<Int>()
    private var createdTasks: Int? = null
    private var doneTasks: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        themeColor = loadMode("theme_color", "preferences")?.let {
            if (it.isNotEmpty()) Integer.parseInt(it) else 0
        } ?: 0

        Utils.onActivityCreateSetTheme(this, themeColor)

        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.background = null

        createdTasks = loadMode("created_tasks", "reports")?.let { Integer.parseInt(it) }
        doneTasks = loadMode("done_tasks", "reports")?.let { Integer.parseInt(it) }

        generateReport()

        binding.bottomNavigation.selectedItemId = R.id.item_report
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.item_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.item_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }
    }

    private fun generateReport() {
        val entries = listOfNotNull(createdTasks?.let {
            PieEntry(it.toFloat(), resources.getString(R.string.created))
        },
            doneTasks?.let { PieEntry(it.toFloat(), resources.getString(R.string.done)) })

        val accentColor = resources.getColor(R.color.app_accent, null)
        val greenColor = resources.getColor(R.color.green, null)
        val colors = arrayListOf(accentColor, greenColor)

        val dataset = PieDataSet(entries, resources.getString(R.string.report))
        dataset.colors = colors

        val textColor = if (isDarkModeOn()) Color.WHITE else Color.BLACK

        dataset.valueTextColor = Color.WHITE
        binding.pieChart.setEntryLabelColor(Color.WHITE)
        binding.pieChart.setCenterTextColor(textColor)

        binding.pieChart.setHoleColor(android.R.attr.background)
        binding.pieChart.legend.textColor = textColor

        val pieData = PieData(dataset)
        binding.pieChart.data = pieData
        binding.pieChart.centerText = resources.getString(R.string.since_app_installation)

        binding.pieChart.animateY(1000, Easing.EaseInOutQuad)
        binding.pieChart.invalidate()
    }

    private fun isDarkModeOn(): Boolean {
        return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun loadMode(type: String, file: String): String? {
        val pref: SharedPreferences = applicationContext.getSharedPreferences(file, Context.MODE_PRIVATE)
        return pref.getString(type, "")
    }
}