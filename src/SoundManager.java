import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

// ENCAPSULATION: the clips and the selected typing sound are private.
// Other classes can only use the public methods.
//
// Sound files go in a folder named "sound" inside the project root
// (the same folder that contains "src"):
//   sound/main-menu-bgm.wav, sound/thock.wav, sound/clack.wav
public class SoundManager {
    public static final String THOCK = "Thock";
    public static final String CLACK = "Clack";

    // Each typing sound has several copies of the clip. A single clip can only
    // play once at a time, so fast typing would cut sounds off or skip them.
    // Rotating through the copies lets quick keystrokes overlap.
    private static final int POOL_SIZE = 8;

    private final Clip music;
    private final Clip[] thockPool;
    private final Clip[] clackPool;
    private int thockIndex = 0;
    private int clackIndex = 0;
    private String typingSound = THOCK;

    public SoundManager() {
        music = load("main-menu-bgm.wav");
        thockPool = loadPool("thock.wav", POOL_SIZE);
        clackPool = loadPool("clack.wav", POOL_SIZE);
    }

    public String getTypingSound() {
        return typingSound;
    }

    // Only accepts valid choices
    public void setTypingSound(String choice) {
        if (THOCK.equals(choice) || CLACK.equals(choice)) {
            typingSound = choice;
        }
    }

    public void playTyping() {
        Clip clip;
        if (THOCK.equals(typingSound)) {
            clip = thockPool[thockIndex];
            thockIndex = (thockIndex + 1) % POOL_SIZE;
        } else {
            clip = clackPool[clackIndex];
            clackIndex = (clackIndex + 1) % POOL_SIZE;
        }
        if (clip == null) return;
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void playMusic() {
        if (music == null || music.isRunning()) return;
        music.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopMusic() {
        if (music != null && music.isRunning()) {
            music.stop();
        }
    }

    private Clip[] loadPool(String fileName, int size) {
        Clip[] pool = new Clip[size];
        for (int i = 0; i < size; i++) {
            pool[i] = load(fileName, i == 0);   // only print the message once
        }
        return pool;
    }

    private Clip load(String fileName) {
        return load(fileName, true);
    }

    private Clip load(String fileName, boolean verbose) {
        File file = new File("sound", fileName);   // sound folder, next to the src folder
        if (!file.exists()) {
            if (verbose) System.out.println("Sound file not found: " + file.getAbsolutePath());
            return null;
        }
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(file)) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            if (verbose) System.out.println("Loaded sound: " + file.getAbsolutePath());
            return clip;
        } catch (Exception e) {
            if (verbose) System.out.println("Could not load " + fileName + ": " + e.getMessage());
            return null;
        }
    }
}