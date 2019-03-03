package com.muflone.android.django_hotels;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class Preferences {
    private final SharedPreferences preferences;
    private Context context;

    public Preferences(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getTabletID() {
        return preferences.getString(this.context.getString(R.string.preferences_tablet_id_id), "");
    }

    public String getTabletKey() {
        return preferences.getString(this.context.getString(R.string.preferences_tablet_key_id), "");
    }
}
