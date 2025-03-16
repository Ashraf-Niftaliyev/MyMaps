package com.esrefnifteliyev.mymaps.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.esrefnifteliyev.mymaps.model.Place

@Database([Place::class], version = 3)
abstract class PlaceDatabase : RoomDatabase(){
    abstract fun placeDao() : PlaceDao
}