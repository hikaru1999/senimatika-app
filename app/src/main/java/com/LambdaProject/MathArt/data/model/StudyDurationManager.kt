package com.LambdaProject.MathArt.data.model

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import java.util.Calendar

object StudyDurationManager {
    private var startTime = 0L
    private var isAppInForeground = false
    private var periodicJob: Job? = null

    private var accumulatedTimeBuffer = 0L

    private val firebaseAuth = FirebaseAuth.getInstance()
    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()

    fun onAppForegrounded() {
        if (isAppInForeground) return
        isAppInForeground = true
        startTime = System.currentTimeMillis()

        firebaseAuth.currentUser?.uid?.let { userId ->
            checkAndResetWeeklyDuration(userId)
        }
        startPeriodicSave()
    }

    /* fun onAppBackgrounded() {
        if (isAppInForeground) {
            stopPeriodicSave()
            val elapsedTime = System.currentTimeMillis() - startTime
            saveStudyDuration(elapsedTime)
            firebaseAuth.currentUser?.uid?.let { userId ->
                checkAndResetWeeklyDuration(userId)
            }
            isAppInForeground = false
        }
    } */

    fun onAppBackgrounded() {
        if (isAppInForeground) {
            stopPeriodicSave()
            val sessionTime = System.currentTimeMillis() - startTime
            accumulatedTimeBuffer += sessionTime

            // Simpan sisa buffer saat aplikasi ditutup
            flushBufferToFirestore()

            isAppInForeground = false
        }
    }

    private fun flushBufferToFirestore() {
        val user = firebaseAuth.currentUser ?: return
        if (accumulatedTimeBuffer <= 0) return

        val timeToSave = accumulatedTimeBuffer
        accumulatedTimeBuffer = 0 // Reset buffer segera

        val userDoc = firestore.collection("durations").document(user.uid)

        // Gunakan set dengan Merge agar tidak perlu cek dokumen exists (Hemat Read)
        userDoc.set(
            mapOf(
                "duration" to FieldValue.increment(timeToSave),
                "lastUpdated" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).addOnFailureListener {
            // Jika gagal, kembalikan ke buffer untuk dicoba nanti
            accumulatedTimeBuffer += timeToSave
            Log.e("StudyDuration", "Failed to save, buffered back")
        }
    }

    private fun saveStudyDuration(elapsedTime: Long) {
        val user = firebaseAuth.currentUser ?: return
        val userDoc = firestore.collection("durations").document(user.uid)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDoc)
            val currentDuration = snapshot.getLong("duration") ?: 0L

            // Cek apakah timestamp awal sudah ada
            if (!snapshot.contains("startTimestamp")) {
                transaction.set(userDoc, mapOf(
                    "duration" to currentDuration + elapsedTime,
                    "startTimestamp" to System.currentTimeMillis()
                ))
            } else {
                transaction.update(userDoc, "duration", currentDuration + elapsedTime)
            }
        }
    }

    fun observeStudyDuration(userId: String, onUpdate: (Long) -> Unit) {
        firestore.collection("durations").document(userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val duration = snapshot.getLong("duration") ?: 0L
                    onUpdate(duration)
                }
            }
    }

    private fun startPeriodicSave() {
        periodicJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(300_000) // 5 menit

                if (isAppInForeground) {
                    val now = System.currentTimeMillis()
                    accumulatedTimeBuffer += (now - startTime)
                    startTime = now
                    flushBufferToFirestore()
                }

                /* val now = System.currentTimeMillis()
                val elapsed = now - startTime
                saveStudyDuration(elapsed)
                startTime = now */
            }
        }
    }

    private fun stopPeriodicSave() {
        periodicJob?.cancel()
        periodicJob = null
    }

    /* fun checkAndResetWeeklyDuration(userId: String, onDone: () -> Unit = {} ) {
        val docRef = firestore.collection("durations").document(userId)
        docRef.get().addOnSuccessListener { document ->
            val now = System.currentTimeMillis()

            val calendar = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val mondayMidnight = calendar.timeInMillis
            val fiveMinutesAgo = now - 5 * 60 * 1000

            if (document.exists()) {
                val lastTimestamp = document.getLong("startTimestamp") ?: 0L

                if (lastTimestamp < mondayMidnight) {
                    docRef.set(
                        mapOf(
                            "duration" to 0L,
                            "startTimestamp" to now
                        )
                    )
                }
            }
            onDone()
        }
    } */

    fun checkAndResetWeeklyDuration(userId: String, onDone: () -> Unit = {} ) {
        val docRef = firestore.collection("durations").document(userId)

        docRef.get(Source.DEFAULT).addOnSuccessListener { document ->
            val now = System.currentTimeMillis()
            val mondayMidnight = getMondayMidnightTimestamp()

            if (document.exists()) {
                val lastTimestamp = document.getLong("startTimestamp") ?: 0L

                if (lastTimestamp < mondayMidnight) {
                    // Reset dilakukan hanya jika perlu (1 Write)
                    docRef.update(
                        mapOf(
                            "duration" to 0L,
                            "startTimestamp" to now
                        )
                    ).addOnSuccessListener { onDone() }
                } else {
                    onDone()
                }
            } else {
                // Dokumen baru (First time user)
                docRef.set(mapOf("duration" to 0L, "startTimestamp" to now))
                onDone()
            }
        }.addOnFailureListener { onDone() }
    }

    /* fun checkAndResetWeeklyDuration(userId: String, onDone: () -> Unit = {}) {
        val docRef = firestore.collection("durations").document(userId)
        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val startTimestamp = document.getLong("startTimestamp") ?: System.currentTimeMillis()
                val now = System.currentTimeMillis()
                val sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L

                if (now - startTimestamp >= sevenDaysMillis) {
                    docRef.set(mapOf(
                        "duration" to 0L,
                        "startTimestamp" to now
                    ))
                }
            }
            onDone()
        }
    } */

    private fun getMondayMidnightTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (System.currentTimeMillis() < timeInMillis) {
                add(Calendar.DAY_OF_YEAR, -7)
            }
        }.timeInMillis
    }
}


