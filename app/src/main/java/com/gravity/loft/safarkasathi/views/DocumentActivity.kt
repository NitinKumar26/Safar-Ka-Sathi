package com.gravity.loft.safarkasathi.views

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.esafirm.imagepicker.features.ImagePicker
import com.google.gson.Gson
import com.gravity.loft.safarkasathi.R
import com.gravity.loft.safarkasathi.commons.BaseActivity
import com.gravity.loft.safarkasathi.commons.Constant
import com.gravity.loft.safarkasathi.commons.RealPathUtil
import com.gravity.loft.safarkasathi.commons.RetrofitBuilder
import com.gravity.loft.safarkasathi.commons.Utility.Companion.getName
import com.gravity.loft.safarkasathi.databinding.ActivityUploadDocumentBinding
import com.gravity.loft.safarkasathi.services.TripService
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*
import kotlin.properties.Delegates

const val REQUEST_PICK_AADHAR_CARD = 101
const val REQUEST_PICK_DRIVING_LICENSE = 201

class DocumentActivity: BaseActivity(){


    private var URI_PICK_AADHAR_CARD : Uri? = null
    private var URI_PICK_DRIVING_LICENSE : Uri? = null
    private var trip_id by Delegates.notNull<Int>()


    private val binding : ActivityUploadDocumentBinding by lazy {
        ActivityUploadDocumentBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        trip_id = intent.getIntExtra("trip_id", 0)

        binding.homeIcon.setOnClickListener {
            finish()
        }

        binding.submitButton.setOnClickListener {
           uploadDocuments()
        }

        binding.removeDL.setOnClickListener {
            vibrate()
            URI_PICK_DRIVING_LICENSE = null
            binding.switcherDL.showNext()
        }

        binding.removeAC.setOnClickListener {
            vibrate()
            URI_PICK_AADHAR_CARD = null
            binding.switcherAC.showNext()
        }

        binding.uploadDL.setOnClickListener {
            pickImage(REQUEST_PICK_DRIVING_LICENSE)
        }

        binding.uploadAC.setOnClickListener {
            pickImage(REQUEST_PICK_AADHAR_CARD)
        }

    }

    private fun pickImage(requestCode: Int){
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        Permissions.check(this, permissions, null, null, object : PermissionHandler() {
            override fun onGranted() {
                ImagePicker.create(this@DocumentActivity)
                    .single()
                    .showCamera(true)
                    .theme(R.style.Theme_SafarKaSathi)
                    .start(requestCode)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val image = ImagePicker.getFirstImageOrNull(data)
        if(image != null){
            when(requestCode){
                REQUEST_PICK_DRIVING_LICENSE -> {
                    // File(uri.path).name
                    URI_PICK_DRIVING_LICENSE = image.uri
                    binding.dlText.text = image.uri.getName(this)
                    binding.switcherDL.showNext()

                }
                REQUEST_PICK_AADHAR_CARD -> {
                    URI_PICK_AADHAR_CARD = image.uri
                    binding.acText.text = image.uri.getName(this)
                    binding.switcherAC.showNext()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadDocuments(){
        // data

        // media type

        // body objects (multiparts)
//            val aadharFile = MultipartBody.Part.create(fileAC.asRequestBody(mediaType))
//            val licenseFile = MultipartBody.Part.create(fileDL.asRequestBody(mediaType))

        when {
            binding.dlNumber.text.toString().trim().length < 6 -> {
                binding.dlNumber.error =  getString(R.string.license_number_empty)
                return
            }
            URI_PICK_DRIVING_LICENSE == null -> {
                toast(R.string.license_document_empty)
                return
            }
            binding.aadharNumber.text.toString().trim().length != 12 -> {
                binding.aadharNumber.error =  getString(R.string.aadhar_number_empty)
                return
            }
            URI_PICK_AADHAR_CARD == null -> {
                toast(R.string.aadhar_document_empty)
                return
            }
            else -> launchWithProgress {
                val service = RetrofitBuilder.buildAuthService(TripService::class.java)

                // data
                val fileAC = File(RealPathUtil.getRealPath(this, URI_PICK_AADHAR_CARD!!)!!)
                val fileDL = File(RealPathUtil.getRealPath(this, URI_PICK_DRIVING_LICENSE!!)!!)
                val numberDL = binding.dlNumber.text.toString()
                val numberAC = binding.aadharNumber.text.toString()

                // media type
                val mediaType = "multipart/form-data".toMediaTypeOrNull()
                val mediaTypeImage = "image/png".toMediaTypeOrNull()

                // body objects (multiparts)
                //            val aadharFile = MultipartBody.Part.create(fileAC.asRequestBody(mediaType))
                //            val licenseFile = MultipartBody.Part.create(fileDL.asRequestBody(mediaType))

                val aadharFile = fileAC.asRequestBody(mediaTypeImage)
                val licenseFile = fileDL.asRequestBody(mediaTypeImage)

                val aadharNumber = numberAC.toRequestBody(mediaType)
                val licenseNumber = numberDL.toRequestBody(mediaType)

                val map: MutableMap<String, RequestBody> = HashMap()
                map["aadharNumber"] = aadharNumber
                map["licenseNumber"] = licenseNumber
                map["image\"; filename=\"some.png\""] = licenseFile
                map["image1\"; filename=\"some1.png\""] = aadharFile

                val response = service.uploadDocuments(map)
                Log.e("DOCS", "Response Code:" + response.code())
                if (response.isSuccessful) {
                    val body = response.body()!!
                    Log.e("BODY", Gson().toJson(body))
                    startActivity(Intent(this, TaskActivity::class.java).putExtra("trip_id", trip_id))
                    finish()
                }
            }
        }

    }


}