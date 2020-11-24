package com.example.video_game_final_project

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.random.Random

//This class stores all instance state information that must persist through configuration changes.

//!!!IMPORTANT NOTE TO PREVENT FUTURE ERRORS:!!! - The fragments that add platforms to the platformsList, and the fragments that allow for games to be rated, unrated, and to change a game's rating
//all need to set GenGamesUpToDate to false so that the below code will work properly.
class GameViewModel : ViewModel() {
    val apiManager = MutableLiveData<APIManager>() // Stores the apiManager reference which allows for calls to the RAWG.io API
    var bestGamesList = MutableLiveData<FavoriteVideoGamesList>() //Stores the list of the user's favorite games, along with quantifications of how much the user liked each genre, tag, and platform from each game in the FavoriteVideoGamesList
    var generalGamesList = MutableLiveData<ArrayList<GeneralGame>>() //This stores the most recent ArrayList of recommended games from the last call to UpdateSuggestedGamesList(), which is sorted such that games the user is most likely to like appear first.
    var currentGame = MutableLiveData<FavoriteGame>() //Stores the game whose details the user is currently looking at (only applicable when the GameDescriptionFragment is onscreen).
    var genGamesUpToDate = MutableLiveData<Boolean>() //A boolean which is true when genGamesList has not changed since the last time updateSuggestedGamesList() was called, and false otherwise (i.e. when the Favorite Games List changes or a new platform is added to the platformsList)
    var platformsList = MutableLiveData<MutableSet<Int>>() //A list of the ID values of the platforms that the user owns.
    var database = MutableLiveData<GameDB>() //A GameDB reference

    init {
        apiManager.value = APIManager(this)
        bestGamesList.value = FavoriteVideoGamesList()
        generalGamesList.value = ArrayList<GeneralGame>()
        genGamesUpToDate.value = false
        platformsList.value = mutableSetOf()
        currentGame.value = FavoriteGame()
    }

    //Function to force the ViewModel to be initialized (prevents a multi-threading data race error)
    fun initialize()
    {
    }

    //getGenresString() returns a String representation of all the genres of games in the user's Favorite Games List.
    //For example, if the user had games in their FavoriteGamesList with genres whose IDs were 5, 2, and 17, then the
    //following string would be returned by getGenresString(): "5,2,17"
    fun getGenresString() : String
    {
        var finalGenreString = ""

        if(bestGamesList.value?.dictionaryUpdated == false)
            bestGamesList.value?.updateDictionaries()

        for(myGenre in bestGamesList.value?.genreDictionary?.keys!!)
        {
            finalGenreString += (myGenre.toString() + ",")
        }

        //removing trailing comma from String
        if(finalGenreString.length != 0)
            finalGenreString = finalGenreString.substring(0, finalGenreString.length - 1)

        return finalGenreString

    }

    //addPlatform() adds the specified platform ID to the platformsList
    fun addPlatform(newPlatform:Int)
    {
        genGamesUpToDate.postValue(false)
        platformsList.value?.add(newPlatform)
    }


    //getPlatformString() returns a String representation of the IDs of all the platforms the user has selected that they own.
    //For example, if the user selected that they owned platforms with IDs of 5, 2, and 17, then the following string
    //would be returned by getPlatformString(): "5,2,17"

    //!!!!!!!IMPORTANT NOTE!!!!!!: The platforms the user owns are determined solely by looking at the platformsList of the viewModel, which is set in the addPlatformFragment
    //The list of games the user has rated is not used to determine what to include in the platform string returned by this function (although genres for the getGenresString() function are determined from looking at the genres of the games that the user has rated)
    fun getPlatformString() : String
    {
        var finalString = ""
        for(e in platformsList.value!!)
        {
            finalString += (e.toString() + ",")
        }

        //Stripping off trailing comma from String
        if(finalString.length != 0)
            finalString = finalString.substring(0, finalString.length - 1)

        return finalString
    }



    //getRandomGame() selects a random game from the list of games for platforms that the user owns, and posts the current game with
    //the information of the randomly selected game (which causes all observers of the current game to be notified).
    //This function is what is called to select the random game in the RandomGameFragment
    fun getRandomGame()
    {
        if(platformsList.value?.isEmpty()!!)
        {
            Log.d("TAG_MSG", "Error: You have to add a platform to your list before a random game can be returned!")
            return
        }

        var platformString = getPlatformString()
        var lastPageIndex = apiManager.value?.getLastValidPage(platformString)!!

        //Choosing a random page to select games from.
        var selectedPage = Random.nextInt(1, lastPageIndex + 1)

        var resultingJSONString = apiManager.value?.getRandomPage(selectedPage, platformString)
        var jsonObj = JSONObject(resultingJSONString)
        if(apiManager.value?.checkIfPageValid(jsonObj) == false)
        {
            Log.d("TAG_MSG", "Error: Page returned in call to getRandomGame() was invalid!")
            return
        }

        var JSONArrayObject = jsonObj.getJSONArray("results")

        //Choosing a random game on this page (the page should have only one game on it since the page size was set to 1, however, so this should always return 0)
        var selectedGameIndex = Random.nextInt(0, JSONArrayObject.length())

        Log.d("TAG_MSG", "Selected Game Index: " + selectedGameIndex.toString())
        var myGameID = JSONArrayObject.getJSONObject(selectedGameIndex).getInt("id")

        //getSpecificGame(myGameID) returns a FavoriteGame object with the attributes of the game whose ID val is equal to myGameID
        currentGame.postValue(apiManager.value?.getSpecificGame(myGameID)!!)
    }


    //updateSuggestedGamesList() is the function which updates the list of recommended games based on the games in the user' profile
    //The results of calling this function are stored in the generalGamesList of the viewModel.
    fun updateSuggestedGamesList()
    {
        //if generalGamesList is already up to date, then we can return without doing any work here.
        if(genGamesUpToDate.value!!)
            return

        //If the dictionaries aren't up to date, we need to update them.
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

        apiManager.value?.hitEnd = false

        //This while loop continues to add games to the reccomended list until either 1000 total games have been added (40 games per page * 25 pages)
        //or until we hit an invalid page (whichever happens first)
        while(apiManager.value?.hitEnd == false && pageIndex <= 25)
        {
            Log.d("TAG_MSG", "Page Num: " + pageIndex.toString())
            apiManager.value?.getSuggestedGames(pageIndex, platformString, genreString)


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

                //Calculating genre heuristic value
                for(myGenre in myGame.genreList)
                {
                    if(bestGamesList.value?.genreDictionary?.contains(myGenre)!!)
                    {
                        genreHeuristic += bestGamesList.value?.genreDictionary[myGenre]!!
                    }
                }

                //Calculating tag heuristic value
                for(myTag in myGame.tagList)
                {
                    if(bestGamesList.value?.tagDictionary?.contains(myTag)!!)
                    {
                        tagHeuristic += bestGamesList.value?.tagDictionary[myTag]!!
                    }
                }

                //Calculating platform heuristic value
                for(myPlatform in myGame.platformList)
                {
                    if(bestGamesList.value?.platformsDictionary?.contains(myPlatform)!!)
                    {
                        platformHeuristic += bestGamesList.value?.platformsDictionary[myPlatform]!!
                    }
                }

                //Final Heuristic value for a game = genre heuristic + tag heuristic + platform heuristic
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

    //isRated(gameID) returns true if the game specified by gameID was rated by the user, and false otherwise.
    //Note that this function queries the SQLite database to check if the game was rated
    fun isRated(gameID: Int) : Boolean
    {
        if(database.value?.gameDAO()?.getRating(gameID)!!.isEmpty())
            return false
        return true
    }

    //getRating(gameID) returns the rating of a game rated by the user, or 5.0 if the game was never rated by the user.
    //Note that this function queries the SQLite database to find how the user rated the game.
    fun getRating(gameID: Int) : Double
    {
        return apiManager.value?.getRating(gameID)!!
    }
}