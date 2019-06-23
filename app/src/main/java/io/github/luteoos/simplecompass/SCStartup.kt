package io.github.luteoos.simplecompass

import android.app.Application
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.luteoos.kotlin.mvvmbaselib.BaseActivityMVVM

class CompassStartUp : Application(){

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG)
            initDebug()
    }

    private fun initDebug(){

    }
}
