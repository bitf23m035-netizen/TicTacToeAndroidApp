package com.tictactoe.game

import com.tictactoe.game.ai.AIPlayer
import com.tictactoe.game.engine.GameBoard
import com.tictactoe.game.model.CellValue
import com.tictactoe.game.model.Difficulty
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [AIPlayer] covering all difficulty levels and critical strategic decisions.
 */
class AIPlayerTest {

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
    // EASY: should always return a valid empty cell
    // ──────────────────────────────────────────────────────────────────

    @Test fun `easy AI returns a valid empty cell`() {
        val board = boardOf("X","_","_", "_","O","_", "_","_","X")
        val move = AIPlayer.getBestMove(board, CellValue.O, Difficulty.EASY)
        assertTrue(move in GameBoard.getEmptyCells(board))
    }

    @Test fun `easy AI returns -1 when board is full`() {
        val board = boardOf("X","O","X","O","X","O","X","O","X")
        assertEquals(-1, AIPlayer.getBestMove(board, CellValue.O, Difficulty.EASY))
    }

    // ──────────────────────────────────────────────────────────────────
    // MEDIUM: win if possible
    // ──────────────────────────────────────────────────────────────────

    @Test fun `medium AI takes winning move in top row`() {
        // O can win by placing at index 2
        val board = boardOf("O","O","_", "X","X","_", "X","_","_")
        val move = AIPlayer.getBestMove(board, CellValue.O, Difficulty.MEDIUM)
        assertEquals(2, move)
    }

    @Test fun `medium AI takes winning move in left column`() {
        // O can win by placing at index 6
        val board = boardOf("O","X","_", "O","X","_", "_","_","X")
        val move = AIPlayer.getBestMove(board, CellValue.O, Difficulty.MEDIUM)
        assertEquals(6, move)
    }

    @Test fun `medium AI takes winning diagonal move`() {
        // O can win by placing at index 8
        val board = boardOf("O","X","_", "X","O","_", "_","_","_")
        val move = AIPlayer.getBestMove(board, CellValue.O, Difficulty.MEDIUM)
        assertEquals(8, move)
    }

    // ──────────────────────────────────────────────────────────────────
    // MEDIUM: block opponent winning move
    // ──────────────────────────────────────────────────────────────────

    @Test fun `medium AI blocks opponent winning move in bottom row`() {
        // X threatens to win at index 8 (bottom row: 6,7,8)
        val board = boardOf("O","_","_", "_","O","_", "X","X","_")
        val move = AIPlayer.getBestMove(board, CellValue.O, Difficulty.MEDIUM)
        assertEquals(8, move)
    }

    @Test fun `medium AI blocks opponent winning move in right column`() {
        // X threatens to win at index 8 (right column: 2,5,8)
        val board = boardOf("O","_","X", "_","O","X", "_","_","_")
        val move = AIPlayer.getBestMove(board, CellValue.O, Difficulty.MEDIUM)
        assertEquals(8, move)
    }

    @Test fun `medium AI prefers winning over blocking`() {
        // O can win at index 2 AND must block X at index 6; winning should take priority
        val board = boardOf("O","O","_", "X","_","_", "X","_","_")
        val move = AIPlayer.getBestMove(board, CellValue.O, Difficulty.MEDIUM)
        assertEquals(2, move) // win, not block
    }

    // ──────────────────────────────────────────────────────────────────
    // HARD (Minimax): never loses
    // ──────────────────────────────────────────────────────────────────

    @Test fun `hard AI takes immediate win`() {
        // O can win at index 2
        val board = boardOf("O","O","_", "X","X","_", "_","_","X")
        val move = AIPlayer.getBestMove(board, CellValue.O, Difficulty.HARD)
        assertEquals(2, move)
    }

    @Test fun `hard AI blocks opponent from winning`() {
        // X can win at index 7; O must block
        val board = boardOf("X","O","_", "_","X","_", "O","_","_")
        val move = AIPlayer.getBestMove(board, CellValue.O, Difficulty.HARD)
        assertEquals(8, move) // X threatens 0,4,8 diagonal
    }

    @Test fun `hard AI takes center on empty board`() {
        val board = GameBoard.emptyBoard()
        val move = AIPlayer.getBestMove(board, CellValue.X, Difficulty.HARD)
        assertEquals(4, move) // center is always optimal
    }

    @Test fun `hard AI does not lose from any opening position`() {
        // Simulate full game: hard AI as O, X plays all possible opening cells
        // AI should never lose (result should be draw or AI win)
        for (xFirst in 0..8) {
            var board = GameBoard.withMove(GameBoard.emptyBoard(), xFirst, CellValue.X)
            // Let AI play to completion
            var current = CellValue.O
            var steps = 0
            while (!GameBoard.isFull(board) && !GameBoard.hasWon(board, CellValue.X) && !GameBoard.hasWon(board, CellValue.O)) {
                if (current == CellValue.O) {
                    val ai = AIPlayer.getBestMove(board, CellValue.O, Difficulty.HARD)
                    if (ai == -1) break
                    board = GameBoard.withMove(board, ai, CellValue.O)
                } else {
                    val empties = GameBoard.getEmptyCells(board)
                    if (empties.isEmpty()) break
                    board = GameBoard.withMove(board, empties.first(), CellValue.X)
                }
                current = current.opponent()
                steps++
                if (steps > 9) break
            }
            // O (hard AI) must never lose
            assertFalse(
                "Hard AI lost when X opened at $xFirst",
                GameBoard.hasWon(board, CellValue.X)
            )
        }
    }

    @Test fun `hard AI wins when given winning opportunity`() {
        // O has two winning threats (fork) — hard AI must exploit
        val board = boardOf("O","_","_", "_","O","_", "_","_","_")
        val move = AIPlayer.getBestMove(board, CellValue.O, Difficulty.HARD)
        // Index 8 closes the main diagonal win
        assertEquals(8, move)
    }

    @Test fun `hard AI returns valid move on complex board`() {
        val board = boardOf("X","O","X", "O","_","X", "O","_","_")
        val move = AIPlayer.getBestMove(board, CellValue.O, Difficulty.HARD)
        assertTrue(move in GameBoard.getEmptyCells(board))
    }

    // ──────────────────────────────────────────────────────────────────
    // Edge cases
    // ──────────────────────────────────────────────────────────────────

    @Test fun `AI returns -1 on completely full board for all difficulties`() {
        val full = boardOf("X","O","X","O","X","O","X","O","X")
        assertEquals(-1, AIPlayer.getBestMove(full, CellValue.X, Difficulty.EASY))
        assertEquals(-1, AIPlayer.getBestMove(full, CellValue.X, Difficulty.MEDIUM))
        assertEquals(-1, AIPlayer.getBestMove(full, CellValue.X, Difficulty.HARD))
    }

    @Test fun `AI can play as X or O`() {
        val board = boardOf("_","_","_", "_","_","_", "_","_","_")
        val moveAsX = AIPlayer.getBestMove(board, CellValue.X, Difficulty.HARD)
        val moveAsO = AIPlayer.getBestMove(board, CellValue.O, Difficulty.HARD)
        assertEquals(4, moveAsX) // Both should prefer center
        assertEquals(4, moveAsO)
    }
}
