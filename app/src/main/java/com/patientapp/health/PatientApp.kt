package com.patientapp.health

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class PatientApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_DAILY_FORM,
                "Daily Health Form Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminder to fill out your daily health form"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_DAILY_FORM = "daily_health_form"
    }
}
