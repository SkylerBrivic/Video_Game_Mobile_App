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

class APIManager(val viewModel: GameViewModel) {
    private val apiURL = "https://api.rawg.io/api/"
    private val apiKey = "9c19375367684241a8b7904be4ca8c96"
    var tempGamesList = ArrayList<FavoriteGame>()
    var hitEnd = false



    fun getSuggestedGames(pageNum: Int, platformString: String, genreString: String)
    {
        Log.d("TAG_MSG", "At start of isProcessing()")
        val retrofit = Retrofit.Builder().baseUrl(apiURL).build()
        val service = retrofit.create(gameService::class.java)
        val call = service.getSuggestedGames("Clark_Video_Game_App", apiKey, pageNum, 40, platformString, genreString)
        hitEnd = false
        var response = call.execute()
        if(response.isSuccessful)
        {
            val body = response.body()
            if(body != null)
                setupArray(body.string())
        }



        Log.d("TAG_MSG", "At end of isProcessing()")
    }

    fun checkIfPageValid(myJsonObject: JSONObject) : Boolean
    {

        if(myJsonObject.has("results") == false)
        {
            return false
        }

        return true
    }

    fun getLastValidPage(platformString: String) : Int {
        val retrofit = Retrofit.Builder().baseUrl(apiURL).build()
        val service = retrofit.create(gameService::class.java)
        val call = service.checkIfValidRandomPage(
            "Clark_Video_Game_App",
            apiKey,
            1,
            40,
            platformString,
            "name"
        )
        var response = call.execute()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                var myJSON = JSONObject(body.string())
                if (checkIfPageValid(myJSON))
                {
                    var size = myJSON.getInt("count")
                    return size/40
                }


            }
        }

        return -1
    }

    fun setupArray(jsonString: String)
    {
        Log.d("TAG_MSG", "At start of setupArray() func")
        tempGamesList = ArrayList<FavoriteGame>()
        var dataArray = JSONObject(jsonString)

        //if jsonString == "    {"detail":"Invalid page."}  ",
        //then we have hit the end of the search results, and should stop processing here.
        if(checkIfPageValid(dataArray) == false)
            {
                hitEnd = true
                Log.d("TAG_MSG", "Hit invalid page in setupArray() func")
                return
            }
        var gameJsonArray = dataArray.getJSONArray("results")
        for(index in 0 until gameJsonArray.length())
        {
            var singleGameJSONObject = gameJsonArray.getJSONObject(index)
            var newFavoriteGame = FavoriteGame()
            newFavoriteGame.gameID = singleGameJSONObject.getInt("id")
            newFavoriteGame.gameName = singleGameJSONObject.getString("name")
            newFavoriteGame.previewURL = singleGameJSONObject.getString("background_image")
            newFavoriteGame.releaseDate = singleGameJSONObject.getString("released")

            var genreJSONArray = singleGameJSONObject.getJSONArray("genres")
            for(genreIndex in 0 until genreJSONArray.length())
            {
                newFavoriteGame.genreList.add(genreJSONArray.getJSONObject(genreIndex).getInt("id"))
            }

            var tagJSONArray = singleGameJSONObject.getJSONArray("tags")
            for(tagIndex in 0 until tagJSONArray.length())
            {
                newFavoriteGame.tagList.add(tagJSONArray.getJSONObject(tagIndex).getInt("id"))
            }

            var platformsJSONArray = singleGameJSONObject.getJSONArray("platforms")
            for(platformsIndex in 0 until platformsJSONArray.length())
            {
                newFavoriteGame.platformList.add(platformsJSONArray.getJSONObject(platformsIndex).getJSONObject("platform").getInt("id"))
            }

            tempGamesList.add(newFavoriteGame)
        }
    }


    fun getRandomPage(pageNum: Int, platformString: String) : String
    {
        val retrofit = Retrofit.Builder().baseUrl(apiURL).build()
        val service = retrofit.create(gameService::class.java)
        val call = service.checkIfValidRandomPage("Clark_Video_Game_App", apiKey, pageNum, 40, platformString, "name")
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


    fun getRating(gameID: Int): Double
    {
        var ratingList = viewModel.database.value?.gameDAO()?.getRating(gameID)
        if(ratingList == null || ratingList.size == 0)
            return 5.0
        else
            return ratingList.get(0)

    }


    fun getSpecificGame(myID: Int) : FavoriteGame
    {
        var returnGame = FavoriteGame()
        val retrofit = Retrofit.Builder().baseUrl(apiURL).build()
        val service = retrofit.create(gameService::class.java)
        val call = service.getSpecificGame("Clark_Video_Game_App", myID, apiKey)
        var response = call.execute()
        if(response.isSuccessful)
        {
            var body = response.body()
            if(body != null)
            {
                var jsonObj = JSONObject(body.string())
                returnGame.gameID = myID
                returnGame.releaseDate = jsonObj.getString("released")
                if(returnGame.releaseDate == null || returnGame.releaseDate.equals("null", true))
                    returnGame.releaseDate = "N/A"

                returnGame.previewURL = jsonObj.getString("background_image")
                if(returnGame.previewURL == null || returnGame.previewURL.equals("null", true))
                    returnGame.previewURL = "image_not_available"

                returnGame.gameName = jsonObj.getString("name")
                if(returnGame.gameName == null || returnGame.gameName.equals("null", true))
                    returnGame.gameName = "N/A"

                returnGame.description = jsonObj.getString("description_raw")
                if(returnGame.description == null || returnGame.description.equals("null", true) || returnGame.description.isBlank())
                    returnGame.description = "No description available."

                if(jsonObj.getJSONArray("developers").length() > 0 && jsonObj.getJSONArray("developers").getJSONObject(0) != null)
                returnGame.devName = jsonObj.getJSONArray("developers").getJSONObject(0).getString("name")
                else
                    returnGame.devName = "N/A"
                returnGame.rating = getRating(myID)
                var platformList = jsonObj.getJSONArray("platforms")
                var platformIndex = 0
                for(platformIndex in 0 until platformList.length())
                {
                    returnGame.platformNames.add(platformList.getJSONObject(platformIndex).getJSONObject("platform").getString("name"))
                }

                if(returnGame.platformNames.isEmpty())
                    returnGame.platformNames.add("N/A")
            }

        }

        return returnGame
    }


    interface gameService
    {


        @GET("games")
        fun getSuggestedGames(
            @Header(value="User-Agent") appName:String,
            @Query(value="key") myApiKey: String,
            @Query(value="page") pageNum: Int,
            @Query(value="page_size") pageSize: Int,
            @Query(value="platforms", encoded = true) platformString: String,
            @Query(value="genres", encoded=true) genreString: String
            )
        : Call<ResponseBody>


        @GET("games")
        fun checkIfValidRandomPage(
            @Header(value="User-Agent") appName:String,
            @Query(value="key") myApiKey: String,
            @Query(value = "page") pageNum: Int,
            @Query(value="page_size") pageSize: Int = 40,
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
