package com.gravity.loft.safarkasathi.commons

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gravity.loft.safarkasathi.MainApp
import com.gravity.loft.safarkasathi.models.Trip

@Database(entities = [Trip::class], version = 1, exportSchema = false)
abstract class LocalDatabase: RoomDatabase(){

    companion object{

        private var instance: LocalDatabase? = null

        fun instance(): LocalDatabase {
            if(instance == null){
               instance = Room.databaseBuilder(MainApp.context(), LocalDatabase::class.java, "local_db")
                   .fallbackToDestructiveMigration()
                   .build()
            }
            return instance!!
        }
    }
}