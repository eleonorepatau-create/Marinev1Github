package fr.marine.routine.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun App(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val parent = state.screen == Screen.PARENT || state.screen == Screen.PARENT_PIN
    MarineTheme(parent = parent) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .safeDrawingPadding(),
        ) {
            when (state.screen) {
                Screen.HOME -> HomeScreen(onMarine = viewModel::openMarine, onParent = viewModel::requestParent)
                Screen.MARINE -> {
                    BackHandler(onBack = viewModel::goHome)
                    MarineScreen(state, viewModel)
                }
                Screen.PARENT_PIN -> {
                    BackHandler(onBack = viewModel::goHome)
                    PinScreen(onSubmit = viewModel::submitPin, onCancel = viewModel::goHome)
                }
                Screen.PARENT -> {
                    BackHandler(onBack = viewModel::goHome)
                    ParentScreen(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(onMarine: () -> Unit, onParent: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Bonjour", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MarineColors.Text)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onMarine,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MarineColors.Violet, contentColor = Color.White),
        ) {
            Text("🌸  Marine", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onParent,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MarineColors.ParentGreen, contentColor = Color.White),
        ) {
            Text("Parent", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Version de démonstration : tout reste sur ce téléphone.",
            color = MarineColors.TextSoft,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PinScreen(onSubmit: (String) -> Boolean, onCancel: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Espace Parent", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MarineColors.ParentGreen)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { v -> pin = v.filter(Char::isDigit).take(8); error = false },
            label = { Text("Code parent") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = error,
            supportingText = { if (error) Text("Code incorrect") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { error = !onSubmit(pin) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        ) { Text("Entrer", fontSize = 18.sp) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        ) { Text("Retour", fontSize = 18.sp) }
    }
}
