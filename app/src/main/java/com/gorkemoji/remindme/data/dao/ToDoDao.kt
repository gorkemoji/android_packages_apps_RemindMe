package com.gorkemoji.remindme.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.gorkemoji.remindme.data.model.ToDo

@Dao
interface ToDoDao {
    // Planning combining functions.
    @Query("SELECT * FROM ToDos ORDER BY id DESC")
    fun getAll(): LiveData<List<ToDo>>

    // Normal list for exporting tasks.
    @Query("SELECT * FROM ToDos ORDER BY id DESC")
    suspend fun exportAll(): List<ToDo>

    @Query("SELECT * FROM ToDos WHERE id = :id LIMIT 1")
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