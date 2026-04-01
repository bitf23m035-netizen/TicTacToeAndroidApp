package com.tictactoe.game

import com.tictactoe.game.model.CellValue
import com.tictactoe.game.model.GameState
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [GameState] data class logic.
 */
class GameStateTest {

    private fun emptyState() = GameState()

    // ──────────────────────────────────────────────────────────────────
    // isMoveAllowed
    // ──────────────────────────────────────────────────────────────────

    @Test fun `isMoveAllowed returns true for empty cell on in-progress game`() {
        assertTrue(emptyState().isMoveAllowed(0))
        assertTrue(emptyState().isMoveAllowed(4))
        assertTrue(emptyState().isMoveAllowed(8))
    }

    @Test fun `isMoveAllowed returns false for out-of-bounds index`() {
        assertFalse(emptyState().isMoveAllowed(-1))
        assertFalse(emptyState().isMoveAllowed(9))
        assertFalse(emptyState().isMoveAllowed(100))
    }

    @Test fun `isMoveAllowed returns false for occupied cell`() {
        val state = emptyState().applyMove(4) // X placed at 4
        assertFalse(state.isMoveAllowed(4))
    }

    @Test fun `isMoveAllowed returns false when game is over`() {
        val state = emptyState().copy(isGameOver = true)
        assertFalse(state.isMoveAllowed(0))
    }

    // ──────────────────────────────────────────────────────────────────
    // applyMove
    // ──────────────────────────────────────────────────────────────────

    @Test fun `applyMove places X on first turn`() {
        val state = emptyState()
        val next = state.applyMove(3)
        assertEquals(CellValue.X, next.board[3])
    }

    @Test fun `applyMove switches player`() {
        val state = emptyState()
        val afterX = state.applyMove(0)
        assertEquals(CellValue.O, afterX.currentPlayer)
        val afterO = afterX.applyMove(1)
        assertEquals(CellValue.X, afterO.currentPlayer)
    }

    @Test fun `applyMove increments moveCount`() {
        var state = emptyState()
        for (i in 0..4) {
            state = state.applyMove(i)
            assertEquals(i + 1, state.moveCount)
        }
    }

    @Test fun `applyMove does not mutate original state`() {
        val original = emptyState()
        original.applyMove(4)
        assertEquals(CellValue.EMPTY, original.board[4]) // original unchanged
    }

    @Test(expected = IllegalArgumentException::class)
    fun `applyMove throws when move not allowed`() {
        val state = emptyState().copy(isGameOver = true)
        state.applyMove(0)
    }

    // ──────────────────────────────────────────────────────────────────
    // CellValue helpers
    // ──────────────────────────────────────────────────────────────────

    @Test fun `CellValue opponent works correctly`() {
        assertEquals(CellValue.O, CellValue.X.opponent())
        assertEquals(CellValue.X, CellValue.O.opponent())
        assertEquals(CellValue.EMPTY, CellValue.EMPTY.opponent())
    }

    @Test fun `CellValue toString works correctly`() {
        assertEquals("X", CellValue.X.toString())
        assertEquals("O", CellValue.O.toString())
        assertEquals("", CellValue.EMPTY.toString())
    }
}
