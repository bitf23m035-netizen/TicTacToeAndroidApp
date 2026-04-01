package com.tictactoe.game.engine

import com.tictactoe.game.model.CellValue
import com.tictactoe.game.model.GameStatus

/**
 * Pure logic class for the Tic Tac Toe board.
 * Contains all win/draw detection and board utility functions.
 * No Android dependencies — fully unit-testable.
 */
object GameBoard {

    /** All 8 possible winning lines (row, column, diagonal indices). */
    val WIN_CONDITIONS: List<List<Int>> = listOf(
        // Rows
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        // Columns
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        // Diagonals
        listOf(0, 4, 8),
        listOf(2, 4, 6)
    )

    /**
     * Evaluates the current board and returns a [GameStatus].
     *
     * @param board A 9-element list representing the 3×3 grid.
     */
    fun evaluate(board: List<CellValue>): GameStatus {
        require(board.size == 9) { "Board must have exactly 9 cells" }

        // Check all win conditions
        for (line in WIN_CONDITIONS) {
            val (a, b, c) = line
            val cellA = board[a]
            if (cellA != CellValue.EMPTY && cellA == board[b] && cellA == board[c]) {
                return GameStatus.Won(winner = cellA, winningLine = line)
            }
        }

        // Check for draw (no empty cells and no winner)
        return if (board.none { it == CellValue.EMPTY }) {
            GameStatus.Draw
        } else {
            GameStatus.InProgress
        }
    }

    /**
     * Returns a list of indices for all empty cells on the board.
     */
    fun getEmptyCells(board: List<CellValue>): List<Int> =
        board.indices.filter { board[it] == CellValue.EMPTY }

    /**
     * Returns true if the given player has won on this board.
     */
    fun hasWon(board: List<CellValue>, player: CellValue): Boolean {
        if (player == CellValue.EMPTY) return false
        return WIN_CONDITIONS.any { (a, b, c) ->
            board[a] == player && board[b] == player && board[c] == player
        }
    }

    /**
     * Returns a fresh empty board.
     */
    fun emptyBoard(): List<CellValue> = List(9) { CellValue.EMPTY }

    /**
     * Returns a board with the given move applied.
     */
    fun withMove(board: List<CellValue>, index: Int, player: CellValue): List<CellValue> {
        require(index in 0..8) { "Index $index out of bounds" }
        require(board[index] == CellValue.EMPTY) { "Cell $index is already occupied" }
        return board.toMutableList().also { it[index] = player }
    }

    /**
     * Counts how many cells a player occupies.
     */
    fun countCells(board: List<CellValue>, player: CellValue): Int =
        board.count { it == player }

    /**
     * Checks if the board is full.
     */
    fun isFull(board: List<CellValue>): Boolean =
        board.none { it == CellValue.EMPTY }

    /**
     * Returns a formatted string representation of the board for debugging.
     */
    fun format(board: List<CellValue>): String = buildString {
        for (row in 0..2) {
            for (col in 0..2) {
                val idx = row * 3 + col
                append(if (board[idx] == CellValue.EMPTY) "." else board[idx].toString())
                if (col < 2) append("|")
            }
            if (row < 2) append("\n-+-+-\n")
        }
    }
}
