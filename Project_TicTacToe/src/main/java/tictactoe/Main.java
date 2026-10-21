package tictactoe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Pixel Tic-Tac-Toe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new GamePanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private static class GamePanel extends JPanel {
        private static final Color PANEL_COLOR = new Color(45, 57, 43);
        private static final Color SCREEN_COLOR = new Color(218, 224, 190);
        private static final Color SCREEN_TEXT_COLOR = new Color(48, 69, 47);
        private static final Color BUTTON_COLOR = new Color(102, 122, 83);
        private static final Color BUTTON_TEXT_COLOR = new Color(246, 241, 218);
        private static final Color X_COLOR = new Color(183, 119, 74);
        private static final Color RESET_COLOR = new Color(139, 82, 57);
        private static final Color O_COLOR = new Color(146, 170, 98);
        private static final Color OUTLINE_COLOR = new Color(31, 39, 30);

        private final TicTacToeGame game = new TicTacToeGame();
        private final JButton[][] cells = new JButton[3][3];
        private final JLabel status = new JLabel("PLAYER X'S TURN", SwingConstants.CENTER);

        private GamePanel() {
            setLayout(new BorderLayout(8, 12));
            setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            setBackground(PANEL_COLOR);

            status.setOpaque(true);
            status.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
            status.setForeground(SCREEN_TEXT_COLOR);
            status.setBackground(SCREEN_COLOR);
            status.setPreferredSize(new Dimension(304, 48));
            status.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE_COLOR, 4),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            add(status, BorderLayout.NORTH);

            JPanel board = new JPanel(new GridLayout(3, 3, 8, 8));
            board.setOpaque(false);
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    cells[row][column] = createCell(row, column);
                    board.add(cells[row][column]);
                }
            }
            add(board, BorderLayout.CENTER);

            JButton resetButton = new JButton("NEW GAME");
            resetButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
            resetButton.setForeground(Color.WHITE);
            resetButton.setBackground(RESET_COLOR);
            resetButton.setPreferredSize(new Dimension(304, 46));
            resetButton.setFocusPainted(false);
            resetButton.setOpaque(true);
            resetButton.setBorder(BorderFactory.createLineBorder(OUTLINE_COLOR, 3));
            resetButton.addActionListener(event -> resetGame());
            add(resetButton, BorderLayout.SOUTH);
        }

        private JButton createCell(int row, int column) {
            JButton cell = new JButton(" ");
            cell.setFont(new Font(Font.MONOSPACED, Font.BOLD, 44));
            cell.setForeground(BUTTON_TEXT_COLOR);
            cell.setBackground(BUTTON_COLOR);
            cell.setPreferredSize(new Dimension(96, 96));
            cell.setMargin(new Insets(0, 0, 0, 0));
            cell.setFocusPainted(false);
            cell.setOpaque(true);
            cell.setBorder(BorderFactory.createLineBorder(OUTLINE_COLOR, 4));
            cell.addActionListener(event -> playAt(row, column));
            return cell;
        }

        private void playAt(int row, int column) {
            if (game.isGameOver() || !cells[row][column].getText().isBlank()) {
                return;
            }

            char player = game.getCurrentPlayer();
            game.play(row, column);
            cells[row][column].setText(Character.toString(player));
            cells[row][column].setForeground(player == 'X' ? X_COLOR : O_COLOR);

            if (game.getWinner() != TicTacToeGame.EMPTY) {
                status.setText("PLAYER " + player + " WINS!");
            } else if (game.isDraw()) {
                status.setText("DRAW - NICE GAME");
            } else {
                status.setText("PLAYER " + game.getCurrentPlayer() + "'S TURN");
            }
        }

        private void resetGame() {
            game.reset();
            status.setText("PLAYER X'S TURN");
            for (JButton[] row : cells) {
                for (JButton cell : row) {
                    cell.setText(" ");
                    cell.setForeground(BUTTON_TEXT_COLOR);
                }
            }
        }
    }
}