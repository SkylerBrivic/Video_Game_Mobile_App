package com.example.video_game_final_project

import android.util.Log

//FavoriteVideoGamesList is a class which contains dictionaries which reflect how popular
//(or unpopular) specific genres, tags, and platforms were for the user.

//Note: despite the word "Favorite" being in the class name, this class stores all games that the user rates,
//which also includes games the user rates poorly/doesn't like. RatedVideoGamesList would thus possibly be a more accurate name for this class...
class FavoriteVideoGamesList {

    //gameList is the list of games the user has rated, which are shown on the user's profile page.
    var gameList = ArrayList<FavoriteGame>()

    //each of these 3 dictionaries is has keys representing the ID value of the genre, tag, and platform respectively for the 3 dictionaries.
    //Additionally, the values in this dictionary are a double indicating how much the user liked this criteria (higher values indicates that games
    //matching this criteria are MORE likely to be liked by users).
    var genreDictionary = HashMap<Int, Double>()
    var tagDictionary = HashMap<Int, Double>()
    var platformsDictionary = HashMap<Int, Double>()

    //This is a boolean which stores if the dictionaries were updated since the gameList was last modified.
    //This is initialized to false, and set to true when the dictionaries are updated as a result of the user
    //navigating to their recommended games list. When the user changes a game or rating in their
    //favorite games list, this is set to false, and remains that way until the user next tries to look
    //at their recommended games list/profile.
    var dictionaryUpdated = false


    init{
        gameList = ArrayList<FavoriteGame>()
        genreDictionary = HashMap<Int, Double>()
        tagDictionary = HashMap<Int, Double>()
        platformsDictionary = HashMap<Int, Double>()
        dictionaryUpdated = false
    }

    //This function sets the user's recommended game's list to newList,
    //and sets dictionaryUpdated to false.
    fun setGamesList(newList: ArrayList<FavoriteGame>)
    {
        gameList = newList
        dictionaryUpdated = false
    }

    //addGameToList() is a function which adds a new game to the user's profile
    fun addGameToList(newGame: FavoriteGame)
    {
        gameList.add(newGame)
        dictionaryUpdated = false
    }

    //function called whenever the user changes the property of a game in this list, such as rating
    fun notifyGameChanged()
    {
        dictionaryUpdated = false
    }

    //This function updates the values in all of the dictionaries, and sets dictionaryUpdated to true.
    fun updateDictionaries()
    {
        Log.d("TAG_MSG", "Called updateDictionaries()!")
        updateGenreDictionary()
        updateTagDictionary()
        updatePlatformsDictionary()
        dictionaryUpdated = true
        Log.d("TAG_MSG", "In updateDictionaries before returning, genre dictionary was size: " + genreDictionary.keys.size.toString())
    }

    //A helper function to update the genre dictionary.
    fun updateGenreDictionary()
    {
        //clearing the genreDictionary
        genreDictionary = HashMap<Int, Double>()
        for(myGame in gameList) //for each game in the user's profile...
        {
            for(myGenre in myGame.genreList) //...For each genre in each game's genre list...
            {
                //If the genre wasn't already in the dictionary, then add it with a weight of 0.0 for how much
                //the user likes the genre
                if(!genreDictionary.containsKey(myGenre))
                {
                   genreDictionary.put(myGenre, 0.0)
                }

                //Genre is so important that it is given 3 times the value of tags and platforms
                if(myGame.rating < 5)
                    genreDictionary[myGenre] = genreDictionary[myGenre]!! + ((3* (myGame.rating - 5)))
                else
                    genreDictionary[myGenre] = genreDictionary[myGenre]!! + ((3*(myGame.rating - 4)))

            }
        }

    }

    //same logic as updateGenreDictionary()
    fun updateTagDictionary()
    {
        tagDictionary = HashMap<Int, Double>()

        for(myGame in gameList)
        {
            for(myTag in myGame.tagList)
            {
                if(!tagDictionary.containsKey(myTag))
                {
                    tagDictionary.put(myTag, 0.0)
                }

                if(myGame.rating < 5)
                    tagDictionary[myTag] = tagDictionary[myTag]!! + (myGame.rating - 5)
                else
                    tagDictionary[myTag] = tagDictionary[myTag]!! + (myGame.rating - 4)
            }
        }

    }

    //Same logic as updateGenreDictionary()
    fun updatePlatformsDictionary()
    {
        platformsDictionary = HashMap<Int, Double>()

        for(myGame in gameList)
        {
            for(myPlatform in myGame.platformList)
            {
                if(!platformsDictionary.containsKey(myPlatform))
                {
                    platformsDictionary.put(myPlatform, 0.0)
                }

                if(myGame.rating < 5)
                    platformsDictionary[myPlatform] = platformsDictionary[myPlatform]!! + (myGame.rating - 5)
                else
                    platformsDictionary[myPlatform] = platformsDictionary[myPlatform]!! + (myGame.rating - 4)
            }
        }

    }
}