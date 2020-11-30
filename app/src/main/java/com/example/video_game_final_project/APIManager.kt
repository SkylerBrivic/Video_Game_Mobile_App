package com.example.video_game_final_project

import android.util.Log
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import kotlin.math.ceil

//The APIManager class handles all calls to the RAWG.io server
//The gameService interface defined at the bottom of this file contains all of the
//retrofit calls to the RAWG.io API for information
class APIManager(val viewModel: GameViewModel) {
    //base URL for all API queries
    private val apiURL = "https://api.rawg.io/api/"

    //The apiKey we registered for our app
    private val apiKey = "9c19375367684241a8b7904be4ca8c96"

    //The tempGamesList list stores the results of the last call to getSuggestedGames()
    //getSuggestedGames() only returns the games on the last page (which is generally
    //40 games per page except for the last page)
    var tempGamesList = ArrayList<FavoriteGame>()

    //hitEnd is a boolean which is set to true when a call to getSuggestedGames()
    //reaches an invalid page (i.e. there are no more games left for the query to return).
    //On every new call to getSuggestedGames(), the variable is reset to false.
    var hitEnd = false



    //getSuggestedGames() takes as input a page number, a String representing the platforms the user
    //owns (ex. of the form "1,4,18", where 1, 4, and 18 are the IDs of Xbox, NES, and GameCube respectively)
    //and a String representing the genres of games the user has played ex.(of the form "2, 5, 9", where 2, 5, and 9
    //are the IDs of the genres "Action", "Fantasy", and "Sports" respectively).

    //Calling getSuggestedGames() sets hitEnd to false, and then attempts to populate tempGamesList with a list of games
    //returned by the call to the server for games on this page for these platforms and genres specified.
    //If pageNum is an invalid page (past the end of the search results), then tempGamesList is set to an empty list,
    //and hitEnd is set to true.
    fun getSuggestedGames(pageNum: Int, platformString: String, genreString: String)
    {
        hitEnd = false
        val retrofit = Retrofit.Builder().baseUrl(apiURL).build()
        val service = retrofit.create(gameService::class.java)
        //the getSuggestedGames() function of the gameService interface is what is used to retrieve games from the API
        val call = service.getSuggestedGames("Clark_Video_Game_App", apiKey, pageNum, 40, platformString, genreString)

        //this line executes the API call in the same thread as this function is in. Thus, this function
        //must NOT be called directly by the MainThread or the app will crash. Instead, a separate thread must be created to call
        //the getSuggestedGames() function in the APIManager class
        var response = call.execute()
        if(response.isSuccessful)
        {
            val body = response.body()
            if(body != null)
                setupReccomendedGamesList(body.string()) //This function call Populates tempGamesList with the results of the API call, and sets hitEnd to true if we have hit an invalid page
        }

    }


    //This is a helper function for the getSuggestedGames() function which takes as input a JsonString representing
    //the response from a call to the API server. This function updates the tempGamesList list to have the games
    //referenced by the json String, and sets hitEnd to true if the current page was an invalid page.
    fun setupReccomendedGamesList(jsonString: String)
    {
        tempGamesList = ArrayList<FavoriteGame>()
        var dataArray = JSONObject(jsonString)

        //if jsonString == "    {"detail":"Invalid page."}  ",
        //then we have hit the end of the search results, and should stop processing here.

        //Calls a helper function to check if we have hit the end of the result set
        if(checkIfPageValid(dataArray) == false)
            {
                hitEnd = true
                Log.d("TAG_MSG", "Hit invalid page in setupArray() func")
                return
            }

        var gameJsonArray = dataArray.getJSONArray("results")

        //Now iterating over each game in the JSONArray
        for(index in 0 until gameJsonArray.length())
        {
            var singleGameJSONObject = gameJsonArray.getJSONObject(index)
            var newFavoriteGame = FavoriteGame()

            //Setting the ID of the Game object
            newFavoriteGame.gameID = singleGameJSONObject.getInt("id")

            //Setting the name of the Game object
            newFavoriteGame.gameName = singleGameJSONObject.getString("name")
            if(newFavoriteGame.gameName.isBlank() || newFavoriteGame.gameName.equals("null", true))
                newFavoriteGame.gameName = "N/A"

            //Setting the previewURL of the Game object (the path to an image preview of the game)
            newFavoriteGame.previewURL = singleGameJSONObject.getString("background_image")
            if(newFavoriteGame.previewURL.isBlank() || newFavoriteGame.previewURL.equals("null", true))
                newFavoriteGame.previewURL = "image_not_available"

            //Setting the release date of the Game object.
            newFavoriteGame.releaseDate = singleGameJSONObject.getString("released")
            if(newFavoriteGame.releaseDate.isBlank() || newFavoriteGame.releaseDate.equals("null", true))
                newFavoriteGame.releaseDate = "N/A"

            //Setting the GenreList of the newly-created object to include the IDs of all the genres in the currently-selected game object
            var genreJSONArray = singleGameJSONObject.getJSONArray("genres")
            for(genreIndex in 0 until genreJSONArray.length())
            {
                newFavoriteGame.genreList.add(genreJSONArray.getJSONObject(genreIndex).getInt("id"))
            }

            //Setting the TagList of the newly-created object to include the IDs of all the tags in the currently-selected game object.
            var tagJSONArray = singleGameJSONObject.getJSONArray("tags")
            for(tagIndex in 0 until tagJSONArray.length())
            {
                newFavoriteGame.tagList.add(tagJSONArray.getJSONObject(tagIndex).getInt("id"))
            }

            //Setting the PlatformList of the newly-created object to include the IDs of all the platforms that the currently-selected game object is available on.
            var platformsJSONArray = singleGameJSONObject.getJSONArray("platforms")
            for(platformsIndex in 0 until platformsJSONArray.length())
            {
                var currentPlatformObject = platformsJSONArray.getJSONObject(platformsIndex).getJSONObject("platform")

                if(currentPlatformObject.has("id"))
                    newFavoriteGame.platformList.add(currentPlatformObject.getInt("id"))
            }

            //Finally, adding the newly constructed game to the tempGamesList list.
            tempGamesList.add(newFavoriteGame)
        }
    }


    //checkIfPageValid() is a helper function which returns true if a JSONObject represents a valid page of game data,
    //and false if we have reached the end of the search results (meaning the page is invalid).
    fun checkIfPageValid(myJsonObject: JSONObject) : Boolean
    {
        if(myJsonObject.has("results") == false)
        {
            return false
        }
        return true
    }

    //getLastValidPage() is a function which takes as input a list of IDs of platforms.
    //The function returns the last valid page number of the results that are returned from querying
    //a list of all games for the selected platforms with a page size of 40.

    //This is used by the random game generator fragment to check what the last valid page
    //to be included in the randomly generated game can be.
    fun getLastValidPage(platformString: String) : Int {
        if(platformString.isBlank() || platformString.equals("null", true))
            return -1

        val retrofit = Retrofit.Builder().baseUrl(apiURL).build()
        val service = retrofit.create(gameService::class.java)
        val call = service.checkIfValidRandomPage("Clark_Video_Game_App", apiKey, 1, 1, platformString, "name")

        //This call is executed in the same thread as getLastValidPage() is called in.
        //As such, this function must NOT be called by the MainThread, or the program will stall.
        var response = call.execute()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                var myJSON = JSONObject(body.string())
                if (checkIfPageValid(myJSON))
                {
                    var size = myJSON.getInt("count")
                    return size
                }
            }
        }
        return -1
    }

    //getRandomPage() takes as input a page number, and a list of platforms called platformString which stores
    //the comma-separated list of IDs of the platforms the user owns.
    //The function returns the JSON String that results from querying the API with the specified parameters
    //using the checkIfValidRandomPage() function of the gameService interface.
    fun getRandomPage(pageNum: Int, platformString: String) : String
    {
        val retrofit = Retrofit.Builder().baseUrl(apiURL).build()
        val service = retrofit.create(gameService::class.java)
        val call = service.checkIfValidRandomPage("Clark_Video_Game_App", apiKey, pageNum, 1, platformString, "name")

        //Executed in the same thread as getRandomPage() - Must NOT be called by the
        //Main Thread. Otherwise, the program will stall
        var responseBody = call.execute()
        if(responseBody.isSuccessful)
        {
            var body = responseBody.body()
            if(body != null)
            {
                return body.string()
            }
        }
        return ""
    }


    //getRating() takes as input an integer representing the ID value of a game.
    //if the user rated the game, then the function returns the user's rating for the game
    //stored in the SQLite database. Otherwise, this function returns 5.0
    fun getRating(gameID: Int): Double
    {
        var ratingList = viewModel.database.value?.gameDAO()?.getRating(gameID)
        if(ratingList == null || ratingList.size == 0)
            return 5.0
        else
            return ratingList.get(0)
    }


    //getSpecificGame() is a function which takes as input an integer representing the ID of a game.
    //The function returns a FavoriteGame object representing the details of this game.
    fun getSpecificGame(myID: Int) : FavoriteGame
    {
        var returnGame = FavoriteGame()
        val retrofit = Retrofit.Builder().baseUrl(apiURL).build()
        val service = retrofit.create(gameService::class.java)
        val call = service.getSpecificGame("Clark_Video_Game_App", myID, apiKey)

        //This call is executed on the same thread as this function was called. As such, this function must NOT be
        //Called by the Main Thread. Otherwise, the program will stall.
        var response = call.execute()
        if(response.isSuccessful)
        {
            var body = response.body()
            if(body != null)
            {
                //Getting the details of the game now.
                var jsonObj = JSONObject(body.string())
                returnGame.gameID = myID

                returnGame.releaseDate = jsonObj.getString("released")
                if(returnGame.releaseDate.equals("null", true))
                    returnGame.releaseDate = "N/A"

                returnGame.previewURL = jsonObj.getString("background_image")
                if(returnGame.previewURL.equals("null", true))
                    returnGame.previewURL = "image_not_available"

                returnGame.gameName = jsonObj.getString("name")
                if(returnGame.gameName.equals("null", true))
                    returnGame.gameName = "N/A"

                returnGame.description = jsonObj.getString("description_raw")
                if(returnGame.description.equals("null", true) || returnGame.description.isBlank())
                    returnGame.description = "No description available."

                if(jsonObj.getJSONArray("developers").length() > 0 && jsonObj.getJSONArray("developers").getJSONObject(0) != null)
                    returnGame.devName = jsonObj.getJSONArray("developers").getJSONObject(0).getString("name")
                else
                    returnGame.devName = "N/A"

                returnGame.rating = getRating(myID)

                var platformList = jsonObj.getJSONArray("platforms")
                for(platformIndex in 0 until platformList.length())
                {
                    returnGame.platformNames.add(platformList.getJSONObject(platformIndex).getJSONObject("platform").getString("name"))
                    returnGame.platformList.add(platformList.getJSONObject(platformIndex).getJSONObject("platform").getInt("id"))
                }

                if(returnGame.platformNames.isEmpty())
                    returnGame.platformNames.add("N/A")

                var genreList = jsonObj.getJSONArray("genres")
                for(genreIndex in 0 until genreList.length())
                {
                    returnGame.genreList.add(genreList.getJSONObject(genreIndex).getInt("id"))
                }

              var tagList = jsonObj.getJSONArray("tags")
                for(tagIndex in 0 until tagList.length())
                {
                    returnGame.tagList.add(tagList.getJSONObject(tagIndex).getInt("id"))
                }

            }

        }

        return returnGame
    }


    //the gameService interface handles all calls to the RAWG.io server

    //the getSuggestedGames() function returns a list of games on the specified page
    //of the results (with a max of 40 results per page or less, depending on the page_size value,
    //the list of platforms the user owns, and a list of genres that the user has played before).

    //checkIfValidRandomPage() is used to check if a particular page in the random game search
    //is in fact a valid page. If it is valid, then a list of games is returned. If it is invalid,
    //then a JSON string containing an error message is returned.

    //getSpecificGame() returns details about a specific game with the ID value matching
    //the value passed into the getSpecificGame() function.

    interface gameService
    {
        @GET("games")
        fun getSuggestedGames(
            @Header(value="User-Agent") appName:String,
            @Query(value="key") myApiKey: String,
            @Query(value="page") pageNum: Int,
            @Query(value="page_size") pageSize: Int = 40,
            @Query(value="platforms", encoded = true) platformString: String,
            @Query(value="genres", encoded=true) genreString: String
            )
        : Call<ResponseBody>


        @GET("games")
        fun checkIfValidRandomPage(
            @Header(value="User-Agent") appName:String,
            @Query(value="key") myApiKey: String,
            @Query(value = "page") pageNum: Int,
            @Query(value="page_size") pageSize: Int = 1,
            @Query(value="platforms", encoded = true) platformString: String,
            @Query(value="ordering") orderingString: String = "name")
        : Call<ResponseBody>


        @GET("games/{id}")
        fun getSpecificGame(
            @Header(value="User-Agent") appName: String,
            @Path("id") myID: Int,
            @Query(value="key") myApiKey: String) : Call<ResponseBody>

    }

}
