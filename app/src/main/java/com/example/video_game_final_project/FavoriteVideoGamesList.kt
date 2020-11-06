package com.example.video_game_final_project

class FavoriteVideoGamesList {

    var gameList = ArrayList<FavoriteGame>()

    //each of these 3 dictionaries is has keys representing the ID value of the genre, tag, and platform respectively for the 3 dictionaries.
    //Additionally, the values in this dictionary are a double indicating how much the user liked this criteria (higher values indicates that games
    //matching this criteria are MORE likely to be liked by users).
    var genreDictionary = HashMap<Int, Double>()
    var tagDictionary = HashMap<Int, Double>()
    var platformsDictionary = HashMap<Int, Double>()

    //This is a boolean which stores if the dictionaries were updated since the gameList was last modified.
    //This is initialized to false, and set to True when the dictionaries are updated as a result of the user
    //navigating to their recccomended games list. When the user changes a game or rating in their
    //favorite games list, this is set to false, and remains that way until the user next tries to look
    //at their reccomended games list.
    var dictionaryUpdated = false


    init{
        gameList = ArrayList<FavoriteGame>()
        genreDictionary = HashMap<Int, Double>()
        tagDictionary = HashMap<Int, Double>()
        platformsDictionary = HashMap<Int, Double>()
        dictionaryUpdated = false
    }

    fun setGamesList(newList: ArrayList<FavoriteGame>)
    {
        gameList = newList
        dictionaryUpdated = false
    }

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

    fun updateDictionaries()
    {
        updateGenreDictionary()
        updateTagDictionary()
        updatePlatformsDictionary()
        dictionaryUpdated = true
    }

    fun updateGenreDictionary()
    {
        //clearing the genreDictionary
        genreDictionary = HashMap<Int, Double>()
        for(myGame in gameList)
        {
            for(myGenre in myGame.genreList)
            {
                if(!genreDictionary.containsKey(myGenre))
                {
                   genreDictionary.put(myGenre, 0.0)
                }

                if(myGame.rating < 5)
                    genreDictionary[myGenre] = genreDictionary[myGenre]!! + ((3* (myGame.rating - 5)))
                else
                    genreDictionary[myGenre] = genreDictionary[myGenre]!! + ((3*(myGame.rating - 4)))

            }
        }

    }

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