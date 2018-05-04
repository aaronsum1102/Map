package com.sum.aaron.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.annotation.RequiresPermission
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallBack: LocationCallback
    val currentLocation: LiveData<LatLng> = MutableLiveData()

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

    private fun getLocationCallBack() {
        locationCallBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let { locationResult ->
                    val lastIndex = locationResult.locations.lastIndex
                    val location = locationResult.locations[lastIndex]
                    Log.d("TAG", "${location.latitude}, ${location.longitude}")
                    currentLocation as MutableLiveData
                    currentLocation.postValue(LatLng(location.latitude, location.longitude))
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    fun subscribeToLocationUpdate() {
        getLocationCallBack()
        locationClient.requestLocationUpdates(locationRequest, locationCallBack, null)
    }

    fun unsubscribeLocationUpdate() {
        if (this::locationCallBack.isInitialized) {
            locationClient.removeLocationUpdates(locationCallBack)
        }
    }
}