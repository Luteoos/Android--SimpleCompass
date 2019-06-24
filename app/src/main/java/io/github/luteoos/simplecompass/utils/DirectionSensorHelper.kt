package io.github.luteoos.simplecompass.utils

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import timber.log.Timber

class DirectionSensorHelper(private val ctx: Context) {

    private val sensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var gravitationValues: FloatArray? = null
    private var magneticValues: FloatArray? = null
    private val direction: MutableLiveData<Int> = MutableLiveData()
    private val sensorAccuarcyDropped: MutableLiveData<Boolean> = MutableLiveData()
    private val magneticSensorListener = object : SensorEventListener{
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            accuarcySensorChanged(p1)
        }

        override fun onSensorChanged(p0: SensorEvent?) {
            if(p0 != null)
                if (p0.accuracy > 1) {
                    magneticValues = p0.values
                    afterUpdatedValues()
                }

        }
    }
    private val accelerometerSensorListener = object : SensorEventListener{
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            accuarcySensorChanged(p1)
        }

        override fun onSensorChanged(p0: SensorEvent?) {
            if(p0 != null)
                if(p0.accuracy > 1){
                    gravitationValues = p0.values
                    afterUpdatedValues()
                }
        }
    }

    fun isRequiredSensorAvailable() =
        ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER) &&
                ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)

    fun start(){
        sensorManager.registerListener(magneticSensorListener, sensorMagnetic, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(accelerometerSensorListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop(){
        sensorManager.unregisterListener(magneticSensorListener)
        sensorManager.unregisterListener(accelerometerSensorListener)
    }

    fun getDirectionLiveData() : LiveData<Int> = direction

    fun getAccuarcyDropUpdates() : LiveData<Boolean> = sensorAccuarcyDropped

    private fun afterUpdatedValues() {
        if (gravitationValues != null && magneticValues != null) {
            val R = FloatArray(9)
            if (SensorManager.getRotationMatrix(R, FloatArray(9), gravitationValues, magneticValues)) {
                val azimuth = SensorManager.getOrientation(R, FloatArray(3))[0]
                val degree = ((Math.toDegrees(azimuth.toDouble()) + 360) % 360).toInt()
                if(degree != direction.value)
                    direction.value = degree
                Timber.i("degree $degree")
            }
        }
    }

    private fun accuarcySensorChanged(acc: Int){
        Timber.i("accuracy changed to $acc")
        when(acc){
            0,1 -> sensorAccuarcyDropped.value = true
            2,3 -> sensorAccuarcyDropped.value = false
        }
    }
}