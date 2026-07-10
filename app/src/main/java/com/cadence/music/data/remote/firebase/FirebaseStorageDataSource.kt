package com.cadence.music.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Handles creator-upload binary storage: /creator_audio/{uploaderId}/{songId}.<ext> and
 *  /cover_art/{uploaderId}/{songId}.jpg. Emits 0f..1f progress so the upload screen can show a
 *  real progress bar rather than an indeterminate spinner. */
@Singleton
class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    fun uploadAudio(uploaderId: String, songId: String, fileUri: Uri): Flow<Pair<Float, String?>> = callbackFlow {
        val ref = storage.reference.child("creator_audio/$uploaderId/$songId.m4a")
        val task = ref.putFile(fileUri)

        task.addOnProgressListener { snapshot ->
            val progress = snapshot.bytesTransferred.toFloat() / snapshot.totalByteCount.toFloat()
            trySend(progress to null)
        }
        task.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { uri -> trySend(1f to uri.toString()) }
        }
        task.addOnFailureListener { close(it) }

        awaitClose { task.cancel() }
    }

    suspend fun uploadCoverArt(uploaderId: String, songId: String, imageUri: Uri): String {
        val ref = storage.reference.child("cover_art/$uploaderId/$songId.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }
}
