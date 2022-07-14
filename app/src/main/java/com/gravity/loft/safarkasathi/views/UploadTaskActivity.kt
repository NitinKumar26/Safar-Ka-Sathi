package com.gravity.loft.safarkasathi.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.anirudh.locationfetch.EasyLocationFetch
import com.esafirm.imagepicker.features.ImagePicker
import com.google.gson.Gson
import com.gravity.loft.safarkasathi.R
import com.gravity.loft.safarkasathi.commons.*
import com.gravity.loft.safarkasathi.commons.Utility.Companion.getName
import com.gravity.loft.safarkasathi.databinding.ActivityUploadTaskBinding
import com.gravity.loft.safarkasathi.models.CmtTask
import com.gravity.loft.safarkasathi.services.TripService
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mapbox.mapboxsdk.geometry.LatLng
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class UploadTaskActivity : BaseActivity() {

    private val REQUEST_PICK_PROOF_IMAGE = 1829
    private var URI_PICK_PROOF_IMAGE: Uri? = null
    private lateinit var cmptTask: CmtTask
    private lateinit var location: LatLng
    private lateinit var timeRecord: String

    private val binding: ActivityUploadTaskBinding by lazy {
        ActivityUploadTaskBinding.inflate(layoutInflater)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        cmptTask = intent.getParcelableExtra("cmt_task")!!

        Log.e("CMP", Gson().toJson(cmptTask))
        binding.title.text = cmptTask.triptask_taskname
        binding.points.text = cmptTask.triptask_points
        binding.uploadtitle.text = cmptTask.triptask_taskname
        binding.tvLocation.text = "Location: " + cmptTask.location

        Log.e("location", cmptTask.location?: "Null")
        binding.container1.visibility = if (cmptTask.triptask_upload == "Goods") View.GONE else View.VISIBLE

        if (cmptTask.status == Constant.TASK_STATUS_REJECT) {
            binding.statusText.text = "Task Rejected"
            binding.tvLocation.visibility = View.GONE
            binding.taskText2.visibility = View.GONE
            binding.container1.visibility = View.GONE
            binding.container2.visibility = View.GONE
            binding.container3.visibility = View.GONE
        }

        binding.homeIcon.setOnClickListener { finish() }

        binding.nowTime.setOnClickListener {
            binding.timeText.text = SimpleDateFormat("hh:mm a").format(Calendar.getInstance().time)
            timeRecord = SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().time)
        }

        binding.uploadProof.setOnClickListener {
            pickImage(REQUEST_PICK_PROOF_IMAGE)
        }

        binding.removeDL.setOnClickListener {
            vibrate()
            URI_PICK_PROOF_IMAGE = null
            binding.switcherDL.showNext()
        }

        binding.submitButton.setOnClickListener {
            uploadDocuments()
        }

        binding.detectLocation.setOnClickListener {
            detectLocation()
        }
    }

    private fun pickImage(requestCode: Int) {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        Permissions.check(this, permissions, null, null, object : PermissionHandler() {
            override fun onGranted() {
                ImagePicker.create(this@UploadTaskActivity)
                    .single()
                    .showCamera(true)
                    .theme(R.style.Theme_SafarKaSathi)
                    .start(requestCode)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val image = ImagePicker.getFirstImageOrNull(data)
        if (image != null) {
            when (requestCode) {
                REQUEST_PICK_PROOF_IMAGE -> {
                    // File(uri.path).name
                    URI_PICK_PROOF_IMAGE = image.uri
                    binding.dlText.text = image.uri.getName(this)
                    binding.switcherDL.showNext()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadDocuments() {
        launchWithProgress {
            val service = RetrofitBuilder.buildAuthService(TripService::class.java)

            // media type
            val mediaType = "multipart/form-data".toMediaTypeOrNull()
            val mediaTypeImage = "image/png".toMediaTypeOrNull()

            // data
            val fileProof = File(RealPathUtil.getRealPath(this, URI_PICK_PROOF_IMAGE!! )!!)
            val proofFile = fileProof.asRequestBody(mediaTypeImage)
            val map: MutableMap<String, RequestBody> = HashMap()
            map["taskid"] = cmptTask.triptask_id.toString().toRequestBody(mediaType)
            map["received"] = "SKS".toRequestBody(mediaType)

            // if selected from now click
            if(::timeRecord.isInitialized){ map["time"] = timeRecord.toRequestBody(mediaType) }
            else{
                val time = SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().time)
                map["time"] = time.toRequestBody(mediaType)
            }

            //location
            if(::location.isInitialized) map["location"] = "${location.latitude}, ${location.longitude}".toRequestBody(mediaType)

            map["image\"; filename=\"some.png\""] = proofFile

            val response = service.uploadTaskDocs(map)
            //Log.e("TOKEN", Settings.getBearerToken()!!)
            //Log.e("DOCS", "Response Code:" + response.code())
            if (response.isSuccessful) {
                // val body = response.body()!!
                //Log.e("BODY", Gson().toJson(body))
                toast("Succes.......s!!!")
                startActivity(Intent(this, TaskStatusPending::class.java).putExtra("cmt_task", cmptTask))
                finish()
            }
        }
    }

    private fun detectLocation() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    val geoLocationModel = EasyLocationFetch(this@UploadTaskActivity).locationData
                    location = LatLng(geoLocationModel.lattitude, geoLocationModel.longitude)
                    val address = if (!geoLocationModel.address.isEmpty()) geoLocationModel.address else "Current Location"
                    //binding.locationText.text = address
                    binding.locationText.setText(address)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    toast("Location permission denied.")
                    finish()
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {}

            }).check()
    }

}