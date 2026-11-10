import javax.swing.*;
import java.awt.*;

// Modal results dialog, dark themed. Blocks on setVisible until user picks an option.
public class ResultsDialog extends JDialog {
    public static final int REPLAY = 0;
    public static final int CHANGE = 1;
    public static final int MENU   = 2;

    private static final Color BG_TOP    = new Color(20, 22, 34);
    private static final Color BG_BOTTOM = new Color(13, 14, 22);
    private static final Color BG_FIELD  = new Color(30, 33, 50);
    private static final Color TEXT      = new Color(232, 234, 246);
    private static final Color TEXT_DIM  = new Color(140, 146, 170);
    private static final Color ACCENT    = new Color(0, 200, 255);
    private static final Color WIN       = new Color(80, 220, 120);
    private static final Color LOSS      = new Color(255, 90, 90);
    private static final Color BORDER    = new Color(55, 60, 85);
    private static final Color GOLD      = new Color(240, 200, 80);

    private int choice = -1;

    public ResultsDialog(JFrame parent, boolean victory, String bossName,
                         double wpm, double accuracy, double seconds,
                         double score, int wordsKilled,
                         double bestWpm, double bestScore) {
        super(parent, "Results", true);
        setUndecorated(true);

        JPanel content = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
                g2.dispose();
            }
        };
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(32, 40, 30, 40));

        JLabel title = new JLabel(victory ? "VICTORY" : "DEFEATED");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(victory ? WIN : LOSS);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);

        JLabel sub = new JLabel(bossName + (victory ? "  -  defeated" : "  -  survived"));
        sub.setFont(new Font("SansSerif", Font.PLAIN, 15));
        sub.setForeground(TEXT_DIM);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(Box.createVerticalStrut(4));
        content.add(sub);
        content.add(Box.createVerticalStrut(24));

        JLabel scoreVal = new JLabel(String.format("%.1f", score));
        scoreVal.setFont(new Font("SansSerif", Font.BOLD, 58));
        scoreVal.setForeground(GOLD);
        scoreVal.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel scoreLbl = new JLabel("SCORE");
        scoreLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        scoreLbl.setForeground(TEXT_DIM);
        scoreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(scoreVal);
        content.add(scoreLbl);
        content.add(Box.createVerticalStrut(22));

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setOpaque(false);
        grid.add(statBox("SPEED",    String.format("%.1f", wpm),      "WPM"));
        grid.add(statBox("ACCURACY", String.format("%.1f", accuracy), "%"));
        grid.add(statBox("TIME",     String.format("%.1f", seconds),  "s"));
        grid.add(statBox("WORDS",    String.valueOf(wordsKilled),     "killed"));
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        content.add(grid);
        content.add(Box.createVerticalStrut(16));

        JLabel best = new JLabel(String.format("Best:  %.1f WPM  -  %.1f score", bestWpm, bestScore));
        best.setFont(new Font("SansSerif", Font.PLAIN, 13));
        best.setForeground(TEXT_DIM);
        best.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(best);
        content.add(Box.createVerticalStrut(24));

        JPanel btns = new JPanel(new GridLayout(1, 3, 8, 0));
        btns.setOpaque(false);
        JButton again = mkBtn("PLAY AGAIN");
        JButton change = mkBtn("CHANGE BOSS");
        JButton menu = mkBtn("MAIN MENU");
        again.addActionListener(e -> { choice = REPLAY; dispose(); });
        change.addActionListener(e -> { choice = CHANGE; dispose(); });
        menu.addActionListener(e -> { choice = MENU; dispose(); });
        btns.add(again); btns.add(change); btns.add(menu);
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        content.add(btns);

        add(content);
        setSize(440, 620);
        setLocationRelativeTo(parent);
    }

    private JPanel statBox(String label, String value, String unit) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_FIELD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel t = new JLabel(label);
        t.setFont(new Font("SansSerif", Font.BOLD, 10));
        t.setForeground(TEXT_DIM);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel v = new JLabel("<html>" + value
                + " <font size='3' color='#8c92aa'>" + unit + "</font></html>");
        v.setFont(new Font("SansSerif", Font.BOLD, 26));
        v.setForeground(TEXT);
        v.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(2));
        p.add(v);
        return p;
    }

    private JButton mkBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(TEXT);
        b.setBackground(BG_FIELD);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(8, 6, 8, 6)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public int getChoice() { return choice; }
}