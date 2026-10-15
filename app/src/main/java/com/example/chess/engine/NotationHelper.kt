package com.example.chess.engine

import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Position

object NotationHelper {

    fun moveToSan(state: GameState, move: Move): String {
        if (move.isCastling) {
            return if (move.to.col == 6) "O-O" else "O-O-O"
        }

        val piece = move.piece
        val sb = StringBuilder()

        if (piece.type == PieceType.PAWN) {
            if (move.capturedPiece != null || move.isEnPassant) {
                sb.append(move.from.file)
                sb.append('x')
            }
            sb.append(move.to.algebraic)
            if (move.promotion != null) {
                sb.append('=')
                sb.append(move.promotion.symbol)
            }
        } else {
            sb.append(piece.getNotationSymbol())

            // Disambiguation if multiple same-type pieces can move to target
            val disambiguation = getDisambiguation(state, move)
            sb.append(disambiguation)

            if (move.capturedPiece != null) {
                sb.append('x')
            }
            sb.append(move.to.algebraic)
        }

        // Test if this move delivers check or checkmate
        val nextBoard = state.board.toMutableMap()
        nextBoard.remove(move.from)
        if (move.isEnPassant) {
            nextBoard.remove(Position(move.from.row, move.to.col))
        }
        val placed = if (move.promotion != null) Piece(move.promotion, piece.color) else piece
        nextBoard[move.to] = placed

        val opponent = piece.color.opposite
        val inCheck = MoveGenerator.isKingInCheck(nextBoard, opponent)
        if (inCheck) {
            // Check if checkmate by quick inspection
            val mockState = state.copy(
                board = nextBoard,
                turn = opponent,
                enPassantTarget = null
            )
            val oppLegal = MoveGenerator.generateLegalMoves(mockState)
            if (oppLegal.isEmpty()) {
                sb.append('#')
            } else {
                sb.append('+')
            }
        }

        return sb.toString()
    }

    private fun getDisambiguation(state: GameState, move: Move): String {
        val samePieces = state.board.filter { (pos, piece) ->
            pos != move.from &&
            piece.color == move.piece.color &&
            piece.type == move.piece.type
        }
        if (samePieces.isEmpty()) return ""

        val candidates = mutableListOf<Position>()
        for ((pos, _) in samePieces) {
            val legal = MoveGenerator.getLegalMovesForPosition(state, pos)
            if (legal.any { it.to == move.to }) {
                candidates.add(pos)
            }
        }
        if (candidates.isEmpty()) return ""

        val sameFile = candidates.any { it.col == move.from.col }
        val sameRank = candidates.any { it.row == move.from.row }

        return when {
            !sameFile -> move.from.file.toString()
            !sameRank -> move.from.rank.toString()
            else -> move.from.algebraic
        }
    }
}
