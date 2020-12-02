package com.example.video_game_final_project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.q42.android.scrollingimageview.ScrollingImageView
import kotlinx.android.synthetic.main.fragment_home_screen.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeScreenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


//HomeScreenFragment is the fragment that first loads onscreen when the user opens the app
//It also contains all the buttons for navigating to the other fragments of the app.
class HomeScreenFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val viewModel: GameViewModel by activityViewModels<GameViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //when the randomButton is clicked, we clear the value of the current game, create a second thread which sets the current game to have
        //a random game stored in it, and navigate to the random game fragment.
        randomButton.setOnClickListener {
            viewModel.currentGame.postValue(null)

            //only one thread may call this function at a time.
            if(viewModel.randomGameLock.value!! == false)
            {
                viewModel.randomGameLock.value = true
                val executorService: ExecutorService = Executors.newFixedThreadPool(1)
                executorService.execute {
                        viewModel.getRandomGame()
                        viewModel.randomGameLock.postValue(false)
                }
            }
            findNavController().navigate(R.id.action_homeScreenFragment_to_gameDescriptionFragment)
        }

        //when the Profile button is clicked, we update the list of games the user has rated in a second thread, and then navigate
        //to the profile fragment
        profileButton.setOnClickListener {
            viewModel.updateProfileList()
            findNavController().navigate(R.id.action_homeScreenFragment_to_profileFragment)
        }

        reccomendedGamesButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_gameSuggestionsFragment)
        }

        generalSearchButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_generalSearchFragment)
        }

        tutorialButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_gettingStartedFragment)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeScreenFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeScreenFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}