package com.example.video_game_final_project

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.random.Random

class GameViewModel : ViewModel() {
    val apiManager = MutableLiveData<APIManager>()
    var bestGamesList = MutableLiveData<FavoriteVideoGamesList>()
    var generalGamesList = MutableLiveData<ArrayList<GeneralGame>>()
    var currentGame = MutableLiveData<FavoriteGame>()
    var genGamesUpToDate = MutableLiveData<Boolean>()
    var platformsList = MutableLiveData<MutableSet<Int>>()
    var database = MutableLiveData<GameDB>()

    init {
        apiManager.value = APIManager(this)
        bestGamesList.value = FavoriteVideoGamesList()
        generalGamesList.value = ArrayList<GeneralGame>()
        genGamesUpToDate.value = false
        platformsList.value = mutableSetOf()
        currentGame.value = FavoriteGame()
    }

    fun checkEverything()
    {

    }

    fun getGenresString() : String
    {
        var finalGenreString = ""

        for(myGenre in bestGamesList.value?.genreDictionary?.keys!!)
        {
            finalGenreString += (myGenre.toString() + ",")
        }

        if(finalGenreString.length != 0)
            finalGenreString = finalGenreString.substring(0, finalGenreString.length - 1)

        return finalGenreString

    }

    fun addPlatform(newPlatform:Int)
    {
        platformsList.value?.add(newPlatform)
    }

    fun getPlatformString() : String
    {
    var finalString = ""
        for(e in platformsList.value!!)
        {
            finalString += (e.toString() + ",")
        }
        if(finalString.length != 0)
            finalString = finalString.substring(0, finalString.length - 1)
        return finalString
    }



    fun getRandomGame()
    {
        if(platformsList.value?.isEmpty()!!)
        {
            Log.d("TAG_MSG", "Error: You have to add a platform to your list before a random game can be returned!")
            return
        }

        var platformString = getPlatformString()
        var lastPageIndex = apiManager.value?.getLastValidPage(platformString)!!

        var selectedPage = Random.nextInt(1, lastPageIndex + 1)
        var resultingJSONString = apiManager.value?.getRandomPage(selectedPage, platformString)
        var jsonObj = JSONObject(resultingJSONString)
        if(apiManager.value?.checkIfPageValid(jsonObj) == false)
        {
            Log.d("TAG_MSG", "Error: Page returned in call to getRandomGame() was invalid!")
            return
        }

        var JSONArrayObject = jsonObj.getJSONArray("results")
        var selectedGameIndex = Random.nextInt(0, JSONArrayObject.length())

        Log.d("TAG_MSG", "Selected Game Index: " + selectedGameIndex.toString())
        var myGameID = JSONArrayObject.getJSONObject(selectedGameIndex).getInt("id")
        currentGame.postValue(apiManager.value?.getSpecificGame(myGameID)!!)
    }


    fun updateSuggestedGamesList()
    {
        //if generalGamesList is already up to date, and the dictionaries haven't changed, then we can return
        if(genGamesUpToDate.value!!)
            return

        if(bestGamesList.value?.dictionaryUpdated == true)
            return

        //otherwise, if the dictionaries aren't up to date, we need to update them.
        if(bestGamesList.value?.dictionaryUpdated == false)
        {
            bestGamesList.value?.updateDictionaries()
        }

        //now, we need to clear the old values from generalGamesList
        generalGamesList.postValue(ArrayList<GeneralGame>())


        var pageIndex = 1

        var platformString = getPlatformString()
        if(platformString.isEmpty() || platformString.isBlank())
        {
            Log.d("TAG_MSG", "Error: Platform list was empty. At least one allowed platform must be in list for any games to be returned.")
            return
        }

        var genreString = getGenresString()
        if(genreString.isEmpty() || genreString.isBlank())
        {
            Log.d("TAG_MSG", "Error: Genre list was empty, which means no games were in the recomended list. Please add at least one game to the list, then try again!")
            return
        }

        while(apiManager.value?.hitEnd == false && pageIndex <= 25)
        {
            Log.d("TAG_MSG", "Page Num: " + pageIndex.toString())
            apiManager.value?.getSuggestedGames(pageIndex, platformString, genreString)


            Log.d("TAG_MSG", "Escaped!")

            //if we hit an invalid page, then we are done processing search results, and can break out of this loop
            if(apiManager.value?.hitEnd == true)
                break

            //...otherwise, we need to assign a heuristic value to each game based on the values in the game array in the apiManager
            //then, we add the resulting general game to generalGamesList
            for(myGame in apiManager.value?.tempGamesList!!)
            {
                var genreHeuristic = 0.0
                var tagHeuristic = 0.0
                var platformHeuristic = 0.0

                for(myGenre in myGame.genreList)
                {
                    if(bestGamesList.value?.genreDictionary?.contains(myGenre)!!)
                    {
                        genreHeuristic += bestGamesList.value?.genreDictionary[myGenre]!!
                    }
                }

                for(myTag in myGame.tagList)
                {
                    if(bestGamesList.value?.tagDictionary?.contains(myTag)!!)
                    {
                        tagHeuristic += bestGamesList.value?.tagDictionary[myTag]!!
                    }
                }

                for(myPlatform in myGame.platformList)
                {
                    if(bestGamesList.value?.platformsDictionary?.contains(myPlatform)!!)
                    {
                        platformHeuristic += bestGamesList.value?.platformsDictionary[myPlatform]!!
                    }
                }

                var finalHeuristic = genreHeuristic + tagHeuristic + platformHeuristic
                var finalGame = GeneralGame(myGame.gameID, finalHeuristic, myGame.gameName, myGame.previewURL, myGame.releaseDate)
                generalGamesList.value?.add(finalGame)
            }
            ++pageIndex
        }


        //lastly, we need to sort the general games list in descending order by heuristic value:
        generalGamesList.value?.sortBy{(it.similarityIndex) * -1}
        genGamesUpToDate.postValue(true)
        Log.d("TAG_MSG", "Done processing gen video games list!")
    }

    fun isRated(gameID: Int) : Boolean
    {
        if(database.value?.gameDAO()?.getRating(gameID)!!.isEmpty())
            return false
        return true
    }

    fun getRating(gameID: Int) : Double
    {
        return apiManager.value?.getRating(gameID)!!
    }
}