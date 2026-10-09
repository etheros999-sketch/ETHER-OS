package com.darkempire.ether

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalTime
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
    HOME("Home"), CHAT("Assistant"), CONNECTIONS("Connections")
}

@Composable
private fun EtherApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var clock by remember { mutableStateOf(LocalTime.now()) }
    var draft by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf("ETHER prototype ready. AI is not connected yet.")) }
    var notice by remember { mutableStateOf("SYSTEM ONLINE · PROTOTYPE MODE") }

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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Panel),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(message, color = TextMain, modifier = Modifier.padding(14.dp))
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
                            if (draft.isNotBlank()) {
                                messages = messages + "You: " + draft.trim() + "\nETHER: The AI service is not connected in this prototype yet."
                                draft = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night)
                    ) { Text("Send") }
                }
                Spacer(Modifier.height(8.dp))
                Text("Local prototype only · no AI request is sent", color = TextMuted, fontSize = 11.sp)
            }
            Screen.CONNECTIONS -> {
                Text("CONNECTION CENTRE", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(14.dp))
                ConnectionCard("AI provider", "Not connected")
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
                Text("Interface scaffold is in place. AI, microphone, speech playback and account connections still need implementation.", color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp)
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
