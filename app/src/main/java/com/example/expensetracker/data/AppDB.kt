package com.example.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.util.concurrent.Executors

@Database(entities = [Expense::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDB : RoomDatabase() {

    abstract fun expenseDAO() : ExpenseDAO

    companion object{

        private var db : AppDB? = null

        private const val NUMBER_OF_THREADS = 4
        val databaseQueryExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        fun getDB(context: Context) : AppDB?{
            if (db == null){
                    db = Room.databaseBuilder(
                        context.applicationContext,
                        AppDB::class.java,
                        " com.example.expenseTracker_db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return db
        }

    }

}

/*
database version number must increase every time you change it
 */