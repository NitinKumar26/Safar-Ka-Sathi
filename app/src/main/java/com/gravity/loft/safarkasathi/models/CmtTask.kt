package com.gravity.loft.safarkasathi.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CmtTask (val trip_id: String, val triptask_id: Int, val triptask_taskname: String,
                    val trip_transporter_name: String, val status: String, val triptask_points: String, val triptask_upload: String,
                    val triptask_image: String, val task_end_time: String, val triptask_message: String, val location: String?): Parcelable {
    fun isGoods(): Boolean{ return triptask_upload == "Goods" }
}


data class UploadTask(val image: String, val taskid: Int, val received: String?, val time: String, val location: String)