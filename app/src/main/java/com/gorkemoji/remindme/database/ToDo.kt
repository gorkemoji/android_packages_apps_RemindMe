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

    @ColumnInfo(name = "lock_status")
    var isLocked: Boolean = false,

    @ColumnInfo(name = "lock_type")
    var lockType: String,

    @ColumnInfo(name = "password")
    var password: String,

    @ColumnInfo(name = "font")
    var font: String,

    @ColumnInfo(name = "title")
    var toDoTitle: String,

    @ColumnInfo(name = "done")
    var isChecked: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
)