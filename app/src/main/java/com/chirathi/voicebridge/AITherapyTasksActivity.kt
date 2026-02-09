package com.chirathi.voicebridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chirathi.voicebridge.api.models.TherapyTask
import com.chirathi.voicebridge.repository.AIRepository
import kotlinx.coroutines.launch

/**
 * Activity to display AI-recommended therapy tasks
 * This uses the updated UI with beautiful cards showing all AI output details
 */
class AITherapyTasksActivity : AppCompatActivity() {
    
    private val aiRepository = AIRepository()
    
    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var loadingMessageText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edu_lesson_list)
        
        // Get data from intent
        val age = intent.getIntExtra("AGE", 6)
        val disorder = intent.getStringExtra("DISORDER") ?: "Stuttering"
        
        // Initialize views
        initializeViews()
        
        // Setup back button
        backButton.setOnClickListener { finish() }
        
        // Load AI recommendations
        loadAIRecommendations(age, disorder)
    }
    
    private fun initializeViews() {
        titleText = findViewById(R.id.tvListTitle)
        subtitleText = findViewById(R.id.tvAiSubtitle)
        loadingMessageText = findViewById(R.id.tvLoadingMessage)
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.rvLessons)
        backButton = findViewById(R.id.back)
        
        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        
        // Show loading state
        progressBar.visibility = View.VISIBLE
        loadingMessageText.visibility = View.VISIBLE
        subtitleText.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }
    
    private fun loadAIRecommendations(age: Int, disorder: String) {
        lifecycleScope.launch {
            try {
                // Call AI API
                val tasks = aiRepository.getRecommendationsByAge(age, disorder)
                
                // Hide loading
                progressBar.visibility = View.GONE
                loadingMessageText.visibility = View.GONE
                
                if (tasks.isNotEmpty()) {
                    // Show success
                    subtitleText.text = "Found ${tasks.size} personalized tasks for age $age with $disorder"
                    subtitleText.visibility = View.VISIBLE
                    recyclerView.visibility = View.VISIBLE
                    
                    // Setup adapter
                    val adapter = AITherapyTaskAdapter(tasks) { task ->
                        showTaskDetails(task)
                    }
                    recyclerView.adapter = adapter
                    
                    Toast.makeText(
                        this@AITherapyTasksActivity,
                        "✓ ${tasks.size} AI recommendations loaded",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                } else {
                    // No tasks found
                    subtitleText.text = "No recommendations found for age $age with $disorder"
                    subtitleText.visibility = View.VISIBLE
                    
                    Toast.makeText(
                        this@AITherapyTasksActivity,
                        "No recommendations available. Try different criteria.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: Exception) {
                // Error handling
                progressBar.visibility = View.GONE
                loadingMessageText.visibility = View.GONE
                
                subtitleText.text = "Failed to load AI recommendations"
                subtitleText.visibility = View.VISIBLE
                
                Toast.makeText(
                    this@AITherapyTasksActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                
                e.printStackTrace()
            }
        }
    }
    
    private fun showTaskDetails(task: TherapyTask) {
        val taskTitle = task.title ?: task.activity.take(50).split(":").firstOrNull() ?: "Therapy Task"
        val matchScore = task.score ?: task.similarity
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("📚 $taskTitle")
            .setMessage("""
                📝 Activity:
                ${task.activity}
                
                👥 Age Group: ${task.ageGroup}
                🎯 Best for: ${task.disorder}
                ⏱️ Duration: ${task.duration ?: "15-20 minutes"}
                
                🎯 Smart Goal:
                ${task.goal}
                
                ${if (task.materials != null) "🛠️ Materials: ${task.materials}\n" else ""}
                ${if (task.tips != null) "💡 Tips: ${task.tips}\n" else ""}
                ${if (matchScore != null) "⭐ AI Match: ${(matchScore * 100).toInt()}%" else ""}
            """.trimIndent())
            .setPositiveButton("Start This Activity") { dialog, _ ->
                Toast.makeText(this, "Starting: $taskTitle", Toast.LENGTH_SHORT).show()
                // Here you can launch the actual therapy activity
                dialog.dismiss()
            }
            .setNegativeButton("Close", null)
            .create()
        
        dialog.show()
    }
}

/**
 * RecyclerView Adapter for AI Therapy Task Cards
 */
class AITherapyTaskAdapter(
    private val tasks: List<TherapyTask>,
    private val onTaskClick: (TherapyTask) -> Unit
) : RecyclerView.Adapter<AITherapyTaskAdapter.TaskViewHolder>() {
    
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconText: TextView = itemView.findViewById(R.id.tvTaskIcon)
        val titleText: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val matchScoreText: TextView = itemView.findViewById(R.id.tvMatchScore)
        val descriptionText: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val ageGroupText: TextView = itemView.findViewById(R.id.tvAgeGroup)
        val durationText: TextView = itemView.findViewById(R.id.tvDuration)
        val disorderText: TextView = itemView.findViewById(R.id.tvDisorder)
        val viewDetailsButton: Button = itemView.findViewById(R.id.btnViewDetails)
        val startButton: Button = itemView.findViewById(R.id.btnStartActivity)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ai_therapy_task, parent, false)
        return TaskViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        
        // Extract title from activity (first sentence or up to 50 chars)
        val taskTitle = task.title ?: task.activity.take(50).split(":").firstOrNull() ?: "Therapy Task"
        val taskDescription = task.description ?: task.activity
        
        // Icon based on task type
        val icon = when {
            taskTitle.contains("breath", ignoreCase = true) || 
            taskDescription.contains("breath", ignoreCase = true) -> "🫁"
            taskTitle.contains("speech", ignoreCase = true) || 
            taskDescription.contains("speech", ignoreCase = true) -> "🗣️"
            taskTitle.contains("read", ignoreCase = true) || 
            taskDescription.contains("read", ignoreCase = true) -> "📖"
            taskTitle.contains("play", ignoreCase = true) || 
            taskDescription.contains("play", ignoreCase = true) -> "🎮"
            taskTitle.contains("music", ignoreCase = true) || 
            taskDescription.contains("music", ignoreCase = true) -> "🎵"
            taskTitle.contains("emotion", ignoreCase = true) || 
            taskDescription.contains("emotion", ignoreCase = true) -> "😊"
            taskTitle.contains("word", ignoreCase = true) || 
            taskDescription.contains("word", ignoreCase = true) -> "📝"
            else -> "📚"
        }
        holder.iconText.text = icon
        
        // Bind task data
        holder.titleText.text = taskTitle
        holder.descriptionText.text = taskDescription
        holder.ageGroupText.text = task.ageGroup
        holder.durationText.text = task.duration ?: "15-20 min"
        holder.disorderText.text = task.disorder
        
        // AI match score
        val matchScore = task.score ?: task.similarity
        if (matchScore != null) {
            val percentage = (matchScore * 100).toInt()
            holder.matchScoreText.text = "$percentage%"
        } else {
            holder.matchScoreText.text = "—"
        }
        
        // Button click handlers
        holder.viewDetailsButton.setOnClickListener {
            onTaskClick(task)
        }
        
        holder.startButton.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "Starting: $taskTitle",
                Toast.LENGTH_SHORT
            ).show()
            // Here you would launch the actual therapy activity
        }
        
        // Card click
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }
    }
    
    override fun getItemCount() = tasks.size
}
