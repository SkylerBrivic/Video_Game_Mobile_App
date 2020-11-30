package com.example.video_game_final_project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_suggestions.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GameSuggestionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameSuggestionsFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_game_suggestions, container, false)
    }

    lateinit var viewAdapter: RecyclerViewAdapter
    lateinit var viewManager: GridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RecyclerViewAdapter.showImages = false
        viewModel.suggestedGamePageNum.value = 1

        if(viewModel.suggestedGamesList.value == null)
            viewAdapter = RecyclerViewAdapter(emptyArray())
        else
        {
           /* if(viewModel.suggestedGamesList.value!!.size > GameSuggestionsFragment.MAX_ENTRIES_PER_PAGE)
                viewAdapter = RecyclerViewAdapter(viewModel.suggestedGamesList.value?.toTypedArray()!!.copyOfRange(0, GameSuggestionsFragment.MAX_ENTRIES_PER_PAGE - 1))
            else*/
                viewAdapter = RecyclerViewAdapter(viewModel.suggestedGamesList.value?.toTypedArray()!!)
        }

        viewAdapter.goToGameDescriptionFunction = goToGameDescriptionFunction
        viewManager = GridLayoutManager(context, 2, LinearLayoutManager.VERTICAL, false)
        suggestionRecycler.adapter = viewAdapter
        suggestionRecycler.layoutManager = viewManager
/*
        backwardsButton.setOnClickListener{
            if(viewModel.suggestedGamePageNum.value!! > 1) {
                viewModel.suggestedGamePageNum.value = viewModel.suggestedGamePageNum.value!! - 1
                viewModel.suggestedGamesList.postValue(viewModel.suggestedGamesList.value)
            }
        }

        forwardButton.setOnClickListener{
            if(viewModel.suggestedGamePageNum.value!! * GameSuggestionsFragment.MAX_ENTRIES_PER_PAGE < viewModel.suggestedGamesList.value?.size!!)
            {
                viewModel.suggestedGamePageNum.value = viewModel.suggestedGamePageNum.value!! + 1
                viewModel.suggestedGamesList.postValue(viewModel.suggestedGamesList.value)
            }
        }
*/
        viewModel.suggestedGamesList.observe(viewLifecycleOwner, {
            if(viewModel.suggestedGamesList.value != null)
            {
                /*
                if(viewModel.suggestedGamePageNum.value!! <= 0)
                    viewModel.suggestedGamePageNum.value = 1
                else if((viewModel.suggestedGamePageNum.value!! - 1) * GameSuggestionsFragment.MAX_ENTRIES_PER_PAGE > viewModel.suggestedGamesList.value?.size!!)
                    viewModel.suggestedGamePageNum.value = viewModel.suggestedGamePageNum.value!! - 1

                if(viewModel.suggestedGamesList.value?.size!! < GameSuggestionsFragment.MAX_ENTRIES_PER_PAGE)
                   */
                RecyclerViewAdapter.showImages = false
                viewAdapter.recyclerGameList = viewModel.suggestedGamesList.value?.toTypedArray()!!
              /*  else if(viewModel.suggestedGamePageNum.value!! * GameSuggestionsFragment.MAX_ENTRIES_PER_PAGE > viewModel.suggestedGamesList.value?.size!!)
                    viewAdapter.recyclerGameList = viewModel.suggestedGamesList.value?.toTypedArray()!!.copyOfRange((viewModel.suggestedGamePageNum.value!! - 1) * GameSuggestionsFragment.MAX_ENTRIES_PER_PAGE, viewModel.suggestedGamesList.value?.size!! - 1)
                else
                    viewAdapter.recyclerGameList = viewModel.suggestedGamesList.value?.toTypedArray()!!.copyOfRange((viewModel.suggestedGamePageNum.value!! - 1) * GameSuggestionsFragment.MAX_ENTRIES_PER_PAGE, (viewModel.suggestedGamePageNum.value!! - 1) * GameSuggestionsFragment.MAX_ENTRIES_PER_PAGE + GameSuggestionsFragment.MAX_ENTRIES_PER_PAGE)
*/
                viewAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        RecyclerViewAdapter.showImages = false
        //only one thread can enter this section of code at a time.
        if(viewModel.suggestedGamesLock.value!! == false) {
            viewModel.suggestedGamesLock.value = true
            viewModel.updateBestGamesList()

            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            executorService.execute {
                viewModel.updateSuggestedGamesList()
                viewModel.suggestedGamesLock.postValue(false)
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
         * @return A new instance of fragment GameSuggestionsFragment.
         */

        var MAX_ENTRIES_PER_PAGE = 5
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GameSuggestionsFragment().apply {
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
        findNavController().navigate(R.id.action_gameSuggestionsFragment_to_gameDescriptionFragment)
    }
}