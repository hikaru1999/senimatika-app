package com.LambdaProject.MathArt.Data

import com.LambdaProject.MathArt.model.Decision
import com.LambdaProject.MathArt.model.KuesionerResult
import com.LambdaProject.MathArt.model.ValidatorRole
import com.LambdaProject.MathArt.model.ValidatorSession

fun ValidatorSession.toKuesionerResult(
    name: String,
    institution: String,
    decision: Decision,
    comment: String?,
    deviceInfo: String?
): KuesionerResult {
    return KuesionerResult(
        userId = validatorId,
        name = name,
        institution = institution,
        role = role.lowercase(), // konversi string ke enum
        responses = responses.associate { it.questionId to it.selectedValue },
        decision = decision,
        comment = comment,
        deviceInfo = deviceInfo,
        submittedAt = System.currentTimeMillis()
    )
}