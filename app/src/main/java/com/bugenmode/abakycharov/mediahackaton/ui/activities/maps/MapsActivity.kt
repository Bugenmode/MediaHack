package com.bugenmode.abakycharov.mediahackaton.ui.activities.maps

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bugenmode.abakycharov.mediahackaton.R
import com.bugenmode.abakycharov.mediahackaton.databinding.ActivityMapsBinding
import com.bugenmode.abakycharov.mediahackaton.di.injector
import com.bugenmode.abakycharov.mediahackaton.ui.activities.main.MainActivity
import com.bugenmode.abakycharov.mediahackaton.ui.base.BaseActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber
import java.util.*


class MapsActivity : BaseActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationListener: FusedLocationProviderClient
    private var TTS : TextToSpeech? = null
    private var ttsEnabled : Boolean = false

    private var geofencingClient : GeofencingClient? = null

    private var googleApiClient : GoogleApiClient? = null

    lateinit var b : ActivityMapsBinding

    private var locationRequest : LocationRequest? = null

    private lateinit var ref : DatabaseReference
    private lateinit var geoFire: GeoFire

    private var currentMarker : Marker? = null

    val viewModel by lazy {
        ViewModelProvider(this, injector.vmfMaps()).get(MapsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupEventListener(this, viewModel)

        ref = FirebaseDatabase.getInstance().getReference("MyLocation")
        geoFire = GeoFire(ref)

        b = DataBindingUtil.setContentView(this, R.layout.activity_maps)
        b.lifecycleOwner = this
        b.vm = viewModel

        fusedLocationListener = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initTextToSpeech()
        setupListeners()

        buildGoogleApiClient()
    }

    private fun setupListeners() {
        b.btnOpen.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
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
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
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
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                GEO_POSITION_CODE
            )
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

    override fun onDestroy() {
        super.onDestroy()
        if(TTS != null) {
            TTS!!.stop()
            TTS!!.shutdown()
            Timber.d("TTS Destroyed");
        }
    }

    private fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest!!.interval = UPDATE_INTERVAL
        locationRequest!!.fastestInterval = FASTEST_INTERVAL
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest!!.smallestDisplacement = DISPLACEMENT

    }

    private fun displayLocation() {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)

        if (lastLocation != null) {
            val latitude = lastLocation.latitude
            val longitude = lastLocation.longitude

            geoFire.setLocation("YOU", GeoLocation(latitude, longitude), object : GeoFire.CompletionListener {
                override fun onComplete(key: String?, error: DatabaseError?) {
                    if (currentMarker != null) {
                        currentMarker?.remove()
                    }

                    currentMarker = mMap.addMarker(MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .title("YOU"))

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 12.0f))

                }
            })
        }
    }

    private fun startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
    }

    override fun onConnected(p0: Bundle?) {
        displayLocation()
        startLocationUpdates()
    }

    override fun onConnectionSuspended(p0: Int) {
        googleApiClient?.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLocationChanged(p0: Location?) {
        displayLocation()
        startLocationUpdates()
    }


    companion object {
        const val GEO_POSITION_CODE = 200
        const val UPDATE_INTERVAL = 5000L
        const val FASTEST_INTERVAL = 3000L
        const val DISPLACEMENT = 10F
        const val PLAY_SERVICES_RESOLUTION_REQUEST = 300193
    }
}