package com.sum.aaron.map

import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_map.*

class MapFragment : Fragment(), OnMapReadyCallback {
    companion object {
        private const val GPS_REQUEST_CODE = 100
    }

    private lateinit var map: GoogleMap
    private lateinit var locationViewModel: LocationViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)
        locationViewModel = ViewModelProviders.of(this)[LocationViewModel::class.java]
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        enableUserLocation()
        this.context?.let { context ->
            locationViewModel.getFusedLocationClient(context)
            val task = locationViewModel.checkUserLocationSetting(context)
            task.apply {
                addOnSuccessListener {

                }
                addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            exception.startResolutionForResult(activity, 10)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Log.d("TAG", "${sendEx.message}")
                        }
                    } else {
                        Log.d("TAG", "${exception.message}")
                    }
                }
            }
        }
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
        val permissions = arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
        this.context?.let { context ->
            if (ContextCompat.checkSelfPermission(context, permissions[0]) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
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
                }
            } else {
                map.isMyLocationEnabled = true

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            GPS_REQUEST_CODE -> {
                if (grantResults.size == 2 &&
                        grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
                    enableUserLocation()
                } else {
                    AlertDialog.Builder(this.context)
                            .setTitle("Alert")
                            .setMessage("Without the permission, there will be limited function for the app.")
                            .setCancelable(true)
                            .setPositiveButton("acknowledge", { dialog, _ -> dialog.dismiss() })
                            .create()
                            .show()
                }
            }
        }
    }
}
