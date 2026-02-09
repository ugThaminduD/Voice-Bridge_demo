////package com.chirathi.voicebridge
////
////import android.content.Context
////import org.tensorflow.lite.Interpreter
////import java.io.FileInputStream
////import java.io.ObjectInputStream
////import java.nio.ByteBuffer
////import java.nio.ByteOrder
////import java.nio.channels.FileChannel
////
////class Edu_TaskRecommender (context: Context) {
////    private var interpreter: Interpreter
////    private var encoders: Map<String, Any>
////    private val inputSize = 3 // Number of input features
////
////    init {
////        // Load the TFLite model
////        val modelFile = context.assets.openFd("task_recommender.tflite")
////        val inputStream = FileInputStream(modelFile.fileDescriptor)
////        val modelBuffer = inputStream.channel.map(
////            FileChannel.MapMode.READ_ONLY,
////            modelFile.startOffset,
////            modelFile.declaredLength
////        )
////        interpreter = Interpreter(modelBuffer)
////
////        // Load encoders
////        val encoderStream = context.assets.open("encoders.pkl").use {
////            ObjectInputStream(it).readObject() as Map<String, Any>
////        }
////        encoders = encoderStream
////    }
////
////    fun predict(ageGroup: Int, disorderType: String, subject: String): String {
////        // Preprocess the inputs (encode and normalize)
////        val inputBuffer = ByteBuffer.allocateDirect(inputSize * 4).apply {
////            order(ByteOrder.nativeOrder())
////            putFloat(encode("ageGroup", ageGroup.toString()))
////            putFloat(encode("disorderType", disorderType))
////            putFloat(encode("subject", subject))
////        }
////
////        // Allocate output buffer
////        val outputBuffer = ByteBuffer.allocateDirect(4 * encoders["next_task"].toString().length).apply {
////            order(ByteOrder.nativeOrder())
////        }
////
////        // Make prediction
////        interpreter.run(inputBuffer, outputBuffer)
////
////        // Post-process the output
////        outputBuffer.rewind()
////        val probabilities = FloatArray(encoders["encoders"].toString().length)
////        outputBuffer.asFloatBuffer().get(probabilities)
////        val predictedIndex = probabilities.indexOfFirst { it == probabilities.maxOrNull() }
////
////        // Decode the prediction
////        return decode("next_task", predictedIndex)
////    }
////
////    private fun encode(feature: String, value: String): Float {
////        // Convert categories to encoded values
////        val encoder = encoders["encoders"] as Map<String, Int>
////        return (encoder[value] ?: -1).toFloat()
////    }
////
////    private fun decode(feature: String, index: Int): String {
////        // Convert back from encoded values to original category
////        val decoder = encoders["decoders"] as Map<String, String>
////        return decoder.entries.first { it.value.toInt() == index }.key
////    }
////}
//
//
//package com.chirathi.voicebridge
//
//import android.content.Context
//import org.tensorflow.lite.Interpreter
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import java.nio.channels.FileChannel
//import kotlin.math.max
//
//class Edu_TaskRecommender(context: Context) {
//
//    private val interpreter: Interpreter
//    private val inputFeatureCount = 3  // Number of input features: age, disorderType, subject
//    private val outputClassCount = 36 // Update based on the model's output classes (e.g., number of tasks)
//
//    init {
//        // Load the TFLite model from assets
//        val modelBuffer = loadModelFile(context, "task_recommender.tflite")
//        interpreter = Interpreter(modelBuffer)
//    }
//
//    private fun loadModelFile(context: Context, modelPath: String): ByteBuffer {
//        val assetFileDescriptor = context.assets.openFd(modelPath)
//        val fileInputStream = assetFileDescriptor.createInputStream()
//        val fileChannel = fileInputStream.channel
//        val startOffset = assetFileDescriptor.startOffset
//        val declaredLength = assetFileDescriptor.declaredLength
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//    }
//
//    fun predict(age: Int, disorderType: Int, subject: Int): Pair<Int, Float> {
//        // Prepare input buffer
//        val inputBuffer = ByteBuffer.allocateDirect(4 * inputFeatureCount).order(ByteOrder.nativeOrder())
//        inputBuffer.putFloat(age.toFloat())           // Add Age
//        inputBuffer.putFloat(disorderType.toFloat()) // Add Disorder Type Index
//        inputBuffer.putFloat(subject.toFloat())      // Add Subject Index
//
//        // Allocate output buffer
//        val outputBuffer = ByteBuffer.allocateDirect(4 * outputClassCount).order(ByteOrder.nativeOrder())
//        outputBuffer.rewind()
//
//        // Run prediction
//        interpreter.run(inputBuffer, outputBuffer)
//
//        // Extract class probabilities
//        val probabilities = FloatArray(outputClassCount)
//        outputBuffer.rewind()
//        outputBuffer.asFloatBuffer().get(probabilities)
//
//        // Identify the best class and probability
//        val bestIndex = probabilities.indices.maxBy { probabilities[it] } ?: -1
//        val bestProbability = probabilities[bestIndex]
//        return Pair(bestIndex, bestProbability)
//    }
//}


package com.chirathi.voicebridge

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class Edu_TaskRecommender(context: Context) {

    private val interpreter: Interpreter
    private val inputFeatureCount = 4  // age, disorderType, severity, subject
    private val outputClassCount = 36  // Based on your trained model

    init {
        val modelBuffer = loadModelFile(context, "task_recommender.tflite")
        interpreter = Interpreter(modelBuffer)
    }

    private fun loadModelFile(context: Context, modelPath: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val inputStream = assetFileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Predict the best lesson recommendation
     * @param age Child's age (6-10)
     * @param disorderType Encoded disorder type (0-4)
     * @param severity Encoded severity (0-2)
     * @param subject Encoded subject (0-2, defaults to 0 for Math)
     * @return Pair of (predicted class index, confidence score 0.0-1.0)
     */
    fun predict(age: Int, disorderType: Int, severity: Int, subject: Int = 0): Pair<Int, Float> {
        // Normalize age to 0-1 range
        val normalizedAge = (age - 6).toFloat() / 4.0f

        // Prepare input buffer (4 features to match model's expected 16 bytes)
        val inputBuffer = ByteBuffer.allocateDirect(4 * inputFeatureCount)
            .order(ByteOrder.nativeOrder())

        inputBuffer.putFloat(normalizedAge)
        inputBuffer.putFloat(disorderType.toFloat())
        inputBuffer.putFloat(severity.toFloat())
        inputBuffer.putFloat(subject.toFloat())

        // Allocate output buffer
        val outputBuffer = ByteBuffer.allocateDirect(4 * outputClassCount)
            .order(ByteOrder.nativeOrder())

        // Run inference
        interpreter.run(inputBuffer, outputBuffer)

        // Extract probabilities
        outputBuffer.rewind()
        val probabilities = FloatArray(outputClassCount)
        outputBuffer.asFloatBuffer().get(probabilities)

        // Find the class with highest probability
        var maxIndex = 0
        var maxProb = probabilities[0]

        for (i in 1 until probabilities.size) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                maxIndex = i
            }
        }

        return Pair(maxIndex, maxProb)
    }


//     * Close the interpreter to free resources
    fun close() {
        interpreter.close()
    }
}