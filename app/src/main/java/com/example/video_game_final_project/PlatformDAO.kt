package com.example.video_game_final_project

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlatformDAO {

    @Query("SELECT * FROM platformDB")
    fun getAll() : List<Platform>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(newOwned: Platform)

    @Query("DELETE FROM platformDB WHERE platformID =:myID")
    fun deletePlatformByID(myID: Int)

    @Query("DELETE FROM platformDB WHERE name =:myName")
    fun deletePlatformByName(myName: String)

    @Query("DELETE FROM gameTable")
    fun deleteAll()
}