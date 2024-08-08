package com.gorkemoji.remindme.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ToDoDao {
    @Query("SELECT * FROM ToDos")
    fun getAll(): LiveData<List<ToDo>>

    // Normal list for exporting tasks.
    @Query("SELECT * FROM ToDos")
    suspend fun getAllTasks(): List<ToDo>

    @Query("SELECT * FROM ToDos WHERE id = :id")
    suspend fun getTaskById(id: Long): ToDo

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: ToDo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(todos: List<ToDo>): List<Long>

    @Delete
    suspend fun delete(todo: ToDo)

    @Update
    suspend fun update(todo: ToDo)
}