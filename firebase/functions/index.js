const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { HttpsError } = require("firebase-functions/v1/https");
const { Change } = require("firebase-functions");
const algoilaFunctions = require("./algolia");


admin.initializeApp();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

exports.createNewUser = functions.https.onCall(async (data, context) => {

  //Check whether this email id is already in use by another person. If so throw error and return
  var existingUser = null;
  try {
    existingUser = await admin.auth().getUserByEmail(data.email);
  } catch (error) {
    existingUser = null;
  }

  if (existingUser != null) {
    throw new HttpsError("already-exists", "This email id is already in use by another user");
  }

  //Good. This email is not in use. Go ahead and create new User
  const userRecord = await admin.auth().createUser({
    email: data.email,
    emailVerified: false,
    password: data.password,
    displayName: data.displayName,
    disabled: false,
  });

  //set the custom claim on the newly created user
  if (data.email === "papcopvtltd@gmail.com")
    await admin.auth().setCustomUserClaims(userRecord.uid, { role: "root" });
  else
    await admin.auth().setCustomUserClaims(userRecord.uid, { role: "guest" });


  //Crate the document for the current user and update the role change timestamp
  const document = admin.firestore().doc('/users/' + userRecord.uid);
  await document.set({
    displayName: userRecord.displayName,
    email: userRecord.email,
    refreshTime: new Date().getTime()
  });

  return userRecord;

});


exports.updateUserClaim = functions.https.onCall(async (data, context) => {

  //First things first. The role of papcopvtltd@gmail.com is always root and cannot be changed even by another root
  if (data.email === "papcopvtltd@gmail.com") {
    throw new HttpsError("failed-precondition", "This account role cannot be channged and will always be root")
  }

  //The user calling this function should be a root
  if (context.auth.token.role != "root") {
    throw new HttpsError("permission-denied", "Only root can update user claims");
  } else {
    //A root cannot change the role of himself to de-promote him
    //This action will make sure that atleast one root user is always there in the app
    if (context.auth.token.email === data.email) {
      throw new HttpsError("failed-precondition", "You cannot change the role of yourself");
    }
  }

  //Ok. the calling user is a root. Now get and update the claim
  const userToUpdate = await admin.auth().getUserByEmail(data.email);
  await admin.auth().setCustomUserClaims(userToUpdate.uid, { role: data.role });

  //modify the refresh time of the user in the user document so that any clients
  //watching this document will be notififed immediately about the role change to take action
  const document = admin.firestore().doc('/users/' + userToUpdate.uid);
  await document.update({
    refreshTime: new Date().getTime()
  });

});

exports.deleteUserDocument = functions.auth.user().onDelete((user) => {

  const document = admin.firestore().doc('/users/' + user.uid);
  document.delete();

});


exports.indexPrintOrder = functions.firestore
  .document('Destinations/{destinationId}/Jobs/{poId}')
  .onWrite((change, context) => {

    const destinationId = context.params.destinationId;

    if (!change.before.exists && change.after.exists) {
      //Creation
      algoilaFunctions.handlePrintOrderCreation(change.after.data(), destinationId);
    }

    if (change.before.exists && change.after.exists) {
      //Updation
      algoilaFunctions.handlePrintOrderUpdation(change.before.data(), change.after.data(), destinationId);
    }

    //Deletion Operation is Ignored since printOrders once created can only be cancelled and not deleted

  });


exports.deleteStorageFile = functions.firestore
  .document('previews/{documentId}')
  .onDelete((snap, context) => {
    
    const deletedRecord = snap.data();
    const storage = admin.storage().bucket();
    const result=storage.file(deletedRecord.path).delete();
    return result;

  });

/*exports.deleteStorageRecord = functions.storage.object().onDelete(async (object) => {

  //Check if this is overwrite. if so, we can return

  if (object.metageneration > 1){
    functions.logger.log("Overwriting detected onDelete");
    return;
  }
  
  const filePath=object.name;
  functions.logger.log("Metageneration:",object.metageneration);
  functions.logger.log("Path of file deleted:",filePath);


});*/