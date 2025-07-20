package com.LambdaProject.MathArt.Data

import com.LambdaProject.MathArt.model.KuesionerResult
import com.LambdaProject.MathArt.model.ValidatorRole
import com.LambdaProject.MathArt.model.ValidatorSession

interface KuesionerRepository {
    suspend fun submitKuesionerResult(result: KuesionerResult)
    suspend fun cacheSession(userId: String, role: ValidatorRole, session: ValidatorSession)
    suspend fun getCachedSession(userId: String, role: ValidatorRole): ValidatorSession?
    suspend fun getKuesionerResult(userId: String, role: ValidatorRole): KuesionerResult?
    suspend fun getSubmittedRole(userId: String): ValidatorRole?
}
