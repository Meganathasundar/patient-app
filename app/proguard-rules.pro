# Add project specific ProGuard rules here.
-keepattributes *Annotation*

# Firebase
-keepclassmembers class * {
    @com.google.firebase.* <fields>;
}
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Firestore model classes
-keep class com.patientapp.health.data.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
