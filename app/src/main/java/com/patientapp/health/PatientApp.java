package com.patientapp.health;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.analytics.FirebaseAnalytics;

public class PatientApp extends Application {

    public static final String CHANNEL_DAILY_FORM = "daily_health_form";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        enableAnalyticsAfterStartup();
    }

    private void enableAnalyticsAfterStartup() {
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true), 3000);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_DAILY_FORM,
                    "Daily Health Form Reminder",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Reminder to fill out your daily health form");
            channel.enableVibration(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
