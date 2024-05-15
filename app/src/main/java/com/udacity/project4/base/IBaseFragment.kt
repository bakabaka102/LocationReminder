package com.udacity.project4.base

import android.os.Bundle

interface IBaseFragment {

    fun initData(data: Bundle?)

    fun initViews()

    fun initActions()

    fun initObservers()
}