package com.example.chess.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.engine.ChessAiEngine
import com.example.chess.engine.MoveGenerator
import com.example.chess.model.AiDifficulty
import com.example.chess.model.BoardThemeType
import com.example.chess.model.GameMode
import com.example.chess.model.GameState
import com.example.chess.model.GameStatus
import com.example.chess.model.Move
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.PlayerSide
import com.example.chess.model.Position
import com.example.chess.sound.SoundEffects
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class ChessViewModel(application: Application) : AndroidViewModel(application) {

    private val soundEffects = SoundEffects(application.applicationContext)

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _historyStates = mutableListOf<GameState>()

    // Mode defaults to PASS_AND_PLAY as user specifically requested to play both sides offline!
    private val _gameMode = MutableStateFlow(GameMode.PASS_AND_PLAY)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    private val _aiDifficulty = MutableStateFlow(AiDifficulty.CASUAL)
    val aiDifficulty: StateFlow<AiDifficulty> = _aiDifficulty.asStateFlow()

    private val _playerSide = MutableStateFlow(PlayerSide.WHITE)
    val playerSide: StateFlow<PlayerSide> = _playerSide.asStateFlow()

    private val _actualPlayerColor = MutableStateFlow(PieceColor.WHITE)
    val actualPlayerColor: StateFlow<PieceColor> = _actualPlayerColor.asStateFlow()

    private val _boardTheme = MutableStateFlow(BoardThemeType.CLASSIC_WOOD)
    val boardTheme: StateFlow<BoardThemeType> = _boardTheme.asStateFlow()

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    private val _selectedPos = MutableStateFlow<Position?>(null)
    val selectedPos: StateFlow<Position?> = _selectedPos.asStateFlow()

    private val _legalMovesForSelected = MutableStateFlow<List<Move>>(emptyList())
    val legalMovesForSelected: StateFlow<List<Move>> = _legalMovesForSelected.asStateFlow()

    private val _pendingPromotionMove = MutableStateFlow<Move?>(null)
    val pendingPromotionMove: StateFlow<Move?> = _pendingPromotionMove.asStateFlow()

    private val _hintMove = MutableStateFlow<Move?>(null)
    val hintMove: StateFlow<Move?> = _hintMove.asStateFlow()

    private val _evaluationScore = MutableStateFlow(0.0f)
    val evaluationScore: StateFlow<Float> = _evaluationScore.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _showEvalBar = MutableStateFlow(true)
    val showEvalBar: StateFlow<Boolean> = _showEvalBar.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private var aiJob: Job? = null

    init {
        updateEvaluation()
    }

    fun onSquareClicked(pos: Position) {
        val current = _gameState.value
        if (current.isGameOver || _isAiThinking.value) return

        // If playing against AI, verify it's the human's turn
        if (_gameMode.value == GameMode.VS_AI && current.turn != _actualPlayerColor.value) {
            return
        }

        val selected = _selectedPos.value
        val pieceOnPos = current.getPiece(pos)

        // Case 1: An existing piece is selected and pos is a legal move target
        if (selected != null) {
            val matchingMove = _legalMovesForSelected.value.find { it.to == pos }
            if (matchingMove != null) {
                // Check if this move requires pawn promotion
                if (matchingMove.piece.type == PieceType.PAWN &&
                    ((matchingMove.piece.isWhite && pos.row == 0) || (matchingMove.piece.isBlack && pos.row == 7))
                ) {
                    _pendingPromotionMove.value = matchingMove
                    return
                }

                executePlayerMove(matchingMove)
                return
            }
        }

        // Case 2: Clicking on friendly piece selects it
        if (pieceOnPos != null && pieceOnPos.color == current.turn) {
            _selectedPos.value = pos
            _legalMovesForSelected.value = MoveGenerator.getLegalMovesForPosition(current, pos)
            _hintMove.value = null
        } else {
            // Case 3: Clicking on empty square or opposing piece that isn't a legal move deselects
            clearSelection()
        }
    }

    fun onPromotionSelected(promotionType: PieceType) {
        val pending = _pendingPromotionMove.value ?: return
        _pendingPromotionMove.value = null
        val fullMove = pending.copy(promotion = promotionType)
        executePlayerMove(fullMove)
    }

    private fun executePlayerMove(move: Move) {
        _historyStates.add(_gameState.value)
        val nextState = MoveGenerator.makeMove(_gameState.value, move)
        _gameState.value = nextState
        clearSelection()
        _hintMove.value = null

        triggerEffectsForMove(move, nextState)
        updateEvaluation()

        // Trigger AI move if in VS_AI mode and not game over
        checkTriggerAi()
    }

    private fun triggerEffectsForMove(move: Move, nextState: GameState) {
        val isCapture = move.capturedPiece != null || move.isEnPassant
        val isCheck = nextState.status == GameStatus.CHECK
        val isVictory = nextState.status == GameStatus.CHECKMATE

        if (_soundEnabled.value) {
            soundEffects.playMove(isCapture, isCheck, isVictory)
        }
        if (_vibrationEnabled.value) {
            soundEffects.vibrateMove(isCapture, isCheck)
        }
    }

    private fun checkTriggerAi() {
        val state = _gameState.value
        if (_gameMode.value == GameMode.VS_AI && !state.isGameOver && state.turn != _actualPlayerColor.value) {
            aiJob?.cancel()
            aiJob = viewModelScope.launch {
                _isAiThinking.value = true
                delay(300) // Small natural delay
                val aiMove = ChessAiEngine.findBestMove(state, _aiDifficulty.value)
                if (aiMove != null && !state.isGameOver) {
                    _historyStates.add(_gameState.value)
                    val afterAiState = MoveGenerator.makeMove(_gameState.value, aiMove)
                    _gameState.value = afterAiState
                    triggerEffectsForMove(aiMove, afterAiState)
                    updateEvaluation()
                }
                _isAiThinking.value = false
            }
        }
    }

    fun undoMove() {
        aiJob?.cancel()
        _isAiThinking.value = false

        if (_historyStates.isEmpty()) return

        if (_gameMode.value == GameMode.VS_AI) {
            // In VS AI mode, undo until it is the player's turn again (usually 2 half-moves)
            if (_historyStates.size >= 2) {
                _historyStates.removeAt(_historyStates.size - 1)
                val prevState = _historyStates.removeAt(_historyStates.size - 1)
                _gameState.value = prevState
            } else if (_historyStates.size == 1) {
                val prevState = _historyStates.removeAt(_historyStates.size - 1)
                _gameState.value = prevState
            }
        } else {
            // In Pass & Play mode, undo exactly 1 move
            val prevState = _historyStates.removeAt(_historyStates.size - 1)
            _gameState.value = prevState
        }

        clearSelection()
        _hintMove.value = null
        updateEvaluation()
    }

    fun requestHint() {
        val state = _gameState.value
        if (state.isGameOver || _isAiThinking.value) return

        viewModelScope.launch {
            // Calculate best move using challenger depth
            val best = ChessAiEngine.findBestMove(state, AiDifficulty.CHALLENGER)
            _hintMove.value = best
        }
    }

    fun flipBoard() {
        _isFlipped.value = !_isFlipped.value
    }

    fun setBoardTheme(theme: BoardThemeType) {
        _boardTheme.value = theme
    }

    fun setGameMode(mode: GameMode) {
        _gameMode.value = mode
        startNewGame()
    }

    fun setAiDifficulty(difficulty: AiDifficulty) {
        _aiDifficulty.value = difficulty
    }

    fun setPlayerSide(side: PlayerSide) {
        _playerSide.value = side
        startNewGame()
    }

    fun toggleEvalBar(show: Boolean) {
        _showEvalBar.value = show
    }

    fun toggleSound(enabled: Boolean) {
        _soundEnabled.value = enabled
    }

    fun toggleVibration(enabled: Boolean) {
        _vibrationEnabled.value = enabled
    }

    fun resign() {
        val current = _gameState.value
        if (current.isGameOver) return
        val winner = current.turn.opposite
        _gameState.value = current.copy(
            status = GameStatus.RESIGNATION,
            winner = winner
        )
    }

    fun startNewGame() {
        aiJob?.cancel()
        _isAiThinking.value = false
        _historyStates.clear()
        clearSelection()
        _hintMove.value = null

        val chosenColor = when (_playerSide.value) {
            PlayerSide.WHITE -> PieceColor.WHITE
            PlayerSide.BLACK -> PieceColor.BLACK
            PlayerSide.RANDOM -> if (Random.nextBoolean()) PieceColor.WHITE else PieceColor.BLACK
        }
        _actualPlayerColor.value = chosenColor
        _isFlipped.value = (chosenColor == PieceColor.BLACK && _gameMode.value == GameMode.VS_AI)

        _gameState.value = GameState()
        updateEvaluation()

        // If playing as Black against AI, AI goes first!
        checkTriggerAi()
    }

    private fun updateEvaluation() {
        _evaluationScore.value = ChessAiEngine.getEvaluationScore(_gameState.value)
    }

    private fun clearSelection() {
        _selectedPos.value = null
        _legalMovesForSelected.value = emptyList()
    }
}
