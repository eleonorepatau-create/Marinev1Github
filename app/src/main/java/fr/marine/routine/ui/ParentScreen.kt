package fr.marine.routine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.marine.routine.core.ALL_DAYS
import fr.marine.routine.core.Anchor
import fr.marine.routine.core.DEFAULT_WAKE_UP_BEFORE_TAXI_MINUTES
import fr.marine.routine.core.ParentPin
import fr.marine.routine.core.Planning
import fr.marine.routine.core.Routine
import fr.marine.routine.core.RoutineActivity
import fr.marine.routine.core.RewardSettings
import fr.marine.routine.core.Rewards
import fr.marine.routine.core.RoutineRunner
import fr.marine.routine.core.SCHOOL_DAYS
import fr.marine.routine.core.Settings
import fr.marine.routine.core.TimePhase
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private val TABS = listOf("Suivi", "Routines", "Taxi", "Réglages")

@Composable
fun ParentScreen(state: UiState, vm: MainViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MarineColors.ParentGreen)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Espace Parent", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = vm::goHome, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Accueil", color = Color.White, fontSize = 16.sp)
            }
        }
        TabRow(selectedTabIndex = tab) {
            TABS.forEachIndexed { i, title ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title, fontSize = 15.sp) })
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            when (tab) {
                0 -> TrackingTab(state, vm)
                1 -> RoutinesTab(state, vm)
                2 -> TaxiTab(state, vm)
                else -> SettingsTab(state, vm)
            }
        }
    }
}

// --- Suivi ------------------------------------------------------------------------------------

@Composable
private fun TrackingTab(state: UiState, vm: MainViewModel) {
    val dateLabel = state.today.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH))
    Text("Aujourd'hui : $dateLabel", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Note(
        "Démonstration sur un seul téléphone : les demandes d'aide et les retards s'affichent ici, " +
            "mais ne sont PAS encore envoyés à un autre téléphone.",
        color = MarineColors.OnHelp,
    )
    WeekReward(state, vm)
    val today = state.settings.routines.filter { Planning.appliesOn(it, state.today) }
    if (today.isEmpty()) Note("Aucune routine prévue aujourd'hui.")
    today.forEach { routine -> RoutineTracking(routine, state, vm) }
}

@Composable
private fun RoutineTracking(routine: Routine, state: UiState, vm: MainViewModel) {
    val run = state.day.runFor(routine.id, state.today.toString())
    val start = Planning.startTime(routine, state.settings, state.today)
    SectionCard(routine.name) {
        Note(
            when {
                start != null -> "Début automatique : ${Planning.format(start)}"
                routine.anchor is Anchor.BeforeTaxi -> "Pas d'heure de taxi aujourd'hui : démarrage manuel"
                else -> "Démarrage manuel"
            },
        )
        if (run == null) {
            Button(onClick = { vm.startRoutine(routine.id) }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Démarrer maintenant")
            }
            return@SectionCard
        }

        run.pendingHelp.forEach { help ->
            val name = run.activities.firstOrNull { it.id == help.activityId }?.name ?: "?"
            val time = java.time.Instant.ofEpochMilli(help.atMs).atZone(java.time.ZoneId.systemDefault()).toLocalTime()
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MarineColors.Help)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "✋ Demande d'aide : $name (${Planning.format(time.hour * 60 + time.minute)})",
                    color = MarineColors.OnHelp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { vm.acknowledgeHelp(routine.id) }) { Text("Vu", color = MarineColors.OnHelp) }
            }
        }

        val current = run.current
        val timing = RoutineRunner.timing(run, state.nowMs, state.settings.significantDelayMinutes)
        if (current != null && timing?.phase == TimePhase.SIGNIFICANT_DELAY) {
            Note("⏳ Retard important sur : ${current.name}", color = MarineColors.OnHelp)
        }

        run.activities.forEachIndexed { index, a ->
            val done = run.isCompleted(a.id)
            val status = when {
                done -> "Terminé"
                index == run.currentIndex -> if (run.isPaused) "En pause" else "En cours"
                else -> "À venir"
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (done) MarineColors.Done else Color.White)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(a.pictogram, fontSize = 22.sp)
                Spacer(Modifier.size(8.dp))
                Text(a.name, color = if (done) Color.White else MarineColors.Text, modifier = Modifier.weight(1f))
                Text(status, color = if (done) Color.White else MarineColors.TextSoft, fontWeight = FontWeight.SemiBold)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            if (!run.isFinished) {
                if (run.isPaused) {
                    Button(onClick = { vm.resume(routine.id) }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Reprendre") }
                } else {
                    OutlinedButton(onClick = { vm.pause(routine.id) }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Pause") }
                }
            }
            OutlinedButton(onClick = { vm.resetRun(routine.id) }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Recommencer")
            }
        }
    }
}

@Composable
private fun WeekReward(state: UiState, vm: MainViewModel) {
    val reward = state.settings.reward
    SectionCard("Semaine et récompense") {
        WeekStars(state)
        if (reward.enabled) {
            val given = Rewards.isGiven(state.day, state.today)
            Toggle(if (given) "✓ Récompense donnée cette semaine" else "Marquer la récompense comme donnée", given) {
                vm.setRewardGiven(!given)
            }
            if (reward.description.isBlank()) Note("Choisissez la récompense dans l'onglet Réglages.")
        }
    }
}

// --- Routines ---------------------------------------------------------------------------------

@Composable
private fun RoutinesTab(state: UiState, vm: MainViewModel) {
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    val editing = state.settings.routines.firstOrNull { it.id == editingId }
    if (editing != null) {
        RoutineEditor(editing, vm, onClose = { editingId = null })
        return
    }
    Note("Durées réglées selon vos indications ; modifiables à tout moment.")
    Note("Une modification s'applique au prochain démarrage de la routine, pas à celle en cours.")
    state.settings.routines.forEach { r ->
        SectionCard(r.name) {
            Note(anchorLabel(r) + " · " + daysLabel(r))
            Note(r.activities.joinToString("  ") { it.pictogram + " " + it.name })
            Button(onClick = { editingId = r.id }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Modifier") }
        }
    }
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = {
            val r = Routine(UUID.randomUUID().toString(), "Nouvelle routine", Anchor.Manual, SCHOOL_DAYS, activities = listOf(newActivity()))
            vm.updateSettings { it.copy(routines = it.routines + r) }
            editingId = r.id
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    ) { Text("+ Ajouter une routine") }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("aujourd'hui" to state.today, "demain" to state.today.plusDays(1)).forEach { (label, date) ->
            OutlinedButton(
                onClick = {
                    val r = Routine(
                        UUID.randomUUID().toString(), "Activité ponctuelle", Anchor.Manual, emptySet(),
                        oneOffDate = date.toString(), activities = listOf(newActivity()),
                    )
                    vm.updateSettings { it.copy(routines = it.routines + r) }
                    editingId = r.id
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
            ) { Text("+ Ponctuelle $label") }
        }
    }
}

private fun newActivity() = RoutineActivity(
    id = UUID.randomUUID().toString(),
    name = "Nouvelle activité",
    instruction = "C'est le moment de faire cette activité.",
    pictogram = "⭐",
    durationMinutes = 10,
)

private fun anchorLabel(r: Routine): String = when (val a = r.anchor) {
    is Anchor.BeforeTaxi -> "Lever ${a.minutesBefore} min avant le taxi"
    is Anchor.FixedTime -> "Début à ${Planning.format(a.minuteOfDay)}"
    Anchor.Manual -> "Démarrage manuel"
}

private fun daysLabel(r: Routine): String = when {
    r.oneOffDate != null -> "le ${r.oneOffDate}"
    r.days == ALL_DAYS -> "tous les jours"
    else -> r.days.sorted().joinToString(", ") { DAY_LONG.getValue(it) }
}

@Composable
private fun RoutineEditor(routine: Routine, vm: MainViewModel, onClose: () -> Unit) {
    var editingActivityId by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    fun update(transform: (Routine) -> Routine) = vm.updateSettings { s ->
        s.copy(routines = s.routines.map { if (it.id == routine.id) transform(it) else it })
    }

    val editingActivity = routine.activities.firstOrNull { it.id == editingActivityId }
    if (editingActivity != null) {
        ActivityEditor(
            activity = editingActivity,
            onPreview = vm::previewVoice,
            onSave = { a ->
                update { r -> r.copy(activities = r.activities.map { if (it.id == a.id) a else it }) }
                editingActivityId = null
            },
            onDelete = {
                update { r -> r.copy(activities = r.activities.filterNot { it.id == editingActivity.id }) }
                editingActivityId = null
            },
            onCancel = { editingActivityId = null },
        )
        return
    }

    OutlinedButton(onClick = onClose, modifier = Modifier.heightIn(min = 48.dp)) { Text("← Retour aux routines") }
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = routine.name,
        onValueChange = { v -> update { it.copy(name = v.take(60)) } },
        label = { Text("Nom de la routine") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    SectionCard("Début") {
        val anchor = routine.anchor
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Toggle("1 h avant le taxi", anchor is Anchor.BeforeTaxi, Modifier.weight(1f)) {
                update { it.copy(anchor = Anchor.BeforeTaxi(DEFAULT_WAKE_UP_BEFORE_TAXI_MINUTES)) }
            }
            Toggle("Heure fixe", anchor is Anchor.FixedTime, Modifier.weight(1f)) {
                if (anchor !is Anchor.FixedTime) update { it.copy(anchor = Anchor.FixedTime(8 * 60)) }
            }
            Toggle("Manuel", anchor is Anchor.Manual, Modifier.weight(1f)) {
                update { it.copy(anchor = Anchor.Manual) }
            }
        }
        when (anchor) {
            is Anchor.BeforeTaxi -> Note("Le lever est calculé chaque jour : heure du taxi moins 1 heure (onglet Taxi).")
            is Anchor.FixedTime -> Stepper("Heure de début", Planning.format(anchor.minuteOfDay), listOf(-60, -5, 5, 60)) { d ->
                update { it.copy(anchor = Anchor.FixedTime(Planning.clampMinute(anchor.minuteOfDay + d))) }
            }
            Anchor.Manual -> Note("La routine démarre quand Marine ou le parent appuie sur son bouton.")
        }
    }

    SectionCard("Jours") {
        if (routine.oneOffDate != null) {
            Note("Routine ponctuelle : uniquement le ${routine.oneOffDate}.")
        } else {
            DaysSelector(routine.days) { d -> update { it.copy(days = d) } }
        }
        Spacer(Modifier.height(8.dp))
        Toggle(if (routine.enabled) "Routine active" else "Routine désactivée", routine.enabled) {
            update { it.copy(enabled = !it.enabled) }
        }
    }

    SectionCard("Activités (dans l'ordre)") {
        routine.activities.forEachIndexed { i, a ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(a.pictogram, fontSize = 22.sp)
                Spacer(Modifier.size(6.dp))
                Column(Modifier.weight(1f)) {
                    Text(a.name, fontWeight = FontWeight.SemiBold)
                    Text("${a.durationMinutes} min · rappel ${reminderLabel(a)}", fontSize = 13.sp, color = MarineColors.TextSoft)
                }
                TextButton(enabled = i > 0, onClick = { update { it.copy(activities = it.activities.move(i, i - 1)) } }) { Text("↑") }
                TextButton(enabled = i < routine.activities.lastIndex, onClick = { update { it.copy(activities = it.activities.move(i, i + 1)) } }) { Text("↓") }
                TextButton(onClick = { editingActivityId = a.id }) { Text("Modifier") }
            }
        }
        Button(
            onClick = {
                val a = newActivity()
                update { it.copy(activities = it.activities + a) }
                editingActivityId = a.id
            },
            modifier = Modifier.heightIn(min = 48.dp),
        ) { Text("+ Ajouter une activité") }
    }

    Spacer(Modifier.height(12.dp))
    OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.heightIn(min = 48.dp)) {
        Text("Supprimer cette routine", color = MarineColors.OnHelp)
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Supprimer « ${routine.name} » ?") },
            text = { Text("La routine et ses activités seront supprimées de ce téléphone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.updateSettings { s -> s.copy(routines = s.routines.filterNot { it.id == routine.id }) }
                    onClose()
                }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Annuler") } },
        )
    }
}

private fun <T> List<T>.move(from: Int, to: Int): List<T> {
    if (to !in indices) return this
    val m = toMutableList()
    val item = m.removeAt(from)
    m.add(to, item)
    return m
}

private fun reminderLabel(a: RoutineActivity): String = when {
    a.reminderMinutesBefore <= 0 -> "aucun"
    a.reminderMinutesBefore >= a.durationMinutes -> "à mi-durée"
    else -> "${a.reminderMinutesBefore} min avant la fin"
}

@Composable
private fun ActivityEditor(
    activity: RoutineActivity,
    onPreview: (String) -> Unit,
    onSave: (RoutineActivity) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    var draft by remember(activity.id) { mutableStateOf(activity) }
    var confirmDelete by remember { mutableStateOf(false) }

    Text("Activité", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = draft.pictogram,
        onValueChange = { draft = draft.copy(pictogram = it.take(8)) },
        label = { Text("Pictogramme (emoji provisoire)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = draft.name,
        onValueChange = { draft = draft.copy(name = it.take(60)) },
        label = { Text("Nom affiché") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = draft.instruction,
        onValueChange = { draft = draft.copy(instruction = it.take(300)) },
        label = { Text("Consigne lue à voix haute") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedButton(onClick = { onPreview(draft.instruction) }, modifier = Modifier.heightIn(min = 48.dp)) {
        Text("🔊 Écouter la consigne")
    }
    OutlinedTextField(
        value = draft.confirmLabel.orEmpty(),
        onValueChange = { draft = draft.copy(confirmLabel = it.take(40).ifEmpty { null }) },
        label = { Text("Texte du grand bouton (vide = « J'ai terminé »)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Stepper("Durée", "${draft.durationMinutes} min", listOf(-5, -1, 1, 5)) { d ->
        draft = draft.copy(durationMinutes = (draft.durationMinutes + d).coerceIn(1, 180))
    }
    Stepper(
        "Rappel vocal avant la fin (0 = aucun)",
        "${draft.reminderMinutesBefore} min",
        listOf(-1, 1),
    ) { d -> draft = draft.copy(reminderMinutesBefore = (draft.reminderMinutesBefore + d).coerceIn(0, 60)) }
    Note(
        when {
            draft.reminderMinutesBefore <= 0 -> "Aucun rappel pour cette activité."
            draft.reminderMinutesBefore >= draft.durationMinutes ->
                "La durée est plus courte que ce délai : le rappel sera donné à mi-durée."
            else -> "Rappel ${draft.reminderMinutesBefore} min avant la fin (2 min par défaut)."
        },
    )

    SectionCard("Jours de cette activité") {
        val all = draft.days == null
        Toggle(if (all) "Tous les jours de la routine" else "Certains jours seulement", all) {
            draft = draft.copy(days = if (all) ALL_DAYS else null)
        }
        draft.days?.let { days -> DaysSelector(days) { draft = draft.copy(days = it) } }
    }

    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                onSave(
                    draft.copy(
                        name = draft.name.ifBlank { "Activité" },
                        pictogram = draft.pictogram.ifBlank { "⭐" },
                        confirmLabel = draft.confirmLabel?.takeIf { it.isNotBlank() },
                    ),
                )
            },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
        ) { Text("Enregistrer") }
        OutlinedButton(onClick = onCancel, modifier = Modifier
            .weight(1f)
            .heightIn(min = 56.dp)) { Text("Annuler") }
    }
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = { confirmDelete = true }) { Text("Supprimer cette activité", color = MarineColors.OnHelp) }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Supprimer « ${activity.name} » ?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Annuler") } },
        )
    }
}

// --- Taxi -------------------------------------------------------------------------------------

@Composable
private fun TaxiTab(state: UiState, vm: MainViewModel) {
    val taxi = state.settings.taxi
    fun updateTaxi(transform: (fr.marine.routine.core.TaxiSchedule) -> fr.marine.routine.core.TaxiSchedule) =
        vm.updateSettings { it.copy(taxi = transform(it.taxi)) }

    Note("Le lever de Marine est fixé à 1 heure avant le passage du taxi.")

    listOf("Aujourd'hui" to state.today, "Demain" to state.today.plusDays(1)).forEach { (label, date) ->
        val time = Planning.taxiTime(taxi, date)
        val overridden = taxi.overrides.containsKey(date.toString())
        SectionCard("$label (${DAY_LONG.getValue(date.dayOfWeek.value)})") {
            Text(
                if (time != null) "Taxi : ${Planning.format(time)} → lever : ${Planning.format(Planning.clampMinute(time - 60))}"
                else "Pas de taxi",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (overridden) Note("Horaire modifié pour ce jour seulement.")
            if (time != null) {
                Stepper("Changer pour ce jour seulement", Planning.format(time), listOf(-15, -5, 5, 15)) { d ->
                    updateTaxi { Planning.withTaxiOverride(it, date, Planning.clampMinute(time + d)) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (time != null) {
                    OutlinedButton(onClick = { updateTaxi { Planning.withTaxiOverride(it, date, null) } }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Pas de taxi ce jour")
                    }
                } else {
                    OutlinedButton(
                        onClick = { updateTaxi { Planning.withTaxiOverride(it, date, taxi.weekly[date.dayOfWeek.value] ?: (8 * 60)) } },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) { Text("Indiquer un taxi") }
                }
                if (overridden) {
                    TextButton(onClick = { updateTaxi { Planning.withoutTaxiOverride(it, date) } }) { Text("Horaire habituel") }
                }
            }
        }
    }

    SectionCard("Horaires habituels de la semaine") {
        Note("Utilisés chaque semaine, sauf si un jour est modifié ci-dessus.")
        (1..7).forEach { d ->
            val t = taxi.weekly[d]
            Column(Modifier.padding(vertical = 4.dp)) {
                Text(DAY_LONG.getValue(d).replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold)
                if (t != null) {
                    Stepper("Taxi du ${DAY_LONG.getValue(d)}", Planning.format(t), listOf(-15, -5, 5, 15)) { delta ->
                        updateTaxi { it.copy(weekly = it.weekly + (d to Planning.clampMinute(t + delta))) }
                    }
                    TextButton(onClick = { updateTaxi { it.copy(weekly = it.weekly - d) } }) { Text("Pas de taxi ce jour-là") }
                } else {
                    OutlinedButton(onClick = { updateTaxi { it.copy(weekly = it.weekly + (d to 8 * 60)) } }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Indiquer l'heure du taxi")
                    }
                }
            }
        }
    }
}

// --- Réglages ---------------------------------------------------------------------------------

@Composable
private fun SettingsTab(state: UiState, vm: MainViewModel) {
    val s: Settings = state.settings
    SectionCard("Retard important") {
        Stepper("Signalé après le temps prévu", "${s.significantDelayMinutes} min", listOf(-5, -1, 1, 5)) { d ->
            vm.updateSettings { it.copy(significantDelayMinutes = (it.significantDelayMinutes + d).coerceIn(1, 60)) }
        }
        Note("Quand le temps est passé, l'activité reste affichée chez Marine et n'est jamais validée automatiquement.")
    }

    SectionCard("Récompense de la semaine") {
        val r = s.reward
        fun updateReward(transform: (RewardSettings) -> RewardSettings) =
            vm.updateSettings { it.copy(reward = transform(it.reward)) }
        Toggle(if (r.enabled) "Récompense activée" else "Récompense désactivée", r.enabled) {
            updateReward { it.copy(enabled = !it.enabled) }
        }
        OutlinedTextField(
            value = r.description,
            onValueChange = { v -> updateReward { it.copy(description = v.take(80)) } },
            label = { Text("Récompense (ex. sortie au parc)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = r.pictogram,
            onValueChange = { v -> updateReward { it.copy(pictogram = v.take(8)) } },
            label = { Text("Pictogramme") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Note("Jour de la récompense")
        DaysSelector(setOf(r.day)) { days ->
            (days - r.day).firstOrNull()?.let { d -> updateReward { it.copy(day = d) } }
        }
        val goal = r.starsGoal
        Toggle(if (goal == null) "Sans objectif : toujours donnée" else "Objectif d'étoiles", goal == null) {
            updateReward { it.copy(starsGoal = if (goal == null) 20 else null) }
        }
        if (goal != null) {
            Stepper("Étoiles à réunir dans la semaine", "$goal", listOf(-5, -1, 1, 5)) { d ->
                updateReward { it.copy(starsGoal = (goal + d).coerceIn(1, 100)) }
            }
        }
        Note("Une étoile par activité confirmée, même après le temps prévu. Aucune étoile n'est retirée.")
    }

    SectionCard("Voix") {
        Stepper("Vitesse de lecture", "%.1f".format(Locale.FRENCH, s.speechRate), listOf(-1, 1)) { d ->
            vm.updateSettings { it.copy(speechRate = (it.speechRate + d * 0.1f).coerceIn(0.5f, 1.5f)) }
        }
        OutlinedButton(onClick = { vm.previewVoice("Bonjour Marine. C'est le moment de te lever.") }, modifier = Modifier.heightIn(min = 48.dp)) {
            Text("🔊 Tester la voix")
        }
        Note(
            when (state.speech) {
                fr.marine.routine.voice.Speaker.Status.READY -> "Voix française disponible."
                fr.marine.routine.voice.Speaker.Status.STARTING -> "Préparation de la voix…"
                fr.marine.routine.voice.Speaker.Status.NO_FRENCH_VOICE ->
                    "Aucune voix française installée : Paramètres Android › Synthèse vocale › installer le français."
                fr.marine.routine.voice.Speaker.Status.UNAVAILABLE -> "Synthèse vocale indisponible sur ce téléphone."
            },
        )
    }

    SectionCard("Code parent") {
        var pin by remember { mutableStateOf("") }
        Note(
            if (s.parentPin != null) "Un code protège l'espace Parent."
            else "Aucun code : Marine peut ouvrir l'espace Parent.",
        )
        OutlinedTextField(
            value = pin,
            onValueChange = { v -> pin = v.filter(Char::isDigit).take(8) },
            label = { Text("Nouveau code (4 à 8 chiffres)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = ParentPin.isValidFormat(pin),
                onClick = { vm.setParentPin(pin); pin = "" },
                modifier = Modifier.heightIn(min = 48.dp),
            ) { Text("Enregistrer le code") }
            if (s.parentPin != null) {
                OutlinedButton(onClick = { vm.setParentPin(null) }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Retirer") }
            }
        }
    }

    SectionCard("Limites de cette version") {
        Note("• Voix et minuterie : fonctionnent quand l'application est ouverte, écran allumé.")
        Note("• Application fermée ou téléphone verrouillé : le lever automatique n'a pas lieu (jalon suivant).")
        Note("• Pas encore de synchronisation entre deux téléphones ni d'alerte sur le téléphone parent.")
        Note("• Mode silencieux / Ne pas déranger : comportement à vérifier sur le téléphone de Marine.")
        Note("• Données conservées uniquement sur ce téléphone, sans sauvegarde en ligne.")
    }
}
