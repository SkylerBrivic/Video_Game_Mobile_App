package com.example.video_game_final_project

//This class represents a game returned by a call to RAWG.io when trying to find similar games to games a user liked.
//Consequently, this class only stores the ID value of a game and the heuristic value for how much the user is expected to like the game.
//When a page of the reccomended games list is loaded, these generalGame IDs are used in calls to RAWG.io to find the detailed
//information about a game, which is stored as a FavoriteGame object.
class GeneralGame(tempID: Int, tempSimilarity:Double, tempName:String, tempURL: String, tempRelease:String) {

    var ID: Int = 0

    //Higher (positive) values here indicate games the user is more likely to like, negative values indicate games the user is
    //less likely to like, and a value of 0 indicates games that the user has no obvious prefernece for or against.
    var similarityIndex = 0.0

    var gameName = ""
    var previewURL = ""
    var releaseDate = ""

    init{
        ID = tempID
        similarityIndex = tempSimilarity
        gameName = tempName
        previewURL = tempURL
        releaseDate = tempRelease
    }
}