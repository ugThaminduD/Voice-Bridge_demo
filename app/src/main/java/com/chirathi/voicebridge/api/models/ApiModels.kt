package com.chirathi.voicebridge.api.models

import com.google.gson.annotations.SerializedName

// ============================================================
// CHATBOT API MODELS
// ============================================================

data class ChatRequest(
    val message: String
)

data class ChatResponse(
    val response: String,
    val intent: String? = null
)

// ============================================================
// RECOMMENDER API MODELS
// ============================================================

data class RecommendByAgeRequest(
    val age: Int,
    val disorder: String
)

data class RecommendByTextRequest(
    val text: String,
    @SerializedName("top_n")
    val topN: Int = 5
)

data class TherapyTask(
    val title: String,
    val description: String,
    @SerializedName("age_group")
    val ageGroup: String,
    val disorder: String,
    val activity: String,
    val materials: String,
    val duration: String,
    val tips: String,
    val similarity: Double? = null
)

data class RecommendationsResponse(
    val recommendations: List<TherapyTask>
)
