package com.example.chess.model

enum class PieceColor {
    WHITE,
    BLACK;

    val opposite: PieceColor
        get() = if (this == WHITE) BLACK else WHITE

    val displayName: String
        get() = if (this == WHITE) "White" else "Black"
}

enum class PieceType(
    val symbol: String,
    val value: Int
) {
    PAWN("P", 100),
    KNIGHT("N", 320),
    BISHOP("B", 330),
    ROOK("R", 500),
    QUEEN("Q", 900),
    KING("K", 20000);

    val isMajorOrMinor: Boolean
        get() = this != PAWN && this != KING
}

data class Piece(
    val type: PieceType,
    val color: PieceColor
) {
    val isWhite: Boolean get() = color == PieceColor.WHITE
    val isBlack: Boolean get() = color == PieceColor.BLACK

    fun getNotationSymbol(): String {
        return when (type) {
            PieceType.PAWN -> ""
            PieceType.KNIGHT -> "N"
            PieceType.BISHOP -> "B"
            PieceType.ROOK -> "R"
            PieceType.QUEEN -> "Q"
            PieceType.KING -> "K"
        }
    }
}
