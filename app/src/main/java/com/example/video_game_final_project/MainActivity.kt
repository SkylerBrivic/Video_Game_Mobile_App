package com.example.video_game_final_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    val viewModel by viewModels<GameViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewModel.checkEverything()
        viewModel.platformsList.value?.add(14)
        viewModel.database.value = GameDB.getDBObject(this)
        var myList = viewModel.database.value?.gameDAO()?.getAll()!!
        for(e in myList)
            Log.d("TAG_MSG", "Game Name: " + e.gameName + "\tRating: " + e.rating.toString())

        /*
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        executorService.execute {
            var newGames = ArrayList<FavoriteGame>()
            var tempFinalGame = FavoriteGame()
            tempFinalGame.gameID = 3
            tempFinalGame.platformList.add(1)
            tempFinalGame.platformList.add(16)
            tempFinalGame.genreList.add(15)
            tempFinalGame.tagList.add(40847)
            tempFinalGame.rating = 9.9
            newGames.add(tempFinalGame)

            viewModel.bestGamesList.value?.gameList  = newGames
            viewModel.updateSuggestedGamesList()
            var resultString = "Size: " + viewModel.generalGamesList.value?.size.toString() + "\n"
            for(e in viewModel.generalGamesList.value!!)
            {
                resultString += ("Name: " + e.gameName + "\tHeuristic Val: " + e.similarityIndex.toString() + "\n")
            }
            Log.d("TAG_MSG", resultString)
        }
        */


    }
}