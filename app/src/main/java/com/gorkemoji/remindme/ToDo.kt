package com.gorkemoji.remindme

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ToDos")
data class ToDo (
    @ColumnInfo(name = "title")
    var toDoTitle: String,

    @ColumnInfo(name = "isChecked")
    var isChecked: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
)