package com.example.video_game_final_project

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import java.net.URL

/**
 * @constructor : to create a PosterLoader instance, use PosterLoader.getInstance()
 */
class PosterLoader {

    companion object {
        private var loader: PosterLoader? = null

        fun getInstance(): PosterLoader {
            if (loader == null) {
                loader = PosterLoader()
            }
            return loader!!
        }
    }


    /**
     * A function to load the poster image from the API
     * @param url: poster path
     * @param imgView: The ImageView to show the returned poster image
     */
    fun loadURL(url: String, imgView: ImageView) {
        Log.e("TAG_MSG", "URL: " + url)
        LoaderAsync(imgView).execute(url)
    }

    private class LoaderAsync(internal var bmImage: ImageView) :
        AsyncTask<String, Void, Bitmap>() {

        override fun doInBackground(vararg urls: String): Bitmap? {
            val urldisplay = urls[0]
            var mIcon11: Bitmap? = null
            try {
                val url = URL(urldisplay)
                mIcon11 = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            } catch (e: Exception) {
                println("error" + e.printStackTrace())
            }

            return mIcon11
        }

        override fun onPostExecute(result: Bitmap) {
            println("result")
            bmImage.setImageBitmap(result)
            println(bmImage)
        }
    }
}

