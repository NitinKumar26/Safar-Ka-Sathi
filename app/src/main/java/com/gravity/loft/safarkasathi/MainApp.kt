package com.gravity.loft.safarkasathi

import android.app.Application
import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes

class MainApp : Application(){

    init { instance = this }

    companion object {
        private var instance: MainApp? = null

        fun context() : Context {
            return instance!!.applicationContext
        }

        fun getString(@StringRes resId: Int): String{
            return context().getString(resId)
        }
    }

}