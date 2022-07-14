package com.gravity.loft.safarkasathi.migrated

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.gravity.loft.safarkasathi.commons.BaseActivity
import com.gravity.loft.safarkasathi.databinding.ActivityCreateVehicleBinding
import java.util.*

open class CreateVehicle : BaseActivity() {
    private lateinit var binding: ActivityCreateVehicleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.hom.setOnClickListener{ finish() }

        binding.button.setOnClickListener(View.OnClickListener {
            val e1 = binding.et1.getText().toString()
            if (e1.trim { it <= ' ' }.isEmpty()) {
                this.toast("Please enter vehicle number.")
                return@OnClickListener
            }
            val e2 = binding.et2.getText().toString()
            if (e2.trim { it <= ' ' }.isEmpty()) {
                this.toast("Please enter vehicle type") //getActivity().showToast("Please enter vehicle type.")
                return@OnClickListener
            }
            val e3 = binding.et3.getText().toString()
            if (e3.trim { it <= ' ' }.isEmpty()) {
                this.toast("Please enter vehicle make")//getActivity().showToast("Please enter vehicle make.")
                return@OnClickListener
            }
            val e4 = binding.et4.getText().toString()
            if (e4.trim { it <= ' ' }.isEmpty()) {
                this.toast("Please enter vehicle model")//getActivity().showToast("Please enter vehicle model.")
                return@OnClickListener
            }
            val e5 = binding.et5.getText().toString()
            if (e4.trim { it <= ' ' }.isEmpty()) {
                this.toast("Please enter vehicle variant.")//getActivity().showToast("Please enter vehicle variant.")
                return@OnClickListener
            }

            val map: MutableMap<String, String> = HashMap()
            map["vehicle_no"] = e1
            map["vehicle_type"] = e2
            map["vehicle_make"] = e3
            map["vehicle_model"] = e4
            map["vehicle_varient"] = e5
            create(getBearerToken(), map)
        })

    }

    private fun create(token: String?, map: Map<String, String>?) {
        val dialog = ProgressDialog(this)
        dialog.setMessage("Creating Vehicle, please wait...")
        dialog.show()
        val queue = Volley.newRequestQueue(this)
        val url = "https://www.gravityloft.com/api/api/vehicle"
        val request: StringRequest = object : StringRequest(Method.POST, url, Response.Listener {
            dialog.dismiss()
            this.toast("Vehicle created!!!")//getActivity().showToast("Vehicle created!!!")
            finish()
        }, Response.ErrorListener {
            dialog.dismiss()
            this.toast("Error while creating trip.") //getActivity().showToast("Error while creating trip.")
        }) {

            override fun getParams(): Map<String, String>? { return map }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = token!!
                println(token)
                println("------------")
                return headers
            }
        }
        queue.add(request)
    }
}