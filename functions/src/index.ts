import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();

/**
 * Scheduled Cloud Function that runs every day at 5:00 PM (17:00) in the
 * function's timezone. Queries all users with role "patient", collects their
 * FCM tokens, and sends a push notification to fill out the Daily Health Form.
 */
export const sendDailyFormReminder = functions
  .region("us-central1")
  .pubsub.schedule("0 17 * * *")
  .timeZone("America/New_York")
  .onRun(async () => {
    const usersRef = db.collection("users");
    const snapshot = await usersRef
      .where("role", "==", "patient")
      .get();

    const tokens: string[] = [];
    snapshot.docs.forEach((doc) => {
      const token = doc.data().fcmToken;
      if (typeof token === "string" && token.length > 0) {
        tokens.push(token);
      }
    });

    if (tokens.length === 0) {
      functions.logger.info("No FCM tokens found for patients. Skipping send.");
      return null;
    }

    const message: admin.messaging.MulticastMessage = {
      tokens,
      notification: {
        title: "Daily Health Form",
        body: "Please fill out your daily health form.",
      },
      android: {
        priority: "high",
        notification: {
          channelId: "daily_health_form",
          priority: "high" as const,
        },
      },
    };

    const response = await admin.messaging().sendEachForMulticast(message);
    functions.logger.info(
      `Sent ${response.successCount} reminders, ${response.failureCount} failures.`
    );

    if (response.failureCount > 0) {
      response.responses.forEach((resp, idx) => {
        if (!resp.success) {
          functions.logger.warn(
            `Token ${tokens[idx]} failed: ${resp.error?.message}`
          );
        }
      });
    }

    return null;
  });
