package com.tictactoe.game

import com.tictactoe.game.engine.GameEngine
import com.tictactoe.game.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [GameEngine] — move flow, win/draw detection, score tracking, and resets.
 */
class GameEngineTest {

    private fun pvpState() = GameState(gameMode = GameMode.PLAYER_VS_PLAYER)
    private fun pvcState(diff: Difficulty = Difficulty.HARD) =
        GameState(gameMode = GameMode.PLAYER_VS_COMPUTER, difficulty = diff)

    private fun boardOf(vararg cells: String): List<CellValue> {
        require(cells.size == 9)
        return cells.map {
            when (it.uppercase()) {
                "X" -> CellValue.X
                "O" -> CellValue.O
                else -> CellValue.EMPTY
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // makeMove — basic flow
    // ──────────────────────────────────────────────────────────────────

    @Test fun `makeMove places symbol on board`() {
        val state = pvpState()
        val next = GameEngine.makeMove(state, 4)
        assertEquals(CellValue.X, next.board[4])
    }

    @Test fun `makeMove switches current player`() {
        val state = pvpState()
        val next = GameEngine.makeMove(state, 4)
        assertEquals(CellValue.O, next.currentPlayer)
    }

    @Test fun `makeMove increments moveCount`() {
        val state = pvpState()
        val next = GameEngine.makeMove(state, 0)
        assertEquals(1, next.moveCount)
    }

    @Test fun `makeMove does nothing if cell already occupied`() {
        val state = pvpState()
        val after1 = GameEngine.makeMove(state, 4)       // X at 4
        val after2 = GameEngine.makeMove(after1, 4)      // attempt O at 4 — should be ignored
        assertEquals(CellValue.X, after2.board[4])
        assertEquals(CellValue.O, after2.currentPlayer)  // still O's turn
        assertEquals(1, after2.moveCount)
    }

    @Test fun `makeMove does nothing when game is over`() {
        // Set up a state where X has already won
        val wonState = pvpState().copy(
            board = boardOf("X","X","X","O","O","_","_","_","_"),
            isGameOver = true,
            winner = CellValue.X
        )
        val attempted = GameEngine.makeMove(wonState, 5)
        assertEquals(wonState, attempted) // state unchanged
    }

    // ──────────────────────────────────────────────────────────────────
    // Win detection via makeMove
    // ──────────────────────────────────────────────────────────────────

    @Test fun `X wins when completing top row`() {
        // X: 0,1,_  O: 3,4,_  — X plays 2
        val state = pvpState().copy(
            board = boardOf("X","X","_","O","O","_","_","_","_"),
            currentPlayer = CellValue.X
        )
        val result = GameEngine.makeMove(state, 2)
        assertTrue(result.isGameOver)
        assertEquals(CellValue.X, result.winner)
        assertEquals(listOf(0, 1, 2), result.winningLine)
        assertEquals(1, result.scoreX)
        assertEquals(0, result.scoreO)
    }

    @Test fun `O wins when completing middle column`() {
        // O: 1,4,_  X: 0,2,_  — O plays 7
        val state = pvpState().copy(
            board = boardOf("X","O","X","_","O","_","_","_","_"),
            currentPlayer = CellValue.O
        )
        val result = GameEngine.makeMove(state, 7)
        assertTrue(result.isGameOver)
        assertEquals(CellValue.O, result.winner)
        assertEquals(listOf(1, 4, 7), result.winningLine)
        assertEquals(0, result.scoreX)
        assertEquals(1, result.scoreO)
    }

    @Test fun `X wins on main diagonal`() {
        val state = pvpState().copy(
            board = boardOf("X","O","_","O","X","_","_","_","_"),
            currentPlayer = CellValue.X
        )
        val result = GameEngine.makeMove(state, 8)
        assertTrue(result.isGameOver)
        assertEquals(CellValue.X, result.winner)
        assertEquals(listOf(0, 4, 8), result.winningLine)
    }

    @Test fun `X wins on anti-diagonal`() {
        val state = pvpState().copy(
            board = boardOf("O","_","X","_","X","O","_","_","_"),
            currentPlayer = CellValue.X
        )
        val result = GameEngine.makeMove(state, 6)
        assertTrue(result.isGameOver)
        assertEquals(CellValue.X, result.winner)
        assertEquals(listOf(2, 4, 6), result.winningLine)
    }

    // ──────────────────────────────────────────────────────────────────
    // Draw detection
    // ──────────────────────────────────────────────────────────────────

    @Test fun `draw is detected when board fills with no winner`() {
        // One move away from a draw (X to play last cell)
        // X O X
        // X X O
        // O X _   <- X fills index 8
        val state = pvpState().copy(
            board = boardOf("X","O","X","X","X","O","O","X","_"),
            currentPlayer = CellValue.X
        )
        val result = GameEngine.makeMove(state, 8)
        assertTrue(result.isGameOver)
        assertTrue(result.isDraw)
        assertNull(result.winner)
        assertEquals(1, result.scoreDraw)
    }

    // ──────────────────────────────────────────────────────────────────
    // Score accumulation
    // ──────────────────────────────────────────────────────────────────

    @Test fun `scores accumulate across multiple rounds`() {
        var state = pvpState()

        // Round 1: X wins top row
        state = state.copy(
            board = boardOf("X","X","_","O","O","_","_","_","_"),
            currentPlayer = CellValue.X
        )
        state = GameEngine.makeMove(state, 2)
        assertEquals(1, state.scoreX); assertEquals(0, state.scoreO)

        // Reset for Round 2
        state = GameEngine.resetRound(state)
        assertEquals(1, state.scoreX); assertEquals(0, state.scoreO) // scores preserved

        // Round 2: O wins left column
        state = state.copy(
            board = boardOf("O","X","_","O","X","_","_","_","_"),
            currentPlayer = CellValue.O
        )
        state = GameEngine.makeMove(state, 6)
        assertEquals(1, state.scoreX); assertEquals(1, state.scoreO)
    }

    // ──────────────────────────────────────────────────────────────────
    // resetRound
    // ──────────────────────────────────────────────────────────────────

    @Test fun `resetRound clears board and game-over flags`() {
        val wonState = pvpState().copy(
            board = boardOf("X","X","X","O","O","_","_","_","_"),
            isGameOver = true,
            winner = CellValue.X,
            winningLine = listOf(0, 1, 2),
            scoreX = 1
        )
        val reset = GameEngine.resetRound(wonState)
        assertFalse(reset.isGameOver)
        assertFalse(reset.isDraw)
        assertNull(reset.winner)
        assertTrue(reset.winningLine.isEmpty())
        assertEquals(0, reset.moveCount)
        assertTrue(reset.board.all { it == CellValue.EMPTY })
        assertEquals(1, reset.scoreX) // scores preserved
    }

    @Test fun `resetRound assigns loser to start next round`() {
        // X won, so O (the loser) starts next round
        val wonState = pvpState().copy(
            isGameOver = true,
            winner = CellValue.X
        )
        val reset = GameEngine.resetRound(wonState)
        assertEquals(CellValue.O, reset.currentPlayer)
    }

    @Test fun `resetRound starts X after a draw`() {
        val drawState = pvpState().copy(isGameOver = true, isDraw = true, winner = null)
        val reset = GameEngine.resetRound(drawState)
        assertEquals(CellValue.X, reset.currentPlayer)
    }

    // ──────────────────────────────────────────────────────────────────
    // resetGame
    // ──────────────────────────────────────────────────────────────────

    @Test fun `resetGame clears all scores`() {
        val state = pvpState().copy(scoreX = 5, scoreO = 3, scoreDraw = 2)
        val reset = GameEngine.resetGame(state)
        assertEquals(0, reset.scoreX)
        assertEquals(0, reset.scoreO)
        assertEquals(0, reset.scoreDraw)
    }

    @Test fun `resetGame preserves game mode and difficulty`() {
        val state = pvcState(Difficulty.MEDIUM).copy(scoreX = 3)
        val reset = GameEngine.resetGame(state)
        assertEquals(GameMode.PLAYER_VS_COMPUTER, reset.gameMode)
        assertEquals(Difficulty.MEDIUM, reset.difficulty)
    }

    // ──────────────────────────────────────────────────────────────────
    // makeAIMove
    // ──────────────────────────────────────────────────────────────────

    @Test fun `makeAIMove places O on the board`() {
        val state = pvcState().copy(currentPlayer = CellValue.O)
        val result = GameEngine.makeAIMove(state)
        assertEquals(1, result.board.count { it == CellValue.O })
    }

    @Test fun `makeAIMove does nothing when game is over`() {
        val state = pvcState().copy(isGameOver = true)
        val result = GameEngine.makeAIMove(state)
        assertEquals(state, result)
    }

    @Test fun `makeAIMove detects AI win`() {
        // O is about to win at index 8 (main diagonal)
        val state = pvcState().copy(
            board = boardOf("O","X","_","X","O","_","_","_","_"),
            currentPlayer = CellValue.O
        )
        val result = GameEngine.makeAIMove(state)
        assertTrue(result.isGameOver)
        assertEquals(CellValue.O, result.winner)
    }
}
