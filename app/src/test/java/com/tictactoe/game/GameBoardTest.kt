package com.tictactoe.game

import com.tictactoe.game.engine.GameBoard
import com.tictactoe.game.model.CellValue
import com.tictactoe.game.model.GameStatus
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests covering all win/draw/edge-case scenarios for [GameBoard].
 */
class GameBoardTest {

    // ──────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────

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

    private fun assertWinner(board: List<CellValue>, expected: CellValue) {
        val status = GameBoard.evaluate(board)
        assertTrue("Expected Won status", status is GameStatus.Won)
        assertEquals(expected, (status as GameStatus.Won).winner)
    }

    private fun assertDraw(board: List<CellValue>) {
        assertEquals(GameStatus.Draw, GameBoard.evaluate(board))
    }

    private fun assertInProgress(board: List<CellValue>) {
        assertEquals(GameStatus.InProgress, GameBoard.evaluate(board))
    }

    // ──────────────────────────────────────────────────────────────────
    // Row wins
    // ──────────────────────────────────────────────────────────────────

    @Test fun `X wins top row`() {
        assertWinner(boardOf("X","X","X", "_","O","_", "O","_","_"), CellValue.X)
    }

    @Test fun `X wins middle row`() {
        assertWinner(boardOf("O","_","_", "X","X","X", "_","O","_"), CellValue.X)
    }

    @Test fun `X wins bottom row`() {
        assertWinner(boardOf("O","_","O", "_","O","_", "X","X","X"), CellValue.X)
    }

    @Test fun `O wins top row`() {
        assertWinner(boardOf("O","O","O", "X","_","X", "_","X","_"), CellValue.O)
    }

    @Test fun `O wins middle row`() {
        assertWinner(boardOf("X","_","X", "O","O","O", "_","X","_"), CellValue.O)
    }

    @Test fun `O wins bottom row`() {
        assertWinner(boardOf("X","_","X", "_","X","_", "O","O","O"), CellValue.O)
    }

    // ──────────────────────────────────────────────────────────────────
    // Column wins
    // ──────────────────────────────────────────────────────────────────

    @Test fun `X wins left column`() {
        assertWinner(boardOf("X","O","_", "X","O","_", "X","_","_"), CellValue.X)
    }

    @Test fun `X wins middle column`() {
        assertWinner(boardOf("O","X","_", "_","X","O", "_","X","_"), CellValue.X)
    }

    @Test fun `X wins right column`() {
        assertWinner(boardOf("O","_","X", "O","_","X", "_","_","X"), CellValue.X)
    }

    @Test fun `O wins left column`() {
        assertWinner(boardOf("O","X","_", "O","X","_", "O","_","X"), CellValue.O)
    }

    @Test fun `O wins middle column`() {
        assertWinner(boardOf("X","O","_", "_","O","X", "_","O","_"), CellValue.O)
    }

    @Test fun `O wins right column`() {
        assertWinner(boardOf("X","_","O", "_","X","O", "_","_","O"), CellValue.O)
    }

    // ──────────────────────────────────────────────────────────────────
    // Diagonal wins
    // ──────────────────────────────────────────────────────────────────

    @Test fun `X wins main diagonal`() {
        assertWinner(boardOf("X","O","_", "O","X","_", "_","_","X"), CellValue.X)
    }

    @Test fun `X wins anti diagonal`() {
        assertWinner(boardOf("_","O","X", "O","X","_", "X","_","_"), CellValue.X)
    }

    @Test fun `O wins main diagonal`() {
        assertWinner(boardOf("O","X","_", "X","O","_", "_","_","O"), CellValue.O)
    }

    @Test fun `O wins anti diagonal`() {
        assertWinner(boardOf("X","_","O", "_","O","X", "O","_","_"), CellValue.O)
    }

    // ──────────────────────────────────────────────────────────────────
    // Winning line indices
    // ──────────────────────────────────────────────────────────────────

    @Test fun `winning line indices are correct for top row`() {
        val board = boardOf("X","X","X", "_","O","_", "O","_","_")
        val status = GameBoard.evaluate(board) as GameStatus.Won
        assertEquals(listOf(0, 1, 2), status.winningLine)
    }

    @Test fun `winning line indices are correct for right column`() {
        val board = boardOf("O","_","X", "_","_","X", "_","_","X")
        val status = GameBoard.evaluate(board) as GameStatus.Won
        assertEquals(listOf(2, 5, 8), status.winningLine)
    }

    @Test fun `winning line indices are correct for anti-diagonal`() {
        val board = boardOf("_","_","X", "_","X","_", "X","_","_")
        val status = GameBoard.evaluate(board) as GameStatus.Won
        assertEquals(listOf(2, 4, 6), status.winningLine)
    }

    // ──────────────────────────────────────────────────────────────────
    // Draw
    // ──────────────────────────────────────────────────────────────────

    @Test fun `full board with no winner is a draw`() {
        // X O X
        // X X O
        // O X O
        assertDraw(boardOf("X","O","X", "X","X","O", "O","X","O"))
    }

    @Test fun `another draw configuration`() {
        // O X O
        // O X X
        // X O X  — wait, need to verify no winner
        // X O X
        // X O O
        // O X X
        assertDraw(boardOf("X","O","X", "X","O","O", "O","X","X"))
    }

    // ──────────────────────────────────────────────────────────────────
    // In-progress
    // ──────────────────────────────────────────────────────────────────

    @Test fun `empty board is in progress`() {
        assertInProgress(GameBoard.emptyBoard())
    }

    @Test fun `partially filled board with no winner is in progress`() {
        assertInProgress(boardOf("X","_","_", "_","O","_", "_","_","X"))
    }

    // ──────────────────────────────────────────────────────────────────
    // Utility functions
    // ──────────────────────────────────────────────────────────────────

    @Test fun `getEmptyCells returns correct indices`() {
        val board = boardOf("X","_","O", "_","X","_", "O","_","_")
        assertEquals(listOf(1, 3, 5, 7, 8), GameBoard.getEmptyCells(board))
    }

    @Test fun `getEmptyCells returns empty list when board is full`() {
        val board = boardOf("X","O","X", "X","O","O", "O","X","X")
        assertTrue(GameBoard.getEmptyCells(board).isEmpty())
    }

    @Test fun `isFull returns true for a full board`() {
        assertTrue(GameBoard.isFull(boardOf("X","O","X","O","X","O","X","O","X")))
    }

    @Test fun `isFull returns false when one cell empty`() {
        assertFalse(GameBoard.isFull(boardOf("X","O","X","O","X","O","X","O","_")))
    }

    @Test fun `withMove applies symbol correctly`() {
        val board = GameBoard.emptyBoard()
        val updated = GameBoard.withMove(board, 4, CellValue.X)
        assertEquals(CellValue.X, updated[4])
        assertEquals(CellValue.EMPTY, updated[0])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `withMove throws on occupied cell`() {
        val board = boardOf("X","_","_","_","_","_","_","_","_")
        GameBoard.withMove(board, 0, CellValue.O)
    }

    @Test fun `hasWon returns true for winner`() {
        val board = boardOf("X","X","X","_","O","_","O","_","_")
        assertTrue(GameBoard.hasWon(board, CellValue.X))
        assertFalse(GameBoard.hasWon(board, CellValue.O))
    }

    @Test fun `countCells counts correctly`() {
        val board = boardOf("X","O","X","_","X","O","O","_","X")
        assertEquals(4, GameBoard.countCells(board, CellValue.X))
        assertEquals(3, GameBoard.countCells(board, CellValue.O))
        assertEquals(2, GameBoard.countCells(board, CellValue.EMPTY))
    }
}
