package com.example.video_game_final_project

//This class represents a game returned by a call to RAWG.io when trying to find similar games to games a user liked.

//similarityIndex is a heuristic value, with higher values indicating games that the user is more likely to enjoy, and lower
//values indicating games the user is less likely to enjoy.

class GeneralGame(tempID: Int, tempSimilarity:Double, tempName:String, tempURL: String, tempRelease:String) {

    var ID: Int = 0

    //Higher (positive) values here indicate games the user is more likely to like, negative values indicate games the user is
    //less likely to like, and a value of 0 indicates games that the user has no obvious preference for or against.
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