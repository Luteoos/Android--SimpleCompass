package io.github.luteoos.simplecompass.utils

import android.location.Location

data class LocData(var lat: Double = 0.0,
                   var lon: Double = 0.0){

    fun getLocation() = Location("LocData").apply {
        latitude = lat
        longitude = lon
    }
}