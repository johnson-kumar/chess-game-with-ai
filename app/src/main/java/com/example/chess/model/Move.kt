package com.example.chess.model

data class Move(
    val from: Position,
    val to: Position,
    val piece: Piece,
    val capturedPiece: Piece? = null,
    val promotion: PieceType? = null,
    val isCastling: Boolean = false,
    val isEnPassant: Boolean = false,
    val san: String = "" // Standard Algebraic Notation (e.g. e4, Nf3, O-O, etc.)
)
