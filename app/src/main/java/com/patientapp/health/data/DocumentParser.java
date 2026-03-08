package com.patientapp.health.data;

import com.google.firebase.firestore.DocumentSnapshot;

public final class DocumentParser {
    private DocumentParser() {}

    public static User toUser(DocumentSnapshot doc) {
        String id = doc.getId();
        String phone = doc.getString(FirestoreConstants.PHONE);
        if (phone == null) return null;
        UserRole role = UserRole.fromString(doc.getString(FirestoreConstants.ROLE));
        if (role == null) return null;
        return new User(
                id, phone, role,
                doc.getString(FirestoreConstants.DISPLAY_NAME),
                doc.getString(FirestoreConstants.DOCTOR_ID),
                doc.getString(FirestoreConstants.FCM_TOKEN));
    }

    public static DailyForm toDailyForm(DocumentSnapshot doc) {
        try {
            Number temp = (Number) doc.get("bodyTemperature");
            Number pain = (Number) doc.get("painLevel");
            Boolean hasOther = doc.getBoolean("hasOtherSymptoms");
            Boolean tookMed = doc.getBoolean("tookMedicine");
            return new DailyForm(
                    doc.getId(),
                    nullSafe(doc.getString("patientId")),
                    nullSafe(doc.getString("doctorId")),
                    temp != null ? temp.doubleValue() : 0.0,
                    nullSafe(doc.getString("symptomsDescription")),
                    pain != null ? pain.intValue() : 0,
                    hasOther != null && hasOther,
                    nullSafe(doc.getString("otherSymptomsDescription")),
                    tookMed != null && tookMed,
                    nullSafe(doc.getString("medicineDescription")),
                    doc.getTimestamp("submittedAt"));
        } catch (Exception e) {
            return null;
        }
    }

    private static String nullSafe(String value) {
        return value != null ? value : "";
    }
}
