package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment<FragmentSaveReminderBinding>() {

    // Get the view model this time as a single to be shared with the another fragment
    private lateinit var reminderDataItem: ReminderDataItem

    override val _viewModel: SaveReminderViewModel by inject()
    override fun layoutViewDataBinding(): Int = R.layout.fragment_save_reminder

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    override fun initData(data: Bundle?) {
        setDisplayHomeAsUpEnabled(true)
        mBinding.viewModel = _viewModel
    }

    override fun initViews() {
        mBinding.lifecycleOwner = this
    }

    override fun initActions() {
        mBinding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            val directions = SaveReminderFragmentDirections
                .actionSaveReminderFragmentToSelectLocationFragment()
            _viewModel.navigationCommand.value = NavigationCommand.To(directions)
        }

        mBinding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            // TODO: use the user entered reminder details to:
            //  1) add a geofencing request
            //  2) save the reminder to the local db
            reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)
            if (_viewModel.validateEnteredData(reminderDataItem)) {
                _viewModel.saveReminder(reminderDataItem)
            }
        }
    }

    override fun initObservers() {
    }
}