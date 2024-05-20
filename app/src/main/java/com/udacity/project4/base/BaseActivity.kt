package com.udacity.project4.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewbinding.ViewBinding
import com.udacity.project4.utils.ToastUtils

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var mBinding: VB

    //abstract fun initViewBinding(): VB

    abstract fun initLayoutId(): Int

    abstract fun initViews()

    abstract fun initActions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //mBinding = initViewBinding()
        mBinding = DataBindingUtil.setContentView(this, initLayoutId())
        setContentView(mBinding.root)
        initViews()
        initActions()
    }

    override fun onStop() {
        super.onStop()
        ToastUtils.cancelToast()
    }

}