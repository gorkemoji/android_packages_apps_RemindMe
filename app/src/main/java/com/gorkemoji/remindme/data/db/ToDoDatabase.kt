package com.gorkemoji.remindme.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gorkemoji.remindme.data.dao.ToDoDao
import com.gorkemoji.remindme.data.model.ToDo

@Database(entities = [ToDo::class], version = 1)
abstract class ToDoDatabase : RoomDatabase() {
    abstract fun getDao(): ToDoDao

    companion object {
        @Volatile
        private var INSTANCE: ToDoDatabase? = null

        fun getDatabase(context: Context): ToDoDatabase {
            val tempInstance = INSTANCE

            if (tempInstance != null) return tempInstance

            synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, ToDoDatabase::class.java, "tasks").allowMainThreadQueries().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}