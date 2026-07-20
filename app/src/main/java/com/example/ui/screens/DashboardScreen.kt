package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.UserEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class SelectedTab {
    CHATBOT,
    IMAGE_GEN,
    VIDEO_GEN,
    UPGRADE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(SelectedTab.CHATBOT) }

    // Detect screen width to provide a truly adaptive website-like sidebar layout!
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val remainingDays = viewModel.getDaysRemaining(user)

    // Sidebar/Drawer navigation block content
    val sidebarContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
                .background(Slate800)
                .border(width = 1.dp, color = BorderSlate, shape = RoundedCornerShape(0.dp))
                .padding(16.dp)
        ) {
            // Sidebar Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AI Spark Suite",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextWhite,
                    letterSpacing = 1.sp
                )
            }

            Divider(color = Slate700, modifier = Modifier.padding(bottom = 16.dp))

            // User Info & 15-Day Trial Progress Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SkyBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = user?.username ?: "Guest Account",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = user?.email ?: "no-email",
                                color = TextGray,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Trial Progress Bar Section
                    if (user?.isPremium == true) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PremiumCoral.copy(0.15f))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = PremiumCoral, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pro Premium Unlocked",
                                color = PremiumCoral,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "15-Day Free Trial",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$remainingDays days left",
                                    color = if (remainingDays <= 2) PremiumCoral else SkyBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = (remainingDays / 15.0f).coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (remainingDays <= 2) PremiumCoral else SkyBlue,
                                trackColor = Slate700
                            )

                            if (remainingDays <= 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Trial expired. Please upgrade!",
                                    color = PremiumCoral,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Navigation Options
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NavigationItem(
                    title = "Spark Chatbot",
                    icon = Icons.Default.SmartToy,
                    selected = currentTab == SelectedTab.CHATBOT,
                    onClick = {
                        currentTab = SelectedTab.CHATBOT
                        scope.launch { drawerState.close() }
                    },
                    testTag = "sidebar_chatbot"
                )

                NavigationItem(
                    title = "Image Studio",
                    icon = Icons.Default.Image,
                    selected = currentTab == SelectedTab.IMAGE_GEN,
                    onClick = {
                        currentTab = SelectedTab.IMAGE_GEN
                        scope.launch { drawerState.close() }
                    },
                    testTag = "sidebar_images"
                )

                NavigationItem(
                    title = "Video Engine",
                    icon = Icons.Default.Videocam,
                    selected = currentTab == SelectedTab.VIDEO_GEN,
                    onClick = {
                        currentTab = SelectedTab.VIDEO_GEN
                        scope.launch { drawerState.close() }
                    },
                    testTag = "sidebar_videos"
                )

                NavigationItem(
                    title = "Upgrade Pro (₹50)",
                    icon = Icons.Default.Bolt,
                    selected = currentTab == SelectedTab.UPGRADE,
                    accentColor = PremiumCoral,
                    onClick = {
                        currentTab = SelectedTab.UPGRADE
                        scope.launch { drawerState.close() }
                    },
                    testTag = "sidebar_upgrade"
                )
            }

            // Interactive simulation helper block for testing trial constraints
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(1.dp, Slate700, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900.copy(0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Trial Simulator", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.simulateDayPassed() },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                        ) {
                            Text("+1 Day", fontSize = 10.sp, color = TextWhite)
                        }
                        Button(
                            onClick = { viewModel.resetTrialSimulation() },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                        ) {
                            Text("Reset Real", fontSize = 10.sp, color = TextWhite)
                        }
                    }
                }
            }

            Divider(color = Slate700, modifier = Modifier.padding(bottom = 12.dp))

            // Logout Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { viewModel.logout() }
                    .padding(vertical = 10.dp, horizontal = 12.dp)
                    .testTag("logout_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Log Out Account", color = TextGray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (isTablet) {
        // Desktop / Tablet View: Sidebar permanently pinned to the left of the screen!
        Row(modifier = Modifier.fillMaxSize().background(Slate900)) {
            sidebarContent()
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                MainContentArea(currentTab = currentTab, viewModel = viewModel)
            }
        }
    } else {
        // Mobile View: Sliding Sidebar Drawer
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Slate800,
                    modifier = Modifier.fillMaxHeight().width(280.dp)
                ) {
                    sidebarContent()
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (currentTab) {
                                    SelectedTab.CHATBOT -> "Spark Chatbot"
                                    SelectedTab.IMAGE_GEN -> "Image Studio"
                                    SelectedTab.VIDEO_GEN -> "Video Engine"
                                    SelectedTab.UPGRADE -> "Premium Center"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = 0.5.sp,
                                color = TextWhite
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = SkyBlue)
                            }
                        },
                        actions = {
                            // Mini dynamic status indicator
                            if (user?.isPremium == true) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .background(PremiumCoral.copy(0.15f), RoundedCornerShape(12.dp))
                                        .border(1.dp, PremiumCoral.copy(0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("PRO", color = PremiumCoral, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .background(SkyBlue.copy(0.15f), RoundedCornerShape(12.dp))
                                        .clickable { currentTab = SelectedTab.UPGRADE }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Trial: $remainingDays d", color = SkyBlue, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate800)
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    MainContentArea(currentTab = currentTab, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainContentArea(
    currentTab: SelectedTab,
    viewModel: MainViewModel
) {
    Crossfade(targetState = currentTab) { tab ->
        when (tab) {
            SelectedTab.CHATBOT -> ChatbotScreen(viewModel = viewModel)
            SelectedTab.IMAGE_GEN -> ImageGenScreen(viewModel = viewModel)
            SelectedTab.VIDEO_GEN -> VideoGenScreen(viewModel = viewModel)
            SelectedTab.UPGRADE -> UpgradeScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun NavigationItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    accentColor: Color = SkyBlue,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val bg = if (selected) accentColor.copy(alpha = 0.15f) else Color.Transparent
    val tint = if (selected) accentColor else TextGray
    val textStyle = if (selected) {
        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextWhite)
    } else {
        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = TextGray)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = title, style = textStyle, fontSize = 13.sp)
    }
}
