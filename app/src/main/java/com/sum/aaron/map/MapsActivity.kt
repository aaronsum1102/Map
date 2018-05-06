package com.sum.aaron.map

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationSettingsStatusCodes
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    companion object {
        const val USER_SETTING_REQUEST_CODE = 10
    }

    private lateinit var mapFragment: MapFragment
    private lateinit var locationViewModel: LocationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationViewModel = ViewModelProviders.of(this)[LocationViewModel::class.java]

        initialUserSettingCheck()

        Single.fromCallable { inflateMap() }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    private fun inflateMap() {
        Thread.sleep(200)
        mapFragment = MapFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, mapFragment)
                .commit()
    }

    private fun initialUserSettingCheck() {
        locationViewModel.getFusedLocationClient(this)
        val task = locationViewModel.checkUserLocationSetting(this)
        task.addOnCompleteListener { task ->
            try {
                val result = task.getResult(ApiException::class.java)
                if (result.locationSettingsStates.isLocationUsable) {
                    locationViewModel.subscribeToLocationUpdate(this)
                }
            } catch (exception: ApiException) {
                if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        (exception as? ResolvableApiException)?.startResolutionForResult(this,
                                USER_SETTING_REQUEST_CODE)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Log.d("ERROR", "${sendEx.message}")
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            USER_SETTING_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    locationViewModel.subscribeToLocationUpdate(this)
                }
            }
        }
    }
}