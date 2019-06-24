package io.github.luteoos.simplecompass.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.luteoos.kotlin.mvvmbaselib.BaseActivityMVVM
import io.github.luteoos.simplecompass.R
import io.github.luteoos.simplecompass.utils.DirectionSensorHelper
import io.github.luteoos.simplecompass.utils.Parameter
import io.github.luteoos.simplecompass.viewmodel.CompassViewModel
import kotlinx.android.synthetic.main.activity_main_compass.*
import timber.log.Timber

class MainCompassActivity : BaseActivityMVVM<CompassViewModel>() {

    private var locCallback : LocationCallback? = null
    private lateinit var sensorHelper : DirectionSensorHelper
    private lateinit var lowAccuracySnackBar : Snackbar

    override fun getLayoutID(): Int = R.layout.activity_main_compass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("init ${this}")
        setPortraitOrientation(true)
        viewModel = getViewModel(this)
        sensorHelper = DirectionSensorHelper(this)
        initSnackBar()
        createLocCallback()
        setBindings()
        checkPermissions()
        connectDirectionSensor()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty())
            when(requestCode){
                Parameter.REQUEST_GET_LOCATION_PERMISSION -> {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        showLocInput()
                        connectLocationListener()
                    }
                    else
                        showPermissionSnackBar()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        checkGPSavailability()
        if(!sensorHelper.isRequiredSensorAvailable())
            showNoRequiredSensorSnackBar()
        else
            sensorHelper.start()
        if(isLocationPermissionGranted())
            connectLocationListener()
    }

    override fun onStop() {
        super.onStop()
        sensorHelper.stop()
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(locCallback)

    }

    private fun isLocationPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Parameter.REQUEST_GET_LOCATION_PERMISSION )
    }

    private fun createLocCallback(){
        locCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation.let {
                    if(it != null)
                        viewModel.updateCurrentLoc(it.latitude, it.longitude)
                    else
                        Timber.e("Location in fusedProvider is null")
                }
            }
        }
    }

    private fun checkPermissions(){
        if(!isLocationPermissionGranted())
            requestLocationPermission()
        else {
            showLocInput()
            connectLocationListener()
        }
    }

    private fun checkGPSavailability(){
        if(!(getSystemService( Context.LOCATION_SERVICE ) as LocationManager).isProviderEnabled( LocationManager.GPS_PROVIDER))
            showGPSisDisabledSnackBar()
    }

    @SuppressLint("MissingPermission")
    private fun connectLocationListener(){
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(
                LocationRequest()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(1000)
                    .setInterval(5000),
                locCallback,
                null)
    }

    private fun initSnackBar(){
        lowAccuracySnackBar = Snackbar
            .make(constraintLayout, R.string.low_accuracy_sensor, Snackbar.LENGTH_INDEFINITE)
    }

    private fun setBindings(){
        viewModel.getTargetBearing()
            .observe(this, Observer {
                rotateTargetPin(it ?: 0f)
            })
        constraintLayout.setOnTouchListener {_,_ ->
            clearFocus()
            return@setOnTouchListener true
        }
        arrayOf(etLat,etLon).forEach {
            it.apply {
                setOnKeyListener { _, keyCode, keyEvent ->
                    if (keyEvent.action == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_ENTER
                    ) {
                        this@MainCompassActivity.clearFocus()
                        return@setOnKeyListener true
                    }
                    return@setOnKeyListener false
                }
                onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    if(!hasFocus)
                        updateLoc()
                }
            }
        }
    }

    private fun connectDirectionSensor(){
        sensorHelper
            .getDirectionLiveData()
            .observe(this, Observer {
                rotateDirectionPin(it ?: 0)
            })
        sensorHelper
            .getAccuarcyDropUpdates()
            .observe(this, Observer {
                showSensorAccuracyChangedSnackBar(it ?: false)
            })
    }

    @SuppressLint("MissingPermission")
    private fun updateLoc() {
        if (etLat.text.isNotEmpty() && etLon.text.isNotEmpty()) {
            val lat = etLat.text.toString().toDoubleOrNull()
            val lon = etLon.text.toString().toDoubleOrNull()
            if (lat != null && lon != null) {
                checkGPSavailability()
                viewModel.updateTargetLoc(lat, lon)
            }
            else
                Toast.makeText(this, R.string.wrong_format, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPermissionSnackBar(){
        Snackbar
            .make(constraintLayout, R.string.required_permission, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) { requestLocationPermission() }
            .show()
    }

    private fun showNoRequiredSensorSnackBar(){
        Snackbar
            .make(constraintLayout, R.string.required_sensor, Snackbar.LENGTH_LONG)
            .show()
    }

    private fun showSensorAccuracyChangedSnackBar(bool: Boolean){
        when(bool){
            false -> lowAccuracySnackBar.dismiss()
            true -> lowAccuracySnackBar.show()
        }
    }

    private fun showGPSisDisabledSnackBar(){
        Snackbar
            .make(constraintLayout, R.string.gps_disabled, Snackbar.LENGTH_LONG)
            .show()
    }

    private fun clearFocus(){
        hideKeyboard()
        etLat.clearFocus()
        etLon.clearFocus()
    }

    private fun showLocInput(){
        layoutLocInput.visibility = View.VISIBLE
    }

    private fun rotateTargetPin(direction: Float){
        ivTargetPin.rotation = direction
    }

    private fun rotateDirectionPin(direction: Int) {
        if(Math.abs(ivDirectionArrow.rotation - direction) > Parameter.DIRECTION_THRESHOLD &&
            Math.abs(ivDirectionArrow.rotation - direction) < Parameter.NORTH_THRESHOLD)
            ivDirectionArrow.rotation = direction.toFloat()
    }
}