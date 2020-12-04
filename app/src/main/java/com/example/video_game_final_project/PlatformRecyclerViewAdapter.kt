package com.example.video_game_final_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.platform_view.view.*

class PlatformRecyclerViewAdapter(var platformData: ArrayList<Platform>) :
    RecyclerView.Adapter<PlatformRecyclerViewAdapter.PlatformRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatformRecyclerViewHolder {
        val viewItem = LayoutInflater.from(parent.context).inflate(
            R.layout.platform_view,
            parent,
            false
        )
        return PlatformRecyclerViewHolder(viewItem)
    }

    lateinit var clickLambda: (Platform) -> Unit
    override fun onBindViewHolder(holder: PlatformRecyclerViewHolder, position: Int) {
        holder.bind(platformData[position], clickLambda)
    }

    override fun getItemCount(): Int {
        return platformData.size
    }

    class PlatformRecyclerViewHolder(val viewItem: View) : RecyclerView.ViewHolder(viewItem) {

        fun bind(platform: Platform, clickLambda: (Platform) -> Unit) {
            val checkBox = viewItem.findViewById<CheckBox>(R.id.platform_checkbox)

            viewItem.findViewById<TextView>(R.id.platform_text).text = platform.name
            checkBox.isChecked = platform.isOwned
            viewItem.findViewById<ImageView>(R.id.platform_image).setImageResource(R.drawable.image_not_available)

            checkBox.setOnClickListener {
                platform.isOwned = checkBox.isChecked
                clickLambda(platform)
            }


        }
    }


}