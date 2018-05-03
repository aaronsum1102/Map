package com.sum.aaron.map

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    fun getFusedLocationClient(context: Context) {
        locationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    fun checkUserLocationSetting(context: Context): Task<LocationSettingsResponse> {
        createLocationRequest()
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)
        return client.checkLocationSettings(builder.build())
    }

}