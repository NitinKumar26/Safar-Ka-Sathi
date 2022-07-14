package com.gravity.loft.safarkasathi.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(val language: String? = null, val phone: String? = null, var otp: String? = null): Parcelable

@Parcelize
data class Profile(val driver_id: String?, val name: String?, var email:String?, var address:String?, var mobile:String?, var dlanguage:String?, var photo:String?, ): Parcelable




