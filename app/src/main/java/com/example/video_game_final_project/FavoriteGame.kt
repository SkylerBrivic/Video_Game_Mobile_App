package com.example.video_game_final_project

//This class represents a game in the user's list of favorite games.
//Additionally, this class is also used to temporarily hold the info from the JSON string of
// games returned by a call to the RAWG.io server
class FavoriteGame {

    var gameID: Int = 0
    var gameName: String = ""

    //Stores the released date as a String in "yyyy-mm-dd" format
    var releaseDate: String = ""

    //Stores the ID vals of the genres for the game
    var genreList = ArrayList<Int>()

    //Stores the ID vals of the tags of the game.
    var tagList = ArrayList<Int>()

    //Stores the ID vals for the platforms of the game.
    var platformList = ArrayList<Int>()

    //for games in the user's reccomended list, this is set to the appropriate value.
    var description = ""

    var devName = ""

    //the user's rating of the game.
    var rating: Double = 5.0
}