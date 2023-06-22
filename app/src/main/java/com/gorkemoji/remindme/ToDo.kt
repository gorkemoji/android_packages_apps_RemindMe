package com.gorkemoji.remindme

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ToDos")
data class ToDo (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "todo")
    var todo: String,

    @ColumnInfo(name = "date")
    var date: String,

    @ColumnInfo(name = "time")
    var time: String
)