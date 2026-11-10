import javax.swing.*;
import java.awt.*;

// Large centered boss with idle bob, white flash and pixel slash on hit.
public class BossDisplay extends JPanel {
    private static final Color TEXT_DIM = new Color(140, 146, 170);

    private final ImageManager images;
    private ImageIcon icon;
    private String placeholder = "";
    private double flash = 0, slash = 0, slashProgress = 0;
    private double bobPhase = 0;
    private final Timer anim;

    public BossDisplay(ImageManager images) {
        this.images = images;
        setOpaque(false);
        anim = new Timer(16, e -> animate());
        anim.start();
    }

    public void setBoss(String file, String name) {
        icon = images.getBossIcon(file, 440, 340);
        placeholder = icon == null ? "[ " + name + " ]" : "";
        flash = slash = slashProgress = 0;
        repaint();
    }

    public void hit() {
        flash = 1.0;
        slash = 1.0;
        slashProgress = 0;
    }

    private void animate() {
        if (flash > 0)           flash = Math.max(0, flash - 0.08);
        if (slashProgress < 1)   slashProgress = Math.min(1, slashProgress + 0.11);
        else if (slash > 0)      slash = Math.max(0, slash - 0.045);
        bobPhase += 0.05;
        if (bobPhase > Math.PI * 2) bobPhase -= Math.PI * 2;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        int cx = getWidth() / 2, cy = getHeight() / 2;
        int bob = (int) (Math.sin(bobPhase) * 5);

        if (icon == null) {
            g2.setColor(TEXT_DIM);
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, cx - fm.stringWidth(placeholder) / 2, cy);
            g2.dispose();
            return;
        }

        int iw = icon.getIconWidth(), ih = icon.getIconHeight();
        int x = cx - iw / 2;
        int y = cy - ih / 2 + bob;

        // ground shadow
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillOval(cx - iw / 3, y + ih - 12, iw * 2 / 3, 22);

        g2.drawImage(icon.getImage(), x, y, null);

        if (flash > 0.01) {
            g2.setColor(new Color(255, 255, 255, (int) (flash * 200)));
            g2.fillRect(x, y, iw, ih);
        }
        if (slash > 0.01) {
            g2.setColor(new Color(255, 255, 255, (int) (slash * 255)));
            int x1 = x + 20, y1 = y + 12, x2 = x + iw - 20, y2 = y + ih - 12;
            int steps = 52;
            int upto = (int) (steps * slashProgress);
            int sz = 9;
            for (int i = 0; i < upto; i++) {
                float t = i / (float) steps;
                int px = (int) (x1 + (x2 - x1) * t);
                int py = (int) (y1 + (y2 - y1) * t);
                g2.fillRect(px - sz / 2, py - sz / 2, sz, sz);
            }
        }
        g2.dispose();
    }
}
