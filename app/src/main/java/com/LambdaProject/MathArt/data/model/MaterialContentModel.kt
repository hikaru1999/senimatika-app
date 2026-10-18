package com.LambdaProject.MathArt.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class MaterialDocument(
    val id: String = "",
    val title: String = "",
    val coverUrl: String = "",
    val classLevel: String = "",
    val order: Int = 1,
    val description: String = "",
    val category: String = "Geometri",
    val exp: Int = 100,
    val isHighlight: Boolean = false,
    val learningGoals: List<String> = emptyList(),
    val learningSteps: List<String> = emptyList(),
    val sections: List<MaterialSection> = emptyList(),
    val quizId: String = "",
    val quiz: MaterialQuiz? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromSnapshot(doc: DocumentSnapshot): MaterialDocument {
            val data = doc.data ?: return MaterialDocument(id = doc.id)
            return fromMap(data, doc.id)
        }

        @Suppress("UNCHECKED_CAST")
        fun fromMap(data: Map<String, Any>, docId: String = ""): MaterialDocument {
            val id = (data["id"] as? String)?.ifBlank { docId } ?: docId
            val title = data["title"] as? String ?: ""
            val coverUrl = data["coverUrl"] as? String ?: ""
            val classLevel = data["classLevel"] as? String ?: ""
            val order = (data["order"] as? Number)?.toInt() ?: 1
            val quizId = data["quizId"] as? String ?: ""
            val description = (data["description"] as? String) ?: (data["deskripsi"] as? String) ?: ""
            val category = (data["category"] as? String) ?: "Geometri"
            val exp = (data["exp"] as? Number)?.toInt() ?: 100
            val isHighlight = (data["isHighlight"] as? Boolean)
                ?: (data["isHighlight"] as? String)?.toBooleanStrictOrNull()
                ?: false
            val learningGoals = ((data["learningGoals"] as? List<*>) ?: (data["goals"] as? List<*>))?.mapNotNull { it?.toString() } ?: emptyList()
            val learningSteps = ((data["learningSteps"] as? List<*>) ?: (data["steps"] as? List<*>))?.mapNotNull { it?.toString() } ?: emptyList()

            val rawSections = data["sections"] as? List<Map<String, Any>> ?: emptyList()
            val sections = rawSections.map { secMap ->
                MaterialSection.fromMap(secMap)
            }

            val quizMap = data["quiz"] as? Map<String, Any>
            val quiz = quizMap?.let { MaterialQuiz.fromMap(it) }

            return MaterialDocument(
                id = id,
                title = title,
                coverUrl = coverUrl,
                classLevel = classLevel,
                order = order,
                description = description,
                category = category,
                exp = exp,
                isHighlight = isHighlight,
                learningGoals = learningGoals,
                learningSteps = learningSteps,
                sections = sections,
                quizId = quizId,
                quiz = quiz
            )
        }
    }
}

data class MaterialQuiz(
    val title: String = "",
    val description: String = "",
    val questions: List<QuizQuestionItem> = emptyList()
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(data: Map<String, Any>): MaterialQuiz {
            val title = data["title"] as? String ?: ""
            val description = data["description"] as? String ?: ""
            val rawQuestions = data["questions"] as? List<Map<String, Any>> ?: emptyList()
            val questions = rawQuestions.map { qMap ->
                QuizQuestionItem.fromMap(qMap)
            }
            return MaterialQuiz(title = title, description = description, questions = questions)
        }
    }
}

data class QuizQuestionItem(
    val id: String = "",
    val imageUrl: String = "",
    val instruction: String = "",
    val options: List<String> = emptyList(),
    val correctAnswers: List<String> = emptyList(),
    val questionType: String = "complex",
    val type: String = "complex",
    val imageResId: Int = 0,
    val explanation: String = ""
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(data: Map<String, Any>): QuizQuestionItem {
            val id = data["id"] as? String ?: ""
            val imageUrl = data["imageUrl"] as? String ?: ""
            val instruction = data["instruction"] as? String ?: (data["question"] as? String) ?: ""
            val options = (data["options"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            val correctAnswers = (data["correctAnswers"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            val questionType = (data["questionType"] as? String) ?: (data["type"] as? String) ?: "complex"
            val type = (data["type"] as? String) ?: questionType
            val explanation = (data["explanation"] as? String) ?: (data["pembahasan"] as? String) ?: ""

            val fallbackRes = when (id) {
                "q1" -> com.LambdaProject.MathArt.R.drawable.img_kain_besurek
                "q2" -> com.LambdaProject.MathArt.R.drawable.img_batik_tujuh_rupa
                "q3" -> com.LambdaProject.MathArt.R.drawable.img_batik_bunga_melati
                else -> 0
            }

            return QuizQuestionItem(
                id = id,
                imageUrl = imageUrl,
                instruction = instruction,
                options = options,
                correctAnswers = correctAnswers,
                questionType = questionType,
                type = type,
                imageResId = fallbackRes,
                explanation = explanation
            )
        }
    }
}

data class MaterialSection(
    val id: String = "",
    val title: String = "",
    val blocks: List<ContentBlock> = emptyList()
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(data: Map<String, Any>): MaterialSection {
            val id = data["id"] as? String ?: ""
            val title = data["title"] as? String ?: ""
            val rawBlocks = data["blocks"] as? List<Map<String, Any>> ?: emptyList()
            val blocks = rawBlocks.map { blockMap ->
                ContentBlock.fromMap(blockMap)
            }

            return MaterialSection(
                id = id,
                title = title,
                blocks = blocks
            )
        }
    }
}

data class FlipCardItem(
    val id: String = "",
    val frontTitle: String = "",
    val frontText: String = "",
    val backTitle: String = "",
    val backText: String = "",
    val backHint: String? = null
)

data class FormulaVariable(
    val symbol: String = "",
    val meaning: String = ""
)

data class WorkedExampleStep(
    val id: String = "",
    val stepTitle: String = "",
    val stepContent: String = "",
    val tip: String? = null
)

data class ContentBlock(
    val type: String = "",
    val text: String? = null,
    val level: Int? = null,
    val imageUrl: String? = null,
    val url: String? = null,
    val title: String? = null,
    val items: List<String>? = null,
    val description: String? = null,
    val caption: String? = null,
    val instruction: String? = null,
    val baseImageUrl: String? = null,
    val solutionImageUrl: String? = null,
    val explanationText: String? = null,
    val badge: String? = null,
    val prompt: String? = null,
    val question: String? = null,
    val options: List<String>? = null,
    val correctAnswer: String? = null,
    val explanation: String? = null,
    val cards: List<FlipCardItem>? = null,
    val formula: String? = null,
    val variables: List<FormulaVariable>? = null,
    val note: String? = null,
    val problemText: String? = null,
    val steps: List<WorkedExampleStep>? = null,
    val finalAnswer: String? = null,
    val aspectRatio: String? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(data: Map<String, Any>): ContentBlock {
            val type = (data["type"] as? String)?.uppercase() ?: "UNKNOWN"
            val text = data["text"] as? String
            val level = (data["level"] as? Number)?.toInt()
            val imageUrl = (data["imageUrl"] as? String) ?: (data["image"] as? String)
            val url = data["url"] as? String
            val title = data["title"] as? String
            val items = (data["items"] as? List<*>)?.mapNotNull { it?.toString() }
            val description = (data["description"] as? String) ?: (data["desc"] as? String)
            val caption = data["caption"] as? String
            val instruction = data["instruction"] as? String
            val baseImageUrl = (data["baseImageUrl"] as? String) ?: (data["baseImage"] as? String)
            val solutionImageUrl = (data["solutionImageUrl"] as? String) ?: (data["solutionImage"] as? String)
            val explanationText = (data["explanationText"] as? String) ?: (data["explanation"] as? String)
            val badge = data["badge"] as? String
            val prompt = data["prompt"] as? String
            val question = data["question"] as? String
            val options = (data["options"] as? List<*>)?.mapNotNull { it?.toString() }
            val correctAnswer = data["correctAnswer"] as? String
            val explanation = data["explanation"] as? String
            val aspectRatio = (data["aspectRatio"] as? String) ?: (data["ratio"] as? String)

            val cards = (data["cards"] as? List<*>)?.mapNotNull { item ->
                (item as? Map<*, *>)?.let { map ->
                    FlipCardItem(
                        id = map["id"]?.toString().orEmpty(),
                        frontTitle = map["frontTitle"]?.toString().orEmpty(),
                        frontText = map["frontText"]?.toString().orEmpty(),
                        backTitle = map["backTitle"]?.toString().orEmpty(),
                        backText = map["backText"]?.toString().orEmpty(),
                        backHint = map["backHint"]?.toString()
                    )
                }
            }

            val formula = data["formula"] as? String
            val note = data["note"] as? String
            val variables = (data["variables"] as? List<*>)?.mapNotNull { item ->
                (item as? Map<*, *>)?.let { map ->
                    FormulaVariable(
                        symbol = map["symbol"]?.toString().orEmpty(),
                        meaning = map["meaning"]?.toString().orEmpty()
                    )
                }
            }

            val problemText = (data["problemText"] as? String)
                ?: (data["problem"] as? String)
                ?: (data["soal"] as? String)
                ?: question

            val finalAnswer = (data["finalAnswer"] as? String)
                ?: (data["answer"] as? String)
                ?: (data["jawaban"] as? String)
                ?: (data["conclusion"] as? String)

            val steps = (data["steps"] as? List<*>)?.mapNotNull { item ->
                (item as? Map<*, *>)?.let { map ->
                    WorkedExampleStep(
                        id = map["id"]?.toString().orEmpty(),
                        stepTitle = (map["stepTitle"] ?: map["title"])?.toString().orEmpty(),
                        stepContent = (map["stepContent"] ?: map["content"] ?: map["text"])?.toString().orEmpty(),
                        tip = (map["tip"] ?: map["note"] ?: map["hint"])?.toString()
                    )
                }
            }

            return ContentBlock(
                type = type,
                text = text,
                level = level,
                imageUrl = imageUrl,
                url = url,
                title = title,
                items = items,
                description = description,
                caption = caption,
                instruction = instruction,
                baseImageUrl = baseImageUrl,
                solutionImageUrl = solutionImageUrl,
                explanationText = explanationText,
                badge = badge,
                prompt = prompt,
                question = question,
                options = options,
                correctAnswer = correctAnswer,
                explanation = explanation,
                cards = cards,
                formula = formula,
                variables = variables,
                note = note,
                problemText = problemText,
                steps = steps,
                finalAnswer = finalAnswer,
                aspectRatio = aspectRatio
            )
        }
    }
}
