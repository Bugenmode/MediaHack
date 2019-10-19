package com.bugenmode.abakycharov.mediahackaton.ui.activities

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.arsy.maps_library.MapRipple
import com.bugenmode.abakycharov.mediahackaton.R
import com.bugenmode.abakycharov.mediahackaton.ui.base.BaseActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import timber.log.Timber
import java.util.*


class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationListener: FusedLocationProviderClient
    private var TTS : TextToSpeech? = null
    private var ttsEnabled : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationListener = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initTextToSpeech()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val geoYkt = LatLng(62.03389, 129.73306)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(geoYkt))

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        updateLocationUI()
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (hasPermission()) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true

                fusedLocationListener.lastLocation.addOnSuccessListener {
                    if (it != null) {
                        lastLocation = it
                        val currentLocation = LatLng(it.latitude, it.longitude)
                        placeMarkerOnMap(currentLocation)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))

                        //TODO Посмотри в конце
//                        val mapRipple = MapRipple(mMap, currentLocation, this)
//                        mapRipple.startRippleMapAnimation()

                        
                    }
                }
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                requestPermission()
            }
        } catch (e: SecurityException) {
            Timber.e("Exception: %s", e.message)
        }

    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), GEO_POSITION_CODE)
        }
    }

    private fun initTextToSpeech() {

        TTS = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                if (TTS?.isLanguageAvailable(Locale(Locale.getDefault().language))
                    == TextToSpeech.LANG_AVAILABLE) {
                    TTS?.language = Locale(Locale.getDefault().language)
                } else {
                    TTS?.language = Locale.US
                }
                TTS?.setPitch(1.3f)
                TTS?.setSpeechRate(0.7f)
                ttsEnabled = true
            } else {
                Toast.makeText(applicationContext, "SOME ERROR", Toast.LENGTH_LONG).show()
                ttsEnabled = false
            }
        })
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        mMap.addMarker(markerOptions)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    private fun speak(text: String) {
        if (!ttsEnabled) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(text)
        } else {
            ttsUnder20(text)
        }
    }

    @SuppressWarnings("deprecation")
    private fun ttsUnder20(text : String) {
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MessageId"
        TTS?.speak(text, TextToSpeech.QUEUE_FLUSH, map)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun ttsGreater21(text : String) {
        val utteranceId = this.hashCode().toString() + ""
        TTS?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    companion object {
        const val GEO_POSITION_CODE = 200
    }
}