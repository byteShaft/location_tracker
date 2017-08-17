package com.byteshaft.locationupdate;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.byteshaft.requests.HttpRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by s9iper1 on 5/21/17.
 */

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, android.location.LocationListener {

    private static int NOTIFICATION_ID = 2112;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private static LocationService sInstance;
    private static ServiceEvents events;
    private LocationManager locationManager;


    public static LocationService getInstance() {
        return sInstance;
    }

    public LocationService() {
        super();
    }

    public LocationService(ServiceEvents serviceEvents) {
        super();
        events = serviceEvents;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sInstance = this;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.location)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.tracking))
                .setContentIntent(pendingIntent).build();
        startForeground(NOTIFICATION_ID, notification);
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                events.onServiceStart();
            }
        }, 500);
        return START_NOT_STICKY;
    }

    private void stopLocationUpdate() {
        if (mGoogleApiClient.isConnected()) {
            locationManager.removeUpdates(this);
            mGoogleApiClient.disconnect();
        }
    }

    public void stopService() {
        onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        events.onServiceStop();
        stopLocationUpdate();
        AppGlobals.saveTackingState(false);
        stopSelf();
        locationManager = null;
        sInstance = null;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(getClass().getSimpleName(), "onConnected");
//        startLocationUpdates();
        startLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.i(getClass().getSimpleName(), "onConnectionSuspended");
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.i(getClass().getSimpleName(), "onConnectionFailed");
//
//    }

    private void startLocation() {
        String locationProvide = LocationManager.NETWORK_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (locationManager != null) {
            locationManager.requestLocationUpdates(locationProvide, 0, 0, this);
        }
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(LocationService.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(LocationService.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (locationManager != null) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, LocationService.this);
                }
            }
        }, 10000);

    }

    private void postLocationUpdate(double lat, double lng) {
        HttpRequest request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject jsonObject = new JSONObject(request.getResponseText());
                                    if (jsonObject.getInt("numMsg") == 1) {
                                        new android.os.Handler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(LocationService.this, "Transmitiendo...", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        stopService();
                                        new android.os.Handler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(LocationService.this,
                                                        "No se puede iniciar el rastreo, por favor verifique sus credenciales", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case HttpURLConnection.HTTP_BAD_REQUEST:

                                break;

                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {

            }
        });
        request.open("GET", String.format("http://reconocer.org/ljrj/rastreo.php?id_entidad=%s&id_dispositivo=%s&latitud=%s&longitud=%s",
                AppGlobals.getEnterPriseId(), AppGlobals.getDeviceId(), lat, lng));
        request.send();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("TAG", "Location changed called" + "Lat " + location.getLatitude() + ", Lng "+ location.getLongitude());
        Log.d("TAG",  "A " + (int) location.getAccuracy());
        if (AppGlobals.isInternetPresent) {
            postLocationUpdate(location.getLatitude(), location.getLongitude());
        } else {
            if (MainActivity.getInstance().foreground) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!AppGlobals.checkingInternet) {
                            Toast.makeText(MainActivity.getInstance(), getResources().getString(
                                    R.string.no_internet), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
