package com.example.chess.model

data class Position(
    val row: Int,
    val col: Int
) {
    val isValid: Boolean
        get() = row in 0..7 && col in 0..7

    val algebraic: String
        get() = "${('a' + col)}${8 - row}"

    val file: Char
        get() = ('a' + col)

    val rank: Int
        get() = 8 - row

    val isLightSquare: Boolean
        get() = (row + col) % 2 == 0

    companion object {
        fun fromAlgebraic(notation: String): Position? {
            if (notation.length != 2) return null
            val col = notation[0] - 'a'
            val rank = notation[1].digitToIntOrNull() ?: return null
            val row = 8 - rank
            val pos = Position(row, col)
            return if (pos.isValid) pos else null
        }
    }
}
