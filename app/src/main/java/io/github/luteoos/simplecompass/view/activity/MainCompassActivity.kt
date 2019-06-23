package io.github.luteoos.simplecompass.view.activity

import android.os.Bundle
import com.luteoos.kotlin.mvvmbaselib.BaseActivityMVVM
import io.github.luteoos.simplecompass.R
import io.github.luteoos.simplecompass.viewmodel.CompassViewModel

class MainCompassActivity : BaseActivityMVVM<CompassViewModel>() {

    override fun getLayoutID(): Int = R.layout.activity_main_compass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel(this)
    }
}