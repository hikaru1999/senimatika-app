package com.LambdaProject.MathArt.data.repository

import android.util.Log
import com.LambdaProject.MathArt.data.model.MaterialDocument
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository terpusat untuk mengelola data Materi dan Progres Siswa.
 * Mengimplementasikan strategi Smart-Caching (In-Memory + Offline Cache)
 * untuk meminimalkan konsumsi kuota Read dan Write Firebase Firestore secara signifikan.
 */
object MaterialRepository {
    private val db = Firebase.firestore

    // Cache memori untuk dokumen materi (key: materialId)
    private val materialCache = ConcurrentHashMap<String, MaterialDocument>()

    // Cache memori untuk progres halaman pengguna (key: "${userId}_${materialId}")
    private val progressCache = ConcurrentHashMap<String, Int>()

    /**
     * Menyimpan dokumen materi ke dalam cache memori (misalnya dari Dashboard atau stream).
     */
    fun cacheMaterial(materialDoc: MaterialDocument) {
        if (materialDoc.id.isNotBlank()) {
            materialCache[materialDoc.id] = materialDoc
        }
    }

    /**
     * Mengambil dokumen materi langsung dari cache memori jika tersedia.
     */
    fun getCachedMaterial(materialId: String): MaterialDocument? {
        return materialCache[materialId]
    }

    /**
     * Mendapatkan dokumen materi secara efisien kuota:
     * 1. Cek memory cache -> Jika ada, return instan (0 Firestore Read).
     * 2. Cek Firestore offline cache -> Jika ada, return dari cache lokal (0 Firestore Read kuota).
     * 3. Jika belum pernah diunduh, baru fetch dari SERVER Firestore dan simpan ke cache.
     */
    fun getMaterial(
        materialId: String,
        forceRefresh: Boolean = false,
        onSuccess: (MaterialDocument) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        if (!forceRefresh) {
            val cached = materialCache[materialId]
            if (cached != null) {
                onSuccess(cached)
                return
            }
        }

        val docRef = db.collection("materials").document(materialId)

        // Coba ambil dari cache lokal Firestore terlebih dahulu untuk menghemat kuota
        docRef.get(Source.CACHE)
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val material = MaterialDocument.fromSnapshot(doc)
                    materialCache[materialId] = material
                    onSuccess(material)
                } else {
                    fetchFromServer(docRef, materialId, onSuccess, onError)
                }
            }
            .addOnFailureListener {
                fetchFromServer(docRef, materialId, onSuccess, onError)
            }
    }

    private fun fetchFromServer(
        docRef: com.google.firebase.firestore.DocumentReference,
        materialId: String,
        onSuccess: (MaterialDocument) -> Unit,
        onError: (Exception) -> Unit
    ) {
        docRef.get(Source.DEFAULT)
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val material = MaterialDocument.fromSnapshot(doc)
                    materialCache[materialId] = material
                    onSuccess(material)
                } else {
                    onError(Exception("Materi tidak ditemukan: $materialId"))
                }
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    /**
     * Mengambil progres halaman terakhir pengguna untuk materi tertentu.
     * Menggunakan cache memori dan cache lokal terlebih dahulu.
     */
    fun getUserMaxPage(
        userId: String,
        materialId: String,
        onResult: (Int) -> Unit
    ) {
        if (userId.isBlank()) {
            onResult(0)
            return
        }

        val cacheKey = "${userId}_$materialId"
        val inMemory = progressCache[cacheKey]
        if (inMemory != null) {
            onResult(inMemory)
            return
        }

        db.collection("userProgress").document(userId)
            .get(Source.CACHE)
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val maxPage = doc.getLong("progress_$materialId")?.toInt()
                        ?: doc.getLong("maxPage")?.toInt()
                        ?: 0
                    progressCache[cacheKey] = maxPage
                    onResult(maxPage)
                } else {
                    getUserMaxPageFromServer(userId, materialId, cacheKey, onResult)
                }
            }
            .addOnFailureListener {
                getUserMaxPageFromServer(userId, materialId, cacheKey, onResult)
            }
    }

    private fun getUserMaxPageFromServer(
        userId: String,
        materialId: String,
        cacheKey: String,
        onResult: (Int) -> Unit
    ) {
        db.collection("userProgress").document(userId)
            .get(Source.DEFAULT)
            .addOnSuccessListener { doc ->
                val maxPage = doc.getLong("progress_$materialId")?.toInt()
                    ?: doc.getLong("maxPage")?.toInt()
                    ?: 0
                progressCache[cacheKey] = maxPage
                onResult(maxPage)
            }
            .addOnFailureListener {
                onResult(0)
            }
    }

    /**
     * Memperbarui progres halaman pengguna dengan optimasi kuota maksimal:
     * - Tidak melakukan docRef.get() sebelum write (menghemat 1 read per perpindahan halaman).
     * - Hanya melakukan write jika newMaxPage benar-benar lebih tinggi dari progres tersimpan.
     * - Menggunakan merge write langsung.
     */
    fun updateAccessiblePage(
        userId: String,
        materialId: String,
        newMaxPage: Int
    ) {
        if (userId.isBlank()) return
        val cacheKey = "${userId}_$materialId"
        val currentMax = progressCache[cacheKey] ?: 0

        // Hindari write Firestore jika pengguna hanya membaca ulang halaman sebelumnya
        if (newMaxPage <= currentMax) {
            return
        }

        // Perbarui memory cache segera
        progressCache[cacheKey] = newMaxPage

        val updateData = mapOf(
            "maxPage" to newMaxPage,
            "progress_$materialId" to newMaxPage,
            "lastUpdated" to FieldValue.serverTimestamp()
        )

        db.collection("userProgress").document(userId)
            .set(updateData, SetOptions.merge())
            .addOnFailureListener { exception ->
                Log.e("MaterialRepository", "Gagal memperbarui userProgress: ${exception.message}")
            }
    }

    /**
     * Mereset progres halaman (misalnya untuk keperluan testing atau mulai ulang materi).
     */
    fun resetAccessiblePage(userId: String, materialId: String = "") {
        if (userId.isBlank()) return
        if (materialId.isNotBlank()) {
            progressCache["${userId}_$materialId"] = 0
        } else {
            progressCache.clear()
        }

        val updateData = if (materialId.isNotBlank()) {
            mapOf("progress_$materialId" to 0)
        } else {
            mapOf("maxPage" to 0)
        }

        db.collection("userProgress").document(userId)
            .set(updateData, SetOptions.merge())
            .addOnFailureListener { exception ->
                Log.e("MaterialRepository", "Gagal mereset progress: ${exception.message}")
            }
    }
}
