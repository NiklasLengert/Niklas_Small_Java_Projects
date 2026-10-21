package currencyconverter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Field Exchange | Currency Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new ConverterPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private static class ConverterPanel extends JPanel {
        private static final Color PANEL_COLOR = new Color(45, 57, 43);
        private static final Color SCREEN_COLOR = new Color(218, 224, 190);
        private static final Color SCREEN_TEXT_COLOR = new Color(48, 69, 47);
        private static final Color BUTTON_COLOR = new Color(102, 122, 83);
        private static final Color BUTTON_TEXT_COLOR = new Color(246, 241, 218);
        private static final Color OPERATION_COLOR = new Color(183, 119, 74);
        private static final Color CLEAR_COLOR = new Color(139, 82, 57);
        private static final Color EQUALS_COLOR = new Color(146, 170, 98);
        private static final Color OUTLINE_COLOR = new Color(31, 39, 30);

        private final FrankfurterClient rateClient = new FrankfurterClient();
        private final CurrencyConverter converter = new CurrencyConverter();
        private final JTextField amountInput = new JTextField("1.00");
        private final JComboBox<Currency> baseCurrency = new JComboBox<>();
        private final JComboBox<Currency> quoteCurrency = new JComboBox<>();
        private final JLabel resultDisplay = new JLabel("--", SwingConstants.CENTER);
        private final JLabel rateDetails = new JLabel("RATE DATE: --", SwingConstants.CENTER);
        private final JLabel status = new JLabel("LOADING CURRENCIES...", SwingConstants.CENTER);
        private final JButton convertButton = new JButton("CONVERT");
        private final JButton syncButton = new JButton("SYNC");

        private ConverterPanel() {
            setLayout(new BorderLayout(10, 12));
            setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            setBackground(PANEL_COLOR);

            add(createHeader(), BorderLayout.NORTH);
            add(createForm(), BorderLayout.CENTER);
            add(createFooter(), BorderLayout.SOUTH);

            loadCurrencies();
        }

        private JPanel createHeader() {
            JPanel header = new JPanel(new BorderLayout(8, 4));
            header.setOpaque(false);

            JPanel titles = new JPanel(new GridLayout(0, 1, 0, 3));
            titles.setOpaque(false);
            JLabel title = new JLabel("FIELD EXCHANGE");
            title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
            title.setForeground(BUTTON_TEXT_COLOR);
            JLabel subtitle = new JLabel("LATEST PUBLISHED CURRENCY RATES");
            subtitle.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            subtitle.setForeground(new Color(190, 194, 157));
            titles.add(title);
            titles.add(subtitle);
            header.add(titles, BorderLayout.CENTER);

            styleButton(syncButton, BUTTON_COLOR, BUTTON_TEXT_COLOR);
            syncButton.setPreferredSize(new Dimension(76, 38));
            syncButton.setToolTipText("Reload available currencies");
            syncButton.addActionListener(event -> loadCurrencies());
            header.add(syncButton, BorderLayout.EAST);
            return header;
        }

        private JPanel createForm() {
            JPanel form = new JPanel(new GridLayout(0, 1, 10, 10));
            form.setOpaque(false);

            amountInput.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
            amountInput.setForeground(SCREEN_TEXT_COLOR);
            amountInput.setBackground(SCREEN_COLOR);
            amountInput.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE_COLOR, 3),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            form.add(labeledControl("AMOUNT  (DECIMALS: . OR ,)", amountInput));

            styleCurrencySelector(baseCurrency);
            styleCurrencySelector(quoteCurrency);
            JButton swapButton = new JButton("SWAP");
            styleButton(swapButton, OPERATION_COLOR, OUTLINE_COLOR);
            swapButton.addActionListener(event -> swapCurrencies());

            JPanel currencyRow = new JPanel(new GridBagLayout());
            currencyRow.setOpaque(false);
            GridBagConstraints currencyConstraints = new GridBagConstraints();
            currencyConstraints.gridy = 0;
            currencyConstraints.fill = GridBagConstraints.HORIZONTAL;
            currencyConstraints.weightx = 1;
            currencyConstraints.insets = new Insets(0, 0, 0, 8);
            currencyRow.add(labeledControl("FROM", baseCurrency), currencyConstraints);

            currencyConstraints.gridx = 1;
            currencyConstraints.weightx = 0;
            currencyConstraints.insets = new Insets(0, 0, 0, 0);
            swapButton.setPreferredSize(new Dimension(88, 42));
            currencyRow.add(swapButton, currencyConstraints);

            currencyConstraints.gridx = 2;
            currencyConstraints.weightx = 1;
            currencyConstraints.insets = new Insets(0, 8, 0, 0);
            currencyRow.add(labeledControl("TO", quoteCurrency), currencyConstraints);
            form.add(currencyRow);

            resultDisplay.setOpaque(true);
            resultDisplay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 27));
            resultDisplay.setForeground(SCREEN_TEXT_COLOR);
            resultDisplay.setBackground(SCREEN_COLOR);
            resultDisplay.setPreferredSize(new Dimension(720, 66));
            resultDisplay.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE_COLOR, 4),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            form.add(resultDisplay);

            styleButton(convertButton, EQUALS_COLOR, OUTLINE_COLOR);
            convertButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
            convertButton.setPreferredSize(new Dimension(720, 48));
            convertButton.addActionListener(event -> convertAmount());
            convertButton.setEnabled(false);
            form.add(convertButton);

            rateDetails.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            rateDetails.setForeground(BUTTON_TEXT_COLOR);
            rateDetails.setToolTipText("Rates update daily and are not live trading quotes.");
            form.add(rateDetails);
            return form;
        }

        private JPanel createFooter() {
            JPanel footer = new JPanel(new GridLayout(0, 1, 0, 3));
            footer.setOpaque(false);
            status.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            status.setForeground(new Color(207, 188, 139));
            JLabel source = new JLabel("FRANKFURTER API  /  DAILY REFERENCE DATA", SwingConstants.CENTER);
            source.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            source.setForeground(new Color(190, 194, 157));
            footer.add(status);
            footer.add(source);
            return footer;
        }

        private JPanel labeledControl(String label, JComponent control) {
            JPanel block = new JPanel(new BorderLayout(0, 4));
            block.setOpaque(false);
            JLabel caption = new JLabel(label);
            caption.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            caption.setForeground(BUTTON_TEXT_COLOR);
            block.add(caption, BorderLayout.NORTH);
            block.add(control, BorderLayout.CENTER);
            return block;
        }

        private void styleCurrencySelector(JComboBox<Currency> selector) {
            selector.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            selector.setForeground(SCREEN_TEXT_COLOR);
            selector.setBackground(SCREEN_COLOR);
            selector.setMaximumRowCount(10);
            selector.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
                    setBackground(isSelected ? BUTTON_COLOR : SCREEN_COLOR);
                    setForeground(isSelected ? BUTTON_TEXT_COLOR : SCREEN_TEXT_COLOR);
                    return this;
                }
            });
        }

        private void styleButton(JButton button, Color background, Color foreground) {
            button.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            button.setForeground(foreground);
            button.setBackground(background);
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE_COLOR, 3),
                    BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        }

        private void loadCurrencies() {
            syncButton.setEnabled(false);
            convertButton.setEnabled(false);
            status.setText("FETCHING CURRENCY LIST...");

            rateClient.fetchCurrencies().whenComplete((currencies, error) ->
                    SwingUtilities.invokeLater(() -> {
                        syncButton.setEnabled(true);
                        if (error != null) {
                            status.setText("CURRENCY LIST FAILED - CHECK CONNECTION, THEN SYNC");
                            return;
                        }

                        String previousBase = selectedCode(baseCurrency);
                        String previousQuote = selectedCode(quoteCurrency);
                        setCurrencies(baseCurrency, currencies);
                        setCurrencies(quoteCurrency, currencies);
                        selectCurrency(baseCurrency, previousBase == null ? "USD" : previousBase);
                        selectCurrency(quoteCurrency, previousQuote == null ? "EUR" : previousQuote);
                        convertButton.setEnabled(true);
                        status.setText("READY - CONVERT TO FETCH THE LATEST RATE");
                    }));
        }

        private void convertAmount() {
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountInput.getText().trim().replace(',', '.'));
                if (amount.signum() < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException exception) {
                status.setText("ENTER A VALID NON-NEGATIVE AMOUNT");
                amountInput.requestFocusInWindow();
                return;
            }

            Currency base = (Currency) baseCurrency.getSelectedItem();
            Currency quote = (Currency) quoteCurrency.getSelectedItem();
            if (base == null || quote == null) {
                status.setText("CURRENCY LIST IS NOT AVAILABLE - PRESS SYNC");
                return;
            }

            if (base.code().equals(quote.code())) {
                showResult(amount, amount, base, quote, BigDecimal.ONE, "SAME CURRENCY");
                return;
            }

            convertButton.setEnabled(false);
            status.setText("FETCHING LATEST PUBLISHED RATE...");
            rateClient.fetchRate(base.code(), quote.code()).whenComplete((rate, error) ->
                    SwingUtilities.invokeLater(() -> {
                        convertButton.setEnabled(true);
                        if (error != null) {
                            status.setText("RATE UNAVAILABLE - " + errorMessage(error));
                            return;
                        }

                        BigDecimal converted = converter.convert(amount, rate.rate());
                        showResult(amount, converted, base, quote, rate.rate(), rate.date().toString());
                    }));
        }

        private void showResult(
                BigDecimal amount,
                BigDecimal converted,
                Currency base,
                Currency quote,
                BigDecimal rate,
                String dateText) {
            resultDisplay.setText(formatAmount(converted) + " " + quote.code());
            rateDetails.setText("1 " + base.code() + " = " + formatRate(rate)
                    + " " + quote.code() + "  /  " + dateText);
            status.setText("" + formatAmount(amount) + " " + base.code()
                    + " CONVERTED TO " + quote.code());
        }

        private void swapCurrencies() {
            Currency base = (Currency) baseCurrency.getSelectedItem();
            Currency quote = (Currency) quoteCurrency.getSelectedItem();
            if (base == null || quote == null) {
                return;
            }

            baseCurrency.setSelectedItem(quote);
            quoteCurrency.setSelectedItem(base);
            resultDisplay.setText("--");
            rateDetails.setText("RATE DATE: --");
            status.setText("CURRENCIES SWAPPED - CONVERT TO REFRESH RATE");
        }

        private void setCurrencies(JComboBox<Currency> selector, List<Currency> currencies) {
            ComboBoxModel<Currency> model = new DefaultComboBoxModel<>(currencies.toArray(Currency[]::new));
            selector.setModel(model);
        }

        private void selectCurrency(JComboBox<Currency> selector, String code) {
            for (int index = 0; index < selector.getItemCount(); index++) {
                Currency currency = selector.getItemAt(index);
                if (currency.code().equals(code)) {
                    selector.setSelectedIndex(index);
                    return;
                }
            }
        }

        private String selectedCode(JComboBox<Currency> selector) {
            Currency selected = (Currency) selector.getSelectedItem();
            return selected == null ? null : selected.code();
        }

        private String formatAmount(BigDecimal amount) {
            NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
            format.setMinimumFractionDigits(2);
            format.setMaximumFractionDigits(2);
            return format.format(amount);
        }

        private String formatRate(BigDecimal rate) {
            return rate.stripTrailingZeros().toPlainString();
        }

        private String errorMessage(Throwable error) {
            Throwable cause = error;
            while (cause instanceof CompletionException && cause.getCause() != null) {
                cause = cause.getCause();
            }
            String message = cause.getMessage();
            if (message == null || message.isBlank()) {
                return "CHECK INTERNET CONNECTION";
            }
            return message.toUpperCase(Locale.ROOT);
        }
    }
}