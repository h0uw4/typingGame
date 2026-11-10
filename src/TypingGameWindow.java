import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// INHERITANCE: the window IS-A JFrame (from Swing)
// Screens: MENU -> OPTIONS / MODE -> GAME
// Reuses Challenge, Player, Result and SoundManager.
public class TypingGameWindow extends JFrame {
    private static final String MENU = "menu";
    private static final String OPTIONS = "options";
    private static final String MODE = "mode";
    private static final String GAME = "game";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final List<Challenge> challenges = new ArrayList<>();
    private final Player player = new Player("Player");
    private final SoundManager sounds = new SoundManager();

    // Mode screen
    private final JTextField nameField = new JTextField("Player", 15);

    // Game screen
    private final JTextArea targetArea = new JTextArea(3, 40);
    private final JTextField inputField = new JTextField();
    private final JLabel modeLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel resultLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel statsLabel = new JLabel(" ", SwingConstants.CENTER);

    private Challenge current;
    private String target = "";
    private long startTime = 0;     // 0 = timer not started yet

    public TypingGameWindow() {
        super("Typing Speed Game");

        // POLYMORPHISM: different subclasses stored as the same type
        challenges.add(new WordChallenge());
        challenges.add(new SentenceChallenge());
        challenges.add(new CodeChallenge());

        root.add(buildMenuPanel(), MENU);
        root.add(buildOptionsPanel(), OPTIONS);
        root.add(buildModePanel(), MODE);
        root.add(buildGamePanel(), GAME);
        add(root);

        showScreen(MENU);   // main menu is the first screen

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 460);
        setMinimumSize(new Dimension(620, 420));
        setLocationRelativeTo(null);
    }

    // Changes screen. Music plays on the menu screens and stops during the game.
    private void showScreen(String name) {
        cards.show(root, name);
        if (GAME.equals(name)) {
            sounds.stopMusic();
        } else {
            sounds.playMusic();
        }
    }

    // ---------------------- SCREENS ----------------------

    private JPanel buildMenuPanel() {
        JPanel p = verticalPanel();

        p.add(Box.createVerticalGlue());
        p.add(title("TYPING SPEED GAME"));
        p.add(Box.createVerticalStrut(30));
        p.add(menuButton("Start", () -> showScreen(MODE)));
        p.add(Box.createVerticalStrut(12));
        p.add(menuButton("Options", () -> showScreen(OPTIONS)));
        p.add(Box.createVerticalStrut(12));
        p.add(menuButton("Exit", () -> System.exit(0)));
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildOptionsPanel() {
        JPanel p = verticalPanel();

        JLabel soundLabel = new JLabel("Typing sound");
        soundLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        soundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JRadioButton thockButton = new JRadioButton(SoundManager.THOCK);
        JRadioButton clackButton = new JRadioButton(SoundManager.CLACK);
        thockButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        clackButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        thockButton.setFocusPainted(false);
        clackButton.setFocusPainted(false);

        ButtonGroup group = new ButtonGroup();
        group.add(thockButton);
        group.add(clackButton);
        if (SoundManager.CLACK.equals(sounds.getTypingSound())) {
            clackButton.setSelected(true);
        } else {
            thockButton.setSelected(true);
        }

        // Selecting a choice saves it and plays a short preview
        thockButton.addActionListener(e -> {
            sounds.setTypingSound(SoundManager.THOCK);
            sounds.playTyping();
        });
        clackButton.addActionListener(e -> {
            sounds.setTypingSound(SoundManager.CLACK);
            sounds.playTyping();
        });

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        row.setOpaque(false);
        row.add(thockButton);
        row.add(clackButton);
        row.setMaximumSize(new Dimension(400, 35));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalGlue());
        p.add(title("OPTIONS"));
        p.add(Box.createVerticalStrut(25));
        p.add(soundLabel);
        p.add(Box.createVerticalStrut(10));
        p.add(row);
        p.add(Box.createVerticalStrut(30));
        p.add(menuButton("Back", () -> showScreen(MENU)));
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildModePanel() {
        JPanel p = verticalPanel();

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        nameRow.setOpaque(false);
        nameRow.add(new JLabel("Your name:"));
        nameRow.add(nameField);
        nameRow.setMaximumSize(new Dimension(400, 30));
        nameRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalGlue());
        p.add(title("SELECT MODE"));
        p.add(Box.createVerticalStrut(20));
        p.add(nameRow);
        p.add(Box.createVerticalStrut(20));

        for (Challenge c : challenges) {
            String label = String.format("%s (x%.1f)", c.getName(), c.getMultiplier());
            p.add(menuButton(label, () -> {
                player.setName(nameField.getText());   // setter validates the name
                updateStats();
                startRound(c);
            }));
            p.add(Box.createVerticalStrut(10));
        }

        p.add(Box.createVerticalStrut(10));
        p.add(menuButton("Back", () -> showScreen(MENU)));
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildGamePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        modeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        targetArea.setEditable(false);
        targetArea.setLineWrap(true);
        targetArea.setWrapStyleWord(true);
        targetArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        targetArea.setMargin(new Insets(10, 10, 10, 10));

        inputField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        inputField.setEnabled(false);

        JPanel middle = new JPanel(new BorderLayout(0, 10));
        middle.add(new JScrollPane(targetArea), BorderLayout.CENTER);
        middle.add(inputField, BorderLayout.SOUTH);

        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statsLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton again = new JButton("Play Again");
        JButton change = new JButton("Change Mode");
        JButton menu = new JButton("Main Menu");
        again.setFocusPainted(false);
        change.setFocusPainted(false);
        menu.setFocusPainted(false);
        again.addActionListener(e -> startRound(current));
        change.addActionListener(e -> showScreen(MODE));
        menu.addActionListener(e -> showScreen(MENU));
        buttons.add(again);
        buttons.add(change);
        buttons.add(menu);

        JPanel bottom = new JPanel(new GridLayout(3, 1, 0, 6));
        bottom.add(resultLabel);
        bottom.add(statsLabel);
        bottom.add(buttons);

        p.add(modeLabel, BorderLayout.NORTH);
        p.add(middle, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);

        // Events
        inputField.addActionListener(e -> finishRound());   // Enter = submit
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                if (inputField.isEnabled()) {
                    sounds.playTyping();   // plays Thock or Clack depending on Options
                }
                onTyping();
            }
            @Override public void removeUpdate(DocumentEvent e)  { onTyping(); }
            @Override public void changedUpdate(DocumentEvent e) { onTyping(); }
        });
        return p;
    }

    // ---------------------- UI HELPERS ----------------------

    private JPanel verticalPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return p;
    }

    private JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 32));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JButton menuButton(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 18));
        b.setFocusPainted(false);   // removes the dotted box around the text
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension size = new Dimension(260, 45);
        b.setPreferredSize(size);
        b.setMinimumSize(size);
        b.setMaximumSize(size);
        b.addActionListener(e -> action.run());
        return b;
    }

    // ---------------------- GAME LOGIC ----------------------

    private void startRound(Challenge challenge) {
        current = challenge;
        target = current.generateText();   // polymorphic call
        startTime = 0;

        modeLabel.setText(current.getName());
        targetArea.setText(target);
        inputField.setText("");
        inputField.setForeground(Color.BLACK);
        inputField.setEnabled(true);
        resultLabel.setText("Start typing - the timer starts on your first key. Press Enter when done.");

        showScreen(GAME);
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    // Starts the timer on first key and turns the text red when there's a mistake
    private void onTyping() {
        if (!inputField.isEnabled()) return;
        if (startTime == 0 && !inputField.getText().isEmpty()) {
            startTime = System.nanoTime();
        }
        boolean ok = target.startsWith(inputField.getText());
        inputField.setForeground(ok ? Color.BLACK : Color.RED);
    }

    private void finishRound() {
        if (!inputField.isEnabled() || startTime == 0) return;

        long end = System.nanoTime();
        String typed = inputField.getText();

        double seconds = Math.max((end - startTime) / 1_000_000_000.0, 0.001);
        double accuracy = calculateAccuracy(target, typed);
        double wpm = (typed.length() / 5.0) / (seconds / 60.0);
        double score = current.computeScore(wpm, accuracy);   // polymorphic call

        Result result = new Result(wpm, accuracy, seconds, score);
        player.recordResult(result);

        inputField.setEnabled(false);
        resultLabel.setText(String.format(
                "%.1f WPM  |  %.1f%% accuracy  |  %.2f s  |  score %.1f",
                result.getWpm(), result.getAccuracy(), result.getSeconds(), result.getScore()));
        updateStats();
    }

    private double calculateAccuracy(String target, String typed) {
        int total = Math.max(target.length(), typed.length());
        if (total == 0) return 0;
        int correct = 0;
        int limit = Math.min(target.length(), typed.length());
        for (int i = 0; i < limit; i++) {
            if (target.charAt(i) == typed.charAt(i)) correct++;
        }
        return correct * 100.0 / total;
    }

    private void updateStats() {
        statsLabel.setText(String.format("%s  |  Games: %d  |  Best WPM: %.1f  |  Best score: %.1f",
                player.getName(), player.getGamesPlayed(), player.getBestWpm(), player.getBestScore()));
    }
}