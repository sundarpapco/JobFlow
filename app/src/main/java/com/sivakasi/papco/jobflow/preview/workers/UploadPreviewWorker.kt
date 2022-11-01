package com.sivakasi.papco.jobflow.preview.workers

import android.app.Notification
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.sivakasi.papco.jobflow.JobFlowApplication
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.preview.JobPreview
import com.sivakasi.papco.jobflow.preview.toPreviewRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@ExperimentalCoroutinesApi
class UploadPreviewWorker(context: Context, workParams: WorkerParameters) :
    CoroutineWorker(context, workParams) {

    companion object {
        private const val INPUT_DATA_PREVIEW_ID = "jobFlow:preview:id"
        private const val Input_DATA_FILE_NAME = "jobFlow:preview:fileName"
        private const val WORK_NAME = "JobFlow:uploadPreviewWork"

        private const val NOTIFICATION_ID_PROGRESS = 1

        fun startWith(context: Context, preview: JobPreview) {

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<UploadPreviewWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        INPUT_DATA_PREVIEW_ID to preview.previewId,
                        Input_DATA_FILE_NAME to preview.fileName
                    )
                )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(WORK_NAME)
                .addTag(preview.fileName)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                preview.previewId,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request
            )
        }
    }

    private val notificationBuilder =
        NotificationCompat.Builder(context, JobFlowApplication.CHANNEL_ID_PREVIEW_UPLOAD).apply {
            setContentTitle(applicationContext.getString(R.string.uploading_preview))
            setProgress(0, 100, true)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                setSmallIcon(R.drawable.ic_logo)
            else
                setSmallIcon(R.drawable.ic_logo_svg)
            foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            priority = NotificationCompat.PRIORITY_DEFAULT
        }

    override suspend fun doWork(): Result {

        setForeground(getForegroundInfo())
        notify(notificationBuilder.build())

        val previewToUpload = getPreview()
        //Make sure the file exists
        val fileToUpload = previewToUpload.localCacheFile()
        if (!fileToUpload.exists())
            return Result.failure()

        //Now the file exists. So, begin uploading and update the progress
        //Step 1. Upload the file
        val result = try {

            uploadFileToStorage(
                previewToUpload.storageReference(),
                fileToUpload
            ).collect {
                updateNotification(it)
            }

            //Step 2. Get the downloadUrl of the uploaded file
            val downloadUrl = previewToUpload.downloadUrl().toString()
            updateNotificationText(
                applicationContext.getString(R.string.creating_fire_store_record)
            )

            //Step 3. Create the corresponding fire store record
            createFireStoreRecord(previewToUpload.copy(downloadUrl = downloadUrl))

            //If all the three steps goes well, then its a success
            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("SUNDAR", "Worker Failed. Will retry later")
            //If this work is re attempting to run more than 10 times, then lets quit
            if (runAttemptCount >= 10)
                Result.failure()
            else
                Result.retry()
        }

        return result
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID_PROGRESS, notificationBuilder.build()
        )
    }

    private fun updateNotification(currentProgress: Int) {

        require(currentProgress <= 100) { "Invalid progress detected" }

        val notification =
            notificationBuilder.apply {
                setProgress(100, currentProgress, false).build()
            }.build()

        notify(notification)

    }

    private fun updateNotificationText(text: String) {

        val notification =
            notificationBuilder.apply {
                setContentText(text)
            }.build()

        notify(notification)
    }

    private fun notify(notification: Notification, id: Int = NOTIFICATION_ID_PROGRESS) {
        NotificationManagerCompat.from(applicationContext).apply {
            notify(id, notification)
        }
    }

    @ExperimentalCoroutinesApi
    private fun uploadFileToStorage(reference: StorageReference, file: File) =
        callbackFlow {
            val fileUri = Uri.fromFile(file)
            val listenerRegistration = reference.putFile(fileUri)
                .addOnProgressListener {
                    val percentageCompletion = (100.0 * it.bytesTransferred) / it.totalByteCount
                    trySend(percentageCompletion.toInt())
                }
                .addOnSuccessListener {
                    close()
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    throw it
                }

            awaitClose { listenerRegistration.cancel() }
        }

    private suspend fun createFireStoreRecord(preview: JobPreview) =
        suspendCancellableCoroutine { continuation ->

            val database = FirebaseFirestore.getInstance()

            database.collection(DatabaseContract.COLLECTION_PREVIEWS)
                .document(preview.documentId())
                .set(preview.toPreviewRecord())
                .addOnSuccessListener {
                    continuation.resume(true)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

    private fun previewId(): String =
        inputData.getString(INPUT_DATA_PREVIEW_ID) ?: error("Preview Id input not found")

    private fun fileName(): String =
        inputData.getString(Input_DATA_FILE_NAME) ?: error("File name input not found")

    private fun getPreview(): JobPreview = JobPreview(
        context = applicationContext,
        previewId = previewId(),
        fileName = fileName()
    )

}