package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import com.udacity.project4.R
import com.udacity.project4.base.BaseActivity
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : BaseActivity<ActivityReminderDescriptionBinding>() {

    companion object {
        private const val EXTRA_REMINDER_ITEM = "EXTRA_REMINDER_ITEM"

        // Receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_REMINDER_ITEM, reminderDataItem)
            return intent
        }
    }

    /*override fun initViewBinding(): ActivityReminderDescriptionBinding {
        return ActivityReminderDescriptionBinding.inflate(layoutInflater)
    }*/

    override fun initLayoutId(): Int {
        return R.layout.activity_reminder_description
    }

    override fun initViews() {

    }

    override fun initActions() {

    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutId = R.layout.activity_reminder_description
        binding = DataBindingUtil.setContentView(this, layoutId)
        // TODO: Add the implementation of the reminder details
    }*/
}