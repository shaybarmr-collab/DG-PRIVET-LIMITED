package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.GeneratedVideoEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoGenScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var promptInput by remember { mutableStateOf("") }
    val isGenerating by viewModel.isGeneratingVideo.collectAsState()
    val generatedVideos by viewModel.generatedVideos.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Rendering configuration states
    var selectedRatio by remember { mutableStateOf("16:9") }
    var selectedFps by remember { mutableStateOf("30") }
    var renderingMode by remember { mutableStateOf("Veo Fast") }

    // Simulated terminal output states during rendering
    var terminalStep by remember { mutableStateOf(0) }
    val terminalLogs = remember {
        listOf(
            "[SYSTEM] Initiating neural engine...",
            "[MODEL] Loading Veo-3.1-Fast video weight tables...",
            "[PARSE] Parsing semantic prompt text...",
            "[LATENT] Injecting motion-flow vector fields...",
            "[DIFFUSE] Iterating frame denoising: 10%...",
            "[DIFFUSE] Iterating frame denoising: 45%...",
            "[DIFFUSE] Iterating frame denoising: 75%...",
            "[INTERPOLATE] Generating intermediate optical flow (30 FPS)...",
            "[ENCODE] Formatting container as H.264 MP4...",
            "[SYSTEM] Render complete. Storing in local database..."
        )
    }

    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            terminalStep = 0
            while (terminalStep < terminalLogs.size - 1) {
                delay(300)
                terminalStep++
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        // Tool Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate800)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = null,
                tint = SkyBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Spark Video Engine",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 16.sp
                )
                Text(
                    text = "Text-to-Video Cinematic Creator",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Configuration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AI Video Director",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = { Text("Describe dynamic camera motions or scene details...", color = TextGray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .testTag("video_prompt_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SkyBlue,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Configuration Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Ratio Picker
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Aspect Ratio", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Slate900)
                                        .padding(2.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    listOf("16:9", "9:16").forEach { ratio ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selectedRatio == ratio) SkyBlue else Color.Transparent)
                                                .clickable { selectedRatio = ratio }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ratio,
                                                color = if (selectedRatio == ratio) Slate900 else TextWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // FPS Picker
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Frame Rate", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Slate900)
                                        .padding(2.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    listOf("24", "30").forEach { fps ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selectedFps == fps) SkyBlue else Color.Transparent)
                                                .clickable { selectedFps = fps }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${fps} FPS",
                                                color = if (selectedFps == fps) Slate900 else TextWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (promptInput.isNotBlank()) {
                                    viewModel.generateVideo(promptInput)
                                    keyboardController?.hide()
                                }
                            },
                            enabled = !isGenerating && promptInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_video_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SkyBlue,
                                contentColor = Slate900,
                                disabledContainerColor = Slate700,
                                disabledContentColor = TextGray
                            )
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Slate900, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rendering Video Assets...")
                            } else {
                                Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesize Video Stream", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Realtime rendering logs terminal
            if (isGenerating) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SkyBlue.copy(0.3f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(SkyBlue, RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LIVE RENDER CONSOLE",
                                    fontFamily = FontFamily.Monospace,
                                    color = SkyBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            // Display latest 3 steps of logs
                            val startLog = (terminalStep - 2).coerceAtLeast(0)
                            for (i in startLog..terminalStep) {
                                Text(
                                    text = terminalLogs[i],
                                    fontFamily = FontFamily.Monospace,
                                    color = if (i == terminalStep) SkyBlue else TextGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // History Header
            if (generatedVideos.isNotEmpty() && !isGenerating) {
                item {
                    Text(
                        text = "Your Cinema Lab",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(generatedVideos) { video ->
                    VideoPlayerCard(video = video)
                }
            } else if (generatedVideos.isEmpty() && !isGenerating) {
                // Empty state
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = Slate700, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No videos created yet.", color = TextGray, fontSize = 14.sp)
                            Text("Type a prompt above to start creating!", color = Slate600, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerCard(video: GeneratedVideoEntity) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0.0f) }
    val scope = rememberCoroutineScope()

    // Smooth playback progress tracker simulation
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            currentProgress = 0.0f
            while (isPlaying) {
                delay(100)
                currentProgress += 0.02f
                if (currentProgress >= 1.0f) {
                    currentProgress = 0.0f
                }
            }
        } else {
            currentProgress = 0.0f
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_card_${video.id}"),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual Player Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                // Async Poster Image representing the generated frames
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.videoUrl) // contains dynamic pollination poster
                        .crossfade(true)
                        .build(),
                    contentDescription = video.prompt,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // High-tech shimmering overlay if playing
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        SkyBlue.copy(alpha = 0.05f),
                                        SkyBlue.copy(alpha = 0.15f),
                                        SkyBlue.copy(alpha = 0.05f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Moving laser scanning line across the media card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (currentProgress * 180).dp)
                            .background(SkyBlue)
                    )
                }

                // Central Interactive Control Overlay
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .background(Color.Black.copy(0.6f), RoundedCornerShape(50))
                        .border(1.dp, SkyBlue.copy(0.4f), RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Playback Control",
                        tint = SkyBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Time duration indicators
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    val sec = (currentProgress * 5).toInt()
                    Text(
                        text = if (isPlaying) "0:0$sec / 0:05" else "0:05",
                        color = TextWhite,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Linear slider progress bar showing status
                if (isPlaying) {
                    LinearProgressIndicator(
                        progress = currentProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter),
                        color = SkyBlue,
                        trackColor = Color.Transparent
                    )
                }
            }

            // Info Section
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = video.prompt,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Rendered: 1080p • 30 FPS", color = TextGray, fontSize = 11.sp)
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Safe License",
                        tint = SkyBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
