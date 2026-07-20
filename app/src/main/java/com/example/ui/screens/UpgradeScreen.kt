package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentStage by remember { mutableStateOf(0) } // 0: Select, 1: Details, 2: Verifying, 3: Success
    var paymentMethod by remember { mutableStateOf("UPI") } // UPI or Card
    var upiId by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    val perks = remember {
        listOf(
            Pair(Icons.Default.SmartToy, "Unlimited Direct Gemini 3.5 Chat queries"),
            Pair(Icons.Default.Image, "Unlimited UHD Image Generations (No Logos)"),
            Pair(Icons.Default.Videocam, "Unlimited Cinematic 1080p Video Renders"),
            Pair(Icons.Default.Storage, "Full Offline Room Database History Persistence"),
            Pair(Icons.Default.Speed, "No Throttling & Priority Neural Weights")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic Glowing Banners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PremiumCoral.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Card
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = PremiumCoral,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 12.dp)
            )

            Text(
                text = "UPGRADE TO PREMIUM",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                color = TextWhite,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Unlock all features forever • Standard trial constraints apply",
                color = TextGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Features Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .border(1.dp, PremiumCoral.copy(0.2f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate800.copy(0.8f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Spark Pro Perks",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    perks.forEach { perk ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(PremiumCoral.copy(0.15f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = perk.first,
                                    contentDescription = null,
                                    tint = PremiumCoral,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = perk.second,
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Divider(color = Slate700, modifier = Modifier.padding(vertical = 16.dp))

                    // Special Price Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "One-Time Payment", color = TextGray, fontSize = 11.sp)
                            Text(text = "Lifetime License", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹500",
                                color = TextGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                style = LocalTextStyle.current.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹50",
                                color = PremiumCoral,
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (currentUser?.isPremium == true) {
                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SkyBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Account Premium Active!", fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                    } else {
                        Button(
                            onClick = {
                                paymentStage = 0
                                showPaymentDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("upgrade_checkout_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PremiumCoral,
                                contentColor = TextWhite
                            )
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pay ₹50 & Unlock Features", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }

    // Dynamic Payment Checkout Overlay Dialog
    if (showPaymentDialog) {
        Dialog(onDismissRequest = { if (paymentStage != 2) showPaymentDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (paymentStage) {
                        0 -> { // Select payment option
                            Text("Secure Payment Gateway", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
                            Text("Choose your preferred billing method to pay ₹50", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 20.dp))

                            // UPI choice
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        paymentMethod = "UPI"
                                        paymentStage = 1
                                    }
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = SkyBlue)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("BHIM UPI / GPay / PhonePe", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                                        Text("Instant checkout via your UPI ID", color = TextGray, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Card choice
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        paymentMethod = "Card"
                                        paymentStage = 1
                                    }
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = NeonIndigo)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Credit / Debit / ATM Card", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                                        Text("All major Indian bank cards supported", color = TextGray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        1 -> { // Detail Inputs
                            Text(text = if (paymentMethod == "UPI") "Enter UPI Credentials" else "Enter Card Credentials", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
                            Text(text = "Amount Payable: ₹50.00", color = SkyBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 20.dp))

                            if (paymentMethod == "UPI") {
                                OutlinedTextField(
                                    value = upiId,
                                    onValueChange = { upiId = it },
                                    label = { Text("UPI Address") },
                                    placeholder = { Text("e.g. mobile@paytm") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 20.dp)
                                        .testTag("upi_id_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SkyBlue, unfocusedBorderColor = Slate700, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )
                            } else {
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { cardNumber = it },
                                    label = { Text("Card Number") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .testTag("card_number_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonIndigo, unfocusedBorderColor = Slate700, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = cardExpiry,
                                        onValueChange = { cardExpiry = it },
                                        label = { Text("Expiry (MM/YY)") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = 20.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonIndigo, unfocusedBorderColor = Slate700, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                    )
                                    OutlinedTextField(
                                        value = cardCvv,
                                        onValueChange = { cardCvv = it },
                                        label = { Text("CVV") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = 20.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonIndigo, unfocusedBorderColor = Slate700, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    paymentStage = 2
                                },
                                enabled = (paymentMethod == "UPI" && upiId.isNotBlank()) || (paymentMethod == "Card" && cardNumber.isNotBlank()),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("confirm_pay_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = PremiumCoral, contentColor = TextWhite),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Pay Securely ₹50", fontWeight = FontWeight.Bold)
                            }
                        }

                        2 -> { // Verifying Progress
                            // Launch simulated bank query delay
                            LaunchedEffect(Unit) {
                                delay(3000)
                                viewModel.upgradeToPremium()
                                paymentStage = 3
                            }

                            CircularProgressIndicator(modifier = Modifier.size(48.dp), color = PremiumCoral)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("Contacting Bank Server...", fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Authorizing secure 3D payment for ₹50...", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }

                        3 -> { // Success Screen
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("PAYMENT SUCCESSFUL!", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
                            Text("Your AI Spark Account is upgraded to Premium.", color = TextGray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 4.dp))

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    showPaymentDialog = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = Slate900),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Let's Create!", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
