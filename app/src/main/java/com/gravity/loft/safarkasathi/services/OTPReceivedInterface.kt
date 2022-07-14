package com.gravity.loft.safarkasathi.services

interface OTPReceivedInterface {
    fun onOtpReceived(otp: String)
    fun onOtpTimeout()
}