package com.tictactoe.game.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.tictactoe.game.R
import com.tictactoe.game.databinding.ActivityMenuBinding
import com.tictactoe.game.model.Difficulty
import com.tictactoe.game.model.GameMode

/**
 * Main menu where the user selects game mode and difficulty.
 */
class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnimations()
        setupClickListeners()
    }

    private fun setupAnimations() {
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        binding.tvTitle.startAnimation(slideIn)
        binding.cardPvp.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up).also {
            it.startOffset = 150
        })
        binding.cardPvc.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up).also {
            it.startOffset = 300
        })
    }

    private fun setupClickListeners() {
        // Player vs Player
        binding.cardPvp.setOnClickListener {
            launchGame(GameMode.PLAYER_VS_PLAYER, Difficulty.HARD)
        }

        // Player vs Computer — show difficulty selector
        binding.cardPvc.setOnClickListener {
            toggleDifficultyPanel()
        }

        binding.btnEasy.setOnClickListener {
            launchGame(GameMode.PLAYER_VS_COMPUTER, Difficulty.EASY)
        }
        binding.btnMedium.setOnClickListener {
            launchGame(GameMode.PLAYER_VS_COMPUTER, Difficulty.MEDIUM)
        }
        binding.btnHard.setOnClickListener {
            launchGame(GameMode.PLAYER_VS_COMPUTER, Difficulty.HARD)
        }
    }

    private fun toggleDifficultyPanel() {
        val panel = binding.difficultyPanel
        if (panel.visibility == View.VISIBLE) {
            panel.visibility = View.GONE
        } else {
            panel.visibility = View.VISIBLE
            panel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        }
    }

    private fun launchGame(mode: GameMode, difficulty: Difficulty) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra(GameActivity.EXTRA_GAME_MODE, mode.name)
            putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty.name)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
    }
}
