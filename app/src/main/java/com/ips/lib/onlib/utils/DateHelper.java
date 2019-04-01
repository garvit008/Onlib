package com.ips.lib.onlib.utils;

import android.util.Log;


import com.ips.lib.onlib.IssueBookActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class DateHelper {
    private static final String TAG = "DateHelper";

    public static Date getFormattedDate(String dateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ddZ", Locale.CANADA);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        simpleDateFormat.setLenient(false);
        try {
            Date date = simpleDateFormat.parse(dateString);
            return date;
        }
        catch (ParseException e){
            return null;
        }
    }

    public static String getDaysDifference(Date from, Date to){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ddZ", Locale.CANADA);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        simpleDateFormat.setLenient(false);
        simpleDateFormat.format(from);
        simpleDateFormat.format(to);
        String difference = "";
        difference = String.valueOf(Math.round(((to.getTime() - from.getTime()) / 1000 / 60 / 60 / 24)));
        Log.d(TAG, "getTimeStampDifference: difference " + Math.round(((to.getTime() - from.getTime()) / 1000 / 60/ 60 / 24)));
        return difference;
    }

    public static String getDateString(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ddZ", Locale.CANADA);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return simpleDateFormat.format(date);
    }

    public static String getSimpleDateString(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM, yyyy", Locale.CANADA);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return simpleDateFormat.format(date);
    }
    public static String getDateStringWithTime(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM, yyyy 'at' hh:mm a");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        return simpleDateFormat.format(date);
    }

}
