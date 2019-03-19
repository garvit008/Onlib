package com.ips.lib.onlib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPrefManager {

    private static final String TAG = "SharedPrefManager";
    private Context context;

    public SharedPrefManager(Context context) {
        this.context = context;
    }

    public void saveUserType(String userType){
        Log.d(TAG, "saveUserType: " + userType);
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserType", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userType", userType);
        editor.commit();
    }

    public String getUserType(){
        SharedPreferences preferences = context.getSharedPreferences("UserType", Context.MODE_PRIVATE);
        Log.d(TAG, "getUserType: returning user " +preferences.getString("userType", ""));
        return preferences.getString("userType", "");
    }

    public void clearUserType(){
        SharedPreferences preferences = context.getSharedPreferences("UserTpe", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
    }


}
