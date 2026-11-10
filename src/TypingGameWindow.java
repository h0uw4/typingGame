import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class TypingGameWindow extends JFrame {
    private static final String MENU = "menu";
    private static final String OPTIONS = "options";
    private static final String MODE = "mode";
    private static final String GAME = "game";

    private static final Color BG_DARK    = new Color(13, 14, 22);
    private static final Color BG_FIELD   = new Color(30, 33, 50);
    private static final Color TEXT       = new Color(232, 234, 246);
    private static final Color TEXT_DIM   = new Color(140, 146, 170);
    private static final Color ACCENT     = new Color(233, 69, 96);
    private static final Color ACCENT2    = new Color(0, 200, 255);
    private static final Color ERROR_RED  = new Color(255, 90, 90);
    private static final Color WIN_GREEN  = new Color(80, 220, 120);
    private static final Color HP_GREEN   = new Color(70, 200, 110);
    private static final Color HP_YELLOW  = new Color(230, 180, 40);
    private static final Color HP_RED     = new Color(220, 60, 60);
    private static final Color BORDER_CLR = new Color(55, 60, 85);

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final List<Challenge> challenges = new ArrayList<>();
    private final Player player = new Player("Player");
    private final SoundManager sounds = new SoundManager();
    private final ImageManager images = new ImageManager();

    private final JTextField nameField = new JTextField("Player", 15);

    // Game screen
    private GamePanel gamePanel;
    private BossDisplay bossDisplay;
    private HealthBar bossHpBar;
    private HealthBar playerHpBar;
    private JLabel bossNameLabel;
    private JLabel bossDiffLabel;
    private JLabel timeLabel;
    private JLabel accLabel;
    private JLabel wpmLabel;
    private WordStreamPanel wordStream;
    private JTextField inputField;

    // Fight state
    private Challenge current;
    private Boss boss;
    private final List<String> wordBank = new ArrayList<>();
    private int wordBankIdx = 0;
    private String target = "";
    private boolean running = false;
    private boolean resetting = false;

    private final Timer gameTimer = new Timer(50, e -> tick());
    private long lastTickNanos = 0;
    private long timeLimitMs = 0;
    private long timeRemainingMs = 0;

    private int totalKeystrokes = 0;
    private int errorKeystrokes = 0;
    private int wordsCompleted = 0;

    public TypingGameWindow() {
        super("Typing Speed Game - Boss Rush");

        challenges.add(new WordChallenge());
        challenges.add(new SentenceChallenge());
        challenges.add(new CodeChallenge());

        root.setBackground(BG_DARK);
        root.add(buildMenuPanel(), MENU);
        root.add(buildOptionsPanel(), OPTIONS);
        root.add(buildModePanel(), MODE);
        root.add(buildGamePanel(), GAME);
        add(root);

        showScreen(MENU);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 800);
        setMinimumSize(new Dimension(820, 720));
        setLocationRelativeTo(null);
    }

    private void showScreen(String name) {
        cards.show(root, name);
        if (GAME.equals(name)) {
            sounds.stopMusic();
        } else {
            running = false;
            gameTimer.stop();
            lastTickNanos = 0;
            sounds.playMusic();
        }
    }

    // ---------------------- UI HELPERS ----------------------

    private JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private void styleButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setForeground(TEXT);
        b.setBackground(BG_FIELD);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1),
                BorderFactory.createEmptyBorder(10, 24, 10, 24)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(45, 50, 75));
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT, 1),
                        BorderFactory.createEmptyBorder(10, 24, 10, 24)));
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(BG_FIELD);
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_CLR, 1),
                        BorderFactory.createEmptyBorder(10, 24, 10, 24)));
            }
        });
    }

    private void styleTextField(JTextField f) {
        f.setBackground(BG_FIELD);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setFont(new Font("SansSerif", Font.PLAIN, 16));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    private JButton menuButton(String text, Runnable action) {
        JButton b = new JButton(text);
        styleButton(b);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension size = new Dimension(400, 52);
        b.setPreferredSize(size);
        b.setMinimumSize(size);
        b.setMaximumSize(size);
        b.addActionListener(e -> action.run());
        return b;
    }

    private JRadioButton darkRadio(String text) {
        JRadioButton r = new JRadioButton(text);
        r.setFont(new Font("SansSerif", Font.PLAIN, 16));
        r.setForeground(TEXT);
        r.setOpaque(false);
        r.setFocusPainted(false);
        return r;
    }

    // ---------------------- SCREENS ----------------------

    private JPanel buildMenuPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        p.add(Box.createVerticalGlue());
        p.add(label("TYPING SPEED GAME", new Font("SansSerif", Font.BOLD, 42), TEXT));
        JLabel sub = label("BOSS RUSH  -  type fast. stay alive.",
                new Font("SansSerif", Font.PLAIN, 16), ACCENT2);
        sub.setBorder(BorderFactory.createEmptyBorder(8, 0, 44, 0));
        p.add(sub);
        p.add(menuButton("START", () -> showScreen(MODE)));
        p.add(Box.createVerticalStrut(14));
        p.add(menuButton("OPTIONS", () -> showScreen(OPTIONS)));
        p.add(Box.createVerticalStrut(14));
        p.add(menuButton("EXIT", () -> System.exit(0)));
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildOptionsPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        p.add(Box.createVerticalGlue());
        p.add(label("OPTIONS", new Font("SansSerif", Font.BOLD, 32), TEXT));
        p.add(Box.createVerticalStrut(30));
        p.add(label("Typing sound", new Font("SansSerif", Font.PLAIN, 18), TEXT));
        p.add(Box.createVerticalStrut(12));

        JRadioButton thock = darkRadio(SoundManager.THOCK);
        JRadioButton clack = darkRadio(SoundManager.CLACK);
        ButtonGroup g = new ButtonGroup();
        g.add(thock); g.add(clack);
        if (SoundManager.CLACK.equals(sounds.getTypingSound())) clack.setSelected(true);
        else thock.setSelected(true);

        thock.addActionListener(e -> { sounds.setTypingSound(SoundManager.THOCK); sounds.playTyping(); });
        clack.addActionListener(e -> { sounds.setTypingSound(SoundManager.CLACK); sounds.playTyping(); });

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 0));
        row.setOpaque(false);
        row.add(thock); row.add(clack);
        row.setMaximumSize(new Dimension(400, 40));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(row);
        p.add(Box.createVerticalStrut(40));
        p.add(menuButton("BACK", () -> showScreen(MENU)));
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildModePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        p.add(Box.createVerticalGlue());
        p.add(label("SELECT BOSS", new Font("SansSerif", Font.BOLD, 32), TEXT));
        p.add(Box.createVerticalStrut(24));

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        nameRow.setOpaque(false);
        JLabel nl = new JLabel("Your name:");
        nl.setForeground(TEXT);
        nl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        styleTextField(nameField);
        nameRow.add(nl); nameRow.add(nameField);
        nameRow.setMaximumSize(new Dimension(420, 40));
        nameRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(nameRow);
        p.add(Box.createVerticalStrut(24));

        for (Challenge c : challenges) {
            Boss b = c.createBoss();
            String lbl = String.format("%s  -  %s  -  %ds",
                    c.getName(), b.getDifficulty(), b.getTimeLimitSeconds());
            p.add(menuButton(lbl, () -> {
                player.setName(nameField.getText());
                startFight(c);
            }));
            p.add(Box.createVerticalStrut(12));
        }
        p.add(Box.createVerticalStrut(16));
        p.add(menuButton("BACK", () -> showScreen(MENU)));
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildGamePanel() {
        gamePanel = new GamePanel(new BorderLayout(0, 8));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // ---------- NORTH: header + big boss HP bar ----------
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        bossNameLabel = new JLabel("");
        bossNameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        bossNameLabel.setForeground(TEXT);
        bossDiffLabel = new JLabel("");
        bossDiffLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        bossDiffLabel.setForeground(ACCENT2);
        bossDiffLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT2, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        header.add(bossNameLabel, BorderLayout.WEST);
        header.add(bossDiffLabel, BorderLayout.EAST);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        north.add(header);
        north.add(Box.createVerticalStrut(6));

        bossHpBar = new HealthBar(HP_RED);
        bossHpBar.setPreferredSize(new Dimension(0, 40));
        bossHpBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bossHpBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(bossHpBar);

        // ---------- CENTER: big boss ----------
        bossDisplay = new BossDisplay(images);

        // ---------- SOUTH: word stream + input + HUD + player HP ----------
        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));

        wordStream = new WordStreamPanel();
        wordStream.setPreferredSize(new Dimension(0, 160));
        wordStream.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        wordStream.setAlignmentX(Component.CENTER_ALIGNMENT);
        south.add(wordStream);
        south.add(Box.createVerticalStrut(6));

        inputField = new JTextField();
        inputField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 26));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setBackground(BG_FIELD);
        inputField.setForeground(TEXT);
        inputField.setCaretColor(ACCENT2);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        inputField.setEnabled(false);
        inputField.setPreferredSize(new Dimension(0, 56));
        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        inputField.setAlignmentX(Component.CENTER_ALIGNMENT);
        south.add(inputField);
        south.add(Box.createVerticalStrut(10));

        JPanel hud = new JPanel(new GridLayout(1, 3, 14, 0));
        hud.setOpaque(false);
        hud.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        timeLabel = label("--", new Font("SansSerif", Font.BOLD, 26), TEXT);
        accLabel = label("--", new Font("SansSerif", Font.BOLD, 26), WIN_GREEN);
        wpmLabel = label("0", new Font("SansSerif", Font.BOLD, 26), ACCENT2);
        hud.add(hudBlock("TIME", timeLabel));
        hud.add(hudBlock("ACCURACY", accLabel));
        hud.add(hudBlock("WPM", wpmLabel));
        south.add(hud);
        south.add(Box.createVerticalStrut(6));

        playerHpBar = new HealthBar(HP_GREEN);
        playerHpBar.setPreferredSize(new Dimension(0, 28));
        playerHpBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        playerHpBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        south.add(playerHpBar);

        gamePanel.add(north, BorderLayout.NORTH);
        gamePanel.add(bossDisplay, BorderLayout.CENTER);
        gamePanel.add(south, BorderLayout.SOUTH);

        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onInsert(e); }
            @Override public void removeUpdate(DocumentEvent e) { updateTypedDisplay(); }
            @Override public void changedUpdate(DocumentEvent e) { updateTypedDisplay(); }
        });

        return gamePanel;
    }

    private JPanel hudBlock(String title, JLabel valueLabel) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 10));
        t.setForeground(TEXT_DIM);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(t);
        p.add(valueLabel);
        return p;
    }

    // ---------------------- TYPING ----------------------

    private void onInsert(DocumentEvent e) {
        if (resetting || !running) return;
        String typed = inputField.getText();
        int off = e.getOffset();
        int len = e.getLength();
        if (off >= 0 && off + len <= typed.length()) {
            String ins = typed.substring(off, off + len);
            for (int i = 0; i < ins.length(); i++) {
                int pos = off + i;
                totalKeystrokes++;
                if (pos >= target.length() || target.charAt(pos) != ins.charAt(i)) {
                    errorKeystrokes++;
                }
            }
        } else {
            totalKeystrokes += e.getLength();
            errorKeystrokes += e.getLength();
        }
        sounds.playTyping();
        updateTypedDisplay();
    }

    private void updateTypedDisplay() {
        if (!running) return;
        String typed = inputField.getText();
        
        // If "typed" is a valid prefix of "target", there are no errors.
        // (Also correctly returns false if typed is longer than target)
        boolean hasError = !target.startsWith(typed);

        inputField.setForeground(hasError ? ERROR_RED : TEXT);
        wordStream.setTyped(typed, hasError);
        updateHud();

        // If fully matched exactly, complete the word
        if (!hasError && !target.isEmpty() && typed.equals(target)) {
            SwingUtilities.invokeLater(this::completeWord);
        }
    }

    // ---------------------- GAME LOGIC ----------------------

    private void startFight(Challenge challenge) {
        current = challenge;
        boss = challenge.createBoss();

        wordBank.clear();
        wordBankIdx = 0;
        wordStream.reset();
        totalKeystrokes = 0;
        errorKeystrokes = 0;
        wordsCompleted = 0;
        target = "";
        resetting = false;

        timeLimitMs = boss.getTimeLimitSeconds() * 1000L;
        timeRemainingMs = timeLimitMs;
        lastTickNanos = 0;

        bossNameLabel.setText(boss.getName());
        bossDiffLabel.setText(boss.getDifficulty() + "  -  " + boss.getTimeLimitSeconds() + "s");
        bossDisplay.setBoss(boss.getImageFile(), boss.getName());
        bossHpBar.setMax(boss.getMaxHp());
        bossHpBar.setValue(boss.getHp());
        bossHpBar.setText(boss.getHp() + " / " + boss.getMaxHp());

        inputField.setEnabled(true);
        gamePanel.vignette = 0;

        showScreen(GAME);
        running = true;

        // First word immediately, next 4 staggered
        fillWordBank();
        String first = nextWord();
        if (!first.isEmpty()) wordStream.pushWord(first);
        target = first;

        resetting = true;
        inputField.setText("");
        resetting = false;
        inputField.setForeground(TEXT);

        final int[] remaining = { 4 };
        final Timer stagger = new Timer(90, null);
        stagger.addActionListener(ev -> {
            if (!running || remaining[0] <= 0) { stagger.stop(); return; }
            String w = nextWord();
            if (w.isEmpty()) { stagger.stop(); return; }
            wordStream.pushWord(w);
            remaining[0]--;
        });
        stagger.start();

        updateHud();
        updatePlayerHp();
        gameTimer.start();
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    private void fillWordBank() {
        String text = current.generateText();
        if (text != null) {
            for (String w : text.split("\\s+")) {
                if (!w.isEmpty()) wordBank.add(w);
            }
        }
    }

    private String nextWord() {
        if (wordBankIdx >= wordBank.size()) fillWordBank();
        if (wordBankIdx >= wordBank.size()) return "";
        return wordBank.get(wordBankIdx++);
    }

    private void fillWordQueue() {
        while (wordStream.getQueueSize() < 5) {
            String w = nextWord();
            if (w.isEmpty()) break;
            wordStream.pushWord(w);
        }
    }

    private void completeWord() {
        if (!running || target.isEmpty()) return;

        int dmg = (int) Math.round(target.length() * 2.0 * current.getMultiplier());
        if (dmg < 1) dmg = 1;
        boss.takeDamage(dmg);
        wordsCompleted++;

        bossHpBar.setValue(boss.getHp());
        bossHpBar.setText(boss.getHp() + " / " + boss.getMaxHp());
        bossDisplay.hit();
        sounds.playHit();

        if (boss.isDefeated()) {
            winFight();
        } else {
            wordStream.advance();
            fillWordQueue();
            target = wordStream.currentWord();
            resetting = true;
            inputField.setText("");
            resetting = false;
            inputField.setForeground(TEXT);
            wordStream.setTyped("", false);
            inputField.requestFocusInWindow();
        }
    }

    private void tick() {
        if (!running) return;
        long now = System.nanoTime();
        long elapsedNs = (lastTickNanos == 0) ? 50_000_000L : now - lastTickNanos;
        lastTickNanos = now;
        if (elapsedNs < 0) elapsedNs = 0;

        timeRemainingMs -= elapsedNs / 1_000_000L;
        if (timeRemainingMs <= 0) {
            timeRemainingMs = 0;
            updateHud();
            updatePlayerHp();
            playerDied();
            return;
        }
        updateHud();
        updatePlayerHp();
    }

    private void updateHud() {
        long totalSec = timeRemainingMs / 1000;
        long tenth = (timeRemainingMs % 1000) / 100;
        timeLabel.setText(String.format("%d.%ds", totalSec, tenth));
        if (timeRemainingMs < timeLimitMs * 0.25)      timeLabel.setForeground(ERROR_RED);
        else if (timeRemainingMs < timeLimitMs * 0.5)  timeLabel.setForeground(HP_YELLOW);
        else                                            timeLabel.setForeground(TEXT);

        if (totalKeystrokes == 0) {
            accLabel.setText("--%");
            accLabel.setForeground(TEXT_DIM);
        } else {
            double acc = accuracy();
            accLabel.setText(String.format("%.1f%%", acc));
            accLabel.setForeground(acc > 90 ? WIN_GREEN : acc > 70 ? HP_YELLOW : ERROR_RED);
        }

        double elapsed = (timeLimitMs - timeRemainingMs) / 1000.0;
        if (elapsed > 0.5) {
            wpmLabel.setText(String.format("%.0f", (totalKeystrokes / 5.0) / (elapsed / 60.0)));
        } else {
            wpmLabel.setText("0");
        }
    }

    private void updatePlayerHp() {
        int hp = (int) Math.round(100.0 * timeRemainingMs / timeLimitMs);
        playerHpBar.setValue(hp);
        long secs = (timeRemainingMs + 999) / 1000;
        playerHpBar.setText("TIME  " + secs + "s  remaining");

        if (hp > 50) playerHpBar.setColor(HP_GREEN);
        else if (hp > 25) playerHpBar.setColor(HP_YELLOW);
        else playerHpBar.setColor(HP_RED);

        double v = 0;
        if (hp < 40) v = Math.min(1.0, (40 - hp) / 40.0) * 0.9;
        gamePanel.vignette = v;
        gamePanel.repaint();
    }

    private void winFight() {
        running = false;
        gameTimer.stop();
        lastTickNanos = 0;
        inputField.setEnabled(false);
        gamePanel.vignette = 0;
        gamePanel.repaint();

        long elapsedMs = timeLimitMs - timeRemainingMs;
        double seconds = Math.max(elapsedMs / 1000.0, 0.001);
        double acc = accuracy();
        double wpm = (totalKeystrokes / 5.0) / (seconds / 60.0);
        double score = current.computeScore(wpm, acc);
        player.recordResult(new Result(wpm, acc, seconds, score));

        showResults(true, boss.getName(), wpm, acc, seconds, score, wordsCompleted);
    }

    private void playerDied() {
        running = false;
        gameTimer.stop();
        lastTickNanos = 0;
        inputField.setEnabled(false);
        gamePanel.vignette = 1.0;
        gamePanel.repaint();

        double seconds = timeLimitMs / 1000.0;
        double acc = accuracy();
        double wpm = (totalKeystrokes / 5.0) / (seconds / 60.0);
        double score = current.computeScore(wpm, acc);
        player.recordResult(new Result(wpm, acc, seconds, score));

        showResults(false, boss.getName(), wpm, acc, seconds, score, wordsCompleted);
    }

    private void showResults(boolean victory, String bossName,
                             double wpm, double acc, double sec, double score, int words) {
        ResultsDialog dlg = new ResultsDialog(this, victory, bossName, wpm, acc, sec, score, words,
                player.getBestWpm(), player.getBestScore());
        dlg.setVisible(true);
        int choice = dlg.getChoice();
        if (choice == ResultsDialog.REPLAY) startFight(current);
        else if (choice == ResultsDialog.CHANGE) showScreen(MODE);
        else showScreen(MENU);
    }

    private double accuracy() {
        if (totalKeystrokes == 0) return 0;
        return Math.max(0, (totalKeystrokes - errorKeystrokes) * 100.0 / totalKeystrokes);
    }

    // ---------------------- INNER UI CLASSES ----------------------

    private class GamePanel extends JPanel {
        double vignette = 0;
        GamePanel(LayoutManager lm) { super(lm); setBackground(BG_DARK); }

        @Override
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            if (vignette <= 0.01 || getWidth() <= 0 || getHeight() <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double pulse = 0.6 + 0.4 * Math.sin(System.currentTimeMillis() / 220.0);
            int alpha = (int) Math.min(220, vignette * pulse * 220);
            if (alpha > 0) {
                Color edge = new Color(200, 20, 40, alpha);
                RadialGradientPaint rgp = new RadialGradientPaint(
                        new Point2D.Float(getWidth() / 2f, getHeight() / 2f),
                        Math.max(getWidth(), getHeight()) * 0.72f,
                        new float[]{ 0.3f, 1f },
                        new Color[]{ new Color(200, 20, 40, 0), edge });
                g2.setPaint(rgp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
        }
    }

    private static class HealthBar extends JComponent {
        private int max = 100, value = 100;
        private Color color;
        private String text = "";

        HealthBar(Color color) { this.color = color; setOpaque(false); }
        void setMax(int m) { max = Math.max(1, m); repaint(); }
        void setValue(int v) { value = Math.max(0, Math.min(max, v)); repaint(); }
        void setText(String t) { if (t != null) text = t; repaint(); }
        void setColor(Color c) { color = c; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int arc = Math.min(12, h);

            g2.setColor(new Color(28, 30, 46));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            int fw = (int) ((double) value / max * w);
            if (fw > 2) {
                g2.setColor(color);
                g2.fillRoundRect(0, 0, fw, h, arc, arc);
                g2.setColor(new Color(255, 255, 255, 45));
                g2.fillRoundRect(0, 0, fw, h / 2, arc, arc);
            }

            g2.setColor(BORDER_CLR);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            String t = text.isEmpty() ? (value + " / " + max) : text;
            g2.setColor(Color.WHITE);
            g2.drawString(t, (w - fm.stringWidth(t)) / 2,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }
}