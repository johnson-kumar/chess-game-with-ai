package com.example.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.AiDifficulty
import com.example.chess.model.GameMode
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor

@Composable
fun PlayerHeader(
    color: PieceColor,
    isCurrentTurn: Boolean,
    isAi: Boolean,
    aiDifficulty: AiDifficulty?,
    capturedPieces: List<Piece>,
    materialAdvantage: Int, // positive if this player has advantage
    isThinking: Boolean,
    modifier: Modifier = Modifier
) {
    val isWhite = color == PieceColor.WHITE
    val badgeColor = if (isWhite) Color(0xFFF8FAFC) else Color(0xFF1E293B)
    val badgeTextColor = if (isWhite) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val activeBorderColor = if (isCurrentTurn) Color(0xFFF59E0B) else Color.Transparent

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, activeBorderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = if (isCurrentTurn) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player Avatar & Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                        .border(1.dp, Color(0x33000000), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAi) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Player",
                            tint = badgeTextColor,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Human Player",
                            tint = badgeTextColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isAi) "AI Engine" else if (isWhite) "White" else "Black",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isAi && aiDifficulty != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = aiDifficulty.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF59E0B),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Captured pieces / material advantage
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (capturedPieces.isNotEmpty()) {
                            val capturedSymbols = capturedPieces.joinToString("") { piece ->
                                when (piece.type) {
                                    com.example.chess.model.PieceType.PAWN -> "♙"
                                    com.example.chess.model.PieceType.KNIGHT -> "♘"
                                    com.example.chess.model.PieceType.BISHOP -> "♗"
                                    com.example.chess.model.PieceType.ROOK -> "♖"
                                    com.example.chess.model.PieceType.QUEEN -> "♕"
                                    else -> ""
                                }
                            }
                            Text(
                                text = capturedSymbols,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (materialAdvantage > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+$materialAdvantage",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }

            // Turn or Thinking State
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isThinking) {
                    Text(
                        text = "Thinking...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF59E0B)
                    )
                } else if (isCurrentTurn) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "To Move",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }
        }
    }
}
