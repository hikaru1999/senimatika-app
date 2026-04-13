package com.LambdaProject.MathArt.data.repository

import com.LambdaProject.MathArt.data.model.KuesionerResult
import com.LambdaProject.MathArt.data.model.ValidatorRole
import com.LambdaProject.MathArt.data.model.ValidatorSession

interface KuesionerRepository {
    suspend fun submitKuesionerResult(result: KuesionerResult)
    suspend fun cacheSession(userId: String, role: ValidatorRole, session: ValidatorSession)
    suspend fun getCachedSession(userId: String, role: ValidatorRole): ValidatorSession?
    suspend fun getKuesionerResult(userId: String, role: ValidatorRole): KuesionerResult?
    suspend fun getSubmittedRole(userId: String): ValidatorRole?
}