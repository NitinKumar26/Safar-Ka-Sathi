package com.gravity.loft.safarkasathi.models

data class Trip(val trip_id: Int, val trip_transporterid: Int, val trip_transporter_name: String, val driver_id: Int,
                val driver_mobile: String, val vehicle_id: Int, val vehicle_no: String, val trip_amount: Float, val trip_starttime: String,
                val trip_validity: String, val trip_startlocation: String, val trip_endlocation: String, val trip_status: String)