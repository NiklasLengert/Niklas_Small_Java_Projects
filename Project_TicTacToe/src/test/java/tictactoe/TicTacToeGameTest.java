package tictactoe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicTacToeGameTest {
    @Test
    void startsWithXAndAlternatesTurns() {
        TicTacToeGame game = new TicTacToeGame();

        assertEquals('X', game.getCurrentPlayer());
        game.play(1, 1);
        assertEquals('X', game.getCell(1, 1));
        assertEquals('O', game.getCurrentPlayer());
    }

    @Test
    void rejectsOccupiedAndOutOfBoundsCells() {
        TicTacToeGame game = new TicTacToeGame();
        game.play(0, 0);

        assertThrows(IllegalArgumentException.class, () -> game.play(0, 0));
        assertThrows(IllegalArgumentException.class, () -> game.play(3, 0));
        assertEquals('O', game.getCurrentPlayer());
    }

    @Test
    void detectsRowWinAndStopsTheGame() {
        TicTacToeGame game = new TicTacToeGame();
        game.play(0, 0);
        game.play(1, 0);
        game.play(0, 1);
        game.play(1, 1);
        game.play(0, 2);

        assertEquals('X', game.getWinner());
        assertTrue(game.isGameOver());
        assertThrows(IllegalStateException.class, () -> game.play(2, 2));
    }

    @Test
    void detectsColumnWin() {
        TicTacToeGame game = new TicTacToeGame();
        game.play(0, 2);
        game.play(0, 0);
        game.play(1, 2);
        game.play(1, 0);
        game.play(2, 2);

        assertEquals('X', game.getWinner());
    }

    @Test
    void detectsDiagonalWin() {
        TicTacToeGame game = new TicTacToeGame();
        game.play(0, 0);
        game.play(0, 1);
        game.play(1, 1);
        game.play(0, 2);
        game.play(2, 2);

        assertEquals('X', game.getWinner());
    }

    @Test
    void detectsDraw() {
        TicTacToeGame game = new TicTacToeGame();
        game.play(0, 0);
        game.play(0, 1);
        game.play(0, 2);
        game.play(1, 1);
        game.play(1, 0);
        game.play(1, 2);
        game.play(2, 1);
        game.play(2, 0);
        game.play(2, 2);

        assertTrue(game.isDraw());
        assertTrue(game.isGameOver());
        assertEquals(TicTacToeGame.EMPTY, game.getWinner());
    }

    @Test
    void resetRestoresAnEmptyBoardAndXTurn() {
        TicTacToeGame game = new TicTacToeGame();
        game.play(1, 1);
        game.reset();

        assertEquals(TicTacToeGame.EMPTY, game.getCell(1, 1));
        assertEquals('X', game.getCurrentPlayer());
        assertFalse(game.isGameOver());
    }
}