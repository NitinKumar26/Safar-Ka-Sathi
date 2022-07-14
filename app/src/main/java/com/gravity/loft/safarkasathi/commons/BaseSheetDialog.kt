package com.gravity.loft.safarkasathi.commons

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseSheetDialog: BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

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