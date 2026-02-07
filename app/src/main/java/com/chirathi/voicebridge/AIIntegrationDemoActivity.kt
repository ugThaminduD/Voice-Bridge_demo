package com.chirathi.voicebridge

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chirathi.voicebridge.repository.AIRepository
import kotlinx.coroutines.launch

/**
 * AI Integration Demo Activity
 * Shows how to use all three AI models:
 * 1. Chatbot (via API)
 * 2. Task Recommender (via API)
 * 3. Symbol Classifier (TensorFlow Lite - on-device)
 */
class AIIntegrationDemoActivity : AppCompatActivity() {
    
    private val aiRepository = AIRepository()
    
    // Temporary hardcoded values (bypass login)
    private val TEMP_CHILD_AGE = 6
    private val TEMP_CHILD_DISORDER = "Stuttering"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_demo) // You'll need to create this layout
        
        setupChatbotDemo()
        setupRecommenderDemo()
    }
    
    /**
     * DEMO 1: Chatbot Integration
     */
    private fun setupChatbotDemo() {
        val inputMessage: EditText = findViewById(R.id.input_message)
        val btnSendMessage: Button = findViewById(R.id.btn_send_message)
        val tvChatResponse: TextView = findViewById(R.id.tv_chat_response)
        
        btnSendMessage.setOnClickListener {
            val message = inputMessage.text.toString().trim()
            
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Show loading
            tvChatResponse.text = "Thinking..."
            
            // Call chatbot API
            lifecycleScope.launch {
                val response = aiRepository.sendChatMessage(message)
                
                if (response != null) {
                    tvChatResponse.text = "🤖 ${response.response}\n\nIntent: ${response.intent ?: "Unknown"}"
                } else {
                    tvChatResponse.text = "❌ Error: Could not connect to chatbot"
                }
            }
        }
    }
    
    /**
     * DEMO 2: Task Recommender Integration
     */
    private fun setupRecommenderDemo() {
        val btnGetRecommendations: Button = findViewById(R.id.btn_get_recommendations)
        val tvRecommendations: TextView = findViewById(R.id.tv_recommendations)
        
        btnGetRecommendations.setOnClickListener {
            // Show loading
            tvRecommendations.text = "Loading recommendations..."
            
            // Get recommendations based on age and disorder
            lifecycleScope.launch {
                val recommendations = aiRepository.getRecommendationsByAge(
                    age = TEMP_CHILD_AGE,
                    disorder = TEMP_CHILD_DISORDER
                )
                
                if (recommendations.isNotEmpty()) {
                    // Display first recommendation
                    val task = recommendations.first()
                    tvRecommendations.text = """
                        📚 ${task.title}
                        
                        ${task.description}
                        
                        Age Group: ${task.ageGroup}
                        Disorder: ${task.disorder}
                        Duration: ${task.duration}
                        
                        Materials: ${task.materials}
                        
                        Tips: ${task.tips}
                        
                        Found ${recommendations.size} total recommendations.
                    """.trimIndent()
                } else {
                    tvRecommendations.text = "❌ No recommendations found"
                }
            }
        }
    }
}
