package com.gravity.loft.safarkasathi.commons

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


open class BaseFragment: BottomSheetDialogFragment() {

    fun launchWithProgress(unit: suspend () -> Unit) = lifecycleScope.launchWhenCreated {
        val activity = activity as BaseActivity
        activity.launchWithProgress {
            unit()
        }
    }

    fun launchWithoutProgress(unit: suspend () -> Unit) = lifecycleScope.launchWhenCreated {
        val activity = activity as BaseActivity
        activity.launchWithoutProgress {
            unit()
        }
    }
    fun toast(@StringRes resId: Int) = Toast.makeText(activity, getString(resId), Toast.LENGTH_SHORT).show()

    fun toast(message: String) = Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

}