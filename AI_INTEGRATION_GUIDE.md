# Voice Bridge AI Integration - Quick Setup Guide

## ✅ What's Been Set Up

### 1. Flask API Server
**Status:** ✅ Running on `http://localhost:5001`
- Chatbot endpoint: `POST /api/chat`
- Recommender endpoint: `POST /api/recommend/age`
- Text search endpoint: `POST /api/recommend/text`

### 2. Models Created
- ✅ Chatbot Model: `chatbot_model.h5` (733 KB)
- ✅ Recommender Models: `therapy_data.pkl`, `tfidf_model.pkl`, `tfidf_matrix.pkl`
- ✅ Symbol Classifier: `symbol_classifier.h5` (11 MB) - **Copied to Android assets**
- ✅ Symbol Labels: `symbol_labels.json` (2.9 KB) - **Copied to Android assets**

### 3. Android Integration Files Created
- ✅ `VoiceBridgeApi.kt` - API interface
- ✅ `ApiModels.kt` - Request/response models
- ✅ `ApiClient.kt` - Retrofit client
- ✅ `AIRepository.kt` - Repository for API calls
- ✅ `AIIntegrationDemoActivity.kt` - Demo activity
- ✅ Retrofit dependencies added to `build.gradle.kts`

---

## 🚀 How to Test the Integration

### Step 1: Verify Flask API is Running
Open a new terminal and test:
```bash
curl -X POST http://localhost:5001/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello"}'
```

Expected response:
```json
{
  "response": "Hello! How can I help you today?",
  "intent": "greeting"
}
```

### Step 2: Sync Gradle
1. Open Android Studio
2. Click "Sync Now" when prompted
3. Wait for Gradle sync to complete

### Step 3: Update API URL for Testing

**For Android Emulator (default):**
- Already configured: `http://10.0.2.2:5001`
- No changes needed

**For Physical Device:**
1. Find your Mac's IP address:
   ```bash
   ipconfig getifaddr en0
   ```
   Example output: `192.168.1.137`

2. Edit `ApiClient.kt` and uncomment the physical device line:
   ```kotlin
   // private const val BASE_URL = "http://10.0.2.2:5001/"
   private const val BASE_URL = "http://192.168.1.137:5001/"  // Update with your IP
   ```

### Step 4: Run the App
1. Build and run the app
2. Navigate to `Education_therapyActivity`
3. The existing `navigateToRecommendListActivity` function will use AI recommendations

---

## 📱 How to Use the AI Features

### Feature 1: Chatbot
```kotlin
// In your activity
private val aiRepository = AIRepository()

lifecycleScope.launch {
    val response = aiRepository.sendChatMessage("How do I help my child with stuttering?")
    if (response != null) {
        println("Bot: ${response.response}")
        println("Intent: ${response.intent}")
    }
}
```

### Feature 2: Task Recommender (By Age)
```kotlin
lifecycleScope.launch {
    val tasks = aiRepository.getRecommendationsByAge(
        age = 6,
        disorder = "Stuttering"
    )
    
    tasks.forEach { task ->
        println("${task.title}: ${task.description}")
        println("Duration: ${task.duration}")
        println("Materials: ${task.materials}")
    }
}
```

### Feature 3: Task Recommender (By Text)
```kotlin
lifecycleScope.launch {
    val tasks = aiRepository.getRecommendationsByText(
        description = "breathing exercises for speech",
        topN = 3
    )
    
    tasks.forEach { task ->
        println("${task.title} (similarity: ${task.similarity})")
    }
}
```

### Feature 4: Symbol Classifier (On-Device)
The symbol classifier model is already in your assets. You can load it with TensorFlow Lite:

```kotlin
// Create SymbolClassifier.kt (optional - if you need symbol recognition)
import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SymbolClassifier(context: Context) {
    private val interpreter: Interpreter
    
    init {
        val model = loadModelFile(context, "models/symbol_classifier.h5")
        interpreter = Interpreter(model)
    }
    
    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    fun classify(bitmap: Bitmap): String {
        // Preprocessing and inference code here
        return "symbol_name"
    }
}
```

---

## 🔧 Current Integration in Your App

### Education_therapyActivity.kt
Your existing `navigateToRecommendListActivity()` function already uses the ML recommender:
- ✅ Fetches user age from Firestore
- ✅ Uses `Edu_TaskRecommender` for predictions
- ✅ Shows AI confidence score
- ✅ Navigates to recommended lessons

**To add API-based recommendations**, you can enhance it:

```kotlin
private fun navigateToRecommendListActivity(ageGroup: String) {
    if (disorderType == null || disorderSeverity == null) {
        Toast.makeText(this, "Disorder type not set!", Toast.LENGTH_SHORT).show()
        return
    }

    Toast.makeText(this, "🤖 Getting AI recommendations...", Toast.LENGTH_SHORT).show()

    lifecycleScope.launch {
        try {
            // Get recommendations from cloud API
            val recommendations = aiRepository.getRecommendationsByAge(
                age = ageGroup.toIntOrNull() ?: 6,
                disorder = disorderType ?: "Stuttering"
            )

            if (recommendations.isNotEmpty()) {
                // Show recommendations in a dialog or list
                showRecommendationsDialog(recommendations)
            } else {
                Toast.makeText(this@Education_therapyActivity, 
                    "No recommendations found", 
                    Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this@Education_therapyActivity, 
                "API Error: ${e.message}", 
                Toast.LENGTH_SHORT).show()
        }
    }
}

private fun showRecommendationsDialog(tasks: List<TherapyTask>) {
    // Create a dialog or navigate to a list activity
    val intent = Intent(this, RecommendationsListActivity::class.java)
    intent.putExtra("RECOMMENDATIONS", ArrayList(tasks))
    startActivity(intent)
}
```

---

## 🧪 Testing Checklist

- [ ] Flask API server running (port 5001)
- [ ] Gradle sync completed
- [ ] API URL configured correctly (emulator or device)
- [ ] Internet permission in AndroidManifest.xml (already added)
- [ ] Test chatbot: Send "Hello" and verify response
- [ ] Test recommender: Request tasks for age 6, disorder "Stuttering"
- [ ] Check LogCat for API responses

---

## 📊 API Response Examples

### Chatbot Response
```json
{
  "response": "For stuttering therapy, try breathing exercises and slow speech practice.",
  "intent": "therapy_advice"
}
```

### Recommender Response
```json
{
  "recommendations": [
    {
      "title": "Breath Control for Fluency",
      "description": "Practice deep breathing to improve speech fluency",
      "age_group": "6-8 years",
      "disorder": "Stuttering",
      "activity": "Breathing exercises with visual cues",
      "materials": "Timer, mirror, visual breathing chart",
      "duration": "15-20 minutes",
      "tips": "Start with short sessions and gradually increase duration"
    }
  ]
}
```

---

## 🔍 Troubleshooting

### Issue: "Connection refused" error
**Solution:** Verify Flask API is running:
```bash
curl http://localhost:5001/api/chat
```

### Issue: "Unable to resolve host"
**Solution:** 
- Emulator: Use `10.0.2.2` instead of `localhost`
- Physical device: Use Mac's IP address (find with `ipconfig getifaddr en0`)

### Issue: Model files not found
**Solution:** Clean and rebuild project:
```bash
./gradlew clean
./gradlew build
```

### Issue: Gradle sync fails
**Solution:** Check internet connection and invalidate caches:
- Android Studio → File → Invalidate Caches / Restart

---

## 📝 Next Steps

1. **Test the integration** using the checklist above
2. **Create UI** for displaying recommendations (optional)
3. **Add error handling** for network failures
4. **Implement caching** for offline support
5. **Add loading indicators** for better UX

---

## 🎉 Summary

You now have:
- ✅ Flask API running with chatbot and recommender
- ✅ Android API client configured
- ✅ Repository pattern for clean architecture
- ✅ Symbol classifier model in Android assets
- ✅ Example code for all three AI features
- ✅ Integration ready for testing

**Server Status:** 🟢 Running on http://localhost:5001
**Models Status:** 🟢 All models trained and deployed
**Android Status:** 🟢 Integration code ready

Happy coding! 🚀
