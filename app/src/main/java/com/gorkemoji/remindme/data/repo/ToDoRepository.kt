package com.gorkemoji.remindme.data.repo

import androidx.lifecycle.LiveData
import com.gorkemoji.remindme.data.model.ToDo
import com.gorkemoji.remindme.data.dao.ToDoDao

class ToDoRepository(private val toDoDao: ToDoDao) {
    val allToDos: LiveData<List<ToDo>> = toDoDao.getAll()

    suspend fun insert(todo: ToDo): Long { return toDoDao.insert(todo) }
    suspend fun delete(todo: ToDo) { toDoDao.delete(todo) }
    suspend fun update(todo: ToDo) { toDoDao.update(todo) }
}