package com.example.socketexample.Utillity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.socketexample.Interface.Constants;

public class PreferenceData implements Constants {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public PreferenceData(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getValueFromKey(String key) {
        return preferences.getString(key, "");
    }

    public String getValueFromKey(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public int getIntValueFromKey(String key) {
        return preferences.getInt(key, -1);
    }

    public int getIntValueFromKey(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }


    public long getLongValue(String key) {
        return preferences.getLong(key, 0);
    }

    public boolean getBooleanValueFromKey(String key) {
        return preferences.getBoolean(key, false);
    }

    public boolean getBooleanValueFromKey(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public boolean isDeviceAdminEnable() {
        return preferences.getBoolean(IS_ADMIN_ACTIVATE, false);
    }

    public boolean setDeviceAdmin(boolean flag) {
        return preferences.edit().putBoolean(IS_ADMIN_ACTIVATE, flag).commit();
    }

    public boolean isRestrictDeviceUse() {
        return preferences.getBoolean(RESTRICT_DEVICE_USE, false);
    }

    public boolean setRestrictDeviceUse(boolean flag) {
        return preferences.edit().putBoolean(RESTRICT_DEVICE_USE, flag).commit();
    }

    public boolean isLogin() {
        return preferences.getBoolean(IS_LOGIN, false);
    }

    public boolean setLogin(boolean flag) {
        return preferences.edit().putBoolean(IS_LOGIN, flag).commit();
    }

   /* public boolean setPhotoCaptured(boolean flag) {
        return preferences.edit().putBoolean(IS_PHOTO_CAPTURED, flag).commit();
    }

    public boolean isPhotoCaptured() {
        return preferences.getBoolean(IS_PHOTO_CAPTURED, false);
    }*/

  /*  public boolean isLogoutPhotoPending() {
        return preferences.getBoolean(IS_LOGOUT_PHOTO_PENDING, false);
    }
    public boolean setLogoutPhotoPending(boolean flag) {
        return preferences.edit().putBoolean(IS_LOGOUT_PHOTO_PENDING, flag).commit();
    }*/



    public boolean isDialogShowing() {
        return preferences.getBoolean(IS_DIALOG_SHOWING, false);
    }

    public boolean setDialogShowing(boolean flag) {
        return preferences.edit().putBoolean(IS_DIALOG_SHOWING, flag).commit();
    }

    public void setValue(String key, String value) {
        editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void setIntValue(String key, int value) {
        editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void setBooleanValue(String key, boolean value) {
        editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void setLongValue(String key, long value) {
        editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void clearData() {
        editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

}