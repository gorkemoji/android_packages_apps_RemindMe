package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gorkemoji.remindme.database.ToDo
import com.gorkemoji.remindme.database.ToDoAdapter
import com.gorkemoji.remindme.database.ToDoDatabase
import com.gorkemoji.remindme.databinding.ActivityMainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var database: ToDoDatabase
    private lateinit var binding: ActivityMainBinding
    private val list = arrayListOf<ToDo>()
    private lateinit var adapter: ToDoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        when (loadMode("theme")) {
            "dark" ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else ->
                saveMode("light", "theme")
        }

        super.onCreate(savedInstanceState)

        database = ToDoDatabase.getDatabase(this)
        adapter = ToDoAdapter(list, database.getDao(), MainScope())

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.selectedItemId = R.id.tasks

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.settings) {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                overridePendingTransition(0, 0)
                true
            } else item.itemId == R.id.tasks
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

        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition
                MainScope().launch {
                    database.getDao().delete(list[position])
                }
            }
        }).attachToRecyclerView(binding.recyclerView)

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

    @Deprecated("Deprecated in Java", ReplaceWith("finishAffinity()"))
    override fun onBackPressed() {
        finishAffinity()
    }
}