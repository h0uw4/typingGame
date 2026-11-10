import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Word queue: current word big & glowing, next words dimmed above.
// Each word pops in with a scale-in animation.
public class WordStreamPanel extends JPanel {
    private static final Color TEXT      = new Color(232, 234, 246);
    private static final Color OK_COLOR  = new Color(80, 220, 120);
    private static final Color ERR_COLOR = new Color(255, 90, 90);
    private static final Color DIM       = new Color(130, 136, 160);
    private static final Color GLOW      = new Color(0, 200, 255);

    private final List<String> queue = new ArrayList<>();
    private final List<Double> spawn = new ArrayList<>();
    private String typed = "";
    private boolean typedHasError = false;
    private final Timer anim;

    public WordStreamPanel() {
        setOpaque(false);
        anim = new Timer(16, e -> tick());
    }

    public void reset() {
        queue.clear();
        spawn.clear();
        typed = "";
        typedHasError = false;
        repaint();
    }

    public void pushWord(String w) {
        queue.add(w);
        spawn.add(0.0);
        if (!anim.isRunning()) anim.start();
        repaint();
    }

    public void setTyped(String t, boolean hasError) {
        typed = t;
        typedHasError = hasError;
        repaint();
    }

    public String currentWord() { return queue.isEmpty() ? "" : queue.get(0); }
    public int getQueueSize()   { return queue.size(); }

    public void advance() {
        if (queue.isEmpty()) return;
        queue.remove(0);
        if (!spawn.isEmpty()) spawn.remove(0);
        typed = "";
        typedHasError = false;
        if (!spawn.isEmpty()) spawn.set(0, 0.0);
        if (!anim.isRunning() && !spawn.isEmpty()) anim.start();
        repaint();
    }

    private void tick() {
        boolean active = false;
        for (int i = 0; i < spawn.size(); i++) {
            if (spawn.get(i) < 1.0) {
                spawn.set(i, Math.min(1.0, spawn.get(i) + 0.14));
                active = true;
            }
        }
        if (!active) anim.stop();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (queue.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), cx = w / 2;

        // --- upcoming words as small dim chips in a row ---
        List<String> up = new ArrayList<>();
        for (int i = 1; i < queue.size() && up.size() < 4; i++) up.add(queue.get(i));
        if (!up.isEmpty()) {
            Font small = new Font(Font.MONOSPACED, Font.BOLD, 17);
            g2.setFont(small);
            FontMetrics fmS = g2.getFontMetrics();
            int sep = 22, total = 0;
            for (String u : up) total += fmS.stringWidth(u) + sep;
            total -= sep;
            int x = cx - total / 2;
            int y = 26;
            for (int i = 0; i < up.size(); i++) {
                String u = up.get(i);
                double a = 0.5 - i * 0.09;
                if (a < 0.15) a = 0.15;
                g2.setColor(new Color(DIM.getRed(), DIM.getGreen(), DIM.getBlue(), (int) (255 * a)));
                g2.drawString(u, x, y);
                x += fmS.stringWidth(u) + sep;
            }
        }

        // --- current word big in center ---
        String current = queue.get(0);
        double s = spawn.isEmpty() ? 1.0 : spawn.get(0);
        double eased = 1.0 - Math.pow(1.0 - s, 3);

        int fontSize = (int) (46 * (0.55 + 0.45 * eased));
        Font big = new Font(Font.MONOSPACED, Font.BOLD, Math.max(18, fontSize));
        g2.setFont(big);
        FontMetrics fm = g2.getFontMetrics();
        int cw = fm.stringWidth(current);
        int startX = cx - cw / 2;
        int baseY = h / 2 + fm.getAscent() / 2 - 2;

        // glow
        g2.setColor(new Color(GLOW.getRed(), GLOW.getGreen(), GLOW.getBlue(), (int) (90 * eased)));
        for (int r = 4; r >= 1; r--) {
            g2.drawString(current, startX - r, baseY + r);
            g2.drawString(current, startX + r, baseY - r);
        }

        int matchLen = 0;
        for (int i = 0; i < typed.length() && i < current.length(); i++) {
            if (current.charAt(i) == typed.charAt(i)) matchLen++;
            else break;
        }
        int textAlpha = (int) (255 * (0.35 + 0.65 * eased));

        if (matchLen > 0) {
            g2.setColor(OK_COLOR);
            g2.drawString(current.substring(0, matchLen), startX, baseY);
        }
        String rest = current.substring(matchLen);
        g2.setColor(new Color(TEXT.getRed(), TEXT.getGreen(), TEXT.getBlue(), textAlpha));
        g2.drawString(rest, startX + fm.stringWidth(current.substring(0, matchLen)), baseY);

        // red caret under the mismatch
        if (typedHasError && matchLen < typed.length()) {
            int errX = startX + fm.stringWidth(current.substring(0, matchLen));
            g2.setColor(ERR_COLOR);
            g2.fillRect(errX, baseY + 4, Math.max(3, fm.charWidth('M')), 3);
        }

        g2.dispose();
    }
}