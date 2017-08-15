package com.byteshaft.locationupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by s9iper1 on 8/8/17.
 */

public class InternetBroadCastReceiver extends BroadcastReceiver {

    private static Context mContext;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e("TAG", "Internet changed");
        mContext = context;
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("TAG", "Internet State" + isNetworkAvailable(context));
                if (isNetworkAvailable(context)) {
                    new CheckInternet().execute();
                }
            }
        }, 5000);

    }

    public static boolean isNetworkAvailable(final Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean isInternetWorking() {
        boolean success = false;
        try {
            URL url = new URL("https://google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.connect();
            success = connection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public static class CheckInternet extends AsyncTask<Void, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            return isInternetWorking();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            AppGlobals.isInternetPresent = aBoolean;
        }

    }
}
