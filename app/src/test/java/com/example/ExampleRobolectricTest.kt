package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.chess.engine.ChessAiEngine
import com.example.chess.engine.MoveGenerator
import com.example.chess.model.AiDifficulty
import com.example.chess.model.GameState
import com.example.chess.model.PieceColor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Chess Master", appName)
    }

    @Test
    fun `initial board has 20 opening legal moves for White`() {
        val initial = GameState()
        val moves = MoveGenerator.generateLegalMoves(initial)
        // Standard chess: 16 pawn moves (8 single + 8 double) + 4 knight moves = 20 legal moves
        assertEquals(20, moves.size)
    }

    @Test
    fun `chess AI engine selects legal move from opening`() = runBlocking {
        val initial = GameState()
        val bestMove = ChessAiEngine.findBestMove(initial, AiDifficulty.BEGINNER)
        assertNotNull(bestMove)
        assertTrue(MoveGenerator.generateLegalMoves(initial).contains(bestMove))
    }
}
