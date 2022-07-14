package com.gravity.loft.safarkasathi.migrated;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gravity.loft.safarkasathi.commons.Settings;


public abstract class BaseActivity extends AppCompatActivity {
    protected Pref pref;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.pref = new Pref(this);
    }

    public String getBearerToken(){
        return Settings.INSTANCE.getBearerToken();
    }

    protected void setBearerToken(String token){
        this.pref.put("TOKENS", token);
    }

    protected void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    protected BaseActivity getActivity(){
        return this;
    }

    protected BaseActivity getBaseActivity(){
        return this;
    }


    protected String getAppVersion(){

        PackageManager manager = getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(
                    getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
