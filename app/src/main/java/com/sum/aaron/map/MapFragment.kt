package com.sum.aaron.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.annotation.RequiresPermission
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_map.*

class MapFragment : Fragment(), OnMapReadyCallback {
    companion object {
        private const val GPS_REQUEST_CODE = 100
        private const val USER_SETTING_REQUEST_CODE = 10
    }

    private lateinit var map: GoogleMap
    private lateinit var locationViewModel: LocationViewModel
    private var currentLocation: LatLng? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)
        locationViewModel = ViewModelProviders.of(this)[LocationViewModel::class.java]

        this.context?.let { context ->
            locationViewModel.getFusedLocationClient(context)
            val task = locationViewModel.checkUserLocationSetting(context)
            task.apply {
                addOnSuccessListener {
                    locationViewModel.subscribeToLocationUpdate()
                }
                addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            exception.startResolutionForResult(this@MapFragment.activity,
                                    USER_SETTING_REQUEST_CODE)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Log.d("TAG", "${sendEx.message}")
                        }
                    } else {
                        Log.d("TAG", "${exception.message}")
                    }
                }
            }
        }

        locationViewModel.currentLocation.observe(this, Observer { position ->
            currentLocation = position
            Log.d("TAG", "current location in fragment $currentLocation")
        })
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        enableUserLocation()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        if (this::locationViewModel.isInitialized) {
            locationViewModel.unsubscribeLocationUpdate()
        }
    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapView?.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapView?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    private fun enableUserLocation() {
        val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
        context?.let { context ->
            if (ContextCompat.checkSelfPermission(context, permissions[0]) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "To request for permission")
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permissions[1])) {
                    AlertDialog.Builder(context)
                            .setTitle("Permission Request Required")
                            .setMessage("This app required your permission in order to provide location awareness service.")
                            .setCancelable(true)
                            .setPositiveButton("acknowledge", { dialog, _ ->
                                run {
                                    dialog.dismiss()
                                    requestPermissions(permissions, GPS_REQUEST_CODE)
                                }
                            })
                            .create()
                            .show()
                } else {
                    requestPermissions(permissions, GPS_REQUEST_CODE)
                }
            } else {
                Log.d("TAG", "Permission already granted by user")
                if (this::map.isInitialized) {
                    map.isMyLocationEnabled = true
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            GPS_REQUEST_CODE -> {
                if (grantResults.size == 2 &&
                        grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
                    enableUserLocation()
                    Log.d("TAG", "Permission granted")
                } else {
                    AlertDialog.Builder(this.context)
                            .setTitle("Alert")
                            .setMessage("Without the permission, there will be limited function for the app.")
                            .setCancelable(true)
                            .setPositiveButton("acknowledge", { dialog, _ -> dialog.dismiss() })
                            .create()
                            .show()
                    Log.d("TAG", "No permission given")
                }
            }
        }
    }
}
