package com.example.video_game_final_project

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


//This is the class used for all database queries for the application
@Dao
interface GameDAO {

    //Used to check if a specific game was rated, and if it was, then what its rating was.
    @Query("SELECT rating FROM gameTable WHERE gameID = :myID")
    fun getRating(myID: Int): List<Double>

    @Query("SELECT * FROM gameTable")
    fun getAll() : List<GameDatabaseObject>

    //Since this function overwrites games with the same ID, it is also used
    //to update information/ratings for games which are already in the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(newGame: GameDatabaseObject)

    @Query("DELETE FROM gameTable WHERE gameID = :myID")
    fun deleteGame(myID: Int)

    //This is used purely for debugging
    @Query("DELETE FROM gameTable")
    fun deleteAll()
}