package com.gravity.loft.safarkasathi.migrated;

import android.content.Context;
import android.content.SharedPreferences;

public class Pref {

    private String PREF_KEY = "default_pref";
    private SharedPreferences sp;


    public Pref(Context context){
        this.sp = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
    }

    public void put(String key, String value){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void put(String key, boolean value){
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void put(String key, int value){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void put(String key, float value){
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public String getString(String key, String default_value){
        return sp.getString(key, default_value);
    }

    public int getInt(String key, int default_value){
        return sp.getInt(key, default_value);
    }

    public boolean getBoolean(String key, boolean default_value){
        return sp.getBoolean(key, default_value);
    }

    public float getFloat(String key, float default_value){
        return sp.getFloat(key, default_value);
    }

    public float getLong(String key, long default_value){
        return sp.getLong(key, default_value);
    }


    public String getString(String key){
        return this.getString(key, null);
    }

    public int getInt(String key){
        return this.getInt(key, 0);
    }

    public boolean getBoolean(String key){
        return this.getBoolean(key, false);
    }

    public float getFloat(String key){
        return this.getFloat(key, 0.0f);
    }

    public float getLong(String key){
        return this.getLong(key, 0);
    }
}
