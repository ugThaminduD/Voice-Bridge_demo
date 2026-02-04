////package com.chirathi.voicebridge
////
////import android.os.Bundle
////import android.view.View
////import android.widget.Button
////import android.widget.ImageView
////import android.widget.TextView
////import androidx.appcompat.app.AppCompatActivity
////import com.chirathi.voicebridge.data.LessonModel
////import android.speech.tts.TextToSpeech
////import java.util.Locale
////
////class Edu_LessonDetailActivity : AppCompatActivity() {
////
////    private lateinit var tts: TextToSpeech
////    private var ttsReady = false
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_edu_lesson_detail)
////
////        // Init TTS
////        tts = TextToSpeech(this) { status ->
////            ttsReady = status == TextToSpeech.SUCCESS
////            if (ttsReady) tts.language = Locale.getDefault()
////        }
////
////        val lesson = intent.getParcelableExtra<LessonModel>("lesson") ?: return
////
////        val ivIcon = findViewById<ImageView>(R.id.ivLessonIcon)
////        val tvTitle = findViewById<TextView>(R.id.tvLessonTitle)
////        val tvContent = findViewById<TextView>(R.id.tvLessonContent)
////        val tvQuestion = findViewById<TextView>(R.id.tvQuestion)
////        val btnHear = findViewById<Button>(R.id.btnHearQuestion)
////        val btnShow = findViewById<Button>(R.id.btnShowAnswer)
////        val btnHowTo = findViewById<Button>(R.id.btnHowTo)
////        val tvAnswer = findViewById<TextView>(R.id.tvAnswer)
////        val tvHowTo = findViewById<TextView>(R.id.tvHowTo)
////
////        val iconId = lesson.getIconResId(this)
////        if (iconId != 0) ivIcon.setImageResource(iconId) else ivIcon.setImageResource(R.drawable.ic_launcher_foreground)
////
////        tvTitle.text = lesson.lessonTitle
////        tvContent.text = lesson.lessonContent
////        tvQuestion.text = lesson.question
////
////        // Hear question aloud
////        btnHear.setOnClickListener {
////            speak("Question: ${lesson.question}")
////        }
////
////        // Show answer only when tapped; speak the answer in simple friendly wording
////        btnShow.setOnClickListener {
////            tvAnswer.visibility = View.VISIBLE
////            val friendlyAnswer = "The answer is: ${lesson.correctAnswer}"
////            tvAnswer.text = friendlyAnswer
////            speak(friendlyAnswer)
////        }
////
////        // Toggle simple "how to answer" hints (short, child-friendly steps + example)
////        btnHowTo.setOnClickListener {
////            if (tvHowTo.visibility == View.VISIBLE) {
////                tvHowTo.visibility = View.GONE
////            } else {
////                val hint = buildHowToHint(lesson.question, lesson.correctAnswer)
////                tvHowTo.text = hint
////                tvHowTo.visibility = View.VISIBLE
////                // optionally read hint aloud
////                speak("How to answer: ${stripNewlinesForSpeech(hint)}")
////            }
////        }
////    }
////
////    private fun buildHowToHint(question: String, answer: String): String {
////        // Keep hints short and simple for children
////        return "How to answer:\n" +
////                "1. Read or listen to the question: \"$question\"\n" +
////                "2. Think of the best word or number.\n" +
////                "3. Say the answer out loud or point to it.\n\n" +
////                "Example: If the question is \"$question\", the answer is \"$answer\"."
////    }
////
////    private fun stripNewlinesForSpeech(text: String): String {
////        return text.replace("\n", ". ")
////    }
////
////    private fun speak(text: String) {
////        if (ttsReady) {
////            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lessonUtterance")
////        }
////    }
////
////    override fun onDestroy() {
////        super.onDestroy()
////        if (::tts.isInitialized) {
////            tts.stop()
////            tts.shutdown()
////        }
////    }
////}
//
//package com.chirathi.voicebridge
//
//import android.os.Bundle
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.chirathi.voicebridge.data.AnswerType
//import com.chirathi.voicebridge.data.LessonModel
//import com.chirathi.voicebridge.data.OptionModel
//import com.chirathi.voicebridge.data.MatchPairModel
//import com.chirathi.voicebridge.DrawingCanvasView
//import com.chirathi.voicebridge.data.OptionAdapter
//import com.chirathi.voicebridge.data.MatchPairAdapter
//import android.speech.tts.TextToSpeech
//import java.util.Locale
//
//class Edu_LessonDetailActivity : AppCompatActivity() {
//
//    private lateinit var tts: TextToSpeech
//    private var ttsReady = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_edu_lesson_detail)
//
//        tts = TextToSpeech(this) { status ->
//            ttsReady = status == TextToSpeech.SUCCESS
//            if (ttsReady) tts.language = Locale.getDefault()
//        }
//
//        val lesson = intent.getParcelableExtra<LessonModel>("lesson") ?: run {
//            finish(); return
//        }
//
//        val back = findViewById<View>(R.id.back)
//        back.setOnClickListener { finish() }
//
////        val ivIcon = findViewById<ImageView>(R.id.ivLessonIcon)
//        val tvTitle = findViewById<TextView>(R.id.tvLessonTitle)
//        val tvContent = findViewById<TextView>(R.id.tvLessonContent)
//        val tvQuestion = findViewById<TextView>(R.id.tvQuestion)
//        val btnHear = findViewById<Button>(R.id.btnHearQuestion)
//        val btnShow = findViewById<Button>(R.id.btnShowAnswer)
//        val btnHowTo = findViewById<Button>(R.id.btnHowTo)
//        val tvAnswer = findViewById<TextView>(R.id.tvAnswer)
//        val tvHowTo = findViewById<TextView>(R.id.tvHowTo)
//
//        val rvOptions = findViewById<RecyclerView>(R.id.rvOptions)
//        val drawingCanvas = findViewById<DrawingCanvasView>(R.id.drawingCanvas)
//        val btnClearDrawing = findViewById<Button>(R.id.btnClearDrawing)
//        val rvMatchPairs = findViewById<RecyclerView>(R.id.rvMatchPairs)
//        val tvMatchStatus = findViewById<TextView>(R.id.tvMatchStatus)
//
//        val iconId = lesson.getIconResId(this)
////        ivIcon.setImageResource(if (iconId != 0) iconId else R.drawable.lesson_icon)
//
//        tvTitle.text = lesson.lessonTitle
//        tvContent.text = lesson.lessonContent
//        tvQuestion.text = lesson.question
//
//        // Answer-type–specific UI
//        when (lesson.answerType) {
//            AnswerType.MCQ -> {
//                rvOptions.visibility = View.VISIBLE
//                rvOptions.layoutManager = LinearLayoutManager(this)
//                rvOptions.adapter = OptionAdapter(lesson.options) { chosen ->
//                    val feedback = if (chosen.isCorrect) "Correct!" else "Try again."
//                    tvAnswer.visibility = View.VISIBLE
//                    tvAnswer.text = feedback
//                    speak(feedback)
//                }
//                // Show-answer button optional
//            }
//            AnswerType.DRAW -> {
//                drawingCanvas.visibility = View.VISIBLE
//                btnClearDrawing.visibility = View.VISIBLE
//                btnClearDrawing.setOnClickListener { drawingCanvas.clearCanvas() }
//                btnShow.visibility = View.GONE // drawing is the answer
//            }
//            AnswerType.MATCH -> {
//                rvMatchPairs.visibility = View.VISIBLE
//                tvMatchStatus.visibility = View.VISIBLE
//                rvMatchPairs.layoutManager = LinearLayoutManager(this)
//                rvMatchPairs.adapter = MatchPairAdapter(lesson.matchPairs) { pair ->
//                    val msg = "Pair: ${pair.left} ↔ ${pair.right}"
//                    tvMatchStatus.text = msg
//                    speak(msg)
//                }
//                btnShow.visibility = View.GONE
//            }
//            else -> { /* TEXT: default behavior */ }
//        }
//
//        btnHear.setOnClickListener { speak("Question: ${lesson.question}") }
//
//        btnShow.setOnClickListener {
//            tvAnswer.visibility = View.VISIBLE
//            val friendlyAnswer = "The answer is: ${lesson.correctAnswer}"
//            tvAnswer.text = friendlyAnswer
//            speak(friendlyAnswer)
//        }
//
//        btnHowTo.setOnClickListener {
//            if (tvHowTo.visibility == View.VISIBLE) {
//                tvHowTo.visibility = View.GONE
//            } else {
//                val hint = if (lesson.howToSteps.isNotEmpty()) {
//                    "How to answer:\n" + lesson.howToSteps.joinToString("\n") { "• $it" }
//                } else {
//                    "How to answer:\n1. Read or listen to the question.\n2. Think carefully.\n3. Respond."
//                }
//                tvHowTo.text = hint
//                tvHowTo.visibility = View.VISIBLE
//                speak("How to answer: ${hint.replace("\n", ". ")}")
//            }
//        }
//    }
//
//    private fun speak(text: String) {
//        if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lessonUtterance")
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (::tts.isInitialized) {
//            tts.stop(); tts.shutdown()
//        }
//    }
//}

package com.chirathi.voicebridge

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chirathi.voicebridge.data.*
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.launch
import java.util.Locale

class Edu_LessonDetailActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private lateinit var geminiHelper: Edu_GeminiHelper

    private var age: String = "6"
    private var disorderType: String? = null
    private var disorderSeverity: String? = null

    private var lessons: ArrayList<LessonModel> = arrayListOf()
    private var lessonIndex: Int = 0

    companion object {
        private const val TAG = "LessonDetail_Gemini"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edu_lesson_detail)

        tts = TextToSpeech(this) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) tts.language = Locale.getDefault()
        }

        geminiHelper = Edu_GeminiHelper(ApiKeyConfig.GEMINI_API_KEY)

        val lesson = intent.getParcelableExtra<LessonModel>("lesson") ?: run {
            finish(); return
        }
        age = intent.getStringExtra("AGE_GROUP") ?: "6"
        disorderType = intent.getStringExtra("DISORDER_TYPE")
        disorderSeverity = intent.getStringExtra("DISORDER_SEVERITY")
        lessons = intent.getParcelableArrayListExtra("LESSON_LIST") ?: arrayListOf(lesson)
        lessonIndex = intent.getIntExtra("LESSON_INDEX", 0)

        val back = findViewById<View>(R.id.back)
        back.setOnClickListener { finish() }

        val tvTitle = findViewById<TextView>(R.id.tvLessonTitle)
        val tvContent = findViewById<TextView>(R.id.tvLessonContent)
        val tvQuestion = findViewById<TextView>(R.id.tvQuestion)
        val btnHear = findViewById<Button>(R.id.btnHearQuestion)
        val btnShow = findViewById<Button>(R.id.btnShowAnswer)
        val btnHowTo = findViewById<Button>(R.id.btnHowTo)
        val tvAnswer = findViewById<TextView>(R.id.tvAnswer)
        val tvHowTo = findViewById<TextView>(R.id.tvHowTo)
        val btnNext = findViewById<Button>(R.id.btnNextLesson)

        val rvOptions = findViewById<RecyclerView>(R.id.rvOptions)
        val drawingCanvas = findViewById<DrawingCanvasView>(R.id.drawingCanvas)
        val btnClearDrawing = findViewById<Button>(R.id.btnClearDrawing)
        val rvMatchPairs = findViewById<RecyclerView>(R.id.rvMatchPairs)
        val tvMatchStatus = findViewById<TextView>(R.id.tvMatchStatus)

        val etUserAnswer = findViewById<EditText>(R.id.etUserAnswer)
        val btnCheckAnswer = findViewById<Button>(R.id.btnCheckAnswer)

        tvTitle.text = lesson.lessonTitle.ifBlank { lesson.lessonHint }

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting Gemini API call...")
                val enhancedContent = geminiHelper.generateLessonContent(
                    age = age,
                    subject = lesson.subject,
                    disorderType = disorderType,
                    severity = disorderSeverity,
                    lessonTitle = lesson.lessonTitle.ifBlank { lesson.lessonHint },
                    baseContent = when {
                        lesson.lessonContent.isNotBlank() -> lesson.lessonHint
                        lesson.lessonHint.isNotBlank() -> lesson.lessonHint
                        else -> "Teach the topic in a child-friendly way."
                    }
                )

                val updatedLesson = lesson.copy(
                    lessonContent = enhancedContent.content,
                    question = enhancedContent.question,
                    correctAnswer = enhancedContent.answer,
                    howToSteps = enhancedContent.howToSteps,
                    answerType = enhancedContent.answerType,
                    options = enhancedContent.options,
                    matchPairs = enhancedContent.matchPairs
                )

                tvContent.text = updatedLesson.lessonContent
                tvQuestion.text = updatedLesson.question

                setupLessonUI(
                    updatedLesson,
                    rvOptions, drawingCanvas, btnClearDrawing,
                    rvMatchPairs, tvMatchStatus, btnShow, btnHear, btnHowTo,
                    tvAnswer, tvHowTo, etUserAnswer, btnCheckAnswer
                )

            } catch (e: Exception) {
                Log.e(TAG, "Gemini API FAILED: ${e.message}", e)
                tvContent.text = lesson.lessonContent.ifBlank { lesson.lessonHint }
                tvQuestion.text = lesson.question.ifBlank { "What did you learn?" }

                setupLessonUI(
                    lesson,
                    rvOptions, drawingCanvas, btnClearDrawing,
                    rvMatchPairs, tvMatchStatus, btnShow, btnHear, btnHowTo,
                    tvAnswer, tvHowTo, etUserAnswer, btnCheckAnswer
                )
            }
        }

        btnNext.setOnClickListener {
            val nextIndex = lessonIndex + 1
            if (nextIndex < lessons.size) {
                val nextIntent = intent.apply {
                    putExtra("lesson", lessons[nextIndex])
                    putExtra("LESSON_INDEX", nextIndex)
                    putParcelableArrayListExtra("LESSON_LIST", lessons)
                }
                finish()
                startActivity(nextIntent)
            } else {
                Toast.makeText(this, "🎉 You finished all lessons!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLessonUI(
        lesson: LessonModel,
        rvOptions: RecyclerView,
        drawingCanvas: DrawingCanvasView,
        btnClearDrawing: Button,
        rvMatchPairs: RecyclerView,
        tvMatchStatus: TextView,
        btnShow: Button,
        btnHear: Button,
        btnHowTo: Button,
        tvAnswer: TextView,
        tvHowTo: TextView,
        etUserAnswer: EditText,
        btnCheckAnswer: Button
    ) {
        // reset visibility
        rvOptions.visibility = View.GONE
        drawingCanvas.visibility = View.GONE
        btnClearDrawing.visibility = View.GONE
        rvMatchPairs.visibility = View.GONE
        tvMatchStatus.visibility = View.GONE
        etUserAnswer.visibility = View.GONE
        btnCheckAnswer.visibility = View.GONE
        btnShow.visibility = View.VISIBLE

        when (lesson.answerType) {
            AnswerType.MCQ -> {
                rvOptions.visibility = View.VISIBLE
                rvOptions.layoutManager = LinearLayoutManager(this)
                rvOptions.adapter = OptionAdapter(lesson.options) { chosen ->
                    validateAnswerWithAI(
                        lesson.question,
                        lesson.correctAnswer,
                        chosen.text,
                        tvAnswer
                    )
                }
            }
            AnswerType.DRAW -> {
                drawingCanvas.visibility = View.VISIBLE
                btnClearDrawing.visibility = View.VISIBLE
                btnClearDrawing.setOnClickListener { drawingCanvas.clearCanvas() }
                btnShow.visibility = View.GONE
            }
            AnswerType.MATCH -> {
                rvMatchPairs.visibility = View.VISIBLE
                tvMatchStatus.visibility = View.VISIBLE
                rvMatchPairs.layoutManager = LinearLayoutManager(this)
                rvMatchPairs.adapter = MatchPairAdapter(lesson.matchPairs) { pair ->
                    val msg = "Pair: ${pair.left} ↔ ${pair.right}"
                    tvMatchStatus.text = msg
                    speak(msg)
                }
                btnShow.visibility = View.GONE
            }
            else -> {
                etUserAnswer.visibility = View.VISIBLE
                btnCheckAnswer.visibility = View.VISIBLE
                btnCheckAnswer.setOnClickListener {
                    val userAnswer = etUserAnswer.text.toString().trim()
                    if (userAnswer.isEmpty()) {
                        Toast.makeText(this, "Please type an answer first", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    validateAnswerWithAI(
                        lesson.question,
                        lesson.correctAnswer,
                        userAnswer,
                        tvAnswer
                    )
                    etUserAnswer.setText("")
                }
            }
        }

        btnHear.setOnClickListener { speak("Question: ${lesson.question}") }

        btnShow.setOnClickListener {
            tvAnswer.visibility = View.VISIBLE
            val friendlyAnswer = "The answer is: ${lesson.correctAnswer}"
            tvAnswer.text = friendlyAnswer
            speak(friendlyAnswer)
        }

        btnHowTo.setOnClickListener {
            if (tvHowTo.visibility == View.VISIBLE) {
                tvHowTo.visibility = View.GONE
            } else {
                lifecycleScope.launch {
                    try {
                        val hint = geminiHelper.generateHint(
                            question = lesson.question,
                            correctAnswer = lesson.correctAnswer,
                            age = age,
                            disorderType = disorderType,
                            severity = disorderSeverity
                        )
                        tvHowTo.text = hint
                        tvHowTo.visibility = View.VISIBLE
                        speak("Hint: $hint")
                    } catch (e: Exception) {
                        val defaultHint = if (lesson.howToSteps.isNotEmpty()) {
                            "How to answer:\n" + lesson.howToSteps.joinToString("\n") { "• $it" }
                        } else {
                            "How to answer:\n1. Read or listen to the question.\n2. Think carefully.\n3. Respond."
                        }
                        tvHowTo.text = defaultHint
                        tvHowTo.visibility = View.VISIBLE
                        speak("How to answer: ${defaultHint.replace("\n", ". ")}")
                    }
                }
            }
        }
    }

    private fun validateAnswerWithAI(
        question: String,
        correctAnswer: String,
        userAnswer: String,
        tvAnswer: TextView
    ) {
        lifecycleScope.launch {
            try {
                val validation = geminiHelper.validateAnswer(
                    question = question,
                    correctAnswer = correctAnswer,
                    userAnswer = userAnswer,
                    age = age,
                    disorderType = disorderType
                )

                val feedbackText = if (validation.isCorrect) {
                    "✅ ${validation.feedback}\n${validation.encouragement}"
                } else {
                    "💡 ${validation.feedback}\n${validation.encouragement}\n\nHint: ${validation.hint}"
                }

                tvAnswer.visibility = View.VISIBLE
                tvAnswer.text = feedbackText
                speak(feedbackText)

            } catch (e: Exception) {
                val basicFeedback = if (userAnswer.equals(correctAnswer, ignoreCase = true)) {
                    "Correct! Great job!"
                } else {
                    "Not quite right. Try again!"
                }
                tvAnswer.visibility = View.VISIBLE
                tvAnswer.text = basicFeedback
                speak(basicFeedback)
            }
        }
    }

    private fun speak(text: String) {
        if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lessonUtterance")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}