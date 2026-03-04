package com.patientapp.health.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore repository for daily health forms.
 * - Submit a form (patientId, doctorId, temperature, symptoms, painLevel).
 * - Real-time flows: forms by patient, forms by doctor.
 */
class DailyFormRepository {

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun submitForm(
        patientId: String,
        doctorId: String,
        temperature: Double,
        symptoms: String,
        painLevel: Int,
        hasOtherSymptoms: Boolean,
        otherSymptomsDescription: String,
        tookMedicine: Boolean,
        medicineDescription: String
    ): Result<Unit> {
        return try {
            val data = hashMapOf(
                "patientId" to patientId,
                "doctorId" to doctorId,
                "bodyTemperature" to temperature,
                "symptomsDescription" to symptoms,
                "painLevel" to painLevel,
                "hasOtherSymptoms" to hasOtherSymptoms,
                "otherSymptomsDescription" to otherSymptomsDescription,
                "tookMedicine" to tookMedicine,
                "medicineDescription" to medicineDescription,
                "submittedAt" to Timestamp.now()
            )
            firestore.collection(DailyForm.COLLECTION).add(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFormsByPatient(patientId: String): Flow<List<DailyForm>> = callbackFlow {
        val listener = firestore.collection(DailyForm.COLLECTION)
            .whereEqualTo(DailyForm.FIELD_PATIENT_ID, patientId)
            .orderBy(DailyForm.FIELD_SUBMITTED_AT, com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val forms = snapshot?.documents?.mapNotNull { it.toDailyForm() } ?: emptyList()
                trySend(forms)
            }
        awaitClose { listener.remove() }
    }

    fun getFormsByDoctor(doctorId: String): Flow<List<DailyForm>> = callbackFlow {
        val listener = firestore.collection(DailyForm.COLLECTION)
            .whereEqualTo(DailyForm.FIELD_DOCTOR_ID, doctorId)
            .orderBy(DailyForm.FIELD_SUBMITTED_AT, com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val forms = snapshot?.documents?.mapNotNull { it.toDailyForm() } ?: emptyList()
                trySend(forms)
            }
        awaitClose { listener.remove() }
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toDailyForm(): DailyForm? {
    return try {
        DailyForm(
            id = id,
            patientId = getString("patientId") ?: "",
            doctorId = getString("doctorId") ?: "",
            bodyTemperature = (get("bodyTemperature") as? Number)?.toDouble() ?: 0.0,
            symptomsDescription = getString("symptomsDescription") ?: "",
            painLevel = (get("painLevel") as? Number)?.toInt() ?: 0,
            hasOtherSymptoms = getBoolean("hasOtherSymptoms") ?: false,
            otherSymptomsDescription = getString("otherSymptomsDescription") ?: "",
            tookMedicine = getBoolean("tookMedicine") ?: false,
            medicineDescription = getString("medicineDescription") ?: "",
            submittedAt = getTimestamp("submittedAt")
        )
    } catch (e: Exception) {
        null
    }
}
