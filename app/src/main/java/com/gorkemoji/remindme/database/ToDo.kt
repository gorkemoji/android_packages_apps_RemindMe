package com.gorkemoji.remindme.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ToDos")
data class ToDo (
    @ColumnInfo(name = "date")
    var dueDate: Long? = null,

    @ColumnInfo(name = "reminder_status")
    var isReminderOn: Boolean = false,

    @ColumnInfo(name = "title")
    var toDoTitle: String,

    @ColumnInfo(name = "done")
    var isChecked: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
)