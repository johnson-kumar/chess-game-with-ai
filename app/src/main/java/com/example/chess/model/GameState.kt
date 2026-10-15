package com.example.chess.model

enum class GameStatus {
    IN_PROGRESS,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_INSUFFICIENT_MATERIAL,
    DRAW_FIFTY_MOVES,
    DRAW_REPETITION,
    RESIGNATION
}

data class CastlingRights(
    val whiteKingside: Boolean = true,
    val whiteQueenside: Boolean = true,
    val blackKingside: Boolean = true,
    val blackQueenside: Boolean = true
)

data class GameState(
    val board: Map<Position, Piece> = initialBoard(),
    val turn: PieceColor = PieceColor.WHITE,
    val castlingRights: CastlingRights = CastlingRights(),
    val enPassantTarget: Position? = null,
    val halfmoveClock: Int = 0,
    val fullmoveNumber: Int = 1,
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val winner: PieceColor? = null,
    val moveHistory: List<Move> = emptyList(),
    val capturedByWhite: List<Piece> = emptyList(),
    val capturedByBlack: List<Piece> = emptyList(),
    val lastMove: Move? = null,
    val checkedKingPos: Position? = null
) {
    val isGameOver: Boolean
        get() = status != GameStatus.IN_PROGRESS && status != GameStatus.CHECK

    val materialAdvantage: Int
        get() {
            val whiteScore = board.values.filter { it.isWhite }.sumOf { it.type.value }
            val blackScore = board.values.filter { it.isBlack }.sumOf { it.type.value }
            return (whiteScore - blackScore) / 100
        }

    fun getPiece(row: Int, col: Int): Piece? = board[Position(row, col)]
    fun getPiece(pos: Position): Piece? = board[pos]

    companion object {
        fun initialBoard(): Map<Position, Piece> {
            val map = mutableMapOf<Position, Piece>()
            // Row 0 = Black major pieces
            map[Position(0, 0)] = Piece(PieceType.ROOK, PieceColor.BLACK)
            map[Position(0, 1)] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
            map[Position(0, 2)] = Piece(PieceType.BISHOP, PieceColor.BLACK)
            map[Position(0, 3)] = Piece(PieceType.QUEEN, PieceColor.BLACK)
            map[Position(0, 4)] = Piece(PieceType.KING, PieceColor.BLACK)
            map[Position(0, 5)] = Piece(PieceType.BISHOP, PieceColor.BLACK)
            map[Position(0, 6)] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
            map[Position(0, 7)] = Piece(PieceType.ROOK, PieceColor.BLACK)
            for (c in 0..7) {
                map[Position(1, c)] = Piece(PieceType.PAWN, PieceColor.BLACK)
            }

            // Row 7 = White major pieces
            map[Position(7, 0)] = Piece(PieceType.ROOK, PieceColor.WHITE)
            map[Position(7, 1)] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
            map[Position(7, 2)] = Piece(PieceType.BISHOP, PieceColor.WHITE)
            map[Position(7, 3)] = Piece(PieceType.QUEEN, PieceColor.WHITE)
            map[Position(7, 4)] = Piece(PieceType.KING, PieceColor.WHITE)
            map[Position(7, 5)] = Piece(PieceType.BISHOP, PieceColor.WHITE)
            map[Position(7, 6)] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
            map[Position(7, 7)] = Piece(PieceType.ROOK, PieceColor.WHITE)
            for (c in 0..7) {
                map[Position(6, c)] = Piece(PieceType.PAWN, PieceColor.WHITE)
            }
            return map
        }
    }
}
