package com.example.chess.model

enum class GameMode(val label: String, val description: String) {
    PASS_AND_PLAY(
        "Pass & Play",
        "Play both White & Black on this device offline"
    ),
    VS_AI(
        "Play vs AI",
        "Challenge the offline AI with custom difficulty"
    )
}

enum class AiDifficulty(
    val title: String,
    val eloEstimate: String,
    val depth: Int,
    val description: String
) {
    BEGINNER("Beginner", "600 ELO", 1, "Casual play with frequent mistakes"),
    CASUAL("Casual", "1100 ELO", 2, "Plays solid basics, occasional tactical slips"),
    CHALLENGER("Challenger", "1600 ELO", 3, "Sharp tactical awareness and strong defense"),
    GRANDMASTER("Grandmaster", "2100+ ELO", 4, "Deep positional search and ruthless tactics")
}

enum class PlayerSide(val label: String) {
    WHITE("White"),
    BLACK("Black"),
    RANDOM("Random")
}

enum class BoardThemeType(
    val title: String,
    val lightSquareHex: Long,
    val darkSquareHex: Long,
    val highlightHex: Long
) {
    CLASSIC_WOOD(
        "Warm Wood",
        0xFFF0D9B5,
        0xFFB58863,
        0x77E69A38
    ),
    TOURNAMENT_GREEN(
        "Tournament Green",
        0xFFEEEED2,
        0xFF769656,
        0x77F7EC59
    ),
    MIDNIGHT_SLATE(
        "Midnight Slate",
        0xFF94A3B8,
        0xFF334155,
        0x7738BDF8
    ),
    EMERALD_NIGHT(
        "Emerald Luxury",
        0xFFD1FAE5,
        0xFF065F46,
        0x7734D399
    )
}
