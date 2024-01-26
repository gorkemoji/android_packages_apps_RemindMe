package com.gorkemoji.remindme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gorkemoji.remindme.database.ToDo
import com.gorkemoji.remindme.database.ToDoDatabase
import com.gorkemoji.remindme.databinding.ActivityTaskBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskBinding

    val database by lazy {
        ToDoDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
                    val id = withContext(Dispatchers.IO) {
                        when (mode) {
                            1 -> return@withContext database.getDao().insert(ToDo(toDoTitle = toDoTitle, isChecked = false))
                            2 -> return@withContext database.getDao().update(ToDo(id = id, toDoTitle = toDoTitle, isChecked = checkBoxState))
                        }
                    }
                }
                finish()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
    }
}