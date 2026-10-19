package fr.marine.routine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.marine.routine.core.Planning
import fr.marine.routine.core.RoutineRun
import fr.marine.routine.core.RoutineRunner
import fr.marine.routine.core.TimePhase
import fr.marine.routine.core.Timing

@Composable
fun MarineScreen(state: UiState, vm: MainViewModel) {
    // L'écran reste allumé pendant la routine (lecture vocale au premier plan).
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MarineColors.VioletLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = vm::goHome, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Accueil", color = MarineColors.VioletDark, fontSize = 16.sp)
            }
        }

        val run = state.activeRun
        val celebration = state.celebration
        when {
            celebration != null -> DoneCard(celebration)
            run != null && !run.isFinished -> CurrentActivity(run, state, vm)
            run != null -> RoutineFinished(run, state)
            else -> NothingNow(state, vm)
        }
    }
}

@Composable
private fun CurrentActivity(run: RoutineRun, state: UiState, vm: MainViewModel) {
    val activity = run.current ?: return
    val timing = RoutineRunner.timing(run, state.nowMs, state.settings.significantDelayMinutes) ?: return

    StepsStrip(run)
    Spacer(Modifier.height(16.dp))
    Text(
        activity.pictogram,
        fontSize = 110.sp,
        modifier = Modifier.clearAndSetSemantics { },
    )
    Text(
        activity.name,
        fontSize = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MarineColors.Text,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.height(8.dp))
    Text(activity.instruction, fontSize = 20.sp, color = MarineColors.TextSoft, textAlign = TextAlign.Center)
    Spacer(Modifier.height(20.dp))

    TimeBar(timing.remaining)
    val confirmLabel = activity.confirmLabel ?: "J'ai terminé"
    val hint = when {
        run.isPaused -> "Pause. On reprend bientôt."
        timing.phase != TimePhase.IN_PROGRESS -> "Prends ton temps. Appuie sur « $confirmLabel » quand c'est fait."
        run.advanceMs >= Timing.MINUTE_MS -> "🌟 Tu as un peu d'avance."
        else -> null
    }
    if (hint != null) {
        Spacer(Modifier.height(8.dp))
        Text(hint, fontSize = 18.sp, color = MarineColors.VioletDark, textAlign = TextAlign.Center)
    }
    Spacer(Modifier.height(20.dp))

    OutlinedButton(
        onClick = vm::repeatInstruction,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp),
        shape = RoundedCornerShape(20.dp),
    ) { Text("🔊  Écouter encore", fontSize = 20.sp, color = MarineColors.VioletDark) }
    Spacer(Modifier.height(12.dp))
    Button(
        onClick = vm::confirmCurrent,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MarineColors.Violet, contentColor = Color.White),
    ) { Text("✓  $confirmLabel", fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
    Spacer(Modifier.height(12.dp))
    Button(
        onClick = vm::requestHelp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MarineColors.Help, contentColor = MarineColors.OnHelp),
    ) { Text("✋  J'ai besoin d'aide", fontSize = 22.sp, fontWeight = FontWeight.SemiBold) }

    state.marineMessage?.let { message ->
        Spacer(Modifier.height(12.dp))
        Text(
            message,
            fontSize = 18.sp,
            color = MarineColors.OnHelp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MarineColors.Help)
                .padding(16.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
    Spacer(Modifier.height(16.dp))
}

/**
 * Réglette du temps restant, comme une clepsydre : pleine au début de
 * l'activité, elle se vide jusqu'à la fin du temps prévu. Aucune heure à lire.
 */
@Composable
private fun TimeBar(remaining: Float) {
    val r = remaining.coerceIn(0f, 1f)
    val description = when {
        r <= 0f -> "Le temps prévu est passé"
        r < 0.25f -> "C'est bientôt fini"
        r < 0.5f -> "Il te reste un peu de temps"
        else -> "Il te reste beaucoup de temps"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("⏳", fontSize = 28.sp, modifier = Modifier.clearAndSetSemantics { })
        Spacer(Modifier.size(8.dp))
        Box(
            Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MarineColors.VioletTrack),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(r)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MarineColors.Violet),
            )
        }
    }
}

/** Les étapes de la routine : vert = terminée (confirmée), violet = en cours. */
@Composable
private fun StepsStrip(run: RoutineRun) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        run.activities.forEachIndexed { index, a ->
            val done = run.isCompleted(a.id)
            val current = index == run.currentIndex
            val label = when {
                done -> "${a.name} : terminé"
                current -> "${a.name} : maintenant"
                else -> "${a.name} : plus tard"
            }
            Box(
                Modifier
                    .size(if (current) 52.dp else 44.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            done -> MarineColors.Done
                            current -> Color.White
                            else -> MarineColors.Upcoming
                        },
                    )
                    .border(if (current) 3.dp else 0.dp, if (current) MarineColors.Violet else Color.Transparent, CircleShape)
                    .clearAndSetSemantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                Text(if (done) "✓" else a.pictogram, fontSize = 20.sp, color = if (done) Color.White else MarineColors.Text)
            }
        }
    }
}

@Composable
private fun DoneCard(celebration: Celebration) {
    val activity = celebration.activity
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 40.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MarineColors.DoneLight)
            .border(4.dp, MarineColors.Done, RoundedCornerShape(28.dp))
            .padding(28.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(activity.pictogram, fontSize = 90.sp, modifier = Modifier.clearAndSetSemantics { })
        Text(activity.name, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MarineColors.Text, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("✓  Terminé !", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MarineColors.Done)
        Spacer(Modifier.height(8.dp))
        Text(
            celebration.feedback.message(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MarineColors.Done,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text("⭐ Une étoile de plus !", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MarineColors.Text)
    }
}

@Composable
private fun RoutineFinished(run: RoutineRun, state: UiState) {
    Spacer(Modifier.height(24.dp))
    Text("🌟", fontSize = 100.sp, modifier = Modifier.clearAndSetSemantics { })
    Text(
        "Bravo, tout est terminé !",
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MarineColors.Done,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    WeekStars(state)
    Spacer(Modifier.height(8.dp))
    run.activities.forEach { a ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MarineColors.DoneLight)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(a.pictogram, fontSize = 28.sp, modifier = Modifier.clearAndSetSemantics { })
            Spacer(Modifier.size(12.dp))
            Text(a.name, fontSize = 20.sp, color = MarineColors.Text, modifier = Modifier.weight(1f))
            Text("✓", fontSize = 24.sp, color = MarineColors.Done, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NothingNow(state: UiState, vm: MainViewModel) {
    Spacer(Modifier.height(32.dp))
    Text("🌤️", fontSize = 100.sp, modifier = Modifier.clearAndSetSemantics { })
    Text(
        "Pas d'activité pour l'instant.",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = MarineColors.Text,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    WeekStars(state)
    Spacer(Modifier.height(8.dp))
    val manual = state.settings.routines.filter { r ->
        Planning.appliesOn(r, state.today) &&
            Planning.startTime(r, state.settings, state.today) == null &&
            Planning.activitiesOn(r, state.today).isNotEmpty() &&
            state.day.runFor(r.id, state.today.toString()) == null
    }
    manual.forEach { r ->
        Button(
            onClick = { vm.startRoutine(r.id) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MarineColors.Violet, contentColor = Color.White),
        ) {
            Text("▶  ${r.name}", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}
