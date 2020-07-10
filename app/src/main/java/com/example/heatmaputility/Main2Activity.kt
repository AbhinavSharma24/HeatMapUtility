package com.example.heatmaputility

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import org.json.JSONArray
import org.json.JSONException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


class Main2Activity : AppCompatActivity(), OnMapReadyCallback,LocationListener {

    private lateinit var locMan: LocationManager
    private lateinit var locLis: LocationListener

    private var mDefaultGradient = true

    private val ALT_HEATMAP_GRADIENT_COLORS = intArrayOf(
            Color.argb(0, 0, 255, 255),  // transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
    )

    val ALT_HEATMAP_GRADIENT_START_POINTS = floatArrayOf(
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
    )

    val ALT_HEATMAP_GRADIENT: Gradient = Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS)

    override fun onLocationChanged(location: Location?) {
        Toast.makeText(this,"lat = ${location?.latitude}    long = ${location?.longitude}",Toast.LENGTH_SHORT).show()
        if(mMap != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location?.latitude?.let {
                LatLng(
                        it, location.longitude
                )
            }, 12f))
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locLis = this
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requests()

        /*val change_gradient = findViewById<Button>(R.id.change_gradient)

        change_gradient.setOnClickListener {
            changeGradient()
        }*/
    }

    private fun requests() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    1234
            )
        }
        else
            startLocationUpdates()
    }

    private lateinit var mProvider : HeatmapTileProvider
    private lateinit var mOverlay : TileOverlay


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        addHeatMapWeighted()
        //addHeatMap();
    }

    private fun addHeatMapWeighted() {
        val list = readItems(R.raw.sample_data)
        val provider = HeatmapTileProvider.Builder().weightedData(list).gradient(HeatmapTileProvider.DEFAULT_GRADIENT).build()
        mMap.addTileOverlay(TileOverlayOptions().tileProvider(provider))
    }

    /*fun addHeatMap() {
        val gradient = Gradient(colors, startpoints)
        val cDat: List<LatLng> = CrimeData().getPositions()
        val provider = HeatmapTileProvider.Builder().data(cDat).gradient(gradient).build()
        mMap.addTileOverlay(TileOverlayOptions().tileProvider(provider))
    }*/

    var colors = intArrayOf(
            Color.GREEN,  // green(0-50)
            Color.YELLOW,  // yellow(51-100)
            Color.rgb(255, 165, 0),  //Orange(101-150)
            Color.RED,  //red(151-200)
            Color.rgb(153, 50, 204),  //dark orchid(201-300)
            Color.rgb(165, 42, 42) //brown(301-500)
    )

    var startpoints = floatArrayOf(
            0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 1.0f
    )


    /*private fun addHeatMap(googleMap: GoogleMap) {
        var list: List<LatLng?>? = null
        mMap = googleMap

        // Get the data: latitude/longitude positions of police stations.
        try {
            list = readItems(R.raw.sample_data)
        } catch (e: JSONException) {
            Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show()
        }

        // Create a heat map tile provider, passing it the lat-longs of the police stations.
        mProvider = HeatmapTileProvider.Builder()
                .data(list)
                .build()
        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(mProvider))

        *//*mProvider.setOpacity(0.5)
        mOverlay.clearTileCache()*//*
    }*/

    @Throws(JSONException::class)
    private fun readItems(resource: Int): ArrayList<WeightedLatLng?>? {
        val list = ArrayList<LatLng?>()
        val list2 = ArrayList<WeightedLatLng?>()
        val inputStream: InputStream = resources.openRawResource(resource)
        val json: String = Scanner(inputStream).useDelimiter("\\A").next()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val `object` = array.getJSONObject(i)
            val lat = `object`.getDouble("lat")
            val lng = `object`.getDouble("long")
            val weight = `object`.getDouble("weight")
            list.add(LatLng(lat, lng))

            val x : LatLng? = list[i]

            if(weight < 2){
                list2.add(WeightedLatLng(x, 1.0))
            }else if(weight < 5 && weight >= 2){
                list2.add(WeightedLatLng(x, 5.0))
            }else if(weight < 8 && weight >= 5){
                list2.add(WeightedLatLng(x, 20.0))
            }else{
                list2.add(WeightedLatLng(x, 50.0))
            }
        }
        return list2
    }

    private fun changeGradient() {
        if (mDefaultGradient) {
            mProvider.setGradient(ALT_HEATMAP_GRADIENT)
        } else {
            mProvider.setGradient(HeatmapTileProvider.DEFAULT_GRADIENT)
        }
        mOverlay.clearTileCache()
        mDefaultGradient = !mDefaultGradient
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(){
        locMan = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                1000,0f,locLis)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1234) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requests()
                startLocationUpdates()
            }
        }
    }

    /*override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        addHeatMap(googleMap)

        with(mMap.uiSettings) {
            setAllGesturesEnabled(true)
            isZoomControlsEnabled = true
        }
        val center = LatLng(28.633159, 77.219613)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 2f))
    }

    fun addHeatMapWeighted() {
        val gradient = Gradient(colors, startPoints)
        val list = readItems(R.raw.sample_data)
        val provider = HeatmapTileProvider.Builder().weightedData(list).gradient(gradient).build()
        mMap.addTileOverlay(TileOverlayOptions().tileProvider(provider))
    }

    var colors = intArrayOf(
            Color.GREEN,  // green(0-50)
            Color.YELLOW,  // yellow(51-100)
            Color.rgb(255, 165, 0),  //Orange(101-150)
            Color.RED,  //red(151-200)
            Color.rgb(153, 50, 204),  //dark orchid(201-300)
            Color.rgb(165, 42, 42) //brown(301-500)
    )

    var startPoints = floatArrayOf(
            0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 1.0f
    )*/

    override fun onDestroy() {
        super.onDestroy()
        locMan.removeUpdates(this)
    }
}
