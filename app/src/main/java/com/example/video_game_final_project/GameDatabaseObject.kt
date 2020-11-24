package com.example.video_game_final_project

import androidx.room.Entity
import androidx.room.PrimaryKey

//This class is used to represent a game in the user's local database
@Entity(tableName = "gameTable")
class GameDatabaseObject {
    @PrimaryKey
    var gameID: Int = -1
    var previewURL:String = ""
    var releaseDate:String = ""
    var gameName:String = ""
    var rating: Double = 5.0
}