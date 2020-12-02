package com.example.video_game_final_project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_general_search.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GeneralSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GeneralSearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val viewModel: GameViewModel by activityViewModels<GameViewModel>()
    lateinit var viewAdapter: RecyclerViewAdapter
    lateinit var viewManager: GridLayoutManager

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
        return inflater.inflate(R.layout.fragment_general_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RecyclerViewAdapter.showImages = false
        if(viewModel.searchGamesList.value == null)
            viewAdapter = RecyclerViewAdapter(emptyArray())
        else
            viewAdapter = RecyclerViewAdapter(viewModel.searchGamesList.value?.toTypedArray()!!)

        viewAdapter.goToGameDescriptionFunction = goToGameDescriptionFunction
        viewManager = GridLayoutManager(context, 2, LinearLayoutManager.VERTICAL, false)
        generalSearchRecycler.adapter = viewAdapter
        generalSearchRecycler.layoutManager = viewManager
        var tempPlatformList = ArrayList<String>()
        tempPlatformList.add("Any")
        for(platform in viewModel.allPlatformsList.value!!)
        {
            tempPlatformList.add(platform.name)
        }
        var finalPlatformList = tempPlatformList.toTypedArray()
        var spinnerAdapter = ArrayAdapter<String>(requireContext(), R.layout.support_simple_spinner_dropdown_item, finalPlatformList)
        platformsSpinner.adapter = spinnerAdapter
        platformsSpinner.setSelection(0)

        viewModel.searchGamesList.observe(viewLifecycleOwner,
            {
            if(viewModel.searchGamesList.value == null)
            {
               viewAdapter.recyclerGameList = emptyArray()
            }
                else
            {
                viewAdapter.recyclerGameList = viewModel.searchGamesList.value!!.toTypedArray()
            }

                viewAdapter.notifyDataSetChanged()
        })
    }


    override fun onResume() {
        super.onResume()
        RecyclerViewAdapter.showImages = false
        if(viewModel.searchGamesList.value == null)
            viewAdapter.recyclerGameList = emptyArray()
        else
            viewAdapter.recyclerGameList = viewModel.searchGamesList.value?.toTypedArray()!!
        viewAdapter.notifyDataSetChanged()

        searchButton.setOnClickListener {
            if(viewModel.searchGamesLock.value!! == false) {
                viewModel.searchGamesLock.value = true
                var platformSelection = platformsSpinner.selectedItem.toString()
                var nullablePlatformString: String? = null
                if (platformSelection.equals("Any", true) == false) {
                    for (element in viewModel.allPlatformsList.value!!) {
                        if (element.name.equals(platformSelection, true)) {
                            nullablePlatformString = element.platformId.toString()
                            break
                        }
                    }
                }
                var nullableSearchField: String? = null
                nullableSearchField = searchField.text.toString()
                if (nullableSearchField?.isBlank())
                    nullableSearchField = null

                val executorService: ExecutorService = Executors.newFixedThreadPool(1)
                executorService.execute {
                    viewModel.searchForMatchingGames(nullablePlatformString, nullableSearchField)
                    viewModel.searchGamesLock.postValue(false)
                }
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
         * @return A new instance of fragment GeneralSearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GeneralSearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    var goToGameDescriptionFunction: (FavoriteGame) -> Unit = {
        viewModel.currentGame.postValue(null)
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        executorService.execute {
            viewModel.updateCurrentGameWithSpecificGame(it.gameID)
        }
        findNavController().navigate(R.id.action_generalSearchFragment_to_gameDescriptionFragment)
    }
}