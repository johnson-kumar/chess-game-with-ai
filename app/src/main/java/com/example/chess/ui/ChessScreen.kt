package com.example.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chess.model.GameMode
import com.example.chess.model.PieceColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessScreen(
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val gameMode by viewModel.gameMode.collectAsStateWithLifecycle()
    val aiDifficulty by viewModel.aiDifficulty.collectAsStateWithLifecycle()
    val playerSide by viewModel.playerSide.collectAsStateWithLifecycle()
    val actualPlayerColor by viewModel.actualPlayerColor.collectAsStateWithLifecycle()
    val boardTheme by viewModel.boardTheme.collectAsStateWithLifecycle()
    val isFlipped by viewModel.isFlipped.collectAsStateWithLifecycle()
    val selectedPos by viewModel.selectedPos.collectAsStateWithLifecycle()
    val legalMoves by viewModel.legalMovesForSelected.collectAsStateWithLifecycle()
    val pendingPromotion by viewModel.pendingPromotionMove.collectAsStateWithLifecycle()
    val hintMove by viewModel.hintMove.collectAsStateWithLifecycle()
    val evalScore by viewModel.evaluationScore.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val showEvalBar by viewModel.showEvalBar.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    // Automatically prompt game over dialog when game ends
    if (gameState.isGameOver && !showGameOverDialog) {
        showGameOverDialog = true
    }

    // Top and Bottom player colors based on board flip
    val topPlayerColor = if (isFlipped) PieceColor.WHITE else PieceColor.BLACK
    val bottomPlayerColor = if (isFlipped) PieceColor.BLACK else PieceColor.WHITE

    val isTopAi = gameMode == GameMode.VS_AI && topPlayerColor != actualPlayerColor
    val isBottomAi = gameMode == GameMode.VS_AI && bottomPlayerColor != actualPlayerColor

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Chess Master",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (gameMode == GameMode.PASS_AND_PLAY) "Pass & Play (Solo)" else "vs AI (${aiDifficulty.title})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.startNewGame() },
                        modifier = Modifier.testTag("top_bar_new_game")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New Game")
                    }
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("top_bar_settings")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Player Header
                PlayerHeader(
                    color = topPlayerColor,
                    isCurrentTurn = gameState.turn == topPlayerColor && !gameState.isGameOver,
                    isAi = isTopAi,
                    aiDifficulty = if (isTopAi) aiDifficulty else null,
                    capturedPieces = if (topPlayerColor == PieceColor.WHITE) gameState.capturedByWhite else gameState.capturedByBlack,
                    materialAdvantage = if (topPlayerColor == PieceColor.WHITE) gameState.materialAdvantage else -gameState.materialAdvantage,
                    isThinking = isTopAi && isAiThinking
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Optional Evaluation Bar
                if (showEvalBar) {
                    EvaluationBar(
                        score = evalScore,
                        isFlipped = isFlipped,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // The Interactive Chessboard
                ChessBoard(
                    gameState = gameState,
                    selectedPos = selectedPos,
                    legalMovesForSelected = legalMoves,
                    hintMove = hintMove,
                    boardTheme = boardTheme,
                    isFlipped = isFlipped,
                    onSquareClick = { pos -> viewModel.onSquareClicked(pos) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Player Header
                PlayerHeader(
                    color = bottomPlayerColor,
                    isCurrentTurn = gameState.turn == bottomPlayerColor && !gameState.isGameOver,
                    isAi = isBottomAi,
                    aiDifficulty = if (isBottomAi) aiDifficulty else null,
                    capturedPieces = if (bottomPlayerColor == PieceColor.WHITE) gameState.capturedByWhite else gameState.capturedByBlack,
                    materialAdvantage = if (bottomPlayerColor == PieceColor.WHITE) gameState.materialAdvantage else -gameState.materialAdvantage,
                    isThinking = isBottomAi && isAiThinking
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Controls row (Undo, Hint, Flip, Notation, Settings)
                ControlBar(
                    canUndo = gameState.moveHistory.isNotEmpty() && !gameState.isGameOver,
                    onUndoClick = { viewModel.undoMove() },
                    onHintClick = { viewModel.requestHint() },
                    onFlipClick = { viewModel.flipBoard() },
                    onMovesSheetClick = { showHistorySheet = true },
                    onSettingsClick = { showSettingsDialog = true },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }

    // Pawn Promotion Dialog
    if (pendingPromotion != null) {
        PromotionDialog(
            color = pendingPromotion!!.piece.color,
            onPieceSelected = { promoPiece -> viewModel.onPromotionSelected(promoPiece) }
        )
    }

    // Game Over Dialog
    if (showGameOverDialog && gameState.isGameOver) {
        GameOverDialog(
            status = gameState.status,
            winner = gameState.winner,
            gameMode = gameMode,
            onNewGame = {
                showGameOverDialog = false
                viewModel.startNewGame()
            },
            onDismiss = { showGameOverDialog = false }
        )
    }

    // Move History Sheet
    if (showHistorySheet) {
        MoveHistorySheet(
            moves = gameState.moveHistory,
            onDismiss = { showHistorySheet = false }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            currentMode = gameMode,
            currentDifficulty = aiDifficulty,
            currentSide = playerSide,
            currentTheme = boardTheme,
            showEvalBar = showEvalBar,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            onModeChange = { viewModel.setGameMode(it) },
            onDifficultyChange = { viewModel.setAiDifficulty(it) },
            onSideChange = { viewModel.setPlayerSide(it) },
            onThemeChange = { viewModel.setBoardTheme(it) },
            onEvalBarToggle = { viewModel.toggleEvalBar(it) },
            onSoundToggle = { viewModel.toggleSound(it) },
            onVibrationToggle = { viewModel.toggleVibration(it) },
            onNewGame = { viewModel.startNewGame() },
            onResign = { viewModel.resign() },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
