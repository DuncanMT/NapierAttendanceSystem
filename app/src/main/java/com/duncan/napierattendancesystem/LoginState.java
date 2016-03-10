package com.duncan.napierattendancesystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Duncan on 18/01/2016.
 */
public class LoginState {
    static final String PREF_USER_NAME= "username";
    static final String PREF_CARDID="cardid";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserName(Context ctx, String userName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.apply();
    }

    public static String getUserName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
    }

    public static void setCardID(Context ctx, String cardid)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_CARDID, cardid);
        editor.apply();
    }

    public static String getCardID(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_CARDID, "");
    }

    public static void clearUserInfo(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear(); //clear all stored data
        editor.apply();
    }
}
