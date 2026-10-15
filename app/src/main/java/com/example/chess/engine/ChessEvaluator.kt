package com.example.chess.engine

import com.example.chess.model.GameState
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Position

object ChessEvaluator {

    // Piece-Square Tables (from White's perspective; flipped for Black)
    private val PAWN_TABLE = intArrayOf(
        0,   0,   0,   0,   0,   0,   0,   0,
        50,  50,  50,  50,  50,  50,  50,  50,
        10,  10,  20,  30,  30,  20,  10,  10,
        5,   5,  10,  25,  25,  10,   5,   5,
        0,   0,   0,  20,  20,   0,   0,   0,
        5,  -5, -10,   0,   0, -10,  -5,   5,
        5,  10,  10, -20, -20,  10,  10,   5,
        0,   0,   0,   0,   0,   0,   0,   0
    )

    private val KNIGHT_TABLE = intArrayOf(
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20,   0,   0,   0,   0, -20, -40,
        -30,   0,  10,  15,  15,  10,   0, -30,
        -30,   5,  15,  20,  20,  15,   5, -30,
        -30,   0,  15,  20,  20,  15,   0, -30,
        -30,   5,  10,  15,  15,  10,   5, -30,
        -40, -20,   0,   5,   5,   0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50
    )

    private val BISHOP_TABLE = intArrayOf(
        -20, -10, -10, -10, -10, -10, -10, -20,
        -10,   0,   5,  10,  10,   5,   0, -10,
        -10,  10,  10,  10,  10,  10,  10, -10,
        -10,   0,  10,  10,  10,  10,   0, -10,
        -10,   5,   5,  10,  10,   5,   5, -10,
        -10,   0,   5,  10,  10,   5,   0, -10,
        -10,   0,   0,   0,   0,   0,   0, -10,
        -20, -10, -10, -10, -10, -10, -10, -20
    )

    private val ROOK_TABLE = intArrayOf(
        0,   0,   0,   0,   0,   0,   0,   0,
        5,  10,  10,  10,  10,  10,  10,   5,
        -5,   0,   0,   0,   0,   0,   0,  -5,
        -5,   0,   0,   0,   0,   0,   0,  -5,
        -5,   0,   0,   0,   0,   0,   0,  -5,
        -5,   0,   0,   0,   0,   0,   0,  -5,
        -5,   0,   0,   0,   0,   0,   0,  -5,
        0,   0,   0,   5,   5,   0,   0,   0
    )

    private val QUEEN_TABLE = intArrayOf(
        -20, -10, -10,  -5,  -5, -10, -10, -20,
        -10,   0,   0,   0,   0,   0,   0, -10,
        -10,   0,   5,   5,   5,   5,   0, -10,
        -5,   0,   5,   5,   5,   5,   0,  -5,
        0,   0,   5,   5,   5,   5,   0,  -5,
        -10,   5,   5,   5,   5,   5,   0, -10,
        -10,   0,   5,   0,   0,   0,   0, -10,
        -20, -10, -10,  -5,  -5, -10, -10, -20
    )

    private val KING_MIDDLE_TABLE = intArrayOf(
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -20, -30, -30, -40, -40, -30, -30, -20,
        -10, -20, -20, -20, -20, -20, -20, -10,
        20,  20,   0,   0,   0,   0,  20,  20,
        20,  30,  10,   0,   0,  10,  30,  20
    )

    /**
     * Positive score favors White, negative favors Black. (Centipawns: 100 = 1 pawn)
     */
    fun evaluate(state: GameState): Int {
        var score = 0

        for ((pos, piece) in state.board) {
            val tableIdx = if (piece.isWhite) {
                pos.row * 8 + pos.col
            } else {
                (7 - pos.row) * 8 + pos.col
            }

            val positionalBonus = when (piece.type) {
                PieceType.PAWN -> PAWN_TABLE[tableIdx]
                PieceType.KNIGHT -> KNIGHT_TABLE[tableIdx]
                PieceType.BISHOP -> BISHOP_TABLE[tableIdx]
                PieceType.ROOK -> ROOK_TABLE[tableIdx]
                PieceType.QUEEN -> QUEEN_TABLE[tableIdx]
                PieceType.KING -> KING_MIDDLE_TABLE[tableIdx]
            }

            val pieceValue = piece.type.value + positionalBonus
            if (piece.isWhite) {
                score += pieceValue
            } else {
                score -= pieceValue
            }
        }

        return score
    }
}
