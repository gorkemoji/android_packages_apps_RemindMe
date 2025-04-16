package com.gorkemoji.remindme.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ToDos")
data class ToDo (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "done")
    var isChecked: Boolean = false,

    @ColumnInfo(name = "priority")
    var priority: Int,

    @ColumnInfo(name = "title")
    var toDoTitle: String,

    @ColumnInfo(name = "font")
    var font: String,

    @ColumnInfo(name = "reminder")
    var isReminderOn: Boolean = false,

    @ColumnInfo(name = "date")
    var dueDate: Long? = null,

    @ColumnInfo(name = "lock")
    var isLocked: Boolean = false,

    @ColumnInfo(name = "lock_type")
    var lockType: String,

    @ColumnInfo(name = "password")
    var password: String
)