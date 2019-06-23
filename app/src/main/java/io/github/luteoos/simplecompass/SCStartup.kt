package io.github.luteoos.simplecompass

import android.app.Application
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import com.luteoos.kotlin.mvvmbaselib.BaseActivityMVVM
import timber.log.Timber

class SCStartup : Application(){

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG)
            initDebug()
    }

    private fun initDebug(){
        Timber.plant(Timber.DebugTree())

        Timber.i("initDebugStuff")
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build())
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .detectFileUriExposure()
                .penaltyLog()
                .penaltyDeath()
                .build())
    }
}
