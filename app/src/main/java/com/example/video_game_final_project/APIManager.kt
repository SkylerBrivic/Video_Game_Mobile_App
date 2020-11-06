package com.example.video_game_final_project

import android.util.Log
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

class APIManager(val viewModel: GameViewModel) {
    private val apiURL = "https://api.rawg.io/api/"
    private val apiKey = "9c19375367684241a8b7904be4ca8c96"
    var tempGamesList = ArrayList<FavoriteGame>()
    var hitEnd = false



    fun getGames(pageNum: Int, platformString: String, genreString: String)
    {
        Log.d("TAG_MSG", "At start of isProcessing()")
        val retrofit = Retrofit.Builder().baseUrl(apiURL).build()
        val service = retrofit.create(gameService::class.java)
        val call = service.getGames("Clark_Video_Game_App", apiKey, pageNum, 40, platformString, genreString)
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

    fun setupArray(jsonString: String)
    {
        //if jsonString == "    {"detail":"Invalid page."}  ",
        //then we have hit the end of the search results, and should stop processing here.
        if(jsonString.equals("{\"detail\":\"Invalid page.\"}", true))
        {
            hitEnd = true
            Log.d("TAG_MSG", "Hit invalid page in setupArray() func")
            return
        }

        Log.d("TAG_MSG", "At start of setupArray() func")
        tempGamesList = ArrayList<FavoriteGame>()
        var dataArray = JSONObject(jsonString)
        var gameJsonArray = dataArray.getJSONArray("results")
        for(index in 0 until gameJsonArray.length())
        {
            var singleGameJSONObject = gameJsonArray.getJSONObject(index)
            var newFavoriteGame = FavoriteGame()
            newFavoriteGame.gameID = singleGameJSONObject.getInt("id")
            newFavoriteGame.gameName = singleGameJSONObject.getString("name")

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

    interface gameService
    {

        @GET("games")
        fun getGames(
            @Header(value="User-Agent") appName:String,
            @Query(value="key") myApiKey: String,
            @Query(value="page") pageNum: Int,
            @Query(value="page_size") pageSize: Int,
            @Query(value="platforms", encoded = true) platformString: String,
            @Query(value="genres", encoded=true) genreString: String
            )
        : Call<ResponseBody>
    }



}
