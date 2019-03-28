package com.muflone.android.django_hotels;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Utility {
    public static Date getCurrentDate(String timezone) {
        // Get current system date only
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getCurrentTime(String timezone) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.YEAR, 1970);
        return calendar.getTime();
    }

    public static int getScreenOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    public static boolean isScreenOrientationPortrait(Context context) {
        return getScreenOrientation(context) == Configuration.ORIENTATION_PORTRAIT;
    }

    public static boolean isScreenOrientationLandscape(Context context) {
        return getScreenOrientation(context) == Configuration.ORIENTATION_LANDSCAPE;
    }
}
