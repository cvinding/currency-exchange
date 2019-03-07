package com.example.testapp.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;
import java.util.Map;

public class NetworkHandler {

    public enum NetworkType {
        TYPE_MOBILE(0), TYPE_WIFI(1);

        private int type;

        NetworkType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

    }

    public static HashMap<NetworkType, Boolean> getNetworkStatus(Context context) {

        HashMap<NetworkType, Boolean> networkStatus = new HashMap<>();

        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        networkStatus.put(NetworkType.TYPE_MOBILE,conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED);
        networkStatus.put(NetworkType.TYPE_WIFI,conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);

        return networkStatus;
    }

}
