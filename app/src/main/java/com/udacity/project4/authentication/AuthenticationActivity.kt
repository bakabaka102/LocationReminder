package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.base.BaseActivity
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.LogUtils
import com.udacity.project4.utils.ToastUtils

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : BaseActivity<ActivityAuthenticationBinding>() {

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            val response = IdpResponse.fromResultIntent(data)
            if (result.resultCode == Activity.RESULT_OK) {
                LogUtils.i("User signed in: ${FirebaseAuth.getInstance().currentUser?.displayName}")
                ToastUtils.showToast(this, "Successfully signed in!.")
                startRemindersActivity()
            } else {
                LogUtils.i("Sign in unsuccessful ${response?.error?.errorCode}")
                ToastUtils.showToast(this, "Sign in unsuccessful!.")
            }
        }

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
        mBinding.btnLogin.setOnClickListener {
            startRemindersActivity()
            //launchSignInFlow()

        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        val authIntent: Intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        resultLauncher.launch(authIntent)
    }

    private fun startRemindersActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        ToastUtils.cancelToast()
    }
}