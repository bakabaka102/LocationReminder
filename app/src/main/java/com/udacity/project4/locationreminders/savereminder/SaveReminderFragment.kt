package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.LogUtils
import com.udacity.project4.utils.ToastUtils
import com.udacity.project4.utils.isAccessFineLocation
import com.udacity.project4.utils.isBackgroundLocationEnable
import com.udacity.project4.utils.isPostNotificationEnable
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment<FragmentSaveReminderBinding>() {

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var mContext: Context
    private lateinit var reminderDataItem: ReminderDataItem
    private val permissionLauncherPostNotify =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                activity?.let { ToastUtils.showToast(it, "Notification is granted.") }
            } else {
                activity?.let { ToastUtils.showToast(it, "Need permission notification.") }
            }
        }

    private val resultLauncherGPS =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                checkDeviceLocationSettingsAndStartGeofence(dataItem = _viewModel.getReminderDataItem())
            }
        }

    companion object {
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 1001
        const val GEOFENCE_RADIUS = 100f
        const val ACTION_GEOFENCE_EVENT = "geofence.action.ACTION_GEOFENCE_EVENT"
        const val REQUEST_CODE_29 = 29
        const val REQUEST_CODE_33 = 33

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()

    override fun layoutViewDataBinding(): Int = R.layout.fragment_save_reminder

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    override fun initData(data: Bundle?) {
        setDisplayHomeAsUpEnabled(true)
        geofencingClient = LocationServices.getGeofencingClient(mContext)
        mBinding.viewModel = _viewModel
    }

    override fun initViews() {
        mBinding.lifecycleOwner = this
        if (activity?.isPostNotificationEnable() == false) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncherPostNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        //permissionLauncherTurnOnGPS.launch()
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
            checkPermissionsAndAddGeofencing()
        }
    }

    override fun initObservers() {
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            checkDeviceLocationSettingsAndStartGeofence(dataItem = _viewModel.getReminderDataItem())
        }
    }*/

    private fun checkPermissionsAndAddGeofencing() {
        if (activity?.isAccessFineLocation() == true && activity?.isBackgroundLocationEnable() == true) {
            checkDeviceLocationSettingsAndStartGeofence(dataItem = _viewModel.getReminderDataItem())
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(
        resolve: Boolean = true,
        dataItem: ReminderDataItem
    ) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .setMaxUpdateDelayMillis(300)
            .build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(mContext)
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    resultLauncherGPS.launch(intentSenderRequest)
                } catch (ex: IntentSender.SendIntentException) {
                    LogUtils.e("Error getting location settings --- ${ex.message}")
                }
            } else {
                Snackbar.make(
                    mBinding.root,
                    R.string.error_location_required,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence(dataItem = _viewModel.getReminderDataItem())
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofence(dataItem)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(reminderDataItem: ReminderDataItem) {
        if (reminderDataItem.longitude != null && reminderDataItem.latitude != null) {
            val geofence = Geofence.Builder().setRequestId(reminderDataItem.id)
                .setCircularRegion(
                    reminderDataItem.latitude!!,
                    reminderDataItem.longitude!!,
                    GEOFENCE_RADIUS
                ).setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER).build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence).build()
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                addOnSuccessListener {
                    LogUtils.d("Add Geofence ${geofence.requestId}")
                }
                addOnFailureListener {
                    activity?.let { it1 ->
                        ToastUtils.showToast(
                            it1,
                            "Failed to add location!!! Try again later!"
                        )
                    }
                }
            }
        }
        _viewModel.validateAndSaveReminder(reminderDataItem)
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val intent = Intent(mContext, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(mContext, 0, intent, flags)
    }

    private fun requestForegroundAndBackgroundLocationPermissions() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                REQUEST_CODE_29
            }

            else -> REQUEST_CODE_33
        }
        LogUtils.d("Request foreground, background location permission")
        requestPermissions(permissions.toTypedArray(), resultCode)
    }

}