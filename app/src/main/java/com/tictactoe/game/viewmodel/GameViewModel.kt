package com.tictactoe.game.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tictactoe.game.engine.GameEngine
import com.tictactoe.game.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel that holds the game state and survives configuration changes.
 * UI observes [gameState] and [uiEvent] LiveData only.
 */
class GameViewModel : ViewModel() {

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> get() = _gameState

    /** One-shot events sent to the UI (e.g., animate win, play sound). */
    private val _uiEvent = MutableLiveData<UiEvent?>()
    val uiEvent: LiveData<UiEvent?> get() = _uiEvent

    /** Prevents duplicate AI triggers during coroutine delay. */
    private var isAiThinking = false

    // ──────────────────────────────────────────────────────────────────
    // Initialization
    // ──────────────────────────────────────────────────────────────────

    fun initGame(mode: GameMode, difficulty: Difficulty) {
        if (_gameState.value != null) return // Don't re-init on rotation
        _gameState.value = GameState(gameMode = mode, difficulty = difficulty)
    }

    fun restoreState(state: GameState) {
        _gameState.value = state
    }

    // ──────────────────────────────────────────────────────────────────
    // Player moves
    // ──────────────────────────────────────────────────────────────────

    fun onCellClicked(index: Int) {
        val current = _gameState.value ?: return
        if (isAiThinking) return
        if (!current.isMoveAllowed(index)) return

        // In PvC mode, only allow moves when it's the human's turn (X)
        if (current.gameMode == GameMode.PLAYER_VS_COMPUTER &&
            current.currentPlayer == CellValue.O) return

        val newState = GameEngine.makeMove(current, index)
        _gameState.value = newState
        handlePostMove(newState)

        // Trigger AI move after a short delay for realism
        if (!newState.isGameOver &&
            newState.gameMode == GameMode.PLAYER_VS_COMPUTER &&
            newState.currentPlayer == CellValue.O) {
            triggerAiMove(newState)
        }
    }

    private fun triggerAiMove(state: GameState) {
        isAiThinking = true
        viewModelScope.launch {
            delay(AI_MOVE_DELAY_MS)
            val aiState = GameEngine.makeAIMove(state)
            _gameState.postValue(aiState)
            isAiThinking = false
            handlePostMove(aiState)
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Post-move events
    // ──────────────────────────────────────────────────────────────────

    private fun handlePostMove(state: GameState) {
        when {
            state.winner != null -> _uiEvent.value = UiEvent.PlayerWon(state.winner, state.winningLine)
            state.isDraw -> _uiEvent.value = UiEvent.Draw
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Game control
    // ──────────────────────────────────────────────────────────────────

    fun resetRound() {
        val current = _gameState.value ?: return
        _gameState.value = GameEngine.resetRound(current)
        _uiEvent.value = null
        isAiThinking = false
    }

    fun resetGame() {
        val current = _gameState.value ?: return
        _gameState.value = GameEngine.resetGame(current)
        _uiEvent.value = null
        isAiThinking = false
    }

    fun consumeUiEvent() {
        _uiEvent.value = null
    }

    // ──────────────────────────────────────────────────────────────────
    // Companion
    // ──────────────────────────────────────────────────────────────────

    companion object {
        private const val AI_MOVE_DELAY_MS = 600L
    }
}

/** One-shot UI events emitted by the ViewModel. */
sealed class UiEvent {
    data class PlayerWon(val winner: CellValue, val winningLine: List<Int>) : UiEvent()
    object Draw : UiEvent()
}
