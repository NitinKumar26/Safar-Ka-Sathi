package com.gravity.loft.safarkasathi.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CmtTrip (val trip_id: Int, val trip_transporterid: Int, val trip_transporter_name: String, val driver_id: Int,
                    val vehicle_id: String, val trip_amount: Float, val trip_starttime: String, val trip_validity: String, val trip_startlocation: String,
                    val trip_pick_up_points: String?, val trip_endlocation: String, val trip_status: String, val trip_totalpoints: String, val trip_task: Int) : Parcelable
