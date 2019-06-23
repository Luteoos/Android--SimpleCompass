package io.github.luteoos.simplecompass.viewmodel

import com.luteoos.kotlin.mvvmbaselib.BaseViewModel
import timber.log.Timber

class CompassViewModel : BaseViewModel() {
    init {
        Timber.i("init ${this}")
    }
}