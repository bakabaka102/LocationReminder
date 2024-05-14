package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.Locale

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var googleMap: GoogleMap
    private var mapMarker: Marker? = null
    private var permissionLocationGranted = false
    private var locationManager: LocationManager? = null
    private var isEnableGPS = false
    private val activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                //checkGPSPermission()
                return@registerForActivityResult
            } else {
                permissionLocationGranted = false
                //showDialogRequest()
            }
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) &&
                !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                //showDialogRequest()
            }
        }

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // PERMISSION GRANTED
        } else {
            // PERMISSION NOT GRANTED
        }
    }

    // Ex. Launching ACCESS_FINE_LOCATION permission.
    private fun startLocationPermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        requireActivity().addMenuProvider(
            mMenuProvider,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
        setDisplayHomeAsUpEnabled(true)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSaveLocation.setOnClickListener {
            // TODO: add the map setup implementation
            // TODO: zoom to the user location after taking his permission
            // TODO: add style to the map
            // TODO: put a marker to location that the user selected
            // TODO: call this function after the user confirms on the selected location
            onLocationSelected()
        }
    }

    private fun onLocationSelected() {
        // TODO: When the user confirms on the selected location,
        //  send back the selected location details to the view model
        //  and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.latitude.value = mapMarker?.position?.latitude
        _viewModel.longitude.value = mapMarker?.position?.longitude
        _viewModel.reminderSelectedLocationStr.value = mapMarker?.title
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setMapLongClick()
        googleMap.setPoiClick()
        if (isPermissionGPSGranted()) {
            getUserLocation()
        } else {
            showDialogRequestPermission()
        }
    }

    private fun showDialogRequestPermission() {
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.location_permission)
                .setMessage(R.string.permission_denied_explanation)
                .setPositiveButton("OK") { _, _ ->
                    requestLocationPermission()
                }
                .create()
                .show()

        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            activityResultLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            /* this.requestPermissions(
                 arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                 PERMISSION_CODE_LOCATION_REQUEST
             )*/
        }
    }

    private fun isPermissionGPSGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        googleMap.isMyLocationEnabled = true
        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener { location ->
            location?.let {
                val userLocation = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        userLocation,
                        DEFAULT_ZOOM_MAP_LEVEL
                    )
                )
                mapMarker = googleMap.addMarker(
                    MarkerOptions().position(userLocation)
                        .title(getString(R.string.dropped_pin))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                )
                mapMarker?.showInfoWindow()
            }
        }
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
        this.setOnPoiClickListener { poi ->
            this.clear()
            mapMarker = this.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            mapMarker?.showInfoWindow()
            this.animateCamera(CameraUpdateFactory.newLatLng(poi.latLng))
        }
    }
}