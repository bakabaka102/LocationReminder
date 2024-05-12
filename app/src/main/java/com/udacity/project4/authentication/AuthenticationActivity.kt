package com.udacity.project4.authentication

import com.udacity.project4.R
import com.udacity.project4.base.BaseActivity
import com.udacity.project4.databinding.ActivityAuthenticationBinding

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : BaseActivity<ActivityAuthenticationBinding>() {

    /*override fun initViewBinding(): ActivityAuthenticationBinding {
        return ActivityAuthenticationBinding.inflate(layoutInflater)
    }*/

    override fun initLayoutId(): Int {
        return R.layout.activity_authentication
    }

    override fun initViews() {
        // TODO: Implement the create account and sign in using FirebaseUI,
        //  use sign in using email and sign in using Google
        // TODO: If the user was authenticated, send him to RemindersActivity
        // TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }

    override fun initActions() {

    }
}