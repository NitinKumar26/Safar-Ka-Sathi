package com.gravity.loft.safarkasathi.commons

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.gravity.loft.safarkasathi.R
import com.gravity.loft.safarkasathi.commons.Settings.getBearerToken
import okhttp3.OkHttpClient

open class BaseActivity: AppCompatActivity(){

    protected val client = OkHttpClient()
    private var progressDialog: Dialog? = null;

    fun Context.toast(@StringRes resId: Int) = Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show()

    fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun progressDialog(show: Boolean){
        if(show){
            val inflate = LayoutInflater.from(applicationContext).inflate(R.layout.dialog_progress, null)
            progressDialog = Dialog(this)
            progressDialog?.setContentView(inflate)
            progressDialog?.setCancelable(false)
            progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            progressDialog?.show()
        }
        else{
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    fun launchWithProgress(unit: suspend () -> Unit) = lifecycleScope.launchWhenCreated {
        progressDialog(true)
        unit()
        progressDialog(false)
    }

    fun launchWithoutProgress(unit: suspend () -> Unit) = lifecycleScope.launchWhenCreated {
        unit()
    }

    fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    open fun getBearerToken(): String? {
        return Settings.getBearerToken()
    }
}