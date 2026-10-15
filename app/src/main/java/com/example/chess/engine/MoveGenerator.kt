package com.example.chess.engine

import com.example.chess.model.CastlingRights
import com.example.chess.model.GameState
import com.example.chess.model.GameStatus
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Position

object MoveGenerator {

    private val KNIGHT_OFFSETS = arrayOf(
        intArrayOf(-2, -1), intArrayOf(-2, 1),
        intArrayOf(-1, -2), intArrayOf(-1, 2),
        intArrayOf(1, -2), intArrayOf(1, 2),
        intArrayOf(2, -1), intArrayOf(2, 1)
    )

    private val BISHOP_DIRECTIONS = arrayOf(
        intArrayOf(-1, -1), intArrayOf(-1, 1),
        intArrayOf(1, -1), intArrayOf(1, 1)
    )

    private val ROOK_DIRECTIONS = arrayOf(
        intArrayOf(-1, 0), intArrayOf(1, 0),
        intArrayOf(0, -1), intArrayOf(0, 1)
    )

    private val KING_OFFSETS = arrayOf(
        intArrayOf(-1, -1), intArrayOf(-1, 0), intArrayOf(-1, 1),
        intArrayOf(0, -1),                     intArrayOf(0, 1),
        intArrayOf(1, -1),  intArrayOf(1, 0),  intArrayOf(1, 1)
    )

    /**
     * Generates all strictly legal moves for the current player whose turn it is.
     */
    fun generateLegalMoves(state: GameState): List<Move> {
        val color = state.turn
        val pseudoMoves = generatePseudoLegalMoves(state, color)
        val legalMoves = mutableListOf<Move>()

        for (move in pseudoMoves) {
            val nextState = applyMoveInternal(state, move, validate = false)
            val kingPos = findKing(nextState.board, color)
            if (kingPos != null && !isSquareAttacked(nextState.board, kingPos, color.opposite)) {
                legalMoves.add(move)
            }
        }
        return legalMoves
    }

    /**
     * Generates legal moves specifically originating from a given board position.
     */
    fun getLegalMovesForPosition(state: GameState, pos: Position): List<Move> {
        val piece = state.board[pos] ?: return emptyList()
        if (piece.color != state.turn) return emptyList()
        return generateLegalMoves(state).filter { it.from == pos }
    }

    fun isKingInCheck(board: Map<Position, Piece>, color: PieceColor): Boolean {
        val kingPos = findKing(board, color) ?: return false
        return isSquareAttacked(board, kingPos, color.opposite)
    }

    fun findKing(board: Map<Position, Piece>, color: PieceColor): Position? {
        for ((pos, piece) in board) {
            if (piece.type == PieceType.KING && piece.color == color) {
                return pos
            }
        }
        return null
    }

    fun isSquareAttacked(board: Map<Position, Piece>, target: Position, byColor: PieceColor): Boolean {
        // 1. Attacked by pawns
        val pawnPusherRow = if (byColor == PieceColor.WHITE) target.row + 1 else target.row - 1
        for (colOffset in arrayOf(-1, 1)) {
            val pCol = target.col + colOffset
            if (pCol in 0..7 && pawnPusherRow in 0..7) {
                val piece = board[Position(pawnPusherRow, pCol)]
                if (piece != null && piece.color == byColor && piece.type == PieceType.PAWN) {
                    return true
                }
            }
        }

        // 2. Attacked by knights
        for (offset in KNIGHT_OFFSETS) {
            val r = target.row + offset[0]
            val c = target.col + offset[1]
            if (r in 0..7 && c in 0..7) {
                val piece = board[Position(r, c)]
                if (piece != null && piece.color == byColor && piece.type == PieceType.KNIGHT) {
                    return true
                }
            }
        }

        // 3. Attacked by king
        for (offset in KING_OFFSETS) {
            val r = target.row + offset[0]
            val c = target.col + offset[1]
            if (r in 0..7 && c in 0..7) {
                val piece = board[Position(r, c)]
                if (piece != null && piece.color == byColor && piece.type == PieceType.KING) {
                    return true
                }
            }
        }

        // 4. Attacked along diagonals (Bishops, Queens)
        for (dir in BISHOP_DIRECTIONS) {
            var r = target.row + dir[0]
            var c = target.col + dir[1]
            while (r in 0..7 && c in 0..7) {
                val piece = board[Position(r, c)]
                if (piece != null) {
                    if (piece.color == byColor && (piece.type == PieceType.BISHOP || piece.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                r += dir[0]
                c += dir[1]
            }
        }

        // 5. Attacked along orthogonals (Rooks, Queens)
        for (dir in ROOK_DIRECTIONS) {
            var r = target.row + dir[0]
            var c = target.col + dir[1]
            while (r in 0..7 && c in 0..7) {
                val piece = board[Position(r, c)]
                if (piece != null) {
                    if (piece.color == byColor && (piece.type == PieceType.ROOK || piece.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                r += dir[0]
                c += dir[1]
            }
        }

        return false
    }

    private fun generatePseudoLegalMoves(state: GameState, color: PieceColor): List<Move> {
        val moves = mutableListOf<Move>()
        val board = state.board

        for ((from, piece) in board) {
            if (piece.color != color) continue

            when (piece.type) {
                PieceType.PAWN -> generatePawnMoves(state, from, piece, moves)
                PieceType.KNIGHT -> generateKnightMoves(board, from, piece, moves)
                PieceType.BISHOP -> generateSlidingMoves(board, from, piece, BISHOP_DIRECTIONS, moves)
                PieceType.ROOK -> generateSlidingMoves(board, from, piece, ROOK_DIRECTIONS, moves)
                PieceType.QUEEN -> {
                    generateSlidingMoves(board, from, piece, BISHOP_DIRECTIONS, moves)
                    generateSlidingMoves(board, from, piece, ROOK_DIRECTIONS, moves)
                }
                PieceType.KING -> generateKingMoves(state, from, piece, moves)
            }
        }
        return moves
    }

    private fun generatePawnMoves(state: GameState, from: Position, piece: Piece, out: MutableList<Move>) {
        val forward = if (piece.isWhite) -1 else 1
        val startRow = if (piece.isWhite) 6 else 1
        val promoRow = if (piece.isWhite) 0 else 7

        // 1 step forward
        val oneStep = Position(from.row + forward, from.col)
        if (oneStep.isValid && state.board[oneStep] == null) {
            if (oneStep.row == promoRow) {
                for (promo in arrayOf(PieceType.QUEEN, PieceType.KNIGHT, PieceType.ROOK, PieceType.BISHOP)) {
                    out.add(Move(from, oneStep, piece, promotion = promo))
                }
            } else {
                out.add(Move(from, oneStep, piece))
            }

            // 2 steps forward from start rank
            if (from.row == startRow) {
                val twoStep = Position(from.row + 2 * forward, from.col)
                if (twoStep.isValid && state.board[twoStep] == null) {
                    out.add(Move(from, twoStep, piece))
                }
            }
        }

        // Diagonal captures
        for (dCol in arrayOf(-1, 1)) {
            val to = Position(from.row + forward, from.col + dCol)
            if (to.isValid) {
                val targetPiece = state.board[to]
                if (targetPiece != null && targetPiece.color != piece.color) {
                    if (to.row == promoRow) {
                        for (promo in arrayOf(PieceType.QUEEN, PieceType.KNIGHT, PieceType.ROOK, PieceType.BISHOP)) {
                            out.add(Move(from, to, piece, capturedPiece = targetPiece, promotion = promo))
                        }
                    } else {
                        out.add(Move(from, to, piece, capturedPiece = targetPiece))
                    }
                } else if (targetPiece == null && state.enPassantTarget == to) {
                    // En Passant capture
                    val victimPos = Position(from.row, to.col)
                    val victimPiece = state.board[victimPos]
                    if (victimPiece != null && victimPiece.color != piece.color && victimPiece.type == PieceType.PAWN) {
                        out.add(Move(from, to, piece, capturedPiece = victimPiece, isEnPassant = true))
                    }
                }
            }
        }
    }

    private fun generateKnightMoves(board: Map<Position, Piece>, from: Position, piece: Piece, out: MutableList<Move>) {
        for (offset in KNIGHT_OFFSETS) {
            val to = Position(from.row + offset[0], from.col + offset[1])
            if (to.isValid) {
                val targetPiece = board[to]
                if (targetPiece == null || targetPiece.color != piece.color) {
                    out.add(Move(from, to, piece, capturedPiece = targetPiece))
                }
            }
        }
    }

    private fun generateSlidingMoves(
        board: Map<Position, Piece>,
        from: Position,
        piece: Piece,
        directions: Array<IntArray>,
        out: MutableList<Move>
    ) {
        for (dir in directions) {
            var r = from.row + dir[0]
            var c = from.col + dir[1]
            while (r in 0..7 && c in 0..7) {
                val to = Position(r, c)
                val targetPiece = board[to]
                if (targetPiece == null) {
                    out.add(Move(from, to, piece))
                } else {
                    if (targetPiece.color != piece.color) {
                        out.add(Move(from, to, piece, capturedPiece = targetPiece))
                    }
                    break
                }
                r += dir[0]
                c += dir[1]
            }
        }
    }

    private fun generateKingMoves(state: GameState, from: Position, piece: Piece, out: MutableList<Move>) {
        // Standard 1-step moves
        for (offset in KING_OFFSETS) {
            val to = Position(from.row + offset[0], from.col + offset[1])
            if (to.isValid) {
                val targetPiece = state.board[to]
                if (targetPiece == null || targetPiece.color != piece.color) {
                    out.add(Move(from, to, piece, capturedPiece = targetPiece))
                }
            }
        }

        // Castling moves
        val row = if (piece.isWhite) 7 else 0
        if (from.row == row && from.col == 4) {
            val cr = state.castlingRights
            val kingsideAllowed = if (piece.isWhite) cr.whiteKingside else cr.blackKingside
            val queensideAllowed = if (piece.isWhite) cr.whiteQueenside else cr.blackQueenside

            // Cannot castle out of check
            if (!isKingInCheck(state.board, piece.color)) {
                // Kingside (e1->g1 or e8->g8)
                if (kingsideAllowed) {
                    val fSquare = Position(row, 5)
                    val gSquare = Position(row, 6)
                    val rookSquare = Position(row, 7)
                    val rook = state.board[rookSquare]

                    if (state.board[fSquare] == null && state.board[gSquare] == null &&
                        rook != null && rook.type == PieceType.ROOK && rook.color == piece.color &&
                        !isSquareAttacked(state.board, fSquare, piece.color.opposite) &&
                        !isSquareAttacked(state.board, gSquare, piece.color.opposite)
                    ) {
                        out.add(Move(from, gSquare, piece, isCastling = true))
                    }
                }

                // Queenside (e1->c1 or e8->c8)
                if (queensideAllowed) {
                    val dSquare = Position(row, 3)
                    val cSquare = Position(row, 2)
                    val bSquare = Position(row, 1)
                    val rookSquare = Position(row, 0)
                    val rook = state.board[rookSquare]

                    if (state.board[dSquare] == null && state.board[cSquare] == null && state.board[bSquare] == null &&
                        rook != null && rook.type == PieceType.ROOK && rook.color == piece.color &&
                        !isSquareAttacked(state.board, dSquare, piece.color.opposite) &&
                        !isSquareAttacked(state.board, cSquare, piece.color.opposite)
                    ) {
                        out.add(Move(from, cSquare, piece, isCastling = true))
                    }
                }
            }
        }
    }

    /**
     * Executes a move on the game state and returns the new resulting GameState.
     */
    fun makeMove(state: GameState, move: Move): GameState {
        return applyMoveInternal(state, move, validate = true)
    }

    private fun applyMoveInternal(state: GameState, move: Move, validate: Boolean): GameState {
        val newBoard = state.board.toMutableMap()
        val piece = move.piece
        newBoard.remove(move.from)

        // Handle regular capture or en passant
        val capturedByWhite = state.capturedByWhite.toMutableList()
        val capturedByBlack = state.capturedByBlack.toMutableList()

        if (move.isEnPassant) {
            val victimPos = Position(move.from.row, move.to.col)
            val victim = newBoard.remove(victimPos)
            if (victim != null) {
                if (piece.isWhite) capturedByWhite.add(victim) else capturedByBlack.add(victim)
            }
        } else if (move.capturedPiece != null) {
            if (piece.isWhite) capturedByWhite.add(move.capturedPiece) else capturedByBlack.add(move.capturedPiece)
        }

        // Place moved piece (or promoted piece)
        val placedPiece = if (move.promotion != null) {
            Piece(move.promotion, piece.color)
        } else {
            piece
        }
        newBoard[move.to] = placedPiece

        // Handle castling rook movement
        if (move.isCastling) {
            val row = move.from.row
            if (move.to.col == 6) {
                // Kingside castle: Rook from col 7 to col 5
                val rook = newBoard.remove(Position(row, 7))
                if (rook != null) newBoard[Position(row, 5)] = rook
            } else if (move.to.col == 2) {
                // Queenside castle: Rook from col 0 to col 3
                val rook = newBoard.remove(Position(row, 0))
                if (rook != null) newBoard[Position(row, 3)] = rook
            }
        }

        // Update Castling Rights
        var newCr = state.castlingRights
        if (piece.type == PieceType.KING) {
            newCr = if (piece.isWhite) {
                newCr.copy(whiteKingside = false, whiteQueenside = false)
            } else {
                newCr.copy(blackKingside = false, blackQueenside = false)
            }
        }
        if (piece.type == PieceType.ROOK) {
            if (piece.isWhite) {
                if (move.from == Position(7, 7)) newCr = newCr.copy(whiteKingside = false)
                if (move.from == Position(7, 0)) newCr = newCr.copy(whiteQueenside = false)
            } else {
                if (move.from == Position(0, 7)) newCr = newCr.copy(blackKingside = false)
                if (move.from == Position(0, 0)) newCr = newCr.copy(blackQueenside = false)
            }
        }
        // If enemy rook was captured in corner
        if (move.to == Position(7, 7)) newCr = newCr.copy(whiteKingside = false)
        if (move.to == Position(7, 0)) newCr = newCr.copy(whiteQueenside = false)
        if (move.to == Position(0, 7)) newCr = newCr.copy(blackKingside = false)
        if (move.to == Position(0, 0)) newCr = newCr.copy(blackQueenside = false)

        // Update En Passant target
        val newEnPassant = if (piece.type == PieceType.PAWN && kotlin.math.abs(move.to.row - move.from.row) == 2) {
            Position((move.from.row + move.to.row) / 2, move.from.col)
        } else {
            null
        }

        // Halfmove clock (resets on pawn move or capture)
        val isPawnMove = piece.type == PieceType.PAWN
        val isCapture = move.capturedPiece != null || move.isEnPassant
        val newHalfmove = if (isPawnMove || isCapture) 0 else state.halfmoveClock + 1

        val nextTurn = state.turn.opposite
        val newFullmove = if (state.turn == PieceColor.BLACK) state.fullmoveNumber + 1 else state.fullmoveNumber

        // Standard Algebraic Notation
        val san = if (validate) NotationHelper.moveToSan(state, move) else ""
        val executedMove = move.copy(san = san)

        var intermediateState = state.copy(
            board = newBoard,
            turn = nextTurn,
            castlingRights = newCr,
            enPassantTarget = newEnPassant,
            halfmoveClock = newHalfmove,
            fullmoveNumber = newFullmove,
            capturedByWhite = capturedByWhite,
            capturedByBlack = capturedByBlack,
            lastMove = executedMove,
            moveHistory = state.moveHistory + executedMove
        )

        if (!validate) {
            return intermediateState
        }

        // Determine Game Status
        val inCheck = isKingInCheck(newBoard, nextTurn)
        val kingPos = if (inCheck) findKing(newBoard, nextTurn) else null
        val legalNextMoves = generateLegalMoves(intermediateState)

        val finalStatus: GameStatus
        val winner: PieceColor?

        if (legalNextMoves.isEmpty()) {
            if (inCheck) {
                finalStatus = GameStatus.CHECKMATE
                winner = state.turn // previous player won!
            } else {
                finalStatus = GameStatus.STALEMATE
                winner = null
            }
        } else if (newHalfmove >= 100) {
            finalStatus = GameStatus.DRAW_FIFTY_MOVES
            winner = null
        } else if (isInsufficientMaterial(newBoard)) {
            finalStatus = GameStatus.DRAW_INSUFFICIENT_MATERIAL
            winner = null
        } else if (inCheck) {
            finalStatus = GameStatus.CHECK
            winner = null
        } else {
            finalStatus = GameStatus.IN_PROGRESS
            winner = null
        }

        return intermediateState.copy(
            status = finalStatus,
            winner = winner,
            checkedKingPos = kingPos
        )
    }

    private fun isInsufficientMaterial(board: Map<Position, Piece>): Boolean {
        val pieces = board.values.toList()
        if (pieces.size <= 2) return true // King vs King

        if (pieces.size == 3) {
            // King + (Bishop or Knight) vs King
            val nonKings = pieces.filter { it.type != PieceType.KING }
            if (nonKings.size == 1) {
                val t = nonKings[0].type
                return t == PieceType.BISHOP || t == PieceType.KNIGHT
            }
        }

        if (pieces.size == 4) {
            // King + Bishop vs King + Bishop (same color square)
            val bishops = board.filter { it.value.type == PieceType.BISHOP }
            if (bishops.size == 2) {
                val bPos = bishops.keys.toList()
                val color1 = (bPos[0].row + bPos[0].col) % 2
                val color2 = (bPos[1].row + bPos[1].col) % 2
                if (color1 == color2) return true
            }
        }

        return false
    }
}
