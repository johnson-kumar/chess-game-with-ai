package com.example.chess.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.BoardThemeType
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.Position

@Composable
fun ChessBoard(
    gameState: GameState,
    selectedPos: Position?,
    legalMovesForSelected: List<Move>,
    hintMove: Move?,
    boardTheme: BoardThemeType,
    isFlipped: Boolean,
    onSquareClick: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    val lightSquareColor = Color(boardTheme.lightSquareHex)
    val darkSquareColor = Color(boardTheme.darkSquareHex)
    val lastMoveHighlightColor = Color(boardTheme.highlightHex)

    // Pulsing animation for king in check
    val infiniteTransition = rememberInfiniteTransition(label = "check_pulse")
    val checkPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "check_alpha"
    )

    val validTargets = remember(legalMovesForSelected) {
        legalMovesForSelected.map { it.to }.toSet()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .testTag("chess_board")
    ) {
        val boardSize = maxWidth
        val squareSize = boardSize / 8

        Column(modifier = Modifier.fillMaxSize()) {
            val rowRange = if (isFlipped) (7 downTo 0) else (0..7)
            val colRange = if (isFlipped) (7 downTo 0) else (0..7)

            for (r in rowRange) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (c in colRange) {
                        val pos = Position(r, c)
                        val isLight = pos.isLightSquare
                        val baseSquareColor = if (isLight) lightSquareColor else darkSquareColor
                        val piece = gameState.getPiece(pos)

                        val isSelected = selectedPos == pos
                        val isValidTarget = validTargets.contains(pos)
                        val isLastMoveSquare = gameState.lastMove?.from == pos || gameState.lastMove?.to == pos
                        val isCheckedKing = gameState.checkedKingPos == pos
                        val isHintSquare = hintMove?.from == pos || hintMove?.to == pos

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(baseSquareColor)
                                .then(
                                    if (isLastMoveSquare) {
                                        Modifier.background(lastMoveHighlightColor)
                                    } else Modifier
                                )
                                .then(
                                    if (isCheckedKing) {
                                        Modifier.background(Color(0xFFFF0055).copy(alpha = checkPulseAlpha))
                                    } else Modifier
                                )
                                .then(
                                    if (isHintSquare) {
                                        Modifier.border(2.5.dp, Color(0xFF10B981))
                                    } else Modifier
                                )
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, Color(0xFFF59E0B))
                                    } else Modifier
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onSquareClick(pos)
                                }
                                .testTag("square_${pos.algebraic}"),
                            contentAlignment = Alignment.Center
                        ) {
                            // File labels on the bottom rank
                            val isBottomRank = if (isFlipped) r == 0 else r == 7
                            val isLeftFile = if (isFlipped) c == 7 else c == 0

                            if (isBottomRank) {
                                Text(
                                    text = pos.file.toString(),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 3.dp, bottom = 1.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLight) darkSquareColor else lightSquareColor
                                )
                            }
                            if (isLeftFile) {
                                Text(
                                    text = pos.rank.toString(),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 3.dp, top = 1.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLight) darkSquareColor else lightSquareColor
                                )
                            }

                            // Render chess piece
                            if (piece != null) {
                                ChessPieceView(
                                    piece = piece,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Legal move indicators
                            if (isValidTarget) {
                                if (piece != null || gameState.enPassantTarget == pos) {
                                    // Capture indicator: outer ring
                                    Canvas(modifier = Modifier.fillMaxSize(0.92f)) {
                                        drawCircle(
                                            color = Color(0x66000000),
                                            radius = size.minDimension / 2f,
                                            style = Stroke(width = size.minDimension * 0.12f)
                                        )
                                    }
                                } else {
                                    // Empty square target dot
                                    Box(
                                        modifier = Modifier
                                            .size(squareSize * 0.32f)
                                            .clip(CircleShape)
                                            .background(Color(0x55000000))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
