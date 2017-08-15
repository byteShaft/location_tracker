package com.byteshaft.locationupdate;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button toggleButton;
    private static final int MY_PERMISSIONS_REQESt_LOCATION = 0;
    private static final int MY_PERMISSIONS_REQESt_PHONE_STATE = 1;
    public boolean foreground = false;
    private static MainActivity sInstance;
    private EditText deviceId;
    private TelephonyManager telephony;
    private EditText enterpriseId;

    public static MainActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        setContentView(R.layout.activity_main);
        deviceId = (EditText) findViewById(R.id.device_id);
        enterpriseId = (EditText) findViewById(R.id.enterprise_id);
        sInstance = this;
        telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        toggleButton = (Button) findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(this);
        foreground = true;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (AppGlobals.getDeviceId().equals("")) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQESt_PHONE_STATE);
            }
        } else {
            deviceId.setText(telephony.getDeviceId());
        }
        if (!AppGlobals.getDeviceId().equals("")) {
            deviceId.setText(AppGlobals.getDeviceId());
        }
        if (!AppGlobals.getEnterPriseId().equals("")) {
            enterpriseId.setText(AppGlobals.getEnterPriseId());
        }

        new InternetBroadCastReceiver.CheckInternet().execute();
        if (AppGlobals.isTracking() && LocationService.getInstance() == null) {
            if (locationEnabled()) {
                if (LocationService.getInstance() == null) {
                    AppGlobals.saveTackingState(true);
                    toggleButton.setText(getResources().getString(R.string.stop_tracking));
                    startService(new Intent(getApplicationContext(), LocationService.class));
                }
            } else {
                dialogForLocationEnableManually(this);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        foreground = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
        if (!AppGlobals.isTracking()) {
            toggleButton.setText(getResources().getString(R.string.start_tracking));
        } else {
            toggleButton.setText(getResources().getString(R.string.stop_tracking));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        foreground = false;
    }


    public static boolean locationEnabled() {
        LocationManager lm = (LocationManager) AppGlobals.getContext()
                .getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        return gps_enabled || network_enabled;
    }

    public static void dialogForLocationEnableManually(final Activity activity) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setMessage("Location is not enabled");
        dialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivityForResult(myIntent, AppGlobals.LOCATION_ENABLE);
                //get gps
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub

            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppGlobals.LOCATION_ENABLE) {
            if (locationEnabled() && LocationService.getInstance() == null) {
                startService(new Intent(getApplicationContext(), LocationService.class));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQESt_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(new Intent(getApplicationContext(), LocationService.class));

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }case MY_PERMISSIONS_REQESt_PHONE_STATE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AppGlobals.saveDeviceId(telephony.getDeviceId());
                    deviceId.setText(telephony.getDeviceId());
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onClick(View view) {
        if (AppGlobals.isTracking()) {
            if (LocationService.getInstance() != null) {
                AppGlobals.saveTackingState(false);
                LocationService.getInstance().stopService();
                toggleButton.setText(getResources().getString(R.string.start_tracking));
            }
            return;
        }
        if (deviceId.getText().toString().equals("") || enterpriseId.getText().toString().equals("")) {
            Toast.makeText(sInstance, R.string.fill_details, Toast.LENGTH_SHORT).show();
            return;
        }
        AppGlobals.saveEnterpriseId(enterpriseId.getText().toString());
        if (!AppGlobals.isTracking()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQESt_LOCATION);
            } else {
                if (locationEnabled()) {
                    if (LocationService.getInstance() == null) {
                        AppGlobals.saveTackingState(true);
                        toggleButton.setText(getResources().getString(R.string.stop_tracking));
                        startService(new Intent(getApplicationContext(), LocationService.class));
                    }
                } else {
                    dialogForLocationEnableManually(this);
                }
            }
        } else {
            if (LocationService.getInstance() != null) {
                AppGlobals.saveTackingState(false);
                LocationService.getInstance().stopService();
                toggleButton.setText(getResources().getString(R.string.start_tracking));
            }

        }

    }
}
