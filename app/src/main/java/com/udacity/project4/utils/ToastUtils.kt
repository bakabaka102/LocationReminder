package com.udacity.project4.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {

    private var mToast: Toast? = null

    fun showToast(context: Context, text: String?) {
        mToast?.cancel()
        mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        //mToast?.setText(text)
        mToast?.show()
    }

    fun cancelToast() {
        mToast?.cancel()
    }
}