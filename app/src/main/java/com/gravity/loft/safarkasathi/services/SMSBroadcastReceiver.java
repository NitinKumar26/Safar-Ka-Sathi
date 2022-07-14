package com.gravity.loft.safarkasathi.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BroadcastReceiver to wait for SMS messages. This can be registered either
 * in the AndroidManifest or at runtime.  Should filter Intents on
 * SmsRetriever.SMS_RETRIEVED_ACTION.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {
    public SmsBroadcastReceiverListener smsBroadcastReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SmsRetriever.SMS_RETRIEVED_ACTION)) {
            Bundle extras = intent.getExtras();
            Status smsRetrieverStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            switch (smsRetrieverStatus.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    Intent messageIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                    if (smsBroadcastReceiverListener != null)
                    smsBroadcastReceiverListener.onSuccess(messageIntent);
                    break;
                case CommonStatusCodes.TIMEOUT:
                    if (smsBroadcastReceiverListener != null)
                    smsBroadcastReceiverListener.onFailure();
                    break;
            }
        }
    }

    public interface SmsBroadcastReceiverListener {
        void onSuccess(Intent intent);
        void onFailure();
    }
}
