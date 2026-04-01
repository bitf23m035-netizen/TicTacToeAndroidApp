package com.tictactoe.game.engine

import com.tictactoe.game.ai.AIPlayer
import com.tictactoe.game.model.*

/**
 * Pure game engine — no Android dependencies.
 * Drives state transitions and delegates AI moves.
 */
object GameEngine {

    /**
     * Processes a human player's move at [index] on the current [state].
     *
     * @return Updated [GameState] after the move and any resulting win/draw evaluation.
     */
    fun makeMove(state: GameState, index: Int): GameState {
        if (!state.isMoveAllowed(index)) return state

        val afterMove = state.applyMove(index)
        // The player who just moved is state.currentPlayer (before flip)
        return evaluateAndUpdate(afterMove, movedPlayer = state.currentPlayer)
    }

    /**
     * Computes and applies the AI's move on the current [state].
     *
     * @return Updated [GameState] after the AI move, or the same state if AI can't move.
     */
    fun makeAIMove(state: GameState): GameState {
        if (state.isGameOver) return state

        val aiPlayer = state.currentPlayer  // AI always plays the current player's symbol
        val moveIndex = AIPlayer.getBestMove(state.board, aiPlayer, state.difficulty)
        if (moveIndex == -1) return state   // No moves available (shouldn't happen normally)

        val afterMove = state.applyMove(moveIndex)
        return evaluateAndUpdate(afterMove, movedPlayer = aiPlayer)
    }

    /**
     * Resets the board for a new round, preserving scores and settings.
     * Alternates who goes first each round (X always starts the very first game).
     */
    fun resetRound(state: GameState): GameState {
        // Winner of the last round goes second in the next (the loser starts)
        val nextStarter = when {
            state.winner != null -> state.winner.opponent()
            else -> CellValue.X
        }
        return state.copy(
            board = GameBoard.emptyBoard(),
            currentPlayer = nextStarter,
            isGameOver = false,
            winner = null,
            winningLine = emptyList(),
            isDraw = false,
            moveCount = 0
        )
    }

    /**
     * Resets the entire game, including scores.
     */
    fun resetGame(state: GameState): GameState = GameState(
        gameMode = state.gameMode,
        difficulty = state.difficulty
    )

    // ──────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────

    /**
     * Evaluates the board after [movedPlayer] played and updates score / game-over flags.
     */
    private fun evaluateAndUpdate(state: GameState, movedPlayer: CellValue): GameState {
        return when (val status = GameBoard.evaluate(state.board)) {
            is GameStatus.Won -> {
                val (newX, newO) = updatedScores(state, status.winner)
                state.copy(
                    isGameOver = true,
                    winner = status.winner,
                    winningLine = status.winningLine,
                    scoreX = newX,
                    scoreO = newO
                )
            }
            is GameStatus.Draw -> {
                state.copy(
                    isGameOver = true,
                    isDraw = true,
                    scoreDraw = state.scoreDraw + 1
                )
            }
            GameStatus.InProgress -> state
        }
    }

    private fun updatedScores(state: GameState, winner: CellValue): Pair<Int, Int> =
        when (winner) {
            CellValue.X -> Pair(state.scoreX + 1, state.scoreO)
            CellValue.O -> Pair(state.scoreX, state.scoreO + 1)
            CellValue.EMPTY -> Pair(state.scoreX, state.scoreO)
        }
}
