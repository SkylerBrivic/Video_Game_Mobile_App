package com.example.video_game_final_project

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//This database class uses the GameDatabaseObject to model each entity,
//and uses the GameDAO class for all database queries.
@Database(entities = [GameDatabaseObject::class], version = 1)
abstract class GameDB : RoomDatabase() {

    abstract fun gameDAO(): GameDAO

    companion object{
        private var INSTANT: GameDB? = null

        fun getDBObject(context: Context): GameDB?{
            if(INSTANT == null)
            {
                synchronized(GameDB::class.java)
                {
                    INSTANT = Room.databaseBuilder(context, GameDB::class.java, "GameDB")
                        .allowMainThreadQueries().fallbackToDestructiveMigration().build()
                }
            }
            return INSTANT
        }

    }
}