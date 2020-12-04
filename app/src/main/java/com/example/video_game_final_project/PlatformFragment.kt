package com.example.video_game_final_project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_platform.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlatformFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlatformFragment : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_platform, container, false)
    }

    lateinit var viewManger: RecyclerView.LayoutManager
    lateinit var viewAdapter: PlatformRecyclerViewAdapter
    val viewModel: GameViewModel by activityViewModels<GameViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewManger = LinearLayoutManager(activity)
        viewAdapter = PlatformRecyclerViewAdapter(viewModel.allPlatformsList.value!!)

        platform_recycler.layoutManager = viewManger
        platform_recycler.adapter = viewAdapter

        val clickLambda:(Platform)->Unit={
            viewModel.updateAllPlatformList(it)
        }
        viewAdapter.clickLambda = clickLambda

        platform_owned_button.setOnClickListener {
            if (platform_owned_button.isChecked){
                val list = viewModel.allPlatformsList.value!!.filter { it.isOwned }
                viewAdapter.platformData = ArrayList(list)
                viewAdapter.notifyDataSetChanged()
            } else {
                viewAdapter.platformData = viewModel.allPlatformsList.value!!
                viewAdapter.notifyDataSetChanged()
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
         * @return A new instance of fragment PlatformFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlatformFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}