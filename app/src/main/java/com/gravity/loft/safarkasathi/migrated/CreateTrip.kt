package com.gravity.loft.safarkasathi.migrated

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.anirudh.locationfetch.EasyLocationFetch
import com.gravity.loft.safarkasathi.R
import com.gravity.loft.safarkasathi.commons.BaseActivity
import com.gravity.loft.safarkasathi.databinding.ActivityCreateTripBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import org.json.JSONException
import java.util.*

class CreateTrip : BaseActivity() {
    var transpoter_id: String? = null
    var vehicle_id: String? = null
    var consignment_type: String? = null
    var latLngEnd: LatLng? = null
    var latLngStart: LatLng? = null

    private lateinit var binding: ActivityCreateTripBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startLoc.setOnClickListener{ detectLocation(true) }
        binding.editText3.setOnClickListener{ detectLocation(false) }

        binding.hom.setOnClickListener{ finish() }
        binding.selectTransporter.setOnClickListener{
            showTrn(getBearerToken())
        }
        binding.sel2.setOnClickListener{ showCns(getBearerToken()) }
        binding.sel3.setOnClickListener{ showVch(getBearerToken()) }

        binding.button.setOnClickListener{
            if (latLngStart == null) {
                this.toast("Please select start location")
                return@setOnClickListener
            }
            if (latLngEnd == null) {
                this.toast("Please select drop locaiton")
                return@setOnClickListener
            }
            if (transpoter_id == null) {
                this.toast("Please select transporter")
                return@setOnClickListener
            }
            if (consignment_type == null) {
                this.toast("Please select consignment type.")
                return@setOnClickListener
            }
            if (vehicle_id == null) {
                this.toast("Please select vehicle.")
                return@setOnClickListener
            }
            val amount = binding.editText.text.toString()
            if (amount.trim { it <= ' ' }.isEmpty()) {
                this.toast("Please enter amount")
                return@setOnClickListener
            }

            val start_point = latLngStart!!.latitude.toString() + ", " + latLngStart!!.latitude
            val dropoff_point = latLngEnd!!.latitude.toString() + ", " + latLngEnd!!.latitude
            val map: MutableMap<String, String> = HashMap()
            map["start_point"] = start_point
            map["dropoff_point"] = dropoff_point
            map["transpoter_id"] = transpoter_id.toString()
            map["transpoter_name"] = binding.tvSelectTransporter.text.toString()
            map["consignment_type"] = consignment_type!!
            map["vehicle_id"] = vehicle_id.toString()
            map["vehicle_no"] = binding.veText.text.toString()
            map["amount"] = amount
            createTrip(getBearerToken(), map)
        }
    }

    private fun showTrn(token: String?) {
        val dialog = ProgressDialog(this)
        dialog.setMessage("Please wait...")
        dialog.show()
        val queue = Volley.newRequestQueue(this)
        val url = "https://www.gravityloft.com/api/api/transpoter"
        val request: JsonObjectRequest =
            object : JsonObjectRequest(url, null, Response.Listener { response ->
                try {
                    val ja = response.getJSONArray("data")
                    val dt: MutableMap<String, String> = HashMap()
                    for (i in 0 until ja.length()) {
                        val jo = ja.getJSONObject(i)
                        dt[jo.getString("transpoter_name")] = jo.getString("transpoter_id")
                    }
                    val view = LayoutInflater.from(this).inflate(R.layout.sheet_layout_30, null)
                    val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
                    val title = view.findViewById<TextView>(R.id.textView18)
                    title.text = "Select Transpoter"
                    recyclerView.adapter = SimpleAdapter(dt) { key, value ->
                        transpoter_id = value
                        binding.tvSelectTransporter.text = key
                        binding.bottomsheet.dismissSheet()
                    }
                    binding.bottomsheet.showWithSheetView(view)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                dialog.dismiss()
            }, Response.ErrorListener { dialog.dismiss()
                this.toast("Something wrong!") //getActivity().showToast("Something wrong!")
            }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = token.toString()
                    return headers
                }
            }
        queue.add(request)
    }

    private fun showCns(token: String?) {
        val dialog = ProgressDialog(this)
        dialog.setMessage("Please wait...")
        dialog.show()
        val queue = Volley.newRequestQueue(this)
        val url = "https://www.gravityloft.com/api/api/consignment"
        val request: JsonObjectRequest = object : JsonObjectRequest(url,
            null, Response.Listener { response ->
                try {
                    val ja = response.getJSONArray("data")
                    val dt: MutableMap<String, String?> = HashMap()
                    for (i in 0 until ja.length()) {
                        val jo = ja.getJSONObject(i)
                        dt[jo.getString("consignment")] = null
                    }
                    val view = LayoutInflater.from(this).inflate(R.layout.sheet_layout_30, null)
                    val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
                    val title = view.findViewById<TextView>(R.id.textView18)
                    title.text = "Select Consignment"
                    recyclerView.adapter = SimpleAdapter(dt) { key, value ->
                        consignment_type = key
                        binding.cnsType.text = key
                        binding.bottomsheet.dismissSheet()
                    }
                    binding.bottomsheet.showWithSheetView(view)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                dialog.dismiss()
            }, Response.ErrorListener {
                dialog.dismiss()
                this.toast("Something wrong!") //getActivity().showToast("Something wrong!")
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = token.toString()
                return headers
            }
        }
        queue.add(request)
    }

    private fun showVch(token: String?) {
        val dialog = ProgressDialog(this)
        dialog.setMessage("Please wait...")
        dialog.show()
        val queue = Volley.newRequestQueue(this)
        val url = "https://www.gravityloft.com/api/api/vehicle"
        val request: JsonObjectRequest = object : JsonObjectRequest(url,
            null, Response.Listener { response ->
                try {
                    val ja = response.getJSONArray("data")
                    val dt: MutableMap<String, String> = HashMap()
                    for (i in 0 until ja.length()) {
                        val jo = ja.getJSONObject(i)
                        dt[jo.getString("vehicle_no")] = jo.getString("vehicle_id")
                    }
                    val view = LayoutInflater.from(this).inflate(R.layout.sheet_layout_30, null)
                    val title = view.findViewById<TextView>(R.id.textView18)
                    val tvAdd = view.findViewById<TextView>(R.id.tv_add)
                    title.text = "Select Vehicle"
                    tvAdd.text = "Add New Vehicle"
                    val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
                    recyclerView.adapter = SimpleAdapter(dt) { key, value ->
                        vehicle_id = value
                        binding.veText.text = key
                        binding.bottomsheet.dismissSheet()
                    }
                    binding.bottomsheet.showWithSheetView(view)
                    view.findViewById<View>(R.id.tv_add).setOnClickListener {
                        startActivity(Intent(this@CreateTrip, CreateVehicle::class.java))
                        binding.bottomsheet.dismissSheet()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                dialog.dismiss()
            }, Response.ErrorListener {
                dialog.dismiss()
                this.toast("Something wrong!") //getActivity().showToast("Something wrong!")
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = token.toString()
                return headers
            }
        }
        queue.add(request)
    }

    private fun createTrip(token: String?, map: Map<String, String>?) {
        val dialog = ProgressDialog(this)
        dialog.setMessage("Creating trip, please wait...")
        dialog.show()
        val queue = Volley.newRequestQueue(this)
        val url = "https://www.gravityloft.com/api/api/Trip/trip"
        val request: StringRequest = object : StringRequest(Method.POST, url, Response.Listener {
            dialog.dismiss()
            this.toast("New trip created!!!") //getActivity().showToast("New trip created!!!")
            finish()
        }, Response.ErrorListener { error ->
            error.message
            dialog.dismiss()
            this.toast("Error while creating trip.") //getActivity().showToast("Error while creating trip.")
        }) {
            override fun getParams(): Map<String, String>? { return map }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = token.toString()
                println(token)
                println("------------")
                return headers
            }
        }
        queue.add(request)
    }

    private fun detectLocation(onlyStart: Boolean) {
        Dexter.withContext(this@CreateTrip)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        this@CreateTrip.toast("Please enable GPS.") //showToast("Please enable GPS.")
                        return
                    }
                    val geoLocationModel = EasyLocationFetch(this@CreateTrip).locationData
                    latLngStart = LatLng(geoLocationModel.lattitude, geoLocationModel.longitude)
                    binding.startLoc.text = if (!geoLocationModel.address.isEmpty()) geoLocationModel.address else "Current Location"

                    // endLocation
                    if (!onlyStart) {
                        val intent = PlaceAutocomplete.IntentBuilder()
                            .accessToken(getString(R.string.mapbox_access_token))
                            .placeOptions(
                                PlaceOptions.builder()
                                    .backgroundColor(Color.parseColor("#80333333"))
                                    .limit(10)
                                    .country("IN")
                                    .build(PlaceOptions.MODE_CARDS)
                            )
                            .build(this@CreateTrip)
                        startActivityForResult(intent, 7)
                        this@CreateTrip.toast("Select drop location.") //getBaseActivity().showToast("Select drop location.")
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    this@CreateTrip.toast("Location permission denied") //getBaseActivity().showToast("Location permission denied.")
                    finish()
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {}
            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 7) {
            val feature = PlaceAutocomplete.getPlace(data)
            latLngEnd = LatLng((feature.geometry() as Point?)!!.latitude(), (feature.geometry() as Point?)!!.longitude())
            binding.editText3.text = feature.text()
        }
    }
}