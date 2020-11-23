package com.example.video_game_final_project

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface GameDAO {

    @Query("SELECT rating FROM gameTable WHERE gameID = :myID")
    fun getRating(myID: Int): List<Double>

    @Query("SELECT * FROM gameTable")
    fun getAll() : List<GameDatabaseObject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(newGame: GameDatabaseObject)

    @Query("DELETE FROM gameTable WHERE gameID = :myID")
    fun deleteGame(myID: Int)

    @Query("DELETE FROM gameTable")
    fun deleteAll()
}