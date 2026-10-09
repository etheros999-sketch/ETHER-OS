package com.darkempire.ether

import android.Manifest
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Locale
import java.time.format.DateTimeFormatter

private val Night = Color(0xFF070B14)
private val Panel = Color(0xFF111A2A)
private val PanelLight = Color(0xFF18243A)
private val Cyan = Color(0xFF56D8F5)
private val TextMain = Color(0xFFF3F7FF)
private val TextMuted = Color(0xFF9BAAC0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Night) {
                    EtherApp()
                }
            }
        }
    }
}

private enum class Screen(val label: String) {
    HOME("Home"), CHAT("Assistant"), AI_SETUP("AI Setup"), WORKSPACE("Workspace"), CAPABILITIES("Capabilities"), CONNECTIONS("Connections")
}

@Composable
private fun EtherApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var clock by remember { mutableStateOf(LocalTime.now()) }
    var draft by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf("ETHER is ready. Add a Gemini API key in AI Setup to enable real AI replies.")) }
    var notice by remember { mutableStateOf("SYSTEM ONLINE · PROTOTYPE MODE") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var apiKeyDraft by remember { mutableStateOf("") }
    var hasGeminiKey by remember { mutableStateOf(ApiKeyVault.hasKey(context)) }
    var aiStatus by remember { mutableStateOf(if (hasGeminiKey) "Gemini key saved on this device." else "AI is not connected.") }
    var workspaceDraft by remember { mutableStateOf("") }
    var workspaceTasks by remember { mutableStateOf(WorkspaceStore.load(context)) }
    var sending by remember { mutableStateOf(false) }
    var micPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var listening by remember { mutableStateOf(false) }
    var pendingListen by remember { mutableStateOf(false) }
    var speechReady by remember { mutableStateOf(false) }
    var voiceNotice by remember { mutableStateOf("Voice input and speech playback are available on this device.") }
    val textToSpeech = remember(context) {
        TextToSpeech(context) { status -> speechReady = status == TextToSpeech.SUCCESS }
    }
    val speechRecognizer = remember(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        micPermission = granted
        if (!granted) {
            pendingListen = false
            voiceNotice = "Microphone permission was not granted."
        }
    }

    DisposableEffect(textToSpeech, speechRecognizer) {
        onDispose {
            speechRecognizer?.destroy()
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                listening = true
                voiceNotice = "Listening… speak now."
            }
            override fun onBeginningOfSpeech() { listening = true }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                listening = false
                voiceNotice = "Processing speech…"
            }
            override fun onError(error: Int) {
                listening = false
                voiceNotice = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Microphone audio error. Please try again."
                    SpeechRecognizer.ERROR_CLIENT -> "Voice input stopped. Tap Listen to try again."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "Speech recognition needs a working network connection."
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        "I didn't catch that. Tap Listen and try again."
                    else -> "Voice recognition failed. Please try again."
                }
            }
            override fun onResults(results: Bundle?) {
                val spokenText = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!spokenText.isNullOrBlank()) {
                    draft = spokenText
                    voiceNotice = "Speech captured. Review it, then tap Send."
                } else {
                    voiceNotice = "No speech was captured. Please try again."
                }
                listening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        onDispose { speechRecognizer?.cancel() }
    }

    LaunchedEffect(pendingListen, micPermission, speechRecognizer) {
        if (pendingListen && micPermission) {
            pendingListen = false
            if (speechRecognizer == null) {
                voiceNotice = "Speech recognition is not available on this device."
            } else {
                val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to ETHER")
                }
                try {
                    speechRecognizer.startListening(speechIntent)
                    listening = true
                    voiceNotice = "Starting microphone…"
                } catch (_: Exception) {
                    listening = false
                    voiceNotice = "Could not start voice input. Please try again."
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            clock = LocalTime.now()
            delay(30_000)
        }
    }

    val greeting = greetingForHour(clock.hour)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Night)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("E T H E R", color = Cyan, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("DARK EMPIRE SYSTEMS", color = TextMuted, fontSize = 10.sp, letterSpacing = 2.sp)
            }
            Box(
                modifier = Modifier
                    .background(Panel, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(clock.format(DateTimeFormatter.ofPattern("HH:mm")), color = TextMain, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Screen.values().forEach { item ->
                TextButton(
                    onClick = { screen = item },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (screen == item) Cyan else TextMuted
                    )
                ) {
                    Text(item.label, fontWeight = if (screen == item) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        when (screen) {
            Screen.HOME -> HomeScreen(greeting, notice) {
                notice = "No external actions are connected yet."
            }
            Screen.CHAT -> {
                Text("ASSISTANT CHANNEL", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                                voiceNotice = "Speech recognition is not available on this device."
                            } else if (!micPermission) {
                                pendingListen = true
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                pendingListen = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
                    ) { Text(if (listening) "Listening…" else "🎙 Listen") }
                    Button(
                        onClick = {
                            if (speechReady) {
                                textToSpeech.language = Locale.getDefault()
                                textToSpeech.speak(
                                    messages.lastOrNull { it.startsWith("ETHER:") }?.removePrefix("ETHER:")?.trim().orEmpty().ifBlank { "ETHER is ready. Configure Gemini in AI Setup to enable AI replies." },
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "ether-prototype-status"
                                )
                                voiceNotice = "Speaking the latest ETHER reply or status."
                            } else {
                                voiceNotice = "Speech engine is starting. Please try Speak again shortly."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PanelLight, contentColor = TextMain)
                    ) { Text("Speak") }
                    TextButton(onClick = {
                        speechRecognizer?.cancel()
                        textToSpeech.stop()
                        listening = false
                        pendingListen = false
                        voiceNotice = "Voice activity stopped."
                    }) { Text("Stop", color = TextMuted) }
                }
                Text(voiceNotice, color = TextMuted, fontSize = 11.sp)
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Panel),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(message, color = TextMain, modifier = Modifier.weight(1f).padding(vertical = 6.dp))
                                TextButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("ETHER message", message))
                                    notice = "Message copied to clipboard."
                                }) { Text("Copy", color = Cyan, fontSize = 11.sp) }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f).padding(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextMain, fontSize = 15.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Cyan),
                        decorationBox = { inner ->
                            if (draft.isEmpty()) Text("Type a message…", color = TextMuted)
                            inner()
                        }
                    )
                    Button(
                        onClick = {
                            val prompt = draft.trim()
                            if (prompt.isNotBlank() && !sending) {
                                messages = messages + "You: " + prompt
                                draft = ""
                                val apiKey = ApiKeyVault.load(context)
                                if (apiKey.isNullOrBlank()) {
                                    messages = messages + "ETHER: Open AI Setup and add a Gemini API key before asking me to answer."
                                    aiStatus = "AI is not connected."
                                } else {
                                    sending = true
                                    aiStatus = "Waiting for Gemini…"
                                    coroutineScope.launch {
                                        try {
                                            val answer = GeminiClient.generateReply(apiKey, prompt)
                                            messages = messages + "ETHER: " + answer
                                            aiStatus = "Gemini responded successfully."
                                        } catch (error: Exception) {
                                            messages = messages + "ETHER: " + (error.message ?: "The request failed. Please try again.")
                                            aiStatus = error.message ?: "AI request failed."
                                        } finally {
                                            sending = false
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !sending,
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
                    ) { Text(if (sending) "Wait…" else "Send") }
                }
                Spacer(Modifier.height(8.dp))
                Text(if (hasGeminiKey) "Gemini configured · requests are sent only when you tap Send" else "No AI request is sent until a key is configured", color = TextMuted, fontSize = 11.sp)
            }
            Screen.WORKSPACE -> {
                Text("BUSINESS WORKSPACE", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text("Capture ideas and organise your next steps.", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Saved locally on this phone. ETHER will not create accounts, publish content, or spend money from this screen.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(12.dp)) {
                    BasicTextField(
                        value = workspaceDraft,
                        onValueChange = { workspaceDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextMain, fontSize = 14.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Cyan),
                        decorationBox = { inner ->
                            if (workspaceDraft.isEmpty()) Text("Add an idea or task…", color = TextMuted)
                            inner()
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val title = workspaceDraft.trim()
                        if (title.isNotEmpty()) {
                            val updated = listOf(WorkspaceTask(System.currentTimeMillis(), title, false)) + workspaceTasks
                            workspaceTasks = updated
                            WorkspaceStore.save(context, updated)
                            workspaceDraft = ""
                            notice = "Workspace item saved on this phone."
                        }
                    },
                    enabled = workspaceDraft.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
                ) { Text("Save idea / task") }
                Spacer(Modifier.height(10.dp))
                if (workspaceTasks.isEmpty()) {
                    Text("No items yet. Try: Draft 3 YouTube Shorts about electrical safety.", color = TextMuted, fontSize = 13.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(workspaceTasks, key = { it.id }) { task ->
                            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(if (task.done) "COMPLETED" else "OPEN", color = if (task.done) Cyan else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(task.title, color = if (task.done) TextMuted else TextMain, fontSize = 14.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = {
                                            val updated = workspaceTasks.map { if (it.id == task.id) it.copy(done = !it.done) else it }
                                            workspaceTasks = updated
                                            WorkspaceStore.save(context, updated)
                                        }) { Text(if (task.done) "Reopen" else "Mark done", color = Cyan) }
                                        TextButton(onClick = {
                                            val updated = workspaceTasks.filterNot { it.id == task.id }
                                            workspaceTasks = updated
                                            WorkspaceStore.save(context, updated)
                                            notice = "Workspace item deleted."
                                        }) { Text("Delete", color = TextMuted) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Screen.CAPABILITIES -> {
                Text("ETHER CAPABILITIES", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text("Honest status of what works and what is still being built.", color = TextMuted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { CapabilityCard("READY", "Local time and greeting", "Uses the phone's clock and selects a greeting for the current hour.", true) }
                    item { CapabilityCard("READY", "Voice input", "Requests microphone permission and puts recognised speech into editable text when supported by the device.", true) }
                    item { CapabilityCard("READY", "Spoken status", "Uses Android text-to-speech to read ETHER's prototype status aloud.", true) }
                    item { CapabilityCard(if (hasGeminiKey) "CONFIGURED" else "NEEDS SETUP", "AI conversations", if (hasGeminiKey) "A Gemini key is saved on this device. Test the connection in AI Setup before use." else "Add your own Gemini API key in AI Setup to enable real replies.", hasGeminiKey) }
                    item { CapabilityCard("PLANNED", "Free AI provider switching", "Try configured free providers in order, handle limits, and never use paid APIs without approval.", false) }
                    item { CapabilityCard("PLANNED", "Gmail and Google Calendar", "Connect through official sign-in and permissions before carrying out approved tasks.", false) }
                    item { CapabilityCard("PLANNED", "YouTube and TikTok", "Authorise each account and add supported publishing and management actions.", false) }
                    item { CapabilityCard("PLANNED", "Business automation", "Help research, draft content, organise tasks and prepare reports. Purchases and payments remain approval-gated.", false) }
                    item { CapabilityCard("PLANNED", "Income support", "Support zero-capital business workflows; earnings cannot be guaranteed or created automatically.", false) }
                }
            }
            Screen.AI_SETUP -> {
                Text("AI PROVIDER SETUP", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(10.dp))
                Text("Connect Gemini to turn on real AI replies.", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("1. Tap Get API key. 2. Create a key in Google AI Studio. 3. Return here, paste it, and tap Save key.", color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))) },
                    colors = ButtonDefaults.buttonColors(containerColor = PanelLight, contentColor = TextMain)
                ) { Text("Get a Gemini API key") }
                Spacer(Modifier.height(12.dp))
                Text("API KEY", color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(14.dp)) {
                    BasicTextField(
                        value = apiKeyDraft,
                        onValueChange = { apiKeyDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextMain, fontSize = 14.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Cyan),
                        visualTransformation = PasswordVisualTransformation(),
                        decorationBox = { inner ->
                            if (apiKeyDraft.isEmpty()) Text("Paste API key here", color = TextMuted)
                            inner()
                        }
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            try {
                                ApiKeyVault.save(context, apiKeyDraft)
                                apiKeyDraft = ""
                                hasGeminiKey = true
                                aiStatus = "Key saved on this device. Test the connection before chatting."
                            } catch (error: Exception) {
                                aiStatus = error.message ?: "Could not save the key."
                            }
                        },
                        enabled = apiKeyDraft.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
                    ) { Text("Save key") }
                    TextButton(onClick = {
                        val key = ApiKeyVault.load(context)
                        if (key.isNullOrBlank()) {
                            aiStatus = "Save a Gemini API key first."
                        } else {
                            aiStatus = "Testing Gemini connection…"
                            coroutineScope.launch {
                                try {
                                    val answer = GeminiClient.generateReply(key, "Reply with exactly: ETHER connection test successful.")
                                    hasGeminiKey = true
                                    aiStatus = "Connection successful. Gemini replied: " + answer.take(140)
                                } catch (error: Exception) {
                                    aiStatus = error.message ?: "Connection test failed."
                                }
                            }
                        }
                    }) { Text("Test connection", color = Cyan) }
                }
                TextButton(onClick = {
                    ApiKeyVault.clear(context)
                    apiKeyDraft = ""
                    hasGeminiKey = false
                    aiStatus = "Gemini key removed from this device."
                }) { Text("Remove saved key", color = TextMuted) }
                Spacer(Modifier.height(8.dp))
                ConnectionCard("Gemini API key", if (hasGeminiKey) "Saved on device" else "Not configured")
                Text(aiStatus, color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("Privacy and cost: the key is encrypted at rest using Android Keystore, but an API key used directly by a mobile app is not as secure as a private backend. Confirm free-tier access and limits in your Google project. ETHER does not automatically switch to a paid provider.", color = TextMuted, fontSize = 11.sp, lineHeight = 17.sp)
            }
            Screen.CONNECTIONS -> {
                Text("CONNECTION CENTRE", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(14.dp))
                ConnectionCard("AI provider", if (hasGeminiKey) "Key saved · test to verify" else "Not connected")
                ConnectionCard("YouTube", "Not connected")
                ConnectionCard("TikTok", "Not connected")
                ConnectionCard("Google services", "Not connected")
                Spacer(Modifier.height(8.dp))
                Text("Connections will be implemented one at a time using official authorisation flows.", color = TextMuted, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(notice, color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun HomeScreen(greeting: String, notice: String, onAction: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("PERSONAL AI · ANDROID", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        Text(greeting, color = TextMain, fontSize = 27.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold)
        Text("Your command centre is taking shape.", color = TextMuted, fontSize = 15.sp)

        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(154.dp)
                    .background(PanelLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .background(Night, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("E", color = Cyan, fontSize = 52.sp, fontWeight = FontWeight.Light)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Panel),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("CURRENT STATUS", color = Cyan, fontSize = 11.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text("Foundation prototype", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(5.dp))
                Text("Voice input and spoken status work on supported devices. Gemini chat needs a configured key; Google and social accounts are not connected yet.", color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp)
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
                ) { Text("Check system status") }
            }
        }
        Text("FINANCIAL SAFETY GATE", color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Text("Spending and purchases require your explicit approval.", color = TextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun ConnectionCard(name: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(status, color = TextMuted, fontSize = 12.sp)
        }
    }
}


@Composable
private fun CapabilityCard(status: String, title: String, description: String, ready: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(status, color = if (ready) Cyan else TextMuted, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Text(title, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = TextMuted, fontSize = 12.sp)
        }
    }
}
