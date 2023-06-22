package com.gorkemoji.remindme

import androidx.room.*

@Dao
interface ToDoDao {
    @Query("SELECT * FROM todo")
    fun getAll(): List<ToDo>

    @Insert
    fun insert(todo: ToDo)

    @Update
    fun update(todo: ToDo)

    @Delete
    fun delete(todo: ToDo)
}