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
import java.text.NumberFormat
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
    HOME("Home"), CHAT("Assistant"), AI_SETUP("AI Setup"), STUDIO("Studio"), VIDEO_AUTOMATION("Video Automation"), OPPORTUNITIES("Work Finder"), MONEY("Money"), WORKSPACE("Workspace"), CAPABILITIES("Capabilities"), CONNECTIONS("Connections"), ABOUT("About")
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
    var contentTopic by remember { mutableStateOf("") }
    var videoNiche by remember { mutableStateOf(context.getSharedPreferences("ether_video_automation", Context.MODE_PRIVATE).getString("niche", "") ?: "") }
    var videoFormat by remember { mutableStateOf(context.getSharedPreferences("ether_video_automation", Context.MODE_PRIVATE).getString("format", "Both long + short") ?: "Both long + short") }
    var videoTargets by remember { mutableStateOf(context.getSharedPreferences("ether_video_automation", Context.MODE_PRIVATE).getString("targets", "YouTube + TikTok") ?: "YouTube + TikTok") }
    var videoBlueprint by remember { mutableStateOf("") }
    var generatingVideoPlan by remember { mutableStateOf(false) }
    var contentDraft by remember { mutableStateOf("") }
    var generatingContent by remember { mutableStateOf(false) }
    var opportunityTitle by remember { mutableStateOf("") }
    var opportunityPlatform by remember { mutableStateOf("Other / direct client") }
    var opportunityBudget by remember { mutableStateOf("") }
    var opportunityUrl by remember { mutableStateOf("") }
    var opportunityDetails by remember { mutableStateOf("") }
    var opportunityProposal by remember { mutableStateOf("") }
    var analyzingOpportunity by remember { mutableStateOf(false) }
    var opportunities by remember { mutableStateOf(OpportunityStore.load(context)) }
    var financeDescription by remember { mutableStateOf("") }
    var financeAmount by remember { mutableStateOf("") }
    var financeCurrency by remember { mutableStateOf("GHS") }
    var financeSource by remember { mutableStateOf("") }
    var financePayoutMethod by remember { mutableStateOf("Bank / mobile money") }
    var financeEntries by remember { mutableStateOf(FinanceStore.load(context)) }
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
                                val history = messages.mapNotNull { line ->
                                    when {
                                        line.startsWith("You:") -> "user" to line.removePrefix("You:").trim()
                                        line.startsWith("ETHER:") -> "model" to line.removePrefix("ETHER:").trim()
                                        else -> null
                                    }
                                }.takeLast(12)
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
                                            val answer = GeminiClient.generateReply(apiKey, prompt, history)
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
            Screen.STUDIO -> {
                Text("CONTENT STUDIO", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text("Create a YouTube Short or TikTok script.", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("This drafts content only. It does not publish anything or connect to your social accounts.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(12.dp)) {
                    BasicTextField(
                        value = contentTopic,
                        onValueChange = { contentTopic = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextMain, fontSize = 14.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Cyan),
                        decorationBox = { inner ->
                            if (contentTopic.isEmpty()) Text("Enter a topic, e.g. electrical safety tips…", color = TextMuted)
                            inner()
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val topic = contentTopic.trim()
                        val key = ApiKeyVault.load(context)
                        if (key.isNullOrBlank()) {
                            contentDraft = "Open AI Setup and add your Gemini API key before generating content."
                        } else if (topic.isNotBlank() && !generatingContent) {
                            generatingContent = true
                            contentDraft = "Creating a draft…"
                            coroutineScope.launch {
                                try {
                                    contentDraft = GeminiClient.generateReply(
                                        key,
                                        "Create a ready-to-record YouTube Short / TikTok vertical-video script about: $topic. " +
                                            "Return: 3 title options, a strong first-2-second hook, a 30-45 second spoken script, " +
                                            "simple visual suggestions, a caption, 5 relevant hashtags, and a clear call to action. " +
                                            "Keep it practical, original, beginner-friendly, and avoid invented statistics or claims."
                                    )
                                } catch (error: Exception) {
                                    contentDraft = error.message ?: "Could not generate the draft. Please try again."
                                } finally {
                                    generatingContent = false
                                }
                            }
                        }
                    },
                    enabled = contentTopic.isNotBlank() && !generatingContent,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
                ) { Text(if (generatingContent) "Generating…" else "Generate script") }
                if (contentDraft.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("DRAFT", color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                                    Text(contentDraft, color = TextMain, fontSize = 13.sp, lineHeight = 20.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("ETHER content draft", contentDraft))
                                            notice = "Content draft copied to clipboard."
                                        }) { Text("Copy draft", color = Cyan) }
                                        TextButton(onClick = {
                                            val updated = listOf(WorkspaceTask(System.currentTimeMillis(), "CONTENT DRAFT — " + contentDraft, false)) + workspaceTasks
                                            workspaceTasks = updated
                                            WorkspaceStore.save(context, updated)
                                            notice = "Draft saved to Business Workspace."
                                        }, enabled = !generatingContent && !contentDraft.startsWith("Open AI Setup") && !contentDraft.startsWith("Could not generate")) { Text("Save draft", color = Cyan) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Screen.VIDEO_AUTOMATION -> {
                Text("AUTONOMOUS VIDEO FACTORY", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text("Set the rules once. Build towards automatic production.", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("This setup saves your niche and publishing targets on this phone. It can generate a detailed production blueprint with Gemini, but automatic MP4 rendering and publishing are not connected yet.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    item {
                        OpportunityInput("Channel niche / topic", videoNiche, { videoNiche = it }, "e.g. practical technology and AI tools", minLines = 2)
                    }
                    item {
                        Text("VIDEO OUTPUT", color = Cyan, fontSize = 10.sp, letterSpacing = 1.5.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Both long + short", "Long videos", "Shorts only").forEach { option ->
                                Button(
                                    onClick = { videoFormat = option },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (videoFormat == option) Cyan else PanelLight,
                                        contentColor = if (videoFormat == option) Night else TextMain
                                    )
                                ) { Text(option, fontSize = 10.sp) }
                            }
                        }
                    }
                    item {
                        Text("PUBLISHING TARGETS", color = Cyan, fontSize = 10.sp, letterSpacing = 1.5.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("YouTube + TikTok", "YouTube first").forEach { option ->
                                Button(
                                    onClick = { videoTargets = option },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (videoTargets == option) Cyan else PanelLight,
                                        contentColor = if (videoTargets == option) Night else TextMain
                                    )
                                ) { Text(option, fontSize = 11.sp) }
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                context.getSharedPreferences("ether_video_automation", Context.MODE_PRIVATE).edit()
                                    .putString("niche", videoNiche.trim())
                                    .putString("format", videoFormat)
                                    .putString("targets", videoTargets)
                                    .apply()
                                notice = "Video business settings saved on this phone."
                            },
                            enabled = videoNiche.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = PanelLight, contentColor = TextMain)
                        ) { Text("Save channel settings") }
                    }
                    item {
                        Button(
                            onClick = {
                                val key = ApiKeyVault.load(context)
                                if (key.isNullOrBlank()) {
                                    videoBlueprint = "Open AI Setup and add a Gemini API key before generating the production blueprint."
                                } else if (videoNiche.isNotBlank() && !generatingVideoPlan) {
                                    generatingVideoPlan = true
                                    videoBlueprint = "Building the production blueprint…"
                                    coroutineScope.launch {
                                        try {
                                            videoBlueprint = GeminiClient.generateReply(
                                                key,
                                                VideoProductionPlanner.buildPrompt(videoNiche, videoFormat, videoTargets)
                                            )
                                        } catch (error: Exception) {
                                            videoBlueprint = error.message ?: "Could not create the blueprint. Please try again."
                                        } finally {
                                            generatingVideoPlan = false
                                        }
                                    }
                                }
                            },
                            enabled = videoNiche.isNotBlank() && !generatingVideoPlan,
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
                        ) { Text(if (generatingVideoPlan) "Planning…" else "Generate production blueprint") }
                    }
                    item {
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.capcut.com/"))) },
                            colors = ButtonDefaults.buttonColors(containerColor = PanelLight, contentColor = TextMain)
                        ) { Text("Open CapCut to test AI video maker") }
                    }
                    item {
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://developers.google.com/youtube/v3/docs/videos/insert"))) },
                            colors = ButtonDefaults.buttonColors(containerColor = PanelLight, contentColor = TextMain)
                        ) { Text("YouTube upload API requirements") }
                    }
                    item {
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://developers.tiktok.com/docs/en/content-posting-api-get-started"))) },
                            colors = ButtonDefaults.buttonColors(containerColor = PanelLight, contentColor = TextMain)
                        ) { Text("TikTok publishing API requirements") }
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text("AUTOMATION STATUS", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Configured locally: niche, output format, target platforms.", color = TextMain, fontSize = 12.sp)
                                Text("Still required: real video-rendering integration, secure backend, account OAuth, publishing API approvals, background scheduler, and end-to-end tests.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                                Text("YouTube API projects that have not passed audit may upload videos as private. TikTok's unaudited Direct Post clients are restricted to private visibility.", color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                    if (videoBlueprint.isNotBlank()) {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("PRODUCTION BLUEPRINT", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(videoBlueprint, color = TextMain, fontSize = 12.sp, lineHeight = 18.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("ETHER video production blueprint", videoBlueprint))
                                            notice = "Production blueprint copied."
                                        }) { Text("Copy", color = Cyan) }
                                        TextButton(onClick = {
                                            val updated = listOf(WorkspaceTask(System.currentTimeMillis(), "VIDEO PRODUCTION BLUEPRINT\n\n" + videoBlueprint, false)) + workspaceTasks
                                            workspaceTasks = updated
                                            WorkspaceStore.save(context, updated)
                                            notice = "Production blueprint saved to Business Workspace."
                                        }, enabled = !generatingVideoPlan && !videoBlueprint.startsWith("Open AI Setup") && !videoBlueprint.startsWith("Could not create")) { Text("Save to Workspace", color = Cyan) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Screen.OPPORTUNITIES -> {
                Text("WORK FINDER", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(6.dp))
                Text("Find work, prepare a proposal, track the job.", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Paste a public listing or customer request. This version helps evaluate it and draft a proposal; it does not scrape websites or submit bids automatically.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OpportunityInput("Job / request title", opportunityTitle, { opportunityTitle = it }, "e.g. Write 5 short video scripts")
                    }
                    item {
                        OpportunityInput("Platform / client source", opportunityPlatform, { opportunityPlatform = it }, "e.g. TikTok, Upwork, direct client")
                    }
                    item {
                        OpportunityInput("Advertised budget (optional)", opportunityBudget, { opportunityBudget = it }, "e.g. $25 or GHS 300")
                    }
                    item {
                        OpportunityInput("Public listing URL (optional)", opportunityUrl, { opportunityUrl = it }, "Paste the listing link")
                    }
                    item {
                        OpportunityInput("Requirements / listing text", opportunityDetails, { opportunityDetails = it }, "Paste the job description and deadline…", minLines = 4)
                    }
                    item {
                        Button(
                            onClick = {
                                val query = if (opportunityTitle.isBlank()) {
                                    "remote freelance script writing caption writing content creation jobs"
                                } else {
                                    "remote freelance ${opportunityTitle.trim()} jobs"
                                }
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PanelLight, contentColor = TextMain)
                        ) { Text("Search public listings") }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    val title = opportunityTitle.trim()
                                    if (title.isNotEmpty()) {
                                        val saved = Opportunity(
                                            id = System.currentTimeMillis(),
                                            title = title,
                                            platform = opportunityPlatform.trim().ifBlank { "Other / direct client" },
                                            budget = opportunityBudget.trim(),
                                            url = opportunityUrl.trim(),
                                            details = opportunityDetails.trim()
                                        )
                                        opportunities = listOf(saved) + opportunities
                                        OpportunityStore.save(context, opportunities)
                                        opportunityProposal = ""
                                        notice = "Opportunity saved. You can now analyse it and draft a proposal."
                                    }
                                },
                                enabled = opportunityTitle.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = PanelLight, contentColor = TextMain)
                            ) { Text("Save job") }
                            Button(
                                onClick = {
                                    val key = ApiKeyVault.load(context)
                                    if (key.isNullOrBlank()) {
                                        opportunityProposal = "Open AI Setup and add a Gemini API key before generating a proposal."
                                    } else if (opportunityTitle.isNotBlank() && !analyzingOpportunity) {
                                        analyzingOpportunity = true
                                        opportunityProposal = "Reviewing opportunity and drafting proposal…"
                                        coroutineScope.launch {
                                            try {
                                                opportunityProposal = GeminiClient.generateReply(
                                                    key,
                                                    "Act as ETHER's freelance work analyst. Review this opportunity and prepare a truthful proposal. " +
                                                        "Do not claim experience, credentials, portfolio items, or past results that were not supplied. " +
                                                        "Return: (1) fit score out of 10 with reasons, (2) missing information or red flags, " +
                                                        "(3) a concise tailored proposal, (4) questions to ask the client, (5) a realistic delivery checklist, " +
                                                        "(6) a pricing suggestion only if enough information is available. " +
                                                        "Warn about requests for upfront fees, off-platform payment demands, free test work that is too large, " +
                                                        "and suspicious links. Do not promise guaranteed results. " +
                                                        "Title: ${opportunityTitle.trim()}\nSource: ${opportunityPlatform.trim()}\nBudget: ${opportunityBudget.trim()}\n" +
                                                        "URL: ${opportunityUrl.trim()}\nListing: ${opportunityDetails.trim()}"
                                                )
                                            } catch (error: Exception) {
                                                opportunityProposal = error.message ?: "Could not draft a proposal. Please try again."
                                            } finally {
                                                analyzingOpportunity = false
                                            }
                                        }
                                    }
                                },
                                enabled = opportunityTitle.isNotBlank() && !analyzingOpportunity,
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
                            ) { Text(if (analyzingOpportunity) "Working…" else "Analyse + draft") }
                        }
                    }
                    if (opportunityProposal.isNotBlank()) {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("AI REVIEW / PROPOSAL", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(opportunityProposal, color = TextMain, fontSize = 13.sp, lineHeight = 19.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("ETHER proposal", opportunityProposal))
                                            notice = "Proposal copied to clipboard."
                                        }) { Text("Copy", color = Cyan) }
                                        TextButton(onClick = {
                                            val updated = listOf(WorkspaceTask(System.currentTimeMillis(), "Freelance proposal: ${opportunityTitle.trim()}\n\n$opportunityProposal", false)) + workspaceTasks
                                            workspaceTasks = updated
                                            WorkspaceStore.save(context, updated)
                                            notice = "Proposal saved to Business Workspace."
                                        }, enabled = !opportunityProposal.startsWith("Open AI Setup") && !opportunityProposal.startsWith("Could not draft")) { Text("Save proposal", color = Cyan) }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Text("SAVED OPPORTUNITIES (${opportunities.size})", color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp)
                    }
                    items(opportunities, key = { it.id }) { item ->
                        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(item.status.uppercase(Locale.ROOT), color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(item.title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("${item.platform} · ${item.budget.ifBlank { "Budget not stated" }}", color = TextMuted, fontSize = 12.sp)
                                if (item.details.isNotBlank()) Text(item.details.take(240), color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    TextButton(onClick = {
                                        opportunityTitle = item.title
                                        opportunityPlatform = item.platform
                                        opportunityBudget = item.budget
                                        opportunityUrl = item.url
                                        opportunityDetails = item.details
                                        screen = Screen.OPPORTUNITIES
                                        notice = "Opportunity loaded into the form."
                                    }) { Text("Edit / review", color = Cyan) }
                                    TextButton(onClick = {
                                        val updated = opportunities.map { if (it.id == item.id) it.copy(status = if (it.status == "New") "Reviewed" else "New") else it }
                                        opportunities = updated
                                        OpportunityStore.save(context, updated)
                                    }) { Text(if (item.status == "New") "Mark reviewed" else "Mark new", color = TextMuted) }
                                    TextButton(onClick = {
                                        opportunities = opportunities.filterNot { it.id == item.id }
                                        OpportunityStore.save(context, opportunities)
                                        notice = "Opportunity removed."
                                    }) { Text("Delete", color = TextMuted) }
                                }
                                if (item.url.isNotBlank()) {
                                    TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url))) }) {
                                        Text("Open listing", color = Cyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Screen.MONEY -> {
                Text("EARNINGS & PAYOUT TRACKER", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(6.dp))
                Text("Track money owed, received, and how you plan to withdraw it.", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text("This is a ledger only. It does not receive, transfer, convert, or withdraw money.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
                val pendingGhs = financeEntries.filter { !it.received && it.currency == "GHS" }.sumOf { it.amount }
                val receivedGhs = financeEntries.filter { it.received && it.currency == "GHS" }.sumOf { it.amount }
                val pendingUsd = financeEntries.filter { !it.received && it.currency == "USD" }.sumOf { it.amount }
                val receivedUsd = financeEntries.filter { it.received && it.currency == "USD" }.sumOf { it.amount }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("RECEIVED", color = TextMuted, fontSize = 10.sp)
                            Text("GHS " + NumberFormat.getNumberInstance(Locale.US).format(receivedGhs), color = Cyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("USD " + NumberFormat.getNumberInstance(Locale.US).format(receivedUsd), color = Cyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("STILL OWED", color = TextMuted, fontSize = 10.sp)
                            Text("GHS " + NumberFormat.getNumberInstance(Locale.US).format(pendingGhs), color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("USD " + NumberFormat.getNumberInstance(Locale.US).format(pendingUsd), color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { OpportunityInput("Income / job description", financeDescription, { financeDescription = it }, "e.g. 3 video scripts for client") }
                    item { OpportunityInput("Amount", financeAmount, { financeAmount = it }, "e.g. 150 or 25.50") }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { financeCurrency = "GHS" }, colors = ButtonDefaults.buttonColors(containerColor = if (financeCurrency == "GHS") Cyan else PanelLight, contentColor = if (financeCurrency == "GHS") Night else TextMain)) { Text("GHS ₵") }
                            Button(onClick = { financeCurrency = "USD" }, colors = ButtonDefaults.buttonColors(containerColor = if (financeCurrency == "USD") Cyan else PanelLight, contentColor = if (financeCurrency == "USD") Night else TextMain)) { Text("USD") }
                        }
                    }
                    item { OpportunityInput("Client / platform", financeSource, { financeSource = it }, "e.g. direct client or freelance platform") }
                    item { OpportunityInput("Payout route", financePayoutMethod, { financePayoutMethod = it }, "e.g. Ghana bank, MTN MoMo, Payoneer") }
                    item {
                        Button(
                            onClick = {
                                val amount = financeAmount.trim().replace(",", "").toDoubleOrNull()
                                if (financeDescription.isNotBlank() && amount != null && amount > 0) {
                                    val entry = FinanceEntry(
                                        id = System.currentTimeMillis(),
                                        description = financeDescription.trim(),
                                        amount = amount,
                                        currency = financeCurrency,
                                        source = financeSource.trim(),
                                        payoutMethod = financePayoutMethod.trim()
                                    )
                                    financeEntries = listOf(entry) + financeEntries
                                    FinanceStore.save(context, financeEntries)
                                    financeDescription = ""
                                    financeAmount = ""
                                    financeSource = ""
                                    notice = "Earnings entry saved. Mark it received only after the money arrives."
                                }
                            },
                            enabled = financeDescription.isNotBlank() && (financeAmount.trim().replace(",", "").toDoubleOrNull() ?: 0.0) > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
                        ) { Text("Save earnings entry") }
                    }
                    item { Text("PAYMENT RECORDS (${financeEntries.size})", color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp) }
                    items(financeEntries, key = { it.id }) { entry ->
                        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(if (entry.received) "RECEIVED" else "PENDING PAYMENT", color = if (entry.received) Cyan else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(entry.description, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("${entry.currency} " + NumberFormat.getNumberInstance(Locale.US).format(entry.amount), color = Cyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                if (entry.source.isNotBlank()) Text("Source: ${entry.source}", color = TextMuted, fontSize = 12.sp)
                                if (entry.payoutMethod.isNotBlank()) Text("Payout plan: ${entry.payoutMethod}", color = TextMuted, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = {
                                        financeEntries = financeEntries.map { if (it.id == entry.id) it.copy(received = !it.received) else it }
                                        FinanceStore.save(context, financeEntries)
                                    }) { Text(if (entry.received) "Mark unpaid" else "Mark received", color = Cyan) }
                                    TextButton(onClick = {
                                        financeEntries = financeEntries.filterNot { it.id == entry.id }
                                        FinanceStore.save(context, financeEntries)
                                    }) { Text("Delete", color = TextMuted) }
                                }
                            }
                        }
                    }
                }
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
                    item { CapabilityCard("READY", "Business workspace", "Save ideas and tasks locally, mark them done, and delete them. No external actions are performed.", true) }
                    item { CapabilityCard("READY", "GHS / USD earnings tracker", "Track expected and received income, source, and intended payout route. Does not move money.", true) }
                    item { CapabilityCard(if (hasGeminiKey) "CONFIGURED" else "NEEDS SETUP", "Work Finder", "Save public job listings, assess fit and risks, draft tailored proposals, and track opportunities locally. Automatic platform scraping and applications are not enabled.", hasGeminiKey) }
                    item { CapabilityCard(if (hasGeminiKey) "CONFIGURED" else "NEEDS SETUP", "Content Studio", "Draft short-form video scripts and captions with Gemini, then copy or save them locally. Publishing is not connected.", hasGeminiKey) }
                    item { CapabilityCard("IN PROGRESS", "Video Automation", "Save a niche and targets, and generate a long + short production blueprint. Automatic rendering and publishing still require provider integrations and approvals.", false) }
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
                Text("Privacy and cost: the key is encrypted at rest using Android Keystore, but an API key used directly by a mobile app is not as secure as a private backend. Free-tier access and quotas can change. If billing is enabled, requests beyond free quota may be charged; for strict zero-cost use, do not enable billing. ETHER does not switch to another provider automatically.", color = TextMuted, fontSize = 11.sp, lineHeight = 17.sp)
            }
            Screen.ABOUT -> {
                Text("ABOUT ETHER", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(12.dp))
                Text("CREATOR", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Text("Emperor Lucian", color = TextMain, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                Text("Also known as Lucian and Alexander Ntow — one person, not separate identities.", color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp)
                Spacer(Modifier.height(12.dp))
                ConnectionCard("Organisation / leadership", "Dark Empire Leadership")
                Text("THE VISION", color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Text("ETHER is being built as a voice-first personal AI assistant to help with learning, planning, organisation, content creation, and carefully controlled business experiments.", color = TextMain, fontSize = 14.sp, lineHeight = 21.sp)
                Spacer(Modifier.height(10.dp))
                Text("PRINCIPLES", color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Text("Tell the truth about what works. Explain things simply. Prefer practical steps and zero-cost options when possible. Never claim a task succeeded without confirmation. Never spend money or make financial commitments without explicit approval.", color = TextMuted, fontSize = 13.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(10.dp))
                Text("This is an early build. Some integrations still require development, account authorisation, or API setup.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
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
            Text(name, modifier = Modifier.weight(1f), color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(status, modifier = Modifier.padding(start = 8.dp), color = TextMuted, fontSize = 12.sp)
        }
    }
}


@Composable
private fun OpportunityInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label.uppercase(Locale.ROOT), color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
        Box(
            modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = minLines,
                textStyle = androidx.compose.ui.text.TextStyle(color = TextMain, fontSize = 13.sp, lineHeight = 18.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Cyan),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(placeholder, color = TextMuted, fontSize = 13.sp)
                    inner()
                }
            )
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
