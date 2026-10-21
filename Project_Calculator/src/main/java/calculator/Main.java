package calculator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.IntBinaryOperator;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Main {
    private static final Color PANEL_COLOR = new Color(45, 57, 43);
    private static final Color SCREEN_COLOR = new Color(218, 224, 190);
    private static final Color SCREEN_TEXT_COLOR = new Color(48, 69, 47);
    private static final Color BUTTON_COLOR = new Color(102, 122, 83);
    private static final Color BUTTON_TEXT_COLOR = new Color(246, 241, 218);
    private static final Color OPERATION_COLOR = new Color(183, 119, 74);
    private static final Color CLEAR_COLOR = new Color(139, 82, 57);
    private static final Color EQUALS_COLOR = new Color(146, 170, 98);
    private static final Color OUTLINE_COLOR = new Color(31, 39, 30);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Pixel Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new CalculatorPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private static class CalculatorPanel extends JPanel {
        private final JTextField display = new JTextField("0");
        private int storedValue;
        private IntBinaryOperator pendingOperation;
        private boolean replaceDisplay = true;

        private CalculatorPanel() {
            setLayout(new BorderLayout(8, 12));
            setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            setBackground(PANEL_COLOR);

            display.setEditable(false);
            display.setHorizontalAlignment(SwingConstants.RIGHT);
            display.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
            display.setForeground(SCREEN_TEXT_COLOR);
            display.setBackground(SCREEN_COLOR);
            display.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE_COLOR, 4),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            display.setPreferredSize(new Dimension(280, 68));
            add(display, BorderLayout.NORTH);

            JPanel keypad = new JPanel(new GridLayout(4, 4, 8, 8));
            keypad.setOpaque(false);
            String[][] buttons = {
                {"7", "8", "9", "÷"},
                {"4", "5", "6", "×"},
                {"1", "2", "3", "-"},
                {"C", "0", "=", "+"}
            };

            for (String[] row : buttons) {
                for (String label : row) {
                    JButton button = createButton(label);
                    keypad.add(button);
                }
            }

            add(keypad, BorderLayout.CENTER);
        }

        private JButton createButton(String label) {
            JButton button = new JButton(label);
            button.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
            button.setPreferredSize(new Dimension(60, 52));
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setBorderPainted(true);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE_COLOR, 3),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)));

            if (label.equals("=")) {
                button.setBackground(EQUALS_COLOR);
                button.setForeground(OUTLINE_COLOR);
            } else if (label.equals("C")) {
                button.setBackground(CLEAR_COLOR);
                button.setForeground(Color.WHITE);
            } else if (isOperation(label)) {
                button.setBackground(OPERATION_COLOR);
                button.setForeground(OUTLINE_COLOR);
            } else {
                button.setBackground(BUTTON_COLOR);
                button.setForeground(BUTTON_TEXT_COLOR);
            }

            button.addActionListener(event -> handleButton(label));
            return button;
        }

        private void handleButton(String label) {
            if (label.length() == 1 && Character.isDigit(label.charAt(0))) {
                enterDigit(label);
            } else if (label.equals("C")) {
                clear();
            } else if (label.equals("=")) {
                calculate();
            } else {
                chooseOperation(operationFor(label));
            }
        }

        private void enterDigit(String digit) {
            if (replaceDisplay || display.getText().equals("Error")) {
                display.setText(digit);
            } else if (display.getText().equals("0")) {
                display.setText(digit);
            } else {
                display.setText(display.getText() + digit);
            }
            replaceDisplay = false;
        }

        private void chooseOperation(IntBinaryOperator operation) {
            try {
                int currentValue = Integer.parseInt(display.getText());
                if (pendingOperation != null && !replaceDisplay) {
                    currentValue = pendingOperation.applyAsInt(storedValue, currentValue);
                    display.setText(Integer.toString(currentValue));
                }
                storedValue = currentValue;
                pendingOperation = operation;
                replaceDisplay = true;
            } catch (IllegalArgumentException exception) {
                showError();
            }
        }

        private void calculate() {
            if (pendingOperation == null || replaceDisplay) {
                return;
            }

            try {
                int currentValue = Integer.parseInt(display.getText());
                int result = pendingOperation.applyAsInt(storedValue, currentValue);
                display.setText(Integer.toString(result));
                pendingOperation = null;
                replaceDisplay = true;
            } catch (IllegalArgumentException exception) {
                showError();
            }
        }

        private void clear() {
            display.setText("0");
            storedValue = 0;
            pendingOperation = null;
            replaceDisplay = true;
        }

        private void showError() {
            display.setText("Error");
            storedValue = 0;
            pendingOperation = null;
            replaceDisplay = true;
        }

        private static boolean isOperation(String label) {
            return label.equals("+") || label.equals("-")
                    || label.equals("×") || label.equals("÷");
        }

        private static IntBinaryOperator operationFor(String label) {
            return switch (label) {
                case "+" -> Main::add;
                case "-" -> Main::subtract;
                case "×" -> Main::multiply;
                case "÷" -> Main::divide;
                default -> throw new IllegalArgumentException("Unknown operation: " + label);
            };
        }
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static int subtract(int a, int b) {
        return a - b;
    }

    public static int multiply(int a, int b) {
        return a * b;
    }

    public static int divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Division by zero is not allowed.");
        }
        return a / b;
    }
}