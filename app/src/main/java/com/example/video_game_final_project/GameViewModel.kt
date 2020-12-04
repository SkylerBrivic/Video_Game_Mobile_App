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
    var suggestedGamesList = MutableLiveData<ArrayList<FavoriteGame>>() //This stores the most recent ArrayList of recommended games from the last call to UpdateSuggestedGamesList(), which is sorted such that games the user is most likely to like appear first.
    var profileGamesList = MutableLiveData<ArrayList<FavoriteGame>>() //This stores the games in the user's profile
    var currentGame = MutableLiveData<FavoriteGame>() //Stores the game whose details the user is currently looking at (only applicable when the GameDescriptionFragment is onscreen).
    var genGamesUpToDate = MutableLiveData<Boolean>() //A boolean which is true when genGamesList has not changed since the last time updateSuggestedGamesList() was called, and false otherwise (i.e. when the Favorite Games List changes or a new platform is added to the platformsList)
    var platformsList = MutableLiveData<MutableSet<Int>>() //A list of the ID values of the platforms that the user owns.
    var searchGamesList = MutableLiveData<ArrayList<FavoriteGame>>() // this list stores the results returned by the function that searches for games matching user search criteria
    var allPlatformsList = MutableLiveData<ArrayList<Platform>>() // A list containing the information for all 51 platforms in the RAWG.io API
    var database = MutableLiveData<GameDB>() //A GameDB reference


    var platformsInitialized = MutableLiveData<Boolean>() // determines if platform list has been initialized yet
    var randomGameLock = MutableLiveData<Boolean>() // lock controlling entry to randomGame function (true when function is in use by a thread)
    var suggestedGamesLock = MutableLiveData<Boolean>() // lock controlling entry to suggestedGames function (true when function is in use by a thread)
    var searchGamesLock = MutableLiveData<Boolean>() // lock controlling entry to gameSearch function (true when function is in use by a thread)

    var suggestedGamePageNum = MutableLiveData<Int>() // specifies which page we are on in the suggested game search fragment
    var profilePageNum = MutableLiveData<Int>() //specifies which page we are on in the user's profile of games.

    init {
        apiManager.value = APIManager(this)
        platformsInitialized.value = false
        bestGamesList.value = FavoriteVideoGamesList()
        suggestedGamesList.value = ArrayList<FavoriteGame>()
        genGamesUpToDate.value = false
        profileGamesList.value = ArrayList<FavoriteGame>()
        platformsList.value = mutableSetOf()
        currentGame.value = FavoriteGame()
        randomGameLock.value = false
        suggestedGamesLock.value = false
        suggestedGamePageNum.value = 1
        profilePageNum.value = 1
        searchGamesList.value = ArrayList<FavoriteGame>()
        allPlatformsList.value = ArrayList<Platform>()
        searchGamesLock.value = false
    }

    //Function to force the ViewModel to be initialized (prevents a multi-threading data race error)
    fun initialize()
    {
    }

    //getGenresString() returns a String representation of all the genres of games in the user's Favorite Games List.
    //For example, if the user had games in their FavoriteGamesList with genres whose IDs were 5, 2, and 17, then the
    //following string would be returned by getGenresString(): "5,2,17"
    fun getGenresString() : String?
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
    fun getPlatformString() : String?
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


    //the updateCurrentGameWithSpecificGame() function takes as input the ID value of a specific game. The function calls the RAWG.io API
    //to get the information for that game, which it then posts in the current game.

    //!!!IMPORTANT NOTE!!!: This function must be called in a seperate thread from the main thread. Otherwise, the call to apiManager.getSpecificGame()
    //will cause the application to stall
    fun updateCurrentGameWithSpecificGame(gameID: Int)
    {
        currentGame.postValue(apiManager.value?.getSpecificGame(gameID))
    }

    //updateSuggestedGamesList() is the function which updates the list of recommended games based on the games in the user' profile
    //The results of calling this function are stored in the generalGamesList of the viewModel.
    fun updateSuggestedGamesList()
    {
        //Log.d("TAG_MSG", "In start of updateSuggestedGamesList()")
        //if generalGamesList is already up to date, then we can return without doing any work here.
        if(genGamesUpToDate.value!!)
            return

        //If the dictionaries aren't up to date, we need to update them.
        if(bestGamesList.value?.dictionaryUpdated == false)
        {
            bestGamesList.value?.updateDictionaries()
        }

        //now, we need to clear the old values from suggestedGamesList
        suggestedGamesList.postValue(ArrayList<FavoriteGame>())


        var pageIndex = 1

        var platformString = getPlatformString()
        if(platformString!!.isEmpty() || platformString.isBlank())
        {
            platformString = null
        }

        var genreString = getGenresString()
        if(genreString!!.isEmpty() || genreString.isBlank())
        {
           genreString = null
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
            Log.d("TAG_MSG", "Genre Dictionary Size before main loop: " + bestGamesList.value?.genreDictionary?.keys?.size.toString())
            Log.d("TAG_MSG", "Platforms Dictionary Size before main loop: " + bestGamesList.value?.platformsDictionary?.keys?.size.toString())
            Log.d("TAG_MSG", "Tag Dictionary Size before main loop: " + bestGamesList.value?.tagDictionary?.keys?.size.toString())

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
                //Log.d("TAG_MSG", "Final Heuristic: " + finalHeuristic.toString())
                var finalGame = FavoriteGame()
                finalGame.gameID = myGame.gameID
                finalGame.similarityIndex = finalHeuristic
                finalGame.gameName = myGame.gameName
                finalGame.previewURL = myGame.previewURL
                finalGame.releaseDate = myGame.releaseDate
                suggestedGamesList.value?.add(finalGame)
            }
            ++pageIndex
        }


        //lastly, we need to sort the general games list in descending order by heuristic value:
        suggestedGamesList.value?.sortBy{(it.similarityIndex) * -1}
        suggestedGamesList.postValue(suggestedGamesList.value)
        genGamesUpToDate.postValue(true)
        //Log.d("TAG_MSG", "At end of updateSuggestedGamesList(), with final list size of " + suggestedGamesList.value?.size.toString())
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

    //This function updates the list of games the user has rated.
    //It then posts the new value of the profileGamesList
    fun updateProfileList() {
            profileGamesList.value = ArrayList<FavoriteGame>()
            var allRatedGamesList = database.value?.gameDAO()!!.getAll()
            Log.d(
                "TAG_MSG",
                "in updateProfileList() function, Num games in list returned from database: " + allRatedGamesList.size.toString()
            )
            if(profileGamesList.value != null)
            Log.d("TAG_MSG", "in updateProfileList(), size of profileGamesList before updating was " + profileGamesList.value?.size.toString())
        bestGamesList.value = FavoriteVideoGamesList()
        for (gameDatabaseObject in allRatedGamesList) {
                var newGame = FavoriteGame()
                newGame.gameID = gameDatabaseObject.gameID
                newGame.gameName = gameDatabaseObject.gameName
                newGame.previewURL = gameDatabaseObject.previewURL
                newGame.releaseDate = gameDatabaseObject.releaseDate
                newGame.genreList = convertStringToArrayListInt(gameDatabaseObject.genreString)
                newGame.tagList = convertStringToArrayListInt(gameDatabaseObject.tagString)
                newGame.platformList = convertStringToArrayListInt(gameDatabaseObject.platformString)
                profileGamesList.value?.add(newGame)
                bestGamesList.value?.addGameToList(newGame)
            }
        }

    fun updateBestGamesList()
    {
       // Log.d("TAG_MSG", "In start of updateBestGamesList()")
        var newGamesList = FavoriteVideoGamesList()
        var allRatedGamesList = database.value?.gameDAO()!!.getAll()
        for(gameDatabaseObject in allRatedGamesList)
        {
            var newGame = FavoriteGame()
            newGame.gameID= gameDatabaseObject.gameID
            newGame.gameName = gameDatabaseObject.gameName
            newGame.previewURL = gameDatabaseObject.previewURL
            newGame.releaseDate = gameDatabaseObject.releaseDate
            newGame.rating = gameDatabaseObject.rating
            newGame.platformList = convertStringToArrayListInt(gameDatabaseObject.platformString)
            newGame.genreList = convertStringToArrayListInt(gameDatabaseObject.genreString)
            newGame.tagList = convertStringToArrayListInt(gameDatabaseObject.tagString)
            newGamesList.addGameToList(newGame)
        }

        bestGamesList.value = newGamesList
       // Log.d("TAG_MSG", "In end of updateBestGamesList()")
    }


    //A helper function which takes as input a string of comma seperated numbers (ex. "3,4,5") and returns the ArrayList of integers
    //from the string
    fun convertStringToArrayListInt(inputString: String) : ArrayList<Int>
    {
        var returnList = ArrayList<Int>()

        var splitList = inputString.split(",")

        if(inputString.isBlank())
            return returnList

        for(element in splitList)
        {
            returnList.add(element.toInt())
        }

        Log.d("TAG_MSG", "In convertStringToArrayList, value of " + returnList.toString())
        return returnList
    }

    //this function takes as input a string representing the platforms the user will accept in their search results (or null if any platform is OK).
    //This function also takes as input a search string which represents the criteria the user wants to search for (or null if the user doesn't want any search terms here).
    //This function updates the searchGamesList with the 99 most-relevant games to the user's search in order of most relevant (or less than 99 if fewer than 99 results were
    //returned by the call to the API)
    fun searchForMatchingGames(platformString: String?, searchString: String?)
    {
        var returnList = ArrayList<FavoriteGame>()

        for(i in 1 .. 3)
        {
            Log.d("TAG_MSG", "i is " + i.toString())
            var jsonObj = JSONObject(apiManager.value?.searchForMatchingGames(i, 33, platformString, searchString)!!)

            if (apiManager.value?.checkIfPageValid(jsonObj)!!)
            {
                matchingGameHelperFunc(returnList, jsonObj)
            }

            else
            {
                Log.d("TAG_MSG", "Page Invalid!")
               break
            }
        }

        searchGamesList.postValue(returnList)
        return
    }

    //helper function which adds the game in jsonObj to the end of returnList
    fun matchingGameHelperFunc(returnList: ArrayList<FavoriteGame>, jsonObj: JSONObject)
    {
        var jsonArray = jsonObj.getJSONArray("results")

        for(i in 0 until jsonArray.length())
        {
            var newFavoriteGameObj = FavoriteGame()
            var gameObj = jsonArray.getJSONObject(i)
            newFavoriteGameObj.gameID = gameObj.getInt("id")
            newFavoriteGameObj.gameName = gameObj.getString("name")
            if(newFavoriteGameObj.gameName.isBlank() || newFavoriteGameObj.gameName.equals("null", true))
                newFavoriteGameObj.gameName = "N/A"
            newFavoriteGameObj.previewURL = gameObj.getString("background_image")
            if(newFavoriteGameObj.previewURL.isBlank() || newFavoriteGameObj.previewURL.equals("null", true))
                newFavoriteGameObj.previewURL = "image_not_available"
            newFavoriteGameObj.releaseDate = gameObj.getString("released")
            if(newFavoriteGameObj.releaseDate.isBlank() || newFavoriteGameObj.releaseDate.equals("null", true))
                newFavoriteGameObj.releaseDate = "N/A"
            returnList.add(newFavoriteGameObj)
        }

        return
    }

    fun updateAllPlatformList(platform: Platform){
        val index = allPlatformsList.value!!.indexOf(platform)
        allPlatformsList.value!![index].isOwned = platform.isOwned
        if (platform.isOwned)
            database.value?.platformDAO()?.insert(platform)
        else{
            database.value?.platformDAO()?.deletePlatformByID(platform.platformId)
        }

    }



}