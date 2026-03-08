package com.patientapp.health.data;

import com.google.firebase.Timestamp;

public class DailyForm {
    public static final String COLLECTION = "daily_forms";
    public static final String FIELD_PATIENT_ID = "patientId";
    public static final String FIELD_DOCTOR_ID = "doctorId";
    public static final String FIELD_SUBMITTED_AT = "submittedAt";

    private final String id;
    private final String patientId;
    private final String doctorId;
    private final double bodyTemperature;
    private final String symptomsDescription;
    private final int painLevel;
    private final boolean hasOtherSymptoms;
    private final String otherSymptomsDescription;
    private final boolean tookMedicine;
    private final String medicineDescription;
    private final Timestamp submittedAt;

    public DailyForm(String id, String patientId, String doctorId, double bodyTemperature,
                     String symptomsDescription, int painLevel, boolean hasOtherSymptoms,
                     String otherSymptomsDescription, boolean tookMedicine,
                     String medicineDescription, Timestamp submittedAt) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.bodyTemperature = bodyTemperature;
        this.symptomsDescription = symptomsDescription;
        this.painLevel = painLevel;
        this.hasOtherSymptoms = hasOtherSymptoms;
        this.otherSymptomsDescription = otherSymptomsDescription;
        this.tookMedicine = tookMedicine;
        this.medicineDescription = medicineDescription;
        this.submittedAt = submittedAt;
    }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public double getBodyTemperature() { return bodyTemperature; }
    public String getSymptomsDescription() { return symptomsDescription; }
    public int getPainLevel() { return painLevel; }
    public boolean isHasOtherSymptoms() { return hasOtherSymptoms; }
    public String getOtherSymptomsDescription() { return otherSymptomsDescription; }
    public boolean isTookMedicine() { return tookMedicine; }
    public String getMedicineDescription() { return medicineDescription; }
    public Timestamp getSubmittedAt() { return submittedAt; }
}
