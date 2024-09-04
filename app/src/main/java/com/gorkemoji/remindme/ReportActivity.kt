package com.gorkemoji.remindme

import android.content.Context
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

class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportBinding
    private var createdTasks: Int? = null
    private var doneTasks: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createdTasks = loadDetails("created_tasks")?.let { Integer.parseInt(it) }
        doneTasks = loadDetails("done_tasks")?.let { Integer.parseInt(it) }

        generateReport()
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

    private fun loadDetails(type: String): String? {
        val pref: SharedPreferences = applicationContext.getSharedPreferences("reports", Context.MODE_PRIVATE)
        return pref.getString(type, "")
    }
}
