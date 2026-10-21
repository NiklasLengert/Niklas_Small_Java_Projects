package pokemonguesser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Pokedex Field Notes | Pokemon Guesser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new GuessingPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private static class GuessingPanel extends JPanel {
        private static final Color PANEL_COLOR = new Color(45, 57, 43);
        private static final Color SCREEN_COLOR = new Color(218, 224, 190);
        private static final Color SCREEN_TEXT_COLOR = new Color(48, 69, 47);
        private static final Color BUTTON_COLOR = new Color(102, 122, 83);
        private static final Color BUTTON_TEXT_COLOR = new Color(246, 241, 218);
        private static final Color ACCENT_COLOR = new Color(183, 119, 74);
        private static final Color RESET_COLOR = new Color(139, 82, 57);
        private static final Color SUCCESS_COLOR = new Color(146, 170, 98);
        private static final Color OUTLINE_COLOR = new Color(31, 39, 30);

        private final PokeApiClient apiClient = new PokeApiClient();
        private final JLabel recordLabel = new JLabel("POKEDEX RECORD // LOADING", SwingConstants.CENTER);
        private final JLabel typesLabel = new JLabel("TYPE: --", SwingConstants.CENTER);
        private final JLabel measurementsLabel = new JLabel("HEIGHT: --   WEIGHT: --", SwingConstants.CENTER);
        private final JLabel statusLabel = new JLabel("CONNECTING TO POKEAPI...", SwingConstants.CENTER);
        private final JPanel statsPanel = new JPanel(new GridLayout(2, 3, 8, 8));
        private final JTextField guessInput = new JTextField();
        private final JButton guessButton = new JButton("GUESS");
        private final JButton nextButton = new JButton("NEW POKEMON");

        private PokemonGuessingRound round;
        private Integer previousSpeciesId;

        private GuessingPanel() {
            setLayout(new BorderLayout(10, 12));
            setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            setBackground(PANEL_COLOR);
            setPreferredSize(new Dimension(680, 550));

            add(createHeader(), BorderLayout.NORTH);
            add(createContent(), BorderLayout.CENTER);
            add(createFooter(), BorderLayout.SOUTH);

            guessButton.addActionListener(event -> submitGuess());
            guessInput.addActionListener(event -> submitGuess());
            nextButton.addActionListener(event -> loadNextPokemon());
            loadNextPokemon();
        }

        private JPanel createHeader() {
            JPanel header = new JPanel(new BorderLayout(8, 4));
            header.setOpaque(false);

            JPanel titles = new JPanel(new GridLayout(0, 1, 0, 4));
            titles.setOpaque(false);
            JLabel title = new JLabel("POKEDEX FIELD NOTES");
            title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 23));
            title.setForeground(BUTTON_TEXT_COLOR);
            JLabel subtitle = new JLabel("IDENTIFY THE SPECIES FROM ITS FIELD RECORD");
            subtitle.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            subtitle.setForeground(new Color(190, 194, 157));
            titles.add(title);
            titles.add(subtitle);
            header.add(titles, BorderLayout.CENTER);

            styleButton(nextButton, RESET_COLOR, Color.WHITE);
            nextButton.setPreferredSize(new Dimension(150, 42));
            header.add(nextButton, BorderLayout.EAST);
            return header;
        }

        private JPanel createContent() {
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);

            JPanel record = new JPanel(new GridLayout(0, 1, 0, 6));
            record.setOpaque(true);
            record.setBackground(SCREEN_COLOR);
            record.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE_COLOR, 4),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));

            recordLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            recordLabel.setForeground(SCREEN_TEXT_COLOR);
            typesLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));
            typesLabel.setForeground(SCREEN_TEXT_COLOR);
            measurementsLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
            measurementsLabel.setForeground(SCREEN_TEXT_COLOR);
            record.add(recordLabel);
            record.add(typesLabel);
            record.add(measurementsLabel);
            content.add(record, BorderLayout.NORTH);

            statsPanel.setOpaque(false);
            content.add(statsPanel, BorderLayout.CENTER);

            JPanel guessArea = new JPanel(new BorderLayout(8, 8));
            guessArea.setOpaque(false);
            guessInput.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));
            guessInput.setForeground(SCREEN_TEXT_COLOR);
            guessInput.setBackground(SCREEN_COLOR);
            guessInput.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE_COLOR, 3),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            guessInput.setToolTipText("Enter a Pokemon name, then press Enter");
            guessArea.add(guessInput, BorderLayout.CENTER);

            styleButton(guessButton, SUCCESS_COLOR, OUTLINE_COLOR);
            guessButton.setPreferredSize(new Dimension(116, 44));
            guessArea.add(guessButton, BorderLayout.EAST);
            content.add(guessArea, BorderLayout.SOUTH);
            return content;
        }

        private JPanel createFooter() {
            JPanel footer = new JPanel(new GridLayout(0, 1, 0, 4));
            footer.setOpaque(false);
            statusLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
            statusLabel.setForeground(new Color(207, 188, 139));
            JLabel source = new JLabel("DATA: POKEAPI  /  5 GUESSES PER SPECIES", SwingConstants.CENTER);
            source.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            source.setForeground(new Color(190, 194, 157));
            footer.add(statusLabel);
            footer.add(source);
            return footer;
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

        private JPanel createStatCard(PokemonStat stat, int index) {
            JPanel card = new JPanel(new BorderLayout(4, 2));
            card.setOpaque(true);
            card.setBackground(index % 2 == 0 ? BUTTON_COLOR : new Color(82, 101, 69));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE_COLOR, 3),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));

            JLabel name = new JLabel(stat.displayName());
            name.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
            name.setForeground(BUTTON_TEXT_COLOR);
            JLabel value = new JLabel(Integer.toString(stat.value()), SwingConstants.RIGHT);
            value.setFont(new Font(Font.MONOSPACED, Font.BOLD, 23));
            value.setForeground(BUTTON_TEXT_COLOR);
            card.add(name, BorderLayout.NORTH);
            card.add(value, BorderLayout.CENTER);
            return card;
        }

        private void loadNextPokemon() {
            nextButton.setEnabled(false);
            guessButton.setEnabled(false);
            guessInput.setEnabled(false);
            guessInput.setText("");
            round = null;
            recordLabel.setText("POKEDEX RECORD // FETCHING SPECIES");
            typesLabel.setText("TYPE: --");
            measurementsLabel.setText("HEIGHT: --   WEIGHT: --");
            statsPanel.removeAll();
            statsPanel.revalidate();
            statsPanel.repaint();
            statusLabel.setText("REQUESTING RANDOM SPECIES FROM POKEAPI...");

            apiClient.fetchRandomPokemon(previousSpeciesId).whenComplete((profile, error) ->
                    SwingUtilities.invokeLater(() -> {
                        nextButton.setEnabled(true);
                        if (error != null) {
                            statusLabel.setText("POKEAPI UNAVAILABLE - CHECK INTERNET, THEN TRY NEW POKEMON");
                            return;
                        }

                        previousSpeciesId = profile.id();
                        round = new PokemonGuessingRound(profile);
                        recordLabel.setText(String.format(Locale.ROOT, "POKEDEX RECORD // #%03d", profile.id()));
                        typesLabel.setText("TYPE: " + profile.types().stream()
                                .map(type -> type.replace('-', ' ').toUpperCase(Locale.ROOT))
                                .reduce((first, second) -> first + " / " + second)
                                .orElse("UNKNOWN"));
                        measurementsLabel.setText(String.format(Locale.ROOT,
                                "HEIGHT: %.1f M     WEIGHT: %.1f KG",
                                profile.heightDecimeters() / 10.0,
                                profile.weightHectograms() / 10.0));

                        for (int index = 0; index < profile.stats().size(); index++) {
                            statsPanel.add(createStatCard(profile.stats().get(index), index));
                        }
                        statsPanel.revalidate();
                        statsPanel.repaint();
                        guessInput.setEnabled(true);
                        guessButton.setEnabled(true);
                        statusLabel.setText(PokemonGuessingRound.MAX_GUESSES + " GUESSES LEFT - WHO IS IT?");
                        guessInput.requestFocusInWindow();
                    }));
        }

        private void submitGuess() {
            if (round == null || !guessButton.isEnabled()) {
                return;
            }
            String guess = guessInput.getText().trim();
            if (guess.isEmpty()) {
                statusLabel.setText("TYPE A GUESS FIRST");
                guessInput.requestFocusInWindow();
                return;
            }

            PokemonGuessingRound.Result result = round.submitGuess(guess);
            switch (result) {
                case CORRECT -> finishRound("CORRECT! IT WAS " + displayName(round.getAnswer().speciesName()));
                case INCORRECT -> {
                    statusLabel.setText("NOT THIS ONE - " + round.getGuessesRemaining() + " GUESSES LEFT");
                    guessInput.setText("");
                    guessInput.requestFocusInWindow();
                }
                case REVEALED -> finishRound("THE ANSWER WAS " + displayName(round.getAnswer().speciesName()));
                case ROUND_FINISHED -> finishRound("ROUND OVER - " + displayName(round.getAnswer().speciesName()));
            }
        }

        private void finishRound(String message) {
            statusLabel.setText(message);
            guessInput.setEnabled(false);
            guessButton.setEnabled(false);
            nextButton.setEnabled(true);
        }

        private String displayName(String slug) {
            return slug.replace('-', ' ').toUpperCase(Locale.ROOT);
        }
    }
}