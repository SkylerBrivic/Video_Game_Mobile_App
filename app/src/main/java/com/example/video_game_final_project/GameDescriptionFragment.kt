package com.example.video_game_final_project

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_game_description.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GameDescriptionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameDescriptionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    val viewModel: GameViewModel by activityViewModels<GameViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detailedPlatformsText.setText("")

       //Setting the rating selector to contain 100 values between 0.0 and 10.0
        var StringArray = ArrayList<String>()
        for(i in 0 .. 10)
        {
            for(trailing in arrayOf(".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9"))
            {
                StringArray.add(i.toString() + trailing)
            }
        }
        StringArray.add("10.0")

        ratingSelector.displayedValues = StringArray.toTypedArray()
        ratingSelector.minValue = 0
        ratingSelector.maxValue = 100
        ratingSelector.value = 50
        ratingSelector.wrapSelectorWheel = false

        //when the current game in the view model is updated, the fields on the gameDescriptionFragment are updated
        //to have the correct values
        viewModel.currentGame.observe(viewLifecycleOwner, Observer<FavoriteGame> {
            if(viewModel.currentGame.value != null)
            {
                detailedGameTitleText.setText(viewModel.currentGame.value?.gameName)
                detailedPlatformsText.setText("Platforms: " + viewModel.currentGame.value?.platformNames?.joinToString(", "))

                //If the current game has a picture associated with it, we display that now. Otherwise, we display a default "image_not_available" image
                if (viewModel.currentGame.value?.previewURL != null && viewModel.currentGame.value?.previewURL!!.isBlank() == false && viewModel.currentGame.value?.previewURL.equals("image_not_available", true) == false && viewModel.currentGame.value?.previewURL.equals("null", true) == false)
                    PosterLoader.getInstance().loadURL(viewModel.currentGame.value?.previewURL!!, detailedGamePicture)
                else
                    detailedGamePicture.setImageResource(resources.getIdentifier("image_not_available", "drawable", "com.example.video_game_final_project"))

                detailedReleaseYear.setText(viewModel.currentGame.value?.releaseDate)
                detailedGameDescription.setText(viewModel.currentGame.value?.description)

                if (viewModel.isRated(viewModel.currentGame.value?.gameID!!)) {
                    ratingSelector.value = (viewModel.getRating(viewModel.currentGame.value?.gameID!!) * 10).toInt()
                    ratingButton.setText("Update Rating in Profile")
                    ratingButton.setBackgroundColor(resources.getColor(R.color.green))
                    unrateButton.visibility = View.VISIBLE
                    unrateButton.setBackgroundColor(resources.getColor(R.color.red))
                }

                else {
                    ratingSelector.value = 50
                    ratingButton.setText("Rate and Add Game to Profile")
                    ratingButton.setBackgroundColor(resources.getColor(R.color.grey))
                    unrateButton.visibility = View.INVISIBLE
                }
            }
    })


        if(viewModel.currentGame.value != null)
        {
            detailedGameTitleText.setText(viewModel.currentGame.value?.gameName)
            detailedPlatformsText.setText("Platforms: " + viewModel.currentGame.value?.platformNames?.joinToString(", "))

            if(viewModel.currentGame.value?.previewURL != null && viewModel.currentGame.value?.previewURL!!.isBlank() == false && viewModel.currentGame.value?.previewURL.equals("image_not_available", true) == false && viewModel.currentGame.value?.previewURL.equals("null", true) == false)
                PosterLoader.getInstance().loadURL(viewModel.currentGame.value?.previewURL!!, detailedGamePicture)
            else
                detailedGamePicture.setImageResource(resources.getIdentifier("image_not_available", "drawable", "com.example.video_game_final_project"))

            detailedReleaseYear.setText(viewModel.currentGame.value?.releaseDate)
            detailedGameDescription.setText(viewModel.currentGame.value?.description)
        }



        //When the rate game button is clicked, we update the game's rating in the database,
        //change the rate game button from grey to green, make the unrate button become visible,
        //and change the rate button text to "Update Rating in Profile"
        ratingButton.setOnClickListener{
            if(viewModel.currentGame.value != null) {
                viewModel.genGamesUpToDate.value = false
                var myNewObject = GameDatabaseObject()
                myNewObject.gameID = viewModel.currentGame.value?.gameID!!
                myNewObject.gameName = viewModel.currentGame.value?.gameName!!
                myNewObject.previewURL = viewModel.currentGame.value?.previewURL!!
                myNewObject.releaseDate = viewModel.currentGame.value?.releaseDate!!
                myNewObject.rating = (ratingSelector.value).toDouble()/10
                myNewObject.platformString = convertArrayListToString(viewModel.currentGame.value?.platformList!!)
                myNewObject.genreString = convertArrayListToString(viewModel.currentGame.value?.genreList!!)
                myNewObject.tagString = convertArrayListToString(viewModel.currentGame.value?.tagList!!)
                viewModel.database.value?.gameDAO()?.insert(myNewObject)
                ratingButton.setText("Update Rating in Profile")
                ratingButton.setBackgroundColor(resources.getColor(R.color.green))
                unrateButton.setBackgroundColor(resources.getColor(R.color.red))
                unrateButton.visibility = View.VISIBLE
            }
        }

        //When the unrate game button is clicked, we delete the game from the database,
        //make the unrate button become invisible, change the rate button back to being grey, and
        //change the rate button text to "Rate and Add Game to Profile"
        unrateButton.setOnClickListener {
            if(viewModel.currentGame.value != null)
            {
                viewModel.genGamesUpToDate.value = false
                viewModel.database.value?.gameDAO()?.deleteGame(viewModel.currentGame.value?.gameID!!)
                ratingButton.setText("Rate and Add Game to Profile")
                unrateButton.visibility = View.INVISIBLE
                ratingButton.setBackgroundColor(resources.getColor(R.color.grey))
            }
        }
    }

    //The onResume() function makes sure that the fragment appropriately reflects whether or not the current game has been rated.
    override fun onResume() {
        super.onResume()
        if(viewModel.currentGame.value != null)
        {
            if(viewModel.isRated(viewModel.currentGame.value?.gameID!!))
            {
                ratingSelector.value = (viewModel.getRating(viewModel.currentGame.value?.gameID!!).toDouble() * 10).toInt()
                ratingButton.setText("Update Rating in Profile")
                unrateButton.visibility = View.VISIBLE
                ratingButton.setBackgroundColor(resources.getColor(R.color.green))
                unrateButton.setBackgroundColor(resources.getColor(R.color.red))
            }

            else
            {
                ratingButton.setText("Rate and Add Game to Profile")
                unrateButton.visibility = View.INVISIBLE
                ratingButton.setBackgroundColor(resources.getColor(R.color.grey))
            }
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GameDescriptionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GameDescriptionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun convertArrayListToString(integerList: ArrayList<Int>) : String
    {
        var returnString = ""
        var tempIndex = 0
        while(tempIndex < integerList.size)
        {
            if(tempIndex != 0)
                returnString += "," + integerList.get(tempIndex).toString()
            else
                returnString += integerList.get(tempIndex).toString()

            ++tempIndex
        }

        return returnString
    }
}