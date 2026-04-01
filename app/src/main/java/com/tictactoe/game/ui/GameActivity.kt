package com.tictactoe.game.ui

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tictactoe.game.R
import com.tictactoe.game.databinding.ActivityGameBinding
import com.tictactoe.game.model.*
import com.tictactoe.game.utils.SoundManager
import com.tictactoe.game.viewmodel.GameViewModel
import com.tictactoe.game.viewmodel.UiEvent

/**
 * Main game Activity. Observes [GameViewModel] and renders the board.
 */
class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private val viewModel: GameViewModel by viewModels()
    private lateinit var soundManager: SoundManager

    /** Ordered list of the 9 cell views for easy indexed access. */
    private val cellViews by lazy {
        listOf(
            binding.cell0, binding.cell1, binding.cell2,
            binding.cell3, binding.cell4, binding.cell5,
            binding.cell6, binding.cell7, binding.cell8
        )
    }

    // ──────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        soundManager = SoundManager(this)

        val mode = GameMode.valueOf(
            intent.getStringExtra(EXTRA_GAME_MODE) ?: GameMode.PLAYER_VS_PLAYER.name
        )
        val difficulty = Difficulty.valueOf(
            intent.getStringExtra(EXTRA_DIFFICULTY) ?: Difficulty.HARD.name
        )

        viewModel.initGame(mode, difficulty)

        setupToolbar(mode)
        setupCellListeners()
        setupButtonListeners()
        observeViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }

    // ──────────────────────────────────────────────────────────────────
    // Setup
    // ──────────────────────────────────────────────────────────────────

    private fun setupToolbar(mode: GameMode) {
        binding.tvModeLabel.text = when (mode) {
            GameMode.PLAYER_VS_PLAYER -> getString(R.string.mode_pvp)
            GameMode.PLAYER_VS_COMPUTER -> getString(R.string.mode_pvc)
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupCellListeners() {
        cellViews.forEachIndexed { index, cell ->
            cell.setOnClickListener {
                it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.cell_tap))
                viewModel.onCellClicked(index)
            }
        }
    }

    private fun setupButtonListeners() {
        binding.btnRestart.setOnClickListener {
            soundManager.playClick()
            viewModel.resetRound()
        }
        binding.btnResetScores.setOnClickListener {
            soundManager.playClick()
            viewModel.resetGame()
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Observation
    // ──────────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.gameState.observe(this) { state -> renderState(state) }
        viewModel.uiEvent.observe(this) { event -> handleUiEvent(event) }
    }

    // ──────────────────────────────────────────────────────────────────
    // Rendering
    // ──────────────────────────────────────────────────────────────────

    private fun renderState(state: GameState) {
        renderBoard(state)
        renderTurnIndicator(state)
        renderScoreboard(state)
        renderResultBanner(state)
    }

    private fun renderBoard(state: GameState) {
        cellViews.forEachIndexed { index, cell ->
            val value = state.board[index]
            cell.text = value.toString()
            cell.isEnabled = state.isMoveAllowed(index)

            // Symbol colour
            cell.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (value == CellValue.X) R.color.player_x else R.color.player_o
                )
            )

            // Winning-line highlight
            val isWinCell = index in state.winningLine
            cell.setBackgroundResource(
                if (isWinCell) R.drawable.cell_winner_bg else R.drawable.cell_bg
            )
        }
    }

    private fun renderTurnIndicator(state: GameState) {
        binding.tvCurrentPlayer.apply {
            if (state.isGameOver) {
                visibility = View.INVISIBLE
            } else {
                visibility = View.VISIBLE
                text = getString(R.string.turn_indicator, state.currentPlayer)
                setTextColor(
                    ContextCompat.getColor(
                        this@GameActivity,
                        if (state.currentPlayer == CellValue.X) R.color.player_x else R.color.player_o
                    )
                )
            }
        }
    }

    private fun renderScoreboard(state: GameState) {
        binding.tvScoreX.text = state.scoreX.toString()
        binding.tvScoreO.text = state.scoreO.toString()
        binding.tvScoreDraw.text = state.scoreDraw.toString()
    }

    private fun renderResultBanner(state: GameState) {
        when {
            state.winner != null -> {
                val name = playerName(state.winner, state.gameMode)
                binding.tvResult.text = getString(R.string.result_winner, name)
                binding.tvResult.setTextColor(
                    ContextCompat.getColor(
                        this,
                        if (state.winner == CellValue.X) R.color.player_x else R.color.player_o
                    )
                )
                binding.tvResult.visibility = View.VISIBLE
            }
            state.isDraw -> {
                binding.tvResult.text = getString(R.string.result_draw)
                binding.tvResult.setTextColor(ContextCompat.getColor(this, R.color.draw_color))
                binding.tvResult.visibility = View.VISIBLE
            }
            else -> binding.tvResult.visibility = View.INVISIBLE
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // One-shot events
    // ──────────────────────────────────────────────────────────────────

    private fun handleUiEvent(event: UiEvent?) {
        when (event) {
            is UiEvent.PlayerWon -> {
                soundManager.playWin()
                animateWin()
                viewModel.consumeUiEvent()
            }
            is UiEvent.Draw -> {
                soundManager.playDraw()
                animateDraw()
                viewModel.consumeUiEvent()
            }
            null -> { /* nothing */ }
        }
    }

    private fun animateWin() {
        val bounce = AnimationUtils.loadAnimation(this, R.anim.bounce)
        binding.tvResult.startAnimation(bounce)
    }

    private fun animateDraw() {
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        binding.tvResult.startAnimation(shake)
    }

    // ──────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────

    private fun playerName(player: CellValue, mode: GameMode): String = when {
        mode == GameMode.PLAYER_VS_COMPUTER && player == CellValue.O ->
            getString(R.string.player_computer)
        player == CellValue.X -> getString(R.string.player_x_name)
        else -> getString(R.string.player_o_name)
    }

    // ──────────────────────────────────────────────────────────────────
    // Companion
    // ──────────────────────────────────────────────────────────────────

    companion object {
        const val EXTRA_GAME_MODE = "extra_game_mode"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
    }
}
