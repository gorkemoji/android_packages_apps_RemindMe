package com.gorkemoji.remindme.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ToDoDao {
    @Query("SELECT * FROM ToDos")
    fun getAll(): LiveData<List<ToDo>>

    @Query("SELECT * FROM ToDos WHERE id = :id")
    suspend fun getTaskById(id: Long): ToDo

    @Insert
    fun insert(todo: ToDo): Long

    @Delete
    fun delete(todo: ToDo)

    @Update
    fun update(todo: ToDo)
}