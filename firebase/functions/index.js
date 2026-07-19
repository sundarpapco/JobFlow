/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const functions = require("firebase-functions/v1");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentWritten, onDocumentDeleted } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
const algoliaFunctions = require("./algolia");

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });


admin.initializeApp();

// 1. CREATE NEW USER (Callable)
// Renamed from createNewUser to satisfy Gen 2 lowercase naming rules
exports.createnewuser = onCall(async (request) => {
  const data = request.data;

  // Check whether this email id is already in use by another person.
  let existingUser = null;
  try {
    existingUser = await admin.auth().getUserByEmail(data.email);
  } catch (error) {
    existingUser = null;
  }

  if (existingUser != null) {
    throw new HttpsError("already-exists", "This email id is already in use by another user");
  }

  // Good. This email is not in use. Go ahead and create new User
  const userRecord = await admin.auth().createUser({
    email: data.email,
    emailVerified: false,
    password: data.password,
    displayName: data.displayName,
    disabled: false,
  });

  // Set the custom claim on the newly created user
  if (data.email === "papcopvtltd@gmail.com") {
    await admin.auth().setCustomUserClaims(userRecord.uid, { role: "root" });
  } else {
    await admin.auth().setCustomUserClaims(userRecord.uid, { role: "guest" });
  }

  // Create the document for the current user and update the role change timestamp
  const document = admin.firestore().doc('/users/' + userRecord.uid);
  await document.set({
    displayName: userRecord.displayName,
    email: userRecord.email,
    refreshTime: new Date().getTime()
  });

  return userRecord;
});


// 2. UPDATE USER CLAIM (Callable)
// Renamed from updateUserClaim
exports.updateuserclaim = onCall(async (request) => {
  const data = request.data;
  const auth = request.auth;

  // Ensure user is authenticated
  if (!auth) {
    throw new HttpsError("unauthenticated", "The function must be called while authenticated.");
  }

  // First things first. The role of papcopvtltd@gmail.com is always root and cannot be changed
  if (data.email === "papcopvtltd@gmail.com") {
    throw new HttpsError("failed-precondition", "This account role cannot be changed and will always be root");
  }

  // The user calling this function should be a root
  if (auth.token.role !== "root") {
    throw new HttpsError("permission-denied", "Only root can update user claims");
  } else {
    // A root cannot change the role of himself to de-promote him
    if (auth.token.email === data.email) {
      throw new HttpsError("failed-precondition", "You cannot change the role of yourself");
    }
  }

  // Ok. the calling user is a root. Now get and update the claim
  const userToUpdate = await admin.auth().getUserByEmail(data.email);
  await admin.auth().setCustomUserClaims(userToUpdate.uid, { role: data.role });

  // Modify the refresh time of the user in the user document
  const document = admin.firestore().doc('/users/' + userToUpdate.uid);
  await document.update({
    refreshTime: new Date().getTime()
  });
});


/**
 * Gen 2 Callable Function: deleteuser
 * Expects: request.data.email (String)
 */
exports.deleteuser = onCall(async (request) => {
  const data = request.data;
  const auth = request.auth;

  // 1. Security Check: Ensure the person calling this function is logged in
  if (!auth) {
    throw new HttpsError(
      "unauthenticated",
      "The function must be called while authenticated."
    );
  }

  // 2. Security Check: Only allow 'root' users to delete other users
  if (auth.token.role !== "root") {
    throw new HttpsError(
      "permission-denied",
      "Only root administrators can delete user accounts."
    );
  }

  // 3. Validation Check: Ensure an email was provided
  if (!data.email || typeof data.email !== "string") {
    throw new HttpsError(
      "invalid-argument",
      "The function must be called with a valid 'email' string."
    );
  }

  // 4. Protection Check: Prevent accidental deletion of the main root account
  if (data.email.toLowerCase() === "papcopvtltd@gmail.com") {
    throw new HttpsError(
      "failed-precondition",
      "The primary root account cannot be deleted."
    );
  }

  try {
    // 5. Look up the target user by email
    const userRecord = await admin.auth().getUserByEmail(data.email);
    const targetUid = userRecord.uid;

    // 6. Delete the user from Firebase Authentication
    await admin.auth().deleteUser(targetUid);

    // 7. Clean up their corresponding Firestore document in /users/{uid}
    const userDocRef = admin.firestore().doc(`/users/${targetUid}`);
    await userDocRef.delete();

    return {
      success: true,
      message: `Successfully deleted user ${data.email} and their data matrix.`,
    };

  } catch (error) {
    // Handle case where user isn't found in Firebase Auth
    if (error.code === "auth/user-not-found") {
      throw new HttpsError(
        "not-found",
        `No user found with the email address: ${data.email}`
      );
    }

    // Catch-all for other system or network errors
    throw new HttpsError(
      "internal",
      `Failed to delete user: ${error.message}`
    );
  }
});


// 4. INDEX PRINT ORDER (Firestore Trigger)
// Renamed from indexPrintOrder. In Gen 2, onWrite becomes onDocumentWritten
exports.indexprintorder = onDocumentWritten('Destinations/{destinationId}/Jobs/{poId}', (event) => {
  const destinationId = event.params.destinationId;
  const change = event.data;

  if (!change.before.exists && change.after.exists) {
    // Creation
    logger.log("Handling the print order creation");
    algoliaFunctions.handlePrintOrderCreation(change.after.data(), destinationId);
  }

  if (change.before.exists && change.after.exists) {
    // Updation
    algoliaFunctions.handlePrintOrderUpdation(change.before.data(), change.after.data(), destinationId);
  }

  // Deletion Operation is Ignored since printOrders once created can only be cancelled and not deleted

});

// Function to clear the corresponding Algolia index whenever a completed print order is deleted from Firestore
// This function will be used when we batch delete (500 Print orders) the oldest print orders which are completed to save
// Algolia free tier limitation of 10,000 Records above which we should pay
exports.clearoldprintorder = onDocumentDeleted('Destinations/Completed/Jobs/{poId}', (event) => {
    const snapShot = event.data;
    algoliaFunctions.handlePrintOrderDeletion(snapShot.data());
});


// 5. DELETE STORAGE FILE (Firestore Trigger)
// Renamed from deleteStorageFile. In Gen 2, onDelete becomes onDocumentDeleted
exports.deletestoragefile = onDocumentDeleted('previews/{documentId}', (event) => {
  const snap = event.data;
  const deletedRecord = snap.data();
  const storage = admin.storage().bucket();

  return storage.file(deletedRecord.path).delete();
});
