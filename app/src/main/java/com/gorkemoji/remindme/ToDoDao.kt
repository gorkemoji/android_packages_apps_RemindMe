package com.gorkemoji.remindme

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ToDoDao {
    @Query("SELECT * FROM ToDos")
    fun getAll(): LiveData<List<ToDo>>

    @Insert
    fun insert(todo: ToDo)
}