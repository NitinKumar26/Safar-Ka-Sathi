package com.gravity.loft.safarkasathi.commons

import android.content.Context
import android.content.SharedPreferences
import com.gravity.loft.safarkasathi.MainApp
import com.gravity.loft.safarkasathi.R

const val PREF_AVATAR_KEY = "PREF_AVATAR_KEY"

object Settings {

    private val preference by lazy {
        Preference()
    }

    fun getBearerToken() : String? {
       return preference.getString( MainApp.getString(R.string.PREF_BEARER_TOKEN))
    }

    fun getAvatarImage() : String? {
        return preference.getString( PREF_AVATAR_KEY)
    }

    fun setAvatarImage(uri: String) {
        preference.put( PREF_AVATAR_KEY, uri)
    }

    fun setBearerToken(token: String){
        preference.put(MainApp.getString(R.string.PREF_BEARER_TOKEN), token)
    }

    fun clearAll(){
        preference.clear()
    }

    fun hasBearerToken(): Boolean{
        return preference.getString(MainApp.getString(R.string.PREF_BEARER_TOKEN)) != null
    }

}

open class Preference{

    private val preferenceFileKey: String = "default_preference"
    private var sp: SharedPreferences = MainApp.context().getSharedPreferences(preferenceFileKey, Context.MODE_PRIVATE)

    fun put(key: String, value: Any){
        with (sp.edit()) {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
            }
            apply()
        }
    }

    fun clear(){
        with(sp.edit()){
            clear()
            commit()
        }
    }

    fun getString(key: String, default_value: String?=null): String?{
        return sp.getString(key, default_value)
    }

    fun getInt(key: String, default_value: Int=0): Int{
        return sp.getInt(key, default_value)
    }

    fun getBool(key: String, default_value: Boolean=false): Boolean{
        return sp.getBoolean(key, default_value)
    }

    fun getFloat(key: String, default_value: Float=0.0f): Float{
        return sp.getFloat(key, default_value)
    }

    fun getLong(key: String, default_value: Long=0): Long{
        return sp.getLong(key, default_value)
    }
}