package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.LogUtils
import com.udacity.project4.utils.isAccessFineLocation
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.shouldAccessLocationRationale
import org.koin.android.ext.android.inject
import java.util.Locale

class SelectLocationFragment : BaseFragment<FragmentSelectLocationBinding>(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    override fun layoutViewDataBinding(): Int = R.layout.fragment_select_location

    private lateinit var googleMap: GoogleMap
    private var mapMarker: Marker? = null
    private val activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                return@registerForActivityResult
            } else {
                showDialogRequestPermission()
            }
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) &&
                !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                showDialogRequestPermission()
            }
        }

    private val mMenuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.map_options, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
            R.id.normal_map -> {
                true
            }

            R.id.hybrid_map -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }

            R.id.satellite_map -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }

            R.id.terrain_map -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }

            else -> true
        }
    }

    companion object {
        const val DEFAULT_ZOOM_MAP_LEVEL = 16F
    }

    override fun initData(data: Bundle?) {
        mBinding.viewModel = _viewModel
        mBinding.lifecycleOwner = this
        setDisplayHomeAsUpEnabled(true)
    }

    override fun initViews() {
        activity?.addMenuProvider(
            mMenuProvider,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun initActions() {
        mBinding.btnSaveLocation.setOnClickListener {
            onLocationSelected()
        }
    }

    override fun initObservers() {

    }

    private fun onLocationSelected() {
        _viewModel.latitude.value = mapMarker?.position?.latitude
        _viewModel.longitude.value = mapMarker?.position?.longitude
        _viewModel.reminderSelectedLocationStr.value = mapMarker?.title
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setMapStyle()
        googleMap.setMapLongClick()
        googleMap.setPoiClick()
        if (activity?.isAccessFineLocation() == true) {
            accessCurrentLocation()
        } else {
            showDialogRequestPermission()
        }
    }

    private fun showDialogRequestPermission() {
        if (requireActivity().shouldAccessLocationRationale()) {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.location_permission)
                .setMessage(R.string.permission_denied_explanation)
                .setPositiveButton("OK") { _, _ ->
                    activityResultLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                }
                .create()
                .show()

        }/* else {
            activityResultLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }*/
    }

    private fun GoogleMap.moveCamera(userLocation: LatLng, zoom: Float = DEFAULT_ZOOM_MAP_LEVEL) {
        this.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoom))
    }

    @SuppressLint("MissingPermission")
    private fun accessCurrentLocation() {
        googleMap.isMyLocationEnabled = true
        val fusedLocationClient: FusedLocationProviderClient? =
            activity?.let {
                LocationServices.getFusedLocationProviderClient(it)
            }
        val lastLocation = fusedLocationClient?.lastLocation
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .setMaxUpdateDelayMillis(300)
            .build()
        val onCallback = object : LocationCallback() {}
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            onCallback,
            Looper.getMainLooper()
        )
        lastLocation?.addOnSuccessListener {
            it?.let { location ->
                val userLocation = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(userLocation)
                googleMap.markerMapLocation(userLocation)
                mapMarker?.showInfoWindow()
            }
        }
    }

    private fun GoogleMap.markerMapLocation(userLocation: LatLng) {
        mapMarker = this.addMarker(
            MarkerOptions().position(userLocation)
                .title(getString(R.string.dropped_pin))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        )
    }

    private fun GoogleMap.setMapLongClick() {
        this.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            this.clear()
            mapMarker = this.addMarker(
                MarkerOptions().position(latLng)
                    .title(getString(R.string.dropped_pin)).snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            mapMarker?.showInfoWindow()
            this.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    private fun GoogleMap.setPoiClick() {
        this.setOnPoiClickListener { poi: PointOfInterest ->
            this.clear()
            mapMarker = this.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            mapMarker?.showInfoWindow()
            this.animateCamera(CameraUpdateFactory.newLatLng(poi.latLng))
        }
    }

    private fun GoogleMap.setMapStyle() {
        try {
            // Customize the styling of the base map using a JSON object defined in a raw resource file.
            val success = this.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_custom)
            )
            if (!success) {
                LogUtils.e("Style parsing is failed.")
            }
        } catch (e: Resources.NotFoundException) {
            LogUtils.e("Can't find style file: $e")
        }
    }
}