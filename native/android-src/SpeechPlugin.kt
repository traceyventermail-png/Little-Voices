package com.littlevoices.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback

@CapacitorPlugin(name = "SpeechPlugin", permissions = [Permission(strings = [Manifest.permission.RECORD_AUDIO], alias = "microphone")])
class SpeechPlugin : Plugin() {
    private var recognizer: SpeechRecognizer? = null
    private var keepListening = false
    private var currentLanguage = "en-US"

    @PluginMethod
    fun available(call: PluginCall) {
        val result = JSObject()
        result.put("available", SpeechRecognizer.isRecognitionAvailable(context))
        call.resolve(result)
    }

    @PluginMethod
    fun start(call: PluginCall) {
        currentLanguage = call.getString("language", "en-US") ?: "en-US"
        if (getPermissionState("microphone") != com.getcapacitor.PermissionState.GRANTED) {
            requestPermissionForAlias("microphone", call, "permissionCallback")
            return
        }
        beginListening(call)
    }

    @PermissionCallback
    private fun permissionCallback(call: PluginCall) {
        if (getPermissionState("microphone") == com.getcapacitor.PermissionState.GRANTED) beginListening(call)
        else call.reject("Microphone permission denied")
    }

    private fun beginListening(call: PluginCall) {
        keepListening = true
        call.resolve()
        activity.runOnUiThread { startRecognizerSession() }
    }

    private fun startRecognizerSession() {
        if (!keepListening) return
        recognizer?.destroy()
        recognizer = if (Build.VERSION.SDK_INT >= 33 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context))
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        else SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onError(error: Int) {
                val data = JSObject().apply { put("code", error) }
                notifyListeners("error", data)
                val recoverable = error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                if (keepListening && recoverable) startRecognizerSession() else keepListening = false
            }
            override fun onResults(results: Bundle?) {
                notifyListeners("finalResults", toPayload(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)))
                if (keepListening) startRecognizerSession()
            }
            override fun onPartialResults(partialResults: Bundle?) {
                notifyListeners("partialResults", toPayload(partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)))
            }
        })
        recognizer?.startListening(intent)
    }

    private fun toPayload(matches: ArrayList<String>?): JSObject {
        val arr = JSArray()
        matches?.forEach { arr.put(it) }
        return JSObject().apply { put("matches", arr) }
    }

    @PluginMethod
    fun stop(call: PluginCall) {
        keepListening = false
        activity.runOnUiThread {
            recognizer?.stopListening()
            recognizer?.destroy()
            recognizer = null
        }
        call.resolve()
    }
}
