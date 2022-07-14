package com.gravity.loft.safarkasathi.views

import android.os.Bundle
import android.util.Log
import com.anirudh.locationfetch.EasyLocationFetch
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.gravity.loft.safarkasathi.commons.BaseActivity
import com.gravity.loft.safarkasathi.databinding.TaskUploadAcceptStatusBinding
import com.gravity.loft.safarkasathi.databinding.TaskUploadPendingStatusBinding
import com.gravity.loft.safarkasathi.models.CmtTask
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TaskStatusPending : BaseActivity() {

    private val binding: TaskUploadPendingStatusBinding by lazy { TaskUploadPendingStatusBinding.inflate(layoutInflater) }
    private lateinit var cmptTask: CmtTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        cmptTask = intent.getParcelableExtra("cmt_task")!!
        Log.e("cmtTask", Gson().toJson(cmptTask))
        cmptTask.triptask_image.let { Glide.with(this).load(cmptTask.triptask_image).into(binding.uploadImage); }
        binding.hom.setOnClickListener { finish() }
        binding.title.text = cmptTask.triptask_taskname
        binding.points.text = cmptTask.triptask_points


        val serverTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") //24 Hours format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a") //12 Hours Format

        val endTime = formatter.format(LocalDateTime.parse("2021-05-06 " + cmptTask.task_end_time, serverTimeFormatter))

        binding.endtime.text = endTime.substringAfter(" ")

        val geoLocationModel = EasyLocationFetch(this@TaskStatusPending)
        geoLocationModel.addressFetch(cmptTask.location?.substringBefore(",")?.toDouble() ?: 0.0, cmptTask.location?.substringAfter(", ")?.toDouble() ?: 0.0)
        binding.location.text = geoLocationModel.locationData.address

    }
}

class TaskStatusAccept : BaseActivity() {

    private val binding: TaskUploadAcceptStatusBinding by lazy { TaskUploadAcceptStatusBinding.inflate(layoutInflater) }
    private lateinit var cmptTask: CmtTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        cmptTask = intent.getParcelableExtra("cmt_task")!!
        binding.titletask.text = cmptTask.triptask_taskname
        binding.taskpoints.text = "${cmptTask.triptask_points} Points Earned"
        cmptTask.triptask_image.let { Glide.with(this).load(cmptTask.triptask_image).into(binding.uploadImage); }

        binding.hom.setOnClickListener { finish() }

    }

}

