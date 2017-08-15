package com.byteshaft.locationupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by s9iper1 on 8/16/17.
 */

public class BootStateListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppGlobals.isTracking()) {
            context.startService(new Intent(context, LocationService.class));
        }

    }
}
