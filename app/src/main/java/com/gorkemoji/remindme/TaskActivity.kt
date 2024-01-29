package com.gorkemoji.remindme

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gorkemoji.remindme.auth.PasswordActivity
import com.gorkemoji.remindme.database.ToDo
import com.gorkemoji.remindme.database.ToDoDatabase
import com.gorkemoji.remindme.databinding.ActivityTaskBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskBinding

    private val database by lazy {
        ToDoDatabase.getDatabase(this)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!loadMode("passkey", "auth").isNullOrEmpty() && loadMode("is_locked", "auth") == "true") {
            startActivity(Intent(this, PasswordActivity::class.java))
            finish()
        }

        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mode = intent.getIntExtra("mode", 1)
        val id = intent.getLongExtra("id", -1L)
        val taskName = intent.getStringExtra("taskName")
        val checkBoxState = intent.getBooleanExtra("cbState", false)

        binding.taskText.hint = taskName

        binding.saveBtn.setOnClickListener {
            if (binding.taskText.text?.isNotBlank() == true) {
                val toDoTitle: String = binding.taskText.text.toString()

                GlobalScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        when (mode) {
                            1 -> return@withContext database.getDao().insert(ToDo(toDoTitle = toDoTitle, isChecked = false))
                            2 -> return@withContext database.getDao().update(ToDo(id = id, toDoTitle = toDoTitle, isChecked = checkBoxState))
                        }
                    }
                }
            }
            startActivity(Intent(this, MainActivity::class.java), ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_bottom, R.anim.slide_out_bottom).toBundle())
            finish()
        }
    }

    private fun loadMode(type: String, file: String): String? {
        val pref : SharedPreferences = applicationContext.getSharedPreferences(file, Context.MODE_PRIVATE)

        return pref.getString(type, "")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java), ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_bottom, R.anim.slide_out_bottom).toBundle())
        finish()
    }
}