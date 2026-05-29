package com.LambdaProject.MathArt.utils

import com.LambdaProject.MathArt.data.model.Decision
import com.LambdaProject.MathArt.data.model.KuesionerResult
import com.LambdaProject.MathArt.data.model.ValidatorSession

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
        role = role.lowercase(),
        responses = responses.associate { it.questionId to it.selectedValue },
        decision = decision,
        comment = comment,
        deviceInfo = deviceInfo,
        submittedAt = System.currentTimeMillis()
    )
}