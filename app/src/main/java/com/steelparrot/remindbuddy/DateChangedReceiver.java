package com.steelparrot.remindbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DateChangedReceiver extends BroadcastReceiver {

    private static final String TAG = "DateChangedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED)) {
            Log.i(TAG, "ACTION_DATE_CHANGED received");
        }
    }
}
