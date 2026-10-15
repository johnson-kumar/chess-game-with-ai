package com.example.chess.engine

import com.example.chess.model.AiDifficulty
import com.example.chess.model.GameState
import com.example.chess.model.GameStatus
import com.example.chess.model.Move
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

object ChessAiEngine {

    suspend fun findBestMove(state: GameState, difficulty: AiDifficulty): Move? = withContext(Dispatchers.Default) {
        val legalMoves = MoveGenerator.generateLegalMoves(state)
        if (legalMoves.isEmpty()) return@withContext null

        val isWhite = state.turn == PieceColor.WHITE

        when (difficulty) {
            AiDifficulty.BEGINNER -> {
                // 35% chance to play a random move, otherwise 1-ply greedy
                if (Random.nextFloat() < 0.35f) {
                    legalMoves.random()
                } else {
                    pickBestMoveWithSearch(state, legalMoves, depth = 1, isWhite = isWhite, useQuiescence = false)
                }
            }
            AiDifficulty.CASUAL -> {
                // 2-ply search with small jitter
                val scoredMoves = scoreMovesAtRoot(state, legalMoves, depth = 2, isWhite = isWhite)
                if (scoredMoves.isNotEmpty() && Random.nextFloat() < 0.15f && scoredMoves.size >= 2) {
                    // Pick 2nd best move occasionally
                    scoredMoves.take(2).random().first
                } else {
                    scoredMoves.firstOrNull()?.first ?: legalMoves.random()
                }
            }
            AiDifficulty.CHALLENGER -> {
                // 3-ply alpha-beta
                pickBestMoveWithSearch(state, legalMoves, depth = 3, isWhite = isWhite, useQuiescence = false)
            }
            AiDifficulty.GRANDMASTER -> {
                // 4-ply alpha-beta with quiescence search & move ordering
                pickBestMoveWithSearch(state, legalMoves, depth = 4, isWhite = isWhite, useQuiescence = true)
            }
        }
    }

    private fun scoreMovesAtRoot(
        state: GameState,
        moves: List<Move>,
        depth: Int,
        isWhite: Boolean
    ): List<Pair<Move, Int>> {
        val ordered = orderMoves(moves)
        val scored = mutableListOf<Pair<Move, Int>>()

        for (move in ordered) {
            val nextState = MoveGenerator.makeMove(state, move)
            val score = minimax(
                state = nextState,
                depth = depth - 1,
                alpha = -1000000,
                beta = 1000000,
                isMaximizing = !isWhite,
                useQuiescence = false
            )
            scored.add(Pair(move, score))
        }

        return if (isWhite) {
            scored.sortedByDescending { it.second }
        } else {
            scored.sortedBy { it.second }
        }
    }

    private fun pickBestMoveWithSearch(
        state: GameState,
        legalMoves: List<Move>,
        depth: Int,
        isWhite: Boolean,
        useQuiescence: Boolean
    ): Move {
        val orderedMoves = orderMoves(legalMoves)
        var bestMove = orderedMoves.first()

        var alpha = -1000000
        var beta = 1000000

        if (isWhite) {
            var maxEval = -1000000
            for (move in orderedMoves) {
                val nextState = MoveGenerator.makeMove(state, move)
                val eval = minimax(nextState, depth - 1, alpha, beta, isMaximizing = false, useQuiescence = useQuiescence)
                if (eval > maxEval) {
                    maxEval = eval
                    bestMove = move
                }
                alpha = kotlin.math.max(alpha, eval)
                if (beta <= alpha) break
            }
        } else {
            var minEval = 1000000
            for (move in orderedMoves) {
                val nextState = MoveGenerator.makeMove(state, move)
                val eval = minimax(nextState, depth - 1, alpha, beta, isMaximizing = true, useQuiescence = useQuiescence)
                if (eval < minEval) {
                    minEval = eval
                    bestMove = move
                }
                beta = kotlin.math.min(beta, eval)
                if (beta <= alpha) break
            }
        }

        return bestMove
    }

    private fun minimax(
        state: GameState,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean,
        useQuiescence: Boolean
    ): Int {
        var currentAlpha = alpha
        var currentBeta = beta

        if (state.isGameOver) {
            return when (state.status) {
                GameStatus.CHECKMATE -> if (state.winner == PieceColor.WHITE) 50000 + depth else -50000 - depth
                GameStatus.STALEMATE,
                GameStatus.DRAW_INSUFFICIENT_MATERIAL,
                GameStatus.DRAW_FIFTY_MOVES,
                GameStatus.DRAW_REPETITION -> 0
                else -> 0
            }
        }

        if (depth <= 0) {
            return if (useQuiescence) {
                quiescence(state, currentAlpha, currentBeta, isMaximizing, qDepth = 2)
            } else {
                ChessEvaluator.evaluate(state)
            }
        }

        val legalMoves = MoveGenerator.generateLegalMoves(state)
        if (legalMoves.isEmpty()) {
            return if (MoveGenerator.isKingInCheck(state.board, state.turn)) {
                if (state.turn == PieceColor.WHITE) -50000 - depth else 50000 + depth
            } else 0
        }

        val orderedMoves = orderMoves(legalMoves)

        if (isMaximizing) {
            var maxEval = -1000000
            for (move in orderedMoves) {
                val nextState = MoveGenerator.makeMove(state, move)
                val eval = minimax(nextState, depth - 1, currentAlpha, currentBeta, isMaximizing = false, useQuiescence = useQuiescence)
                maxEval = kotlin.math.max(maxEval, eval)
                currentAlpha = kotlin.math.max(currentAlpha, eval)
                if (currentBeta <= currentAlpha) break
            }
            return maxEval
        } else {
            var minEval = 1000000
            for (move in orderedMoves) {
                val nextState = MoveGenerator.makeMove(state, move)
                val eval = minimax(nextState, depth - 1, currentAlpha, currentBeta, isMaximizing = true, useQuiescence = useQuiescence)
                minEval = kotlin.math.min(minEval, eval)
                currentBeta = kotlin.math.min(currentBeta, eval)
                if (currentBeta <= currentAlpha) break
            }
            return minEval
        }
    }

    private fun quiescence(
        state: GameState,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean,
        qDepth: Int
    ): Int {
        var currentAlpha = alpha
        var currentBeta = beta
        val standPat = ChessEvaluator.evaluate(state)

        if (qDepth <= 0 || state.isGameOver) return standPat

        if (isMaximizing) {
            if (standPat >= currentBeta) return currentBeta
            if (standPat > currentAlpha) currentAlpha = standPat

            val captures = MoveGenerator.generateLegalMoves(state).filter { it.capturedPiece != null || it.promotion != null }
            val orderedCaptures = orderMoves(captures)

            var maxEval = standPat
            for (move in orderedCaptures) {
                val nextState = MoveGenerator.makeMove(state, move)
                val score = quiescence(nextState, currentAlpha, currentBeta, isMaximizing = false, qDepth - 1)
                maxEval = kotlin.math.max(maxEval, score)
                currentAlpha = kotlin.math.max(currentAlpha, score)
                if (currentBeta <= currentAlpha) break
            }
            return maxEval
        } else {
            if (standPat <= currentAlpha) return currentAlpha
            if (standPat < currentBeta) currentBeta = standPat

            val captures = MoveGenerator.generateLegalMoves(state).filter { it.capturedPiece != null || it.promotion != null }
            val orderedCaptures = orderMoves(captures)

            var minEval = standPat
            for (move in orderedCaptures) {
                val nextState = MoveGenerator.makeMove(state, move)
                val score = quiescence(nextState, currentAlpha, currentBeta, isMaximizing = true, qDepth - 1)
                minEval = kotlin.math.min(minEval, score)
                currentBeta = kotlin.math.min(currentBeta, score)
                if (currentBeta <= currentAlpha) break
            }
            return minEval
        }
    }

    private fun orderMoves(moves: List<Move>): List<Move> {
        return moves.sortedByDescending { move ->
            var score = 0
            if (move.capturedPiece != null) {
                // MVV-LVA: Most Valuable Victim - Least Valuable Attacker
                score += 10 * move.capturedPiece.type.value - move.piece.type.value
            }
            if (move.promotion != null) {
                score += move.promotion.value
            }
            if (move.isCastling) {
                score += 50
            }
            score
        }
    }

    fun getEvaluationScore(state: GameState): Float {
        val centipawns = ChessEvaluator.evaluate(state)
        return centipawns / 100f
    }
}
