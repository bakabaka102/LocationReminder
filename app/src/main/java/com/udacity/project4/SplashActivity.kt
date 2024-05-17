package com.udacity.project4

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationState
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.locationreminders.RemindersActivity


class SplashActivity : AppCompatActivity() {

    private val viewModel: AuthenticationViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({ navigateActivity() }, 1000)

    }

    private fun navigateActivity() {
        viewModel.authenticationState.observe(this) { state ->
            val intent = if (state == AuthenticationState.AUTHENTICATED) {
                Intent(this@SplashActivity, RemindersActivity::class.java)
            } else {
                Intent(this@SplashActivity, AuthenticationActivity::class.java)
            }
            startActivity(intent)
            finish()
        }

    }
}