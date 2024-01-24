package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gorkemoji.remindme.database.ToDo
import com.gorkemoji.remindme.database.ToDoAdapter
import com.gorkemoji.remindme.database.ToDoDatabase
import com.gorkemoji.remindme.databinding.ActivityMainBinding
import kotlinx.coroutines.MainScope

class MainActivity : AppCompatActivity() {

    private lateinit var database: ToDoDatabase
    private lateinit var binding: ActivityMainBinding
    private val list = arrayListOf<ToDo>()
    private lateinit var adapter: ToDoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = ToDoDatabase.getDatabase(this)
        adapter = ToDoAdapter(list, database.getDao(), MainScope())

        when (loadMode("theme")) {
            "null" -> {
                saveMode("light", "theme")
                startActivity(Intent(this, MainActivity::class.java))
            }

            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        database.getDao().getAll().observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                list.clear()
                list.addAll(it)
                list.reverse()
                adapter.notifyDataSetChanged()
            }
            else {
                list.clear()
                adapter.notifyDataSetChanged()
            }
        })

        binding.settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom)
        }

        binding.addBtn.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom)
        }
    }

    private fun loadMode(type: String): String? {
        val pref : SharedPreferences = applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        return pref.getString("theme", type)
    }

    private fun saveMode(data: String, type: String) {
        val pref : SharedPreferences = applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = pref.edit()

        editor.putString("theme", data)
        editor.apply()
    }

}