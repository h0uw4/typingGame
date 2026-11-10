import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// ENCAPSULATION: loading and caching of images is hidden here.
// Same idea as SoundManager. Image files go in a folder named "images"
// inside the project root (the same folder that contains "src" and "sound"):
//   images/word-golem.png, images/sentence-dragon.png, images/code-demon.png
public class ImageManager {
    private final Map<String, ImageIcon> cache = new HashMap<>();

    // Returns the image scaled to fit inside maxW x maxH (keeps the aspect ratio).
    // Returns null if the file is missing, so the game still works without it.
    public ImageIcon getBossIcon(String fileName, int maxW, int maxH) {
        String key = fileName + "@" + maxW + "x" + maxH;
        if (!cache.containsKey(key)) {
            cache.put(key, load(fileName, maxW, maxH));   // null is cached too
        }
        return cache.get(key);
    }

    private ImageIcon load(String fileName, int maxW, int maxH) {
        File file = new File("images", fileName);
        if (!file.exists()) {
            System.out.println("Image not found: " + file.getAbsolutePath());
            return null;
        }
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                System.out.println("Could not read image: " + file.getAbsolutePath());
                return null;
            }
            double scale = Math.min((double) maxW / img.getWidth(),
                                    (double) maxH / img.getHeight());
            int w = Math.max(1, (int) Math.round(img.getWidth() * scale));
            int h = Math.max(1, (int) Math.round(img.getHeight() * scale));

            // Nearest-neighbor keeps pixel art sharp (no smoothing/blur)
            BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                               RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(img, 0, 0, w, h, null);
            g.dispose();

            System.out.println("Loaded image: " + file.getAbsolutePath());
            return new ImageIcon(scaled);
        } catch (IOException e) {
            System.out.println("Could not load " + fileName + ": " + e.getMessage());
            return null;
        }
    }
}