package com.example.pickmecustomers.Model;

import android.location.Location;

import com.example.pickmecustomers.Fare.GoogleClient;
import com.example.pickmecustomers.Fare.IGoogleAPI;
import com.example.pickmecustomers.Notification.FCMClient;
import com.example.pickmecustomers.Notification.IFCMService;

public class Common {

    public static String CURRENT_TOKEN = "";
    public static boolean isDriverFound = false;
    public static String driver_id = "";
    public static Location lastLocation=null;
    public static final String fcmbaseURL = "https://fcm.googleapis.com/";
    public static final String googleURL = "https://maps.googleapis.com/";




    public static double base_fare=2.55;
    public static double time_rate=0.35;
    public static double distance_rate=1.75;

    public static double getPrice(double km, int min){

        return (base_fare+(time_rate*min)+(distance_rate*km));

    }


    public static IFCMService getFCMService() {

        return FCMClient.getFCMClient(fcmbaseURL).create(IFCMService.class);

    }

    public static IGoogleAPI getGoogleService(){

        return GoogleClient.getGoogleClient(googleURL).create(IGoogleAPI.class);
    }
}
