package io.github.luteoos.simplecompass.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.luteoos.kotlin.mvvmbaselib.BaseViewModel
import io.github.luteoos.simplecompass.utils.LocData
import timber.log.Timber

class CompassViewModel : BaseViewModel() {
    init {
        Timber.i("init ${this}")
    }

    private val angleToTarget = MutableLiveData<Float>()
    private val target = LocData()
    private val current = LocData()

    fun getTargetBearing(): LiveData<Float> = angleToTarget

    fun updateTargetLoc(lat: Double, lon: Double){
        Timber.i("target $lat $lon")
        target.lat = lat
        target.lon = lon
        updateAngle()
    }

    fun updateCurrentLoc(lat: Double, lon: Double){
        Timber.i("current $lat $lon")
        current.lat = lat
        current.lon = lon
        updateAngle()
    }

    private fun convertToDegree(bearing: Float): Float{
        return if(bearing >= 0)
            bearing
        else{
            360f + bearing
        }
    }

    private fun updateAngle(){
        if(target.getLocation() != null && current.getLocation() != null)
            angleToTarget.value = convertToDegree(current.getLocation()!!.bearingTo(target.getLocation()!!))
    }
}