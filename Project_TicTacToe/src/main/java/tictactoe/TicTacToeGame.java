package tictactoe;

public class TicTacToeGame {
    public static final int BOARD_SIZE = 3;
    public static final char EMPTY = ' ';

    private final char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
    private char currentPlayer;
    private char winner;
    private int movesPlayed;

    public TicTacToeGame() {
        reset();
    }

    public char getCell(int row, int column) {
        checkBounds(row, column);
        return board[row][column];
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    public char getWinner() {
        return winner;
    }

    public boolean isDraw() {
        return winner == EMPTY && movesPlayed == BOARD_SIZE * BOARD_SIZE;
    }

    public boolean isGameOver() {
        return winner != EMPTY || isDraw();
    }

    public void play(int row, int column) {
        checkBounds(row, column);
        if (isGameOver()) {
            throw new IllegalStateException("The game is already over.");
        }
        if (board[row][column] != EMPTY) {
            throw new IllegalArgumentException("That cell is already occupied.");
        }

        board[row][column] = currentPlayer;
        movesPlayed++;

        if (hasWon(currentPlayer)) {
            winner = currentPlayer;
        } else if (!isDraw()) {
            currentPlayer = currentPlayer == 'X' ? 'O' : 'X';
        }
    }

    public void reset() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                board[row][column] = EMPTY;
            }
        }
        currentPlayer = 'X';
        winner = EMPTY;
        movesPlayed = 0;
    }

    private boolean hasWon(char player) {
        for (int index = 0; index < BOARD_SIZE; index++) {
            if (board[index][0] == player && board[index][1] == player
                    && board[index][2] == player) {
                return true;
            }
            if (board[0][index] == player && board[1][index] == player
                    && board[2][index] == player) {
                return true;
            }
        }

        return (board[0][0] == player && board[1][1] == player && board[2][2] == player)
                || (board[0][2] == player && board[1][1] == player && board[2][0] == player);
    }

    private static void checkBounds(int row, int column) {
        if (row < 0 || row >= BOARD_SIZE || column < 0 || column >= BOARD_SIZE) {
            throw new IllegalArgumentException("Cell coordinates must be between 0 and 2.");
        }
    }
}