package io.github.luteoos.simplecompass.utils

import android.location.Location

data class LocData(var lat: Double? = null,
                   var lon: Double? = null){

    fun getLocation() =
        if(lat != null && lon != null)
        Location("LocData").apply {
            latitude = lat!!
            longitude = lon!!
        }
    else
        null
}