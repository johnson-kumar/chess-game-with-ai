package com.example.chess.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType

@Composable
fun ChessPieceView(
    piece: Piece,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.85f)) {
            val isWhite = piece.color == PieceColor.WHITE
            val fillColor = if (isWhite) Color(0xFFF8FAFC) else Color(0xFF1E293B)
            val strokeColor = if (isWhite) Color(0xFF0F172A) else Color(0xFFF1F5F9)
            val accentColor = if (isWhite) Color(0xFFCBD5E1) else Color(0xFF475569)

            val strokeWidth = size.width * 0.05f

            when (piece.type) {
                PieceType.PAWN -> drawPawn(fillColor, strokeColor, accentColor, strokeWidth)
                PieceType.KNIGHT -> drawKnight(fillColor, strokeColor, accentColor, strokeWidth)
                PieceType.BISHOP -> drawBishop(fillColor, strokeColor, accentColor, strokeWidth)
                PieceType.ROOK -> drawRook(fillColor, strokeColor, accentColor, strokeWidth)
                PieceType.QUEEN -> drawQueen(fillColor, strokeColor, accentColor, strokeWidth)
                PieceType.KING -> drawKing(fillColor, strokeColor, accentColor, strokeWidth)
            }
        }
    }
}

private fun DrawScope.drawPawn(fill: Color, stroke: Color, accent: Color, strokeWidth: Float) {
    val w = size.width
    val h = size.height

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.22f, h * 0.88f)
        lineTo(w * 0.78f, h * 0.88f)
        lineTo(w * 0.72f, h * 0.78f)
        lineTo(w * 0.28f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Body
    val bodyPath = Path().apply {
        moveTo(w * 0.32f, h * 0.78f)
        cubicTo(w * 0.38f, h * 0.60f, w * 0.40f, h * 0.50f, w * 0.42f, h * 0.44f)
        lineTo(w * 0.58f, h * 0.44f)
        cubicTo(w * 0.60f, h * 0.50f, w * 0.62f, h * 0.60f, w * 0.68f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, fill)
    drawPath(bodyPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Head collar
    drawOval(
        color = fill,
        topLeft = Offset(w * 0.36f, h * 0.40f),
        size = Size(w * 0.28f, h * 0.08f)
    )
    drawOval(
        color = stroke,
        topLeft = Offset(w * 0.36f, h * 0.40f),
        size = Size(w * 0.28f, h * 0.08f),
        style = Stroke(strokeWidth)
    )

    // Head circle
    drawCircle(
        color = fill,
        radius = w * 0.17f,
        center = Offset(w * 0.50f, h * 0.27f)
    )
    drawCircle(
        color = stroke,
        radius = w * 0.17f,
        center = Offset(w * 0.50f, h * 0.27f),
        style = Stroke(strokeWidth)
    )
}

private fun DrawScope.drawKnight(fill: Color, stroke: Color, accent: Color, strokeWidth: Float) {
    val w = size.width
    val h = size.height

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        lineTo(w * 0.80f, h * 0.88f)
        lineTo(w * 0.74f, h * 0.78f)
        lineTo(w * 0.26f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Horse head & mane
    val horsePath = Path().apply {
        moveTo(w * 0.30f, h * 0.78f)
        cubicTo(w * 0.32f, h * 0.65f, w * 0.25f, h * 0.58f, w * 0.22f, h * 0.48f)
        cubicTo(w * 0.19f, h * 0.40f, w * 0.24f, h * 0.34f, w * 0.30f, h * 0.34f)
        cubicTo(w * 0.33f, h * 0.28f, w * 0.38f, h * 0.18f, w * 0.48f, h * 0.14f)
        cubicTo(w * 0.58f, h * 0.12f, w * 0.68f, h * 0.16f, w * 0.72f, h * 0.24f)
        cubicTo(w * 0.74f, h * 0.32f, w * 0.76f, h * 0.45f, w * 0.74f, h * 0.60f)
        lineTo(w * 0.72f, h * 0.78f)
        close()
    }
    drawPath(horsePath, fill)
    drawPath(horsePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Eye
    drawCircle(
        color = stroke,
        radius = w * 0.04f,
        center = Offset(w * 0.42f, h * 0.32f)
    )

    // Mane notch
    drawLine(
        color = accent,
        start = Offset(w * 0.64f, h * 0.28f),
        end = Offset(w * 0.55f, h * 0.35f),
        strokeWidth = strokeWidth * 0.8f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = accent,
        start = Offset(w * 0.68f, h * 0.42f),
        end = Offset(w * 0.58f, h * 0.48f),
        strokeWidth = strokeWidth * 0.8f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawBishop(fill: Color, stroke: Color, accent: Color, strokeWidth: Float) {
    val w = size.width
    val h = size.height

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        lineTo(w * 0.80f, h * 0.88f)
        lineTo(w * 0.72f, h * 0.78f)
        lineTo(w * 0.28f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Mitre / Body
    val bodyPath = Path().apply {
        moveTo(w * 0.30f, h * 0.78f)
        cubicTo(w * 0.34f, h * 0.65f, w * 0.32f, h * 0.45f, w * 0.38f, h * 0.32f)
        cubicTo(w * 0.42f, h * 0.22f, w * 0.48f, h * 0.16f, w * 0.50f, h * 0.16f)
        cubicTo(w * 0.52f, h * 0.16f, w * 0.58f, h * 0.22f, w * 0.62f, h * 0.32f)
        cubicTo(w * 0.68f, h * 0.45f, w * 0.66f, h * 0.65f, w * 0.70f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, fill)
    drawPath(bodyPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Mitre cleft
    drawLine(
        color = stroke,
        start = Offset(w * 0.45f, h * 0.28f),
        end = Offset(w * 0.56f, h * 0.42f),
        strokeWidth = strokeWidth * 0.9f,
        cap = StrokeCap.Round
    )

    // Finial ball on top
    drawCircle(
        color = fill,
        radius = w * 0.06f,
        center = Offset(w * 0.50f, h * 0.13f)
    )
    drawCircle(
        color = stroke,
        radius = w * 0.06f,
        center = Offset(w * 0.50f, h * 0.13f),
        style = Stroke(strokeWidth)
    )
}

private fun DrawScope.drawRook(fill: Color, stroke: Color, accent: Color, strokeWidth: Float) {
    val w = size.width
    val h = size.height

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.18f, h * 0.88f)
        lineTo(w * 0.82f, h * 0.88f)
        lineTo(w * 0.75f, h * 0.78f)
        lineTo(w * 0.25f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Tower Body
    val bodyPath = Path().apply {
        moveTo(w * 0.28f, h * 0.78f)
        lineTo(w * 0.33f, h * 0.40f)
        lineTo(w * 0.67f, h * 0.40f)
        lineTo(w * 0.72f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, fill)
    drawPath(bodyPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Crenellations / Battlements
    val battlementsPath = Path().apply {
        moveTo(w * 0.22f, h * 0.40f)
        lineTo(w * 0.22f, h * 0.24f)
        lineTo(w * 0.34f, h * 0.24f)
        lineTo(w * 0.34f, h * 0.31f)
        lineTo(w * 0.44f, h * 0.31f)
        lineTo(w * 0.44f, h * 0.24f)
        lineTo(w * 0.56f, h * 0.24f)
        lineTo(w * 0.56f, h * 0.31f)
        lineTo(w * 0.66f, h * 0.31f)
        lineTo(w * 0.66f, h * 0.24f)
        lineTo(w * 0.78f, h * 0.24f)
        lineTo(w * 0.78f, h * 0.40f)
        close()
    }
    drawPath(battlementsPath, fill)
    drawPath(battlementsPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))
}

private fun DrawScope.drawQueen(fill: Color, stroke: Color, accent: Color, strokeWidth: Float) {
    val w = size.width
    val h = size.height

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.16f, h * 0.88f)
        lineTo(w * 0.84f, h * 0.88f)
        lineTo(w * 0.76f, h * 0.78f)
        lineTo(w * 0.24f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Crown / Corset Body
    val crownPath = Path().apply {
        moveTo(w * 0.27f, h * 0.78f)
        lineTo(w * 0.18f, h * 0.32f) // left tip
        lineTo(w * 0.34f, h * 0.48f)
        lineTo(w * 0.50f, h * 0.25f) // center tip
        lineTo(w * 0.66f, h * 0.48f)
        lineTo(w * 0.82f, h * 0.32f) // right tip
        lineTo(w * 0.73f, h * 0.78f)
        close()
    }
    drawPath(crownPath, fill)
    drawPath(crownPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Jewels on crown points
    val pointCenters = arrayOf(
        Offset(w * 0.18f, h * 0.32f),
        Offset(w * 0.50f, h * 0.25f),
        Offset(w * 0.82f, h * 0.32f)
    )
    for (center in pointCenters) {
        drawCircle(fill, radius = w * 0.045f, center = center)
        drawCircle(stroke, radius = w * 0.045f, center = center, style = Stroke(strokeWidth))
    }
}

private fun DrawScope.drawKing(fill: Color, stroke: Color, accent: Color, strokeWidth: Float) {
    val w = size.width
    val h = size.height

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.16f, h * 0.88f)
        lineTo(w * 0.84f, h * 0.88f)
        lineTo(w * 0.76f, h * 0.78f)
        lineTo(w * 0.24f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Royal Robe / Body
    val bodyPath = Path().apply {
        moveTo(w * 0.26f, h * 0.78f)
        cubicTo(w * 0.22f, h * 0.58f, w * 0.26f, h * 0.38f, w * 0.35f, h * 0.32f)
        lineTo(w * 0.65f, h * 0.32f)
        cubicTo(w * 0.74f, h * 0.38f, w * 0.78f, h * 0.58f, w * 0.74f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, fill)
    drawPath(bodyPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Crown rim
    drawOval(
        color = fill,
        topLeft = Offset(w * 0.28f, h * 0.28f),
        size = Size(w * 0.44f, h * 0.10f)
    )
    drawOval(
        color = stroke,
        topLeft = Offset(w * 0.28f, h * 0.28f),
        size = Size(w * 0.44f, h * 0.10f),
        style = Stroke(strokeWidth)
    )

    // King's Cross on top
    drawLine(
        color = stroke,
        start = Offset(w * 0.50f, h * 0.12f),
        end = Offset(w * 0.50f, h * 0.28f),
        strokeWidth = strokeWidth * 1.3f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = stroke,
        start = Offset(w * 0.40f, h * 0.17f),
        end = Offset(w * 0.60f, h * 0.17f),
        strokeWidth = strokeWidth * 1.3f,
        cap = StrokeCap.Round
    )
}
