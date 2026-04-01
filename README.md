# Tic Tac Toe — Android App

A polished, production-grade Tic Tac Toe game for Android written in **Kotlin**.

---

## Features

| Feature | Details |
|---|---|
| **Game Modes** | Player vs Player · Player vs Computer |
| **AI Difficulty** | Easy (random) · Medium (win/block/strategic) · Hard (Minimax + alpha-beta pruning) |
| **UI** | Dark theme · Material Design · ConstraintLayout + GridLayout |
| **Animations** | Cell tap · Win bounce · Draw shake · Screen transitions |
| **Scoreboard** | Persistent across rounds; reset button included |
| **State Safety** | ViewModel survives screen rotation; no lost game state |
| **Edge Cases** | Filled-cell guard · Post-game-over lock · Draw detection · AI delay |
| **Tests** | 40+ unit tests: GameBoard · GameEngine · AIPlayer · GameState |

---

## Project Structure

```
TicTacToe/
├── app/
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/tictactoe/game/
│       │   │   ├── model/
│       │   │   │   └── GameState.kt          ← CellValue, GameStatus, Difficulty, GameMode, GameState
│       │   │   ├── engine/
│       │   │   │   ├── GameBoard.kt           ← Pure board logic, win/draw detection
│       │   │   │   └── GameEngine.kt          ← Move orchestration, score tracking
│       │   │   ├── ai/
│       │   │   │   └── AIPlayer.kt            ← Easy/Medium/Hard (Minimax + alpha-beta)
│       │   │   ├── viewmodel/
│       │   │   │   └── GameViewModel.kt       ← LiveData, AI coroutine, state survival
│       │   │   ├── ui/
│       │   │   │   ├── SplashActivity.kt
│       │   │   │   ├── MenuActivity.kt
│       │   │   │   └── GameActivity.kt
│       │   │   └── utils/
│       │   │       └── SoundManager.kt        ← SoundPool wrapper (graceful no-op if no audio)
│       │   └── res/
│       │       ├── layout/
│       │       │   ├── activity_splash.xml
│       │       │   ├── activity_menu.xml
│       │       │   └── activity_game.xml
│       │       ├── drawable/
│       │       │   ├── cell_bg.xml            ← Ripple + rounded rect cell
│       │       │   ├── cell_winner_bg.xml     ← Highlighted winning cell
│       │       │   ├── icon_btn_bg.xml
│       │       │   ├── ic_grid.xml            ← SVG splash logo
│       │       │   ├── ic_back.xml
│       │       │   ├── ic_reset.xml
│       │       │   └── ic_arrow_right.xml
│       │       ├── anim/
│       │       │   ├── fade_in.xml · fade_out.xml
│       │       │   ├── slide_up.xml
│       │       │   ├── slide_right_in.xml · slide_left_out.xml
│       │       │   ├── cell_tap.xml
│       │       │   ├── bounce.xml
│       │       │   └── shake.xml
│       │       ├── font/
│       │       │   ├── montserrat_bold.xml    ← Google Fonts downloadable font
│       │       │   └── montserrat_regular.xml
│       │       └── values/
│       │           ├── strings.xml
│       │           ├── colors.xml
│       │           ├── themes.xml
│       │           └── font_certs.xml
│       └── test/java/com/tictactoe/game/
│           ├── GameBoardTest.kt   (25 tests)
│           ├── AIPlayerTest.kt    (16 tests)
│           ├── GameEngineTest.kt  (18 tests)
│           └── GameStateTest.kt   (12 tests)
└── build.gradle
```

---

## Requirements

| Tool | Version |
|---|---|
| Android Studio | Hedgehog (2023.1.1) or newer |
| Kotlin | 1.9.10 |
| Compile SDK | 34 (Android 14) |
| Min SDK | 24 (Android 7.0) |
| Gradle | 8.2+ |

---

## How to Open & Run

### 1. Open in Android Studio

```
File → Open → select the TicTacToe/ root folder
```

Wait for Gradle sync to complete (first sync downloads dependencies).

### 2. Add Montserrat fonts (required — 2 minutes)

The app uses **Montserrat** via Google Fonts Downloadable Fonts.
The font XML files are already in `res/font/`. Android fetches the font at runtime automatically.

**If you prefer to bundle the font locally:**
1. Download `Montserrat-Bold.ttf` and `Montserrat-Regular.ttf` from [fonts.google.com/specimen/Montserrat](https://fonts.google.com/specimen/Montserrat)
2. Place both `.ttf` files in `app/src/main/res/font/`
3. In each layout/style XML that references `@font/montserrat_bold`, Android will use the `.ttf` automatically.

### 3. Add sound effects (optional)

Place these files in `app/src/main/res/raw/`:
- `click.mp3` — cell tap sound
- `win.mp3`   — victory sound  
- `draw.mp3`  — draw sound

If the files are absent, `SoundManager` silently skips playback — no crashes.

### 4. Run on device or emulator

- Select a device (API 24+ emulator or physical device)
- Press **Run ▶** (Shift+F10)

### 5. Run unit tests

```bash
# From terminal in project root:
./gradlew test

# Or in Android Studio:
# Right-click src/test → Run Tests
```

---

## Architecture

```
┌─────────────────────────────────────┐
│           UI Layer                  │
│  SplashActivity                     │
│  MenuActivity                       │
│  GameActivity  ←──── observes ────► │
└─────────────┬───────────────────────┘
              │ delegates to
              ▼
┌─────────────────────────────────────┐
│         ViewModel Layer             │
│  GameViewModel                      │
│  • LiveData<GameState>              │
│  • LiveData<UiEvent>                │
│  • Coroutine for AI delay           │
└─────────────┬───────────────────────┘
              │ calls
              ▼
┌─────────────────────────────────────┐
│          Engine Layer               │
│  GameEngine   ← pure Kotlin logic   │
│  GameBoard    ← board/win/draw      │
│  AIPlayer     ← Easy/Medium/Hard    │
└─────────────────────────────────────┘
              ▲
              │ operates on
┌─────────────────────────────────────┐
│          Model Layer                │
│  GameState  (Parcelable data class) │
│  CellValue  (enum)                  │
│  GameStatus (sealed class)          │
│  GameMode / Difficulty (enums)      │
└─────────────────────────────────────┘
```

**Key design decisions:**
- `GameState` is **immutable** — every operation returns a new copy. No shared mutable state.
- `GameBoard` and `AIPlayer` are **pure Kotlin singletons** (no Android dependencies) — fully testable with plain JUnit.
- `GameViewModel` holds the **only mutable state** via `MutableLiveData`, surviving rotation.
- AI moves run in a `viewModelScope` coroutine with a 600ms delay for natural feel.

---

## AI Algorithm — Minimax with Alpha-Beta Pruning

The **Hard** difficulty uses the classic Minimax algorithm with alpha-beta pruning:

```
minimax(board, depth, isMaximizing, alpha, beta):
  if terminal state:
    return score (±10 adjusted for depth to prefer faster wins)
  
  if maximizing (AI's turn):
    best = -∞
    for each empty cell:
      score = minimax(board + AI move, depth+1, false, alpha, beta)
      best = max(best, score)
      alpha = max(alpha, best)
      if beta ≤ alpha: break  // prune
    return best
  
  else (opponent's turn):
    best = +∞
    for each empty cell:
      score = minimax(board + opponent move, depth+1, true, alpha, beta)
      best = min(best, score)
      beta = min(beta, best)
      if beta ≤ alpha: break  // prune
    return best
```

On a 3×3 board the search tree is small enough to solve completely, so **Hard AI never loses**.

---

## Dependency List

```gradle
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0
androidx.constraintlayout:constraintlayout:2.1.4
androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0
androidx.lifecycle:lifecycle-livedata-ktx:2.7.0
androidx.activity:activity-ktx:1.8.2

// Test
junit:junit:4.13.2
org.mockito:mockito-core:5.3.1
```

---

## Troubleshooting

| Issue | Fix |
|---|---|
| `Unresolved reference: montserrat_bold` | Ensure font XML files exist in `res/font/` or add `.ttf` files |
| Fonts appear as system default | Add internet permission to manifest, or bundle `.ttf` files locally |
| Build fails on `Parcelize` | Add `id 'kotlin-parcelize'` plugin to `app/build.gradle` |
| GridLayout cells not square | Ensure `app:layout_constraintDimensionRatio="1:1"` is on the GridLayout |
| AI moves too fast | Increase `AI_MOVE_DELAY_MS` in `GameViewModel.kt` |
