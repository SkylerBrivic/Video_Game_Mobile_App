package com.example.video_game_final_project

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "platformDB")
class Platform(
    @PrimaryKey
    var platformId: Int,
    var name: String,
    var isOwned: Boolean,
    var pictureResourceID: Int) {
}