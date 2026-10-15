package com.example.chess.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun EvaluationBar(
    score: Float, // Positive = White advantage, Negative = Black advantage
    isFlipped: Boolean,
    modifier: Modifier = Modifier
) {
    // Clamp score between -10.0 and +10.0 for visual bar
    val clamped = score.coerceIn(-10f, 10f)
    // White fraction from 0.0 to 1.0 (0.5 is even)
    val targetFraction = (clamped + 10f) / 20f
    val animatedFraction by animateFloatAsState(targetValue = targetFraction, label = "eval_fraction")

    val whiteFraction = if (isFlipped) 1f - animatedFraction else animatedFraction
    val blackFraction = 1f - whiteFraction

    val scoreDisplay = if (score >= 0) "+${String.format("%.1f", score)}" else String.format("%.1f", score)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF1E293B))
    ) {
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .weight(whiteFraction.coerceAtLeast(0.02f))
                    .fillMaxHeight()
                    .background(Color(0xFFF8FAFC))
            )
            Box(
                modifier = Modifier
                    .weight(blackFraction.coerceAtLeast(0.02f))
                    .fillMaxHeight()
                    .background(Color(0xFF0F172A))
            )
        }

        Text(
            text = scoreDisplay,
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color(0x99000000), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
