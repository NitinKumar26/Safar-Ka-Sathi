package com.gravity.loft.safarkasathi.views

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.gravity.loft.safarkasathi.R
import com.gravity.loft.safarkasathi.commons.BaseActivity
import com.gravity.loft.safarkasathi.commons.RetrofitBuilder
import com.gravity.loft.safarkasathi.commons.Settings
import com.gravity.loft.safarkasathi.databinding.ActivityOtpValidateBinding
import com.gravity.loft.safarkasathi.databinding.ActivityRegisterBinding
import com.gravity.loft.safarkasathi.databinding.ActivitySendOtpBinding
import com.gravity.loft.safarkasathi.databinding.ActivitySplashBinding
import com.gravity.loft.safarkasathi.services.LoginService
import com.gravity.loft.safarkasathi.services.SMSBroadcastReceiver
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

const val TAG = "LoginGroup"
const val SPLASH_TIMEOUT: Long = 100

const val EXTRA_PHONE = "phone_extra_object"
const val EXTRA_TOKEN = "token_extra_object"

val STARTUP_ACTIVITY = MainActivity::class.java

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)  //MODE_NIGHT_FOLLOW_SYSTEM

        setContentView(binding.root)

        // delay and go to next
        Handler().postDelayed({ startMain() }, SPLASH_TIMEOUT)
    }

    private fun startMain() {
        if (Settings.getBearerToken() != null)
            startActivity(Intent(this, STARTUP_ACTIVITY))
        else
            startActivity(Intent(this, SendOtp::class.java))
        finish()
    }
}

class SendOtp : BaseActivity() {

    private lateinit var binding: ActivitySendOtpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendOtpBinding.inflate(layoutInflater)

        /*
        // Saving state of our app
        // using SharedPreferences
        // Saving state of our app
        // using SharedPreferences
        val sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        val isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false)
        // When user reopens the app
        // after applying dark/light mode
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            //binding.includeDrawerLayout.tvEnableDarkMode.text = "Disable Dark Mode"
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            //binding.includeDrawerLayout.tvEnableDarkMode.text = "Enable Dark Mode"
        }
         */

        setContentView(binding.root)

        binding.submitButton.setOnClickListener {
            val phone: String = binding.editMobile.text.toString()

            if (phone.length == 10) sendOtp(phone)
            else this.toast(R.string.SEND_OTP_INVALID_NUMBER)

        }

    }

    private fun sendOtp(phone: String) = launchWithProgress {
        val data = mapOf("language" to "en", "phone" to phone)

        val response = try {
            RetrofitBuilder.buildService(LoginService::class.java).registerMobile(data)
        }
        catch (e: Exception){
            toast(R.string.API_ERROR_MESSAGE)
            return@launchWithProgress
        }

        Log.e(TAG, response.body()!!)

        val intent = Intent(this, OtpValidate::class.java).apply {
            putExtra(EXTRA_PHONE, phone)
        }
        startActivity(intent)
        finish()
    }

}

class OtpValidate : BaseActivity() {

    private lateinit var binding: ActivityOtpValidateBinding
    private lateinit var smsBroadcastReceiver: SMSBroadcastReceiver
    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpValidateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startSmsUserConsent()

        //get intent
        phone = intent.getStringExtra(EXTRA_PHONE)!!

        binding.phoneText.append(phone)

        binding.submitButton.setOnClickListener {
            val otp = binding.otpView.otp
            if (otp.length == 4) {
                otpValidate(phone, otp)
            } else {
                this.toast(R.string.INVALID_OTP)
            }
        }

        object : CountDownTimer(120000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val time = String.format("%02d min, %02d sec",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)))
                binding.tvTimeRemaining.text = time
            }

            override fun onFinish() {
                // Called after timer finishes
            }
        }.start()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                //That gives all message to us.
                // We need to get the code from inside with regex
                val message: String? = data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                //Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                //textViewMessage.setText(String.format("%s - %s", getString(R.string.received_message), message))
                getOtpFromMessage(message!!, phone)
            }
        }
    }

    private fun getOtpFromMessage(message: String, phone: String) {
        // This will match any 6 digit number in the message
        val pattern: Pattern = Pattern.compile("(|^)\\d{4}")
        val matcher: Matcher = pattern.matcher(message)
        if (matcher.find()) {
            val otp = matcher.group(0)
            binding.otpView.otp = otp

            if (otp?.length == 4) otpValidate(phone, otp)
            else this.toast(R.string.INVALID_OTP)

        }
    }

    private fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SMSBroadcastReceiver()
        smsBroadcastReceiver.smsBroadcastReceiverListener =
            object : SMSBroadcastReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    startActivityForResult(intent, 100)
                }

                override fun onFailure() {}
            }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    override fun onStart() {
        super.onStart()
        registerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }

    private fun startSmsUserConsent() {
        val client = SmsRetriever.getClient(this)
        //We can add sender phone number or leave it blank
        // I'm adding null here
        client.startSmsUserConsent(null).addOnSuccessListener {
            Log.e("success", "consentSuccess")
            //Toast.makeText(applicationContext, "On Success", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Log.e("failure", it.message ?: "null")
            //Toast.makeText(applicationContext, "On OnFailure", Toast.LENGTH_LONG).show()
        }
    }

    private fun otpValidate(phone: String, otp: String) = launchWithProgress {
        val data = mapOf("phone" to phone, "otp" to otp)

        val response = try {
            RetrofitBuilder.buildService(LoginService::class.java).validateOtp(data)
        } catch (e: Exception) {
            toast(R.string.INVALID_OTP)
            return@launchWithProgress
        }

        //Log.e(TAG, response.body())

        val intent = Intent(this, RegisterUser::class.java)
        intent.apply { putExtra(EXTRA_TOKEN, response.body()!!.token ) }
        startActivity(intent)
        finish()
    }
}

class RegisterUser : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get intent
        val token = intent.getStringExtra(EXTRA_TOKEN)!!

        binding.recordTripYes.setOnClickListener {
            binding.recordTripYes.setBackgroundResource(R.drawable.border_blue)
            binding.recordTripNo.setBackgroundResource(R.drawable.border_empty)
        }

        binding.recordTripNo.setOnClickListener {
            binding.recordTripYes.setBackgroundResource(R.drawable.border_empty)
            binding.recordTripNo.setBackgroundResource(R.drawable.border_blue)
        }

        binding.submitButton.setOnClickListener {
            val termsconditions = "1"
            val fullName: String = binding.fullName.text.toString()
            val recordTrip: String by lazy {
                if(binding.recordTripYes.background == ContextCompat.getDrawable(this, R.drawable.border_blue)) "Yes"
                else "No"
            }

            if(fullName.length < 3){
                toast(R.string.FORM_ENTER_FULL_NAME)
                return@setOnClickListener
            }
            if (!binding.acceptTerm.isChecked){
                toast(R.string.FORM_CHECK_TERM_CONDITION)
                return@setOnClickListener
            }

            launchWithProgress {
                val data = mutableMapOf("name" to fullName, "recordtrip" to recordTrip, "termsconditions" to termsconditions)

                //save token to preferences
                Settings.setBearerToken(token)

                val response = try {
                    RetrofitBuilder.buildAuthService(LoginService::class.java).updateDetails(data)
                } catch (e: Exception) {
                    toast(R.string.API_ERROR_MESSAGE)
                    Settings.clearAll()
                    return@launchWithProgress
                }

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

            }
        }
    }

}
