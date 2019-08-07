package com.muflone.android.django_hotels;

import android.content.Context;

import com.muflone.android.django_hotels.api.Api;
import com.muflone.android.django_hotels.api.ApiData;
import com.muflone.android.django_hotels.commands.CommandFactory;
import com.muflone.android.django_hotels.database.AppDatabase;
import com.muflone.android.django_hotels.database.models.Structure;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

public class Singleton implements Serializable {
    private static volatile Singleton instance;
    public Api api;
    public ApiData apiData;
    public Settings settings;
    public Date selectedDate;
    public Structure selectedStructure;
    public CommandFactory commandFactory;
    public AppDatabase database;
    public String defaultTimeFormat;
    public String defaultDateFormat;
    public DateFormat defaultDateFormatter;

    private Singleton() {
        // Prevent form the reflection api.
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

    }

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

    public void openDatabase(Context context) {
        this.database = AppDatabase.getAppDatabase(context);
    }
}
