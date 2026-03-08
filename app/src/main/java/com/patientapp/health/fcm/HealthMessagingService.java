package com.patientapp.health.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class HealthMessagingService extends FirebaseMessagingService {

    private static final String TAG = "HealthMessaging";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        if (message.getNotification() != null) {
            Log.d(TAG, "Notification: " + message.getNotification().getTitle()
                    + " - " + message.getNotification().getBody());
        }
    }
}
