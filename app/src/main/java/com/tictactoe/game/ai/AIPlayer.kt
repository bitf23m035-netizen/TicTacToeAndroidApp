package com.tictactoe.game.ai

import com.tictactoe.game.engine.GameBoard
import com.tictactoe.game.model.CellValue
import com.tictactoe.game.model.Difficulty

/**
 * AI opponent for Tic Tac Toe.
 *
 * - EASY   : Picks a random empty cell.
 * - MEDIUM : Wins if it can, blocks the opponent's winning move, else random.
 * - HARD   : Uses the Minimax algorithm — never loses.
 */
object AIPlayer {

    /**
     * Returns the index of the best cell to play for [aiPlayer] given [board] and [difficulty].
     * Returns -1 if no move is possible (board full).
     */
    fun getBestMove(
        board: List<CellValue>,
        aiPlayer: CellValue,
        difficulty: Difficulty
    ): Int {
        val emptyCells = GameBoard.getEmptyCells(board)
        if (emptyCells.isEmpty()) return -1

        return when (difficulty) {
            Difficulty.EASY -> getRandomMove(emptyCells)
            Difficulty.MEDIUM -> getMediumMove(board, aiPlayer, emptyCells)
            Difficulty.HARD -> getMinimaxMove(board, aiPlayer)
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // EASY: random
    // ──────────────────────────────────────────────────────────────────

    private fun getRandomMove(emptyCells: List<Int>): Int = emptyCells.random()

    // ──────────────────────────────────────────────────────────────────
    // MEDIUM: win > block > strategic > random
    // ──────────────────────────────────────────────────────────────────

    private fun getMediumMove(
        board: List<CellValue>,
        aiPlayer: CellValue,
        emptyCells: List<Int>
    ): Int {
        val opponent = aiPlayer.opponent()

        // 1. Win if possible
        findWinningMove(board, aiPlayer, emptyCells)?.let { return it }

        // 2. Block opponent's winning move
        findWinningMove(board, opponent, emptyCells)?.let { return it }

        // 3. Take center if free
        if (4 in emptyCells) return 4

        // 4. Take a corner
        val corners = emptyCells.filter { it in listOf(0, 2, 6, 8) }
        if (corners.isNotEmpty()) return corners.random()

        // 5. Random remaining
        return emptyCells.random()
    }

    /** Returns the first move in [emptyCells] that immediately wins for [player], or null. */
    private fun findWinningMove(
        board: List<CellValue>,
        player: CellValue,
        emptyCells: List<Int>
    ): Int? {
        for (index in emptyCells) {
            val testBoard = GameBoard.withMove(board, index, player)
            if (GameBoard.hasWon(testBoard, player)) return index
        }
        return null
    }

    // ──────────────────────────────────────────────────────────────────
    // HARD: Minimax with alpha-beta pruning
    // ──────────────────────────────────────────────────────────────────

    private fun getMinimaxMove(board: List<CellValue>, aiPlayer: CellValue): Int {
        var bestScore = Int.MIN_VALUE
        var bestMove = -1

        for (index in GameBoard.getEmptyCells(board)) {
            val newBoard = GameBoard.withMove(board, index, aiPlayer)
            val score = minimax(
                board = newBoard,
                depth = 0,
                isMaximizing = false,
                aiPlayer = aiPlayer,
                alpha = Int.MIN_VALUE,
                beta = Int.MAX_VALUE
            )
            if (score > bestScore) {
                bestScore = score
                bestMove = index
            }
        }
        return bestMove
    }

    /**
     * Minimax with alpha-beta pruning.
     *
     * @param board         current board state
     * @param depth         depth of recursion (used to prefer faster wins)
     * @param isMaximizing  true when it's the AI's turn
     * @param aiPlayer      the AI's CellValue (X or O)
     * @param alpha         best value the maximizer can guarantee so far
     * @param beta          best value the minimizer can guarantee so far
     */
    private fun minimax(
        board: List<CellValue>,
        depth: Int,
        isMaximizing: Boolean,
        aiPlayer: CellValue,
        alpha: Int,
        beta: Int
    ): Int {
        val opponent = aiPlayer.opponent()

        // Terminal state evaluation
        when {
            GameBoard.hasWon(board, aiPlayer) -> return 10 - depth   // AI wins faster = better
            GameBoard.hasWon(board, opponent) -> return depth - 10   // Opponent wins faster = worse
            GameBoard.isFull(board) -> return 0                       // Draw
        }

        val emptyCells = GameBoard.getEmptyCells(board)
        var currentAlpha = alpha
        var currentBeta = beta

        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (index in emptyCells) {
                val newBoard = GameBoard.withMove(board, index, aiPlayer)
                val score = minimax(newBoard, depth + 1, false, aiPlayer, currentAlpha, currentBeta)
                best = maxOf(best, score)
                currentAlpha = maxOf(currentAlpha, best)
                if (currentBeta <= currentAlpha) break // Beta cut-off
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for (index in emptyCells) {
                val newBoard = GameBoard.withMove(board, index, opponent)
                val score = minimax(newBoard, depth + 1, true, aiPlayer, currentAlpha, currentBeta)
                best = minOf(best, score)
                currentBeta = minOf(currentBeta, best)
                if (currentBeta <= currentAlpha) break // Alpha cut-off
            }
            best
        }
    }
}
