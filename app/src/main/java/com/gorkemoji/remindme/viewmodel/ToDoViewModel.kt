package com.gorkemoji.remindme.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.gorkemoji.remindme.data.model.ToDo
import com.gorkemoji.remindme.data.db.ToDoDatabase
import com.gorkemoji.remindme.data.repo.ToDoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToDoViewModel(application: Application) : AndroidViewModel(application) {
    val allToDos: LiveData<List<ToDo>>
    private val repository: ToDoRepository

    init {
        val dao = ToDoDatabase.getDatabase(application).getDao()
        repository = ToDoRepository(dao)
        allToDos = repository.allToDos
    }

    suspend fun insertToDo(todo: ToDo): Long  { return repository.insert(todo) }
    fun deleteToDo(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) { repository.delete(todo) }
    fun updateToDo(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) { repository.update(todo) }
}