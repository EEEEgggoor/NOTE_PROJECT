package com.glv.note_project;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Internet_check {
    public static boolean InternetIsConn(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm!=null){
            NetworkInfo active_inter = cm.getActiveNetworkInfo();
            return active_inter != null && active_inter.isConnectedOrConnecting();
        }
        return false;
    }
}
