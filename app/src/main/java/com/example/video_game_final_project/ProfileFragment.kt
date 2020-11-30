package com.example.video_game_final_project

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    lateinit var viewAdapter: RecyclerViewAdapter
    lateinit var viewManager: GridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewManager = GridLayoutManager(context, 2, LinearLayoutManager.VERTICAL, false)

        if(viewModel.profileGamesList.value == null )
            viewAdapter = RecyclerViewAdapter(emptyArray())
        else
            viewAdapter = RecyclerViewAdapter(viewModel.profileGamesList.value?.toTypedArray()!!)

        viewAdapter.goToGameDescriptionFunction = goToGameDescriptionFunction
        RecyclerViewAdapter.showImages = true

        profileRecycler.layoutManager = viewManager
        profileRecycler.adapter = viewAdapter


        viewModel.profileGamesList.observe(viewLifecycleOwner, {
            profileRecycler.removeAllViews()
            viewAdapter.recyclerGameList = emptyArray()
            viewAdapter.notifyDataSetChanged()
            if(viewModel.profileGamesList.value != null)
                viewAdapter.recyclerGameList = viewModel.profileGamesList.value?.toTypedArray()!!
            else
                viewAdapter.recyclerGameList = emptyArray()
            viewAdapter.notifyDataSetChanged()
        })
    }


    var goToGameDescriptionFunction: (FavoriteGame) -> Unit = {
        viewModel.currentGame.postValue(null)
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        executorService.execute {
            viewModel.updateCurrentGameWithSpecificGame(it.gameID)
        }
        findNavController().navigate(R.id.action_profileFragment_to_gameDescriptionFragment)
    }


    override fun onResume() {
        super.onResume()
        RecyclerViewAdapter.showImages = true
        if(viewModel.profileGamesList.value != null)
            Log.d("TAG_MSG", "In Profile Fragment onResume(), profileGamesList size was: " + viewModel.profileGamesList.value?.size.toString())

        viewModel.updateProfileList()
        if(viewModel.profileGamesList.value != null)
        {
            viewAdapter.recyclerGameList = viewModel.profileGamesList.value?.toTypedArray()!!
            viewAdapter.notifyDataSetChanged()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}