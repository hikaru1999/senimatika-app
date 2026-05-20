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

            flushBufferToFirestore()

            isAppInForeground = false
        }
    }

    private fun flushBufferToFirestore() {
        val user = firebaseAuth.currentUser ?: return
        if (accumulatedTimeBuffer <= 0) return

        val timeToSave = accumulatedTimeBuffer
        accumulatedTimeBuffer = 0

        val userDoc = firestore.collection("durations").document(user.uid)

        userDoc.set(
            mapOf(
                "duration" to FieldValue.increment(timeToSave),
                "lastUpdated" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).addOnFailureListener {
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
                delay(300_000)

                if (isAppInForeground) {
                    val now = System.currentTimeMillis()
                    accumulatedTimeBuffer += (now - startTime)
                    startTime = now
                    flushBufferToFirestore()
                }
            }
        }
    }

    private fun stopPeriodicSave() {
        periodicJob?.cancel()
        periodicJob = null
    }

    fun checkAndResetWeeklyDuration(userId: String, onDone: () -> Unit = {} ) {
        val docRef = firestore.collection("durations").document(userId)

        docRef.get(Source.DEFAULT).addOnSuccessListener { document ->
            val now = System.currentTimeMillis()
            val mondayMidnight = getMondayMidnightTimestamp()

            if (document.exists()) {
                val lastTimestamp = document.getLong("startTimestamp") ?: 0L

                if (lastTimestamp < mondayMidnight) {
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
                docRef.set(mapOf("duration" to 0L, "startTimestamp" to now))
                onDone()
            }
        }.addOnFailureListener { onDone() }
    }

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


