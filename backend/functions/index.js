const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { onCall, HttpsError } = require("firebase-functions/v2/https");

initializeApp();
const db = getFirestore();

/**
 * PHASE 2 — email-login backup. The client (CloudSync.*.kt) already writes backups directly
 * to backend/backups/{uid} via the Firestore SDK (see firestore.rules for the security rule
 * restricting each user to their own document), so no callable function is strictly required
 * for the basic case. This one is a thin server-side fallback/verification hook you can call
 * from a "پشتیبان‌گیری دستی" button if you want an extra confirmation step server-side.
 */
exports.confirmBackup = onCall(async (request) => {
  const uid = request.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Sign in first");
  const snap = await db.collection("backups").doc(uid).get();
  if (!snap.exists) throw new HttpsError("not-found", "No backup found yet");
  const data = snap.data();
  return { updatedAtEpochMs: data.updatedAtEpochMs || null, sizeBytes: (data.data || "").length };
});
