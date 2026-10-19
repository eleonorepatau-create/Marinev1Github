package fr.marine.routine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.marine.routine.core.Rewards

private val RewardGold = Color(0xFFFFF4D6)
private val OnRewardGold = Color(0xFF5A4300)

/**
 * Étoiles de la semaine. Un jour sans étoile reste neutre : pas de croix,
 * pas de rouge, rien qui ressemble à un échec.
 */
@Composable
fun WeekStars(state: UiState) {
    val days = Rewards.weekDays(state.today)
    val total = Rewards.weekStars(state.day, state.today)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "⭐ $total étoile${if (total > 1) "s" else ""} cette semaine",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MarineColors.Text,
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            days.forEach { d ->
                val n = Rewards.starsOn(state.day, d)
                val isToday = d == state.today
                Column(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isToday) MarineColors.VioletLight else Color.Transparent)
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .clearAndSetSemantics {
                            contentDescription = "${DAY_LONG.getValue(d.dayOfWeek.value)} : $n étoile${if (n > 1) "s" else ""}"
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(DAY_SHORT.getValue(d.dayOfWeek.value), fontSize = 14.sp, color = MarineColors.TextSoft)
                    Text(if (n > 0) "⭐" else "·", fontSize = 20.sp, color = MarineColors.TextSoft)
                    Text(if (n > 0) "$n" else " ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MarineColors.Text)
                }
            }
        }
        RewardLine(state, total)
    }
}

@Composable
private fun RewardLine(state: UiState, total: Int) {
    val reward = state.settings.reward
    if (!reward.enabled) return
    val what = reward.description.ifBlank { "une surprise" }
    val dayName = DAY_LONG.getValue(reward.day)
    val goal = reward.starsGoal
    val pic = reward.pictogram.ifBlank { "🎁" }
    val text = when {
        Rewards.isRewardDay(reward, state.today) && Rewards.isEarned(reward, total) ->
            "$pic C'est le jour de ta récompense : $what !"
        goal != null -> "$pic $dayName : $what ($total étoiles sur $goal)"
        else -> "$pic $dayName : $what"
    }
    Spacer(Modifier.height(10.dp))
    Text(
        text,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = OnRewardGold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RewardGold)
            .padding(12.dp),
    )
}
