package com.gorkemoji.remindme.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ToDoDao {
    @Query("SELECT * FROM ToDos")
    fun getAll(): LiveData<List<ToDo>>

    @Insert
    fun insert(todo: ToDo)

    @Delete
    fun delete(todo: ToDo)

    @Update
    fun update(todo: ToDo)
}