const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { HttpsError } = require("firebase-functions/v1/https");
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

  return userRecord;

});


exports.updateUserClaim = functions.https.onCall(async (data,context) => {

  //The user calling this function should be a root
  if(context.auth.token.role != "root"){
    throw new HttpsError("permission-denied","Only root can update user claims");
  }else{
    //A root cannot change the role of himself to de-promote him
    //This action will make sure that atleast one root user is always there in the app
    if(context.auth.token.email===data.email){
      throw new HttpsError("failed-precondition","You cannot change the role of yourself");
    }
  }
  
  //Ok. the calling user is a root. Now get and update the claim
  const userToUpdate = await admin.auth().getUserByEmail(data.email);
  await admin.auth().setCustomUserClaims(userToUpdate.uid,{ role: data.role });

});
