package com.patientapp.health.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class DailyForm(
    @DocumentId
    val id: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val bodyTemperature: Double = 0.0,
    val symptomsDescription: String = "",
    val painLevel: Int = 0,
    val hasOtherSymptoms: Boolean = false,
    val otherSymptomsDescription: String = "",
    val tookMedicine: Boolean = false,
    val medicineDescription: String = "",
    @ServerTimestamp
    val submittedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION = "daily_forms"
        const val FIELD_PATIENT_ID = "patientId"
        const val FIELD_DOCTOR_ID = "doctorId"
        const val FIELD_SUBMITTED_AT = "submittedAt"
    }
}
