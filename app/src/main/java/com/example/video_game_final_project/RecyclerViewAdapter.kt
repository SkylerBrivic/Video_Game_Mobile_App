package com.example.video_game_final_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


//This is the RecyclerViewAdapter class which the user's profile page uses, and which the general game search feature uses
//to display games as well

class RecyclerViewAdapter(var recyclerGameList: Array<FavoriteGame>)
    : RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>()
{
    companion object {
        var showImages: Boolean = false
    }

    class RecyclerViewHolder(val viewItem: View) : RecyclerView.ViewHolder(viewItem)
    {
        fun bind(myGame: FavoriteGame, goToGameDescriptionFunction: (FavoriteGame) -> Unit)
        {
            //If there is no image associated with this game, then we display the "image_not_available" image
            if(showImages)
            {
                if(myGame.previewURL.equals("image_not_available", true) || myGame.previewURL.equals("null", true) || myGame.previewURL.isBlank())
                    viewItem.findViewById<ImageView>(R.id.gamePreviewImage).setImageResource(viewItem.resources.getIdentifier("image_not_available", "drawable", "com.example.video_game_final_project"))

                //...Otherwise, we load the image from the internet and put it into the image view for the game preview.
                else
                    PosterLoader.getInstance().loadURL(myGame.previewURL, viewItem.findViewById<ImageView>(R.id.gamePreviewImage))
            }

            if(myGame.releaseDate.equals("null", true) || myGame.releaseDate.isBlank())
                myGame.releaseDate = "N/A"

            if(myGame.gameName.equals("null", true) || myGame.gameName.isBlank())
                myGame.gameName = "N/A"

            viewItem.findViewById<TextView>(R.id.releaseDatePreview).setText(myGame.releaseDate)
            viewItem.findViewById<TextView>(R.id.namePreview).setText(myGame.gameName)

            //When the game is clicked on, the goToGameDescription() function is called.
            viewItem.setOnClickListener {
                goToGameDescriptionFunction(myGame)
            }
        }
    }

    lateinit var goToGameDescriptionFunction: (FavoriteGame) -> Unit
    //Setting the gamePreview layout to be the layout file for each object in the recycler view.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val viewItem = LayoutInflater.from(parent.context).inflate(R.layout.game_preview, parent, false)
        return RecyclerViewHolder(viewItem)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return recyclerGameList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.bind(recyclerGameList[position], goToGameDescriptionFunction)
    }
}
