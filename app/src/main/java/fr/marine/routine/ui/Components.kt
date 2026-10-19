package fr.marine.routine.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.marine.routine.core.IsoDay

val DAY_SHORT = mapOf(1 to "Lun", 2 to "Mar", 3 to "Mer", 4 to "Jeu", 5 to "Ven", 6 to "Sam", 7 to "Dim")
val DAY_LONG = mapOf(1 to "lundi", 2 to "mardi", 3 to "mercredi", 4 to "jeudi", 5 to "vendredi", 6 to "samedi", 7 to "dimanche")

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

/** Réglage par boutons plutôt que par clavier : grands boutons, valeur lue par le lecteur d'écran. */
@Composable
fun Stepper(
    label: String,
    valueText: String,
    steps: List<Int>,
    onStep: (Int) -> Unit,
) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            steps.filter { it < 0 }.forEach { s ->
                OutlinedButton(
                    onClick = { onStep(s) },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "Moins ${-s}, $label" },
                ) { Text("$s") }
            }
            Text(
                valueText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .widthIn(min = 90.dp)
                    .padding(horizontal = 4.dp),
            )
            steps.filter { it > 0 }.forEach { s ->
                OutlinedButton(
                    onClick = { onStep(s) },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "Plus $s, $label" },
                ) { Text("+$s") }
            }
        }
    }
}

@Composable
fun DaysSelector(selected: Set<IsoDay>, onChange: (Set<IsoDay>) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..7).forEach { d ->
            val on = d in selected
            Toggle(
                text = DAY_SHORT.getValue(d),
                selected = on,
                description = "${DAY_LONG.getValue(d)} ${if (on) "inclus" else "non inclus"}",
                modifier = Modifier.weight(1f),
                onClick = { onChange(if (on) selected - d else selected + d) },
            )
        }
    }
}

@Composable
fun Toggle(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    description: String? = null,
    onClick: () -> Unit,
) {
    val m = modifier
        .heightIn(min = 48.dp)
        .semantics { if (description != null) contentDescription = description }
    if (selected) {
        Button(
            onClick = onClick,
            modifier = m,
            contentPadding = ButtonDefaults.TextButtonContentPadding,
        ) { Text(text, fontSize = 14.sp) }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = m,
            contentPadding = ButtonDefaults.TextButtonContentPadding,
        ) { Text(text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface) }
    }
}

@Composable
fun Note(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text, fontSize = 14.sp, color = color, modifier = Modifier.padding(vertical = 4.dp))
}
