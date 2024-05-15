package com.udacity.project4.locationreminders

import android.view.MenuItem
import androidx.navigation.findNavController
import com.udacity.project4.R
import com.udacity.project4.base.BaseActivity
import com.udacity.project4.databinding.ActivityRemindersBinding

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : BaseActivity<ActivityRemindersBinding>() {

    override fun initLayoutId(): Int {
        return R.layout.activity_reminders
    }

    /*override fun initViewBinding(): ActivityRemindersBinding {
        return ActivityRemindersBinding.inflate(layoutInflater)
    }*/

    override fun initViews() {

    }

    override fun initActions() {

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //(mBinding.navHostFragment as NavHostFragment).navController.popBackStack()
                findNavController(R.id.nav_host_fragment).popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
