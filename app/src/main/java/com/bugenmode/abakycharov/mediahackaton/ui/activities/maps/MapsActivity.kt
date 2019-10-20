package com.bugenmode.abakycharov.mediahackaton.ui.activities.maps

import android.Manifest
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
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
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
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
    private lateinit var fusedLocationListener: FusedLocationProviderClient
    private var TTS: TextToSpeech? = null
    private var ttsEnabled: Boolean = false

    private var geofencingClient: GeofencingClient? = null

    private var googleApiClient: GoogleApiClient? = null

    lateinit var b: ActivityMapsBinding


    private var locationRequest: LocationRequest? = null
    private lateinit var ref: DatabaseReference
    private lateinit var geoFire: GeoFire
    private var lastLocation: Location? = null

    private var currentMarker: Marker? = null

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

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        ref = FirebaseDatabase.getInstance().getReference("MyLocation")
        geoFire = GeoFire(ref)

        initTextToSpeech()
        setupListeners()

        setupLocation()
    }

    private fun setupLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                GEO_POSITION_CODE
            )
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient()
                createLocationRequest()
                displayLocation()
            }
        }
    }

    private fun checkPlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                ).show()
            } else {
                Toast.makeText(this, "Не поддерживается", Toast.LENGTH_LONG).show()
            }
            return false
        }
        return true
    }

    private fun setupListeners() {
        b.btnOpen.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val geoYkt = LatLng(62.0324905, 129.750206)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(geoYkt))

        mMap.addCircle(
            CircleOptions()
                .center(geoYkt)
                .radius(100.0)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f)
        )

        mMap.setOnMarkerClickListener(this)

        val geoQuery = geoFire.queryAtLocation(GeoLocation(geoYkt.latitude, geoYkt.longitude), 0.5)

        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onGeoQueryReady() {
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                sendNotification("Bugenmode", String.format("%s entered the geoYkt area", key))
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                Timber.d("moved within geoYkt area")
            }

            override fun onKeyExited(key: String?) {
                sendNotification("Bugenmode", String.format("%s exited the geoYkt area", key))
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Timber.d("error %s", error)
            }
        })
    }

    private fun sendNotification(title: String, content: String) {
        val intent = Intent(this, MapsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val resultPendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)


        val builder = NotificationCompat.Builder(this, "MyNotifications")
            .setContentText(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentText(content)
            .setContentIntent(resultPendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MyNotifications",
                title,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "description"
            manager.createNotificationChannel(channel)
        }

        manager.notify(999, builder.build())
    }

    private fun initTextToSpeech() {

        TTS = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                if (TTS?.isLanguageAvailable(Locale(Locale.getDefault().language))
                    == TextToSpeech.LANG_AVAILABLE
                ) {
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
    private fun ttsUnder20(text: String) {
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MessageId"
        TTS?.speak(text, TextToSpeech.QUEUE_FLUSH, map)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun ttsGreater21(text: String) {
        val utteranceId = this.hashCode().toString() + ""
        TTS?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (TTS != null) {
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

        googleApiClient!!.connect()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest!!.interval = UPDATE_INTERVAL
        locationRequest!!.fastestInterval = FASTEST_INTERVAL
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest!!.smallestDisplacement = DISPLACEMENT

    }

    private fun displayLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (googleApiClient != null) {
            if (googleApiClient!!.isConnected) {
                lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            }
        }

        if (lastLocation != null) {
            val latitude = lastLocation!!.latitude
            val longitude = lastLocation!!.longitude

            geoFire.setLocation(
                "YOU",
                GeoLocation(latitude, longitude),
                object : GeoFire.CompletionListener {
                    override fun onComplete(key: String?, error: DatabaseError?) {
                        if (currentMarker != null) {
                            currentMarker?.remove()
                        }

                        currentMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(latitude, longitude))
                                .title("YOU")
                        )

                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    latitude,
                                    longitude
                                ), 17.0f
                            )
                        )

                    }
                })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == GEO_POSITION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkPlayServices()) {
                    buildGoogleApiClient()
                    createLocationRequest()
                    displayLocation()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
            googleApiClient,
            locationRequest,
            this
        )
    }

    override fun onConnected(p0: Bundle?) {
        displayLocation()
        startLocationUpdates()
    }

    override fun onConnectionSuspended(p0: Int) {
        googleApiClient?.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            lastLocation = location
        }
        displayLocation()
    }


    companion object {
        const val GEO_POSITION_CODE = 200
        const val UPDATE_INTERVAL = 5000L
        const val FASTEST_INTERVAL = 3000L
        const val DISPLACEMENT = 10F
        const val PLAY_SERVICES_RESOLUTION_REQUEST = 300193
    }
}