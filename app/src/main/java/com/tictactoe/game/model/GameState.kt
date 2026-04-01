package com.tictactoe.game.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a cell value on the board.
 */
enum class CellValue {
    EMPTY, X, O;

    fun opponent(): CellValue = when (this) {
        X -> O
        O -> X
        EMPTY -> EMPTY
    }

    override fun toString(): String = when (this) {
        X -> "X"
        O -> "O"
        EMPTY -> ""
    }
}

/**
 * Represents the current status of the game.
 */
sealed class GameStatus {
    object InProgress : GameStatus()
    data class Won(val winner: CellValue, val winningLine: List<Int>) : GameStatus()
    object Draw : GameStatus()
}

/**
 * Difficulty level for the AI opponent.
 */
enum class Difficulty {
    EASY, MEDIUM, HARD
}

/**
 * Game mode selection.
 */
enum class GameMode {
    PLAYER_VS_PLAYER,
    PLAYER_VS_COMPUTER
}

/**
 * Immutable snapshot of the entire game state, safe to pass between components.
 */
@Parcelize
data class GameState(
    val board: List<CellValue> = List(9) { CellValue.EMPTY },
    val currentPlayer: CellValue = CellValue.X,
    val scoreX: Int = 0,
    val scoreO: Int = 0,
    val scoreDraw: Int = 0,
    val gameMode: GameMode = GameMode.PLAYER_VS_PLAYER,
    val difficulty: Difficulty = Difficulty.HARD,
    val isGameOver: Boolean = false,
    val winner: CellValue? = null,
    val winningLine: List<Int> = emptyList(),
    val isDraw: Boolean = false,
    val moveCount: Int = 0
) : Parcelable {

    /** Returns true if the given cell index is empty and the game is still in progress. */
    fun isMoveAllowed(index: Int): Boolean =
        !isGameOver && index in 0..8 && board[index] == CellValue.EMPTY

    /** Returns a new board with the move applied (does NOT update scores or check win). */
    fun applyMove(index: Int): GameState {
        require(isMoveAllowed(index)) { "Move not allowed at index $index" }
        val newBoard = board.toMutableList().also { it[index] = currentPlayer }
        return copy(
            board = newBoard,
            currentPlayer = currentPlayer.opponent(),
            moveCount = moveCount + 1
        )
    }
}
