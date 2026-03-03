package com.patientapp.health.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.patientapp.health.PatientApp

class HealthMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token: $token")
        // Token is saved to Firestore from MainActivity/ViewModel when user is logged in
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let { notification ->
            Log.d(TAG, "Notification: ${notification.title} - ${notification.body}")
            // Display is handled by the system when app is in background.
            // For foreground you can show a custom in-app notification here.
        }
    }

    companion object {
        private const val TAG = "HealthMessaging"
    }
}
