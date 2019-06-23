package io.github.luteoos.simplecompass.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
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
import io.github.luteoos.simplecompass.utils.Parameter
import io.github.luteoos.simplecompass.viewmodel.CompassViewModel
import kotlinx.android.synthetic.main.activity_main_compass.*
import timber.log.Timber

class MainCompassActivity : BaseActivityMVVM<CompassViewModel>() {

    private var locCallback: LocationCallback? = null

    override fun getLayoutID(): Int = R.layout.activity_main_compass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("init ${this}")
        setPortraitOrientation(true)
        viewModel = getViewModel(this)
        createLocCallback()
        setBindings()
        checkPermissions()
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
                        showPermissionSnackbar()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        if(isLocationPermissionGranted())
            connectLocationListener()
        (getSystemService(Context.SENSOR_SERVICE) as
        SensorManager).getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onStop() {
        super.onStop()
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

    private fun setBindings(){
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

    @SuppressLint("MissingPermission")
    private fun updateLoc() {
        if (etLat.text.isNotEmpty() && etLon.text.isNotEmpty()) {
            val lat = etLat.text.toString().toDoubleOrNull()
            val lon = etLon.text.toString().toDoubleOrNull()
            if (lat != null && lon != null) {
                viewModel.updateTargetLoc(lat, lon)
            }
            else
                Toast.makeText(this, R.string.wrong_format, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPermissionSnackbar(){
        Snackbar
            .make(constraintLayout, R.string.required_permission, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) { requestLocationPermission() }
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
}